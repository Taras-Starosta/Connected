FROM ghcr.io/graalvm/jdk:java17-21.3.0
COPY target/scala-2.13/connected.jar .

CMD java -jar connected.jar

EXPOSE 9999
