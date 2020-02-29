package tech.artcoded.atriangle.api;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static tech.artcoded.atriangle.api.ModelUtils.modelToLang;

public interface ElasticSearchRdfService {
  Logger LOGGER = LoggerFactory.getLogger(ElasticSearchRdfService.class);

  RestHighLevelClient getClient();

  @SneakyThrows
  default String inputStreamToString(InputStream is) {
    return IOUtils.toString(is, StandardCharsets.UTF_8);
  }

  default Settings.Builder inputStreamToSettingsJson(InputStream is) {
    return Settings.builder()
                   .loadFromSource(inputStreamToString(is), XContentType.JSON);
  }

  default CreateIndexResponse createIndex(String index, InputStream source) {
    return createIndex(index, createIndexRequest -> createIndexRequest.source(inputStreamToString(source), XContentType.JSON));
  }

  @SneakyThrows
  default CreateIndexResponse createIndex(String index,
                                          Function<CreateIndexRequest, CreateIndexRequest> requestTransformer) {
    CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
    return getClient().indices()
                      .create(requestTransformer.apply(createIndexRequest), RequestOptions.DEFAULT);
  }

  @SneakyThrows
  default CreateIndexResponse createIndex(String index) {
    return createIndex(index, createIndexRequest -> createIndexRequest);
  }

  @SneakyThrows
  default CreateIndexResponse createIndex(String index, InputStream settings,
                                          InputStream mapping,
                                          InputStream aliases) {
    return createIndex(index, createIndexRequest -> createIndexRequest.settings(inputStreamToSettingsJson(settings).build())
                                                                      .mapping(inputStreamToString(mapping), XContentType.JSON)
                                                                      .aliases(inputStreamToString(aliases), XContentType.JSON)
    );
  }


  default void deleteIndexAsync(String index) {
    DeleteIndexRequest request = new DeleteIndexRequest(index);
    getClient().indices()
               .deleteAsync(request, RequestOptions.DEFAULT, getDefaultAcknowledgeCallback());
  }

  @SneakyThrows
  default AcknowledgedResponse deleteIndex(String index) {
    DeleteIndexRequest request = new DeleteIndexRequest(index);
    return getClient().indices()
                      .delete(request, RequestOptions.DEFAULT);
  }


  @SneakyThrows
  default void indexAsync(String index, Function<IndexRequest, IndexRequest> requestTransformer) {
    IndexRequest request = new IndexRequest(index);
    getClient().indexAsync(requestTransformer.apply(request), RequestOptions.DEFAULT, getDefaultIndexCallback());
  }

  @SneakyThrows
  default IndexResponse index(String index, Function<IndexRequest, IndexRequest> requestTransformer) {
    IndexRequest request = new IndexRequest(index);
    request.opType(DocWriteRequest.OpType.INDEX);
    return getClient().index(requestTransformer.apply(request), RequestOptions.DEFAULT);
  }

  default IndexResponse index(String index, String id, Model model) {
    return index(index, id, modelToLang(model, Lang.JSONLD));
  }

  default IndexResponse index(String index, String id, String body) {
    return index(index, this.defaultIndexRequest(id, body));
  }

  default void indexAsync(String index, String id, Model model) {
    indexAsync(index, id, modelToLang(model, Lang.JSONLD));
  }

  default void indexAsync(String index, String id, String body) {
    indexAsync(index, id, body);
  }

  @SneakyThrows
  default IndexResponse indexWithRandomId(String index, Model model) {
    return index(index, UUID.randomUUID()
                            .toString(), model);
  }

  @SneakyThrows
  default SearchResponse search(Function<SearchRequest, SearchRequest> requestTransformer,
                                String... indexes) {
    SearchRequest request = new SearchRequest();
    request.indices(indexes);
    return getClient().search(requestTransformer.apply(request), RequestOptions.DEFAULT);
  }

  @SneakyThrows
  default SearchResponse searchAll(String... indexes) {
    return transformAndSearch(searchSourceBuilder -> searchSourceBuilder.query(QueryBuilders.matchAllQuery()), indexes);
  }

  @SneakyThrows
  default SearchResponse fuzzyQuery(String key, Object value, Fuzziness fuzziness, float boost, String... indexes) {
    return transformAndSearch(searchSourceBuilder -> searchSourceBuilder.query(QueryBuilders.fuzzyQuery(key, value)
                                                                                            .fuzziness(fuzziness)
                                                                                            .boost(boost))
      , indexes);
  }

  @SneakyThrows
  default SearchResponse termQuery(String key, Object term, String... indexes) {
    return transformAndSearch(searchSourceBuilder -> searchSourceBuilder.query(QueryBuilders.termQuery(key, term)), indexes);
  }

  @SneakyThrows
  default SearchResponse scriptQuery(String script, Map<String, Object> scriptParams, String... indexes) {
    SearchTemplateRequest request = new SearchTemplateRequest();
    request.setRequest(new SearchRequest(indexes));

    request.setScriptType(ScriptType.INLINE);
    request.setScript(script);
    request.setScriptParams(scriptParams);

    return search(r -> request.getRequest());
  }

  @SneakyThrows
  default SearchResponse matchQuery(String key, Object text, String... indexes) {
    return transformAndSearch(searchSourceBuilder -> searchSourceBuilder.query(QueryBuilders.matchQuery(key, text)), indexes);
  }

  @SneakyThrows
  default SearchResponse transformAndSearch(Function<SearchSourceBuilder, SearchSourceBuilder> transformer,
                                            String... indices) {
    return search(request -> request.indices(indices)
                                    .source(transformer.apply(SearchSourceBuilder.searchSource())), indices);
  }

  @SneakyThrows
  default boolean indexExist(String index) {
    GetIndexRequest request = new GetIndexRequest(index);
    return getClient().indices()
                      .exists(request, RequestOptions.DEFAULT);
  }

  default void reindexAsync(String dest, String... source) {
    ReindexRequest request = new ReindexRequest();
    request.setSourceIndices(source);
    request.setDestIndex(dest);
    getClient().reindexAsync(request, RequestOptions.DEFAULT, getDefaultReindexCallback());
  }

  default boolean ping() throws IOException {
    return getClient().ping(RequestOptions.DEFAULT);
  }

  default MainResponse info() throws IOException {
    return getClient().info(RequestOptions.DEFAULT);
  }

  default ActionListener<BulkByScrollResponse> getDefaultReindexCallback() {
    return ActionListener.wrap(response -> {
      LOGGER.info("Result of reindexing: {}", response.toString());
    }, e -> LOGGER.error("error during deletion", e));
  }

  default ActionListener<IndexResponse> getDefaultIndexCallback() {
    return ActionListener.wrap(response -> {
      LOGGER.info("Result of reindexing: {}", response.toString());
    }, e -> LOGGER.error("error during deletion", e));
  }

  default ActionListener<AcknowledgedResponse> getDefaultAcknowledgeCallback() {
    return ActionListener.wrap(response -> LOGGER.info("acknowledge: " + response.isAcknowledged()),
                               e -> LOGGER.error("error during deletion", e));
  }

  default Function<IndexRequest, IndexRequest> defaultIndexRequest(String id, String json) {
    if (Stream.of(id, json)
              .anyMatch(String::isEmpty)) throw new RuntimeException("id and json cannot be empty");
    return request -> request.id(id)
                             .source(json, XContentType.JSON);
  }


}
