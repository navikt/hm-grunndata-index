ARCHIVED - THIS CODE HAS BEEN MOVED INTO hm-grunndata-db

# hm-grunndata-index
index grunndata to opensearch

Run on localhost:
```
docker-compose up -d
export SERVER_PORT=8082 # Otional: If you want to run besides grunndata-db
export GRUNNDATA_DB_URL=http://localhost:8080
export RAPIDSANDRIVERS_ENABLED=true
./gradlew build run

```

Manually sync data from hm-grunndata-db over REST:
````
curl -X post http://localhost:8082/internal/index/all/
````
