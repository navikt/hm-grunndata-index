FROM ghcr.io/navikt/baseimages/temurin:17
USER apprunner
COPY build/libs/hm-grunndata-index-all.jar ./app.jar

