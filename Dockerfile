FROM navikt/java:17
USER apprunner
COPY build/libs/hm-grunndata-index-all.jar ./app.jar

