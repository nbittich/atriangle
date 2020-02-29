docker run --name artcodedmovies-virtuoso \
        -p 8890:8890 -p 1111:1111 \
        -e DBA_PASSWORD=dba \
        -e SPARQL_UPDATE=true \
        -e DEFAULT_GRAPH=https://www.artcoded.tech/artcodedmovies-graph \
        -v /tmp/virtuoso/db:/data \
        -d tenforce/virtuoso


        `` git clone https://github.com/wurstmeister/kafka-docker.git ``
        `` cd kafka-docker ``

        # Update KAFKA_ADVERTISED_HOST_NAME inside 'docker-compose.yml',
        # For example, set it to 172.17.0.1
        `` vim docker-compose.yml ``
        `` docker-compose up -d ``

        # Optional - Scale the cluster by adding more brokers (Will start a single zookeeper instance)
        `` docker-compose scale kafka=3 ``

        # You can check the proceses running with:
        `` docker-compose ps ``

        # Destroy the cluster when you are done with it
        `` docker-compose stop ``