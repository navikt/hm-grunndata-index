FROM navikt/java:17
USER root
RUN apt-get update && apt-get install -y curl
USER apprunner
COPY build/libs/hm-grunndata-index-all.jar ./app.jar

