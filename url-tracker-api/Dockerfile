FROM java:8
COPY ./target/url-tracker-api.jar /usr/src/url-tracker-api/
WORKDIR /usr/src/url-tracker-api
RUN apt-get install libfontconfig
EXPOSE 8080
CMD ["java", "-Dserver.port=8080", "-jar", "url-tracker-api.jar"]
