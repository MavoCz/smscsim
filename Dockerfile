FROM openjdk:11-jre-slim

COPY target/smscsim.jar /
COPY ./docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]