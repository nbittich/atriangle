docker run --name artcodedmovies-virtuoso \
        -p 8890:8890 -p 1111:1111 \
        -e DBA_PASSWORD=dba \
        -e SPARQL_UPDATE=true \
        -e DEFAULT_GRAPH=https://www.artcoded.tech/artcodedmovies-graph \
        -v /tmp/virtuoso/db:/data \
        -d tenforce/virtuoso