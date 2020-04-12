prefix bds: <http://www.bigdata.com/rdf/search#>
select ?s ?p ?o ?score ?rank
where {
?o bds:search "${searchTerm}" .
?o bds:matchAllTerms "${matchAllTerm}" .
?o bds:minRelevance "${minRelevance}" .
?o bds:relevance ?score .
?o bds:maxRank "${maxRank}" .
?o bds:rank ?rank .
?s ?p ?o .
}
