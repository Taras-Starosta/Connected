FROM ghcr.io/graalvm/jdk:java17-22.0.0
COPY target/universal/ .

CMD stage/bin/connected

EXPOSE 9000