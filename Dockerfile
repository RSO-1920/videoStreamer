FROM openjdk:11.0.4-jre-slim

RUN mkdir /app
RUN mkdir /app/streamFiles

WORKDIR /app

ADD ./video-streamer-api/target/video-streamer-api-1.0.0-SNAPSHOT.jar /app
ADD ./file_example_MP4_1920_18MG.mp4 /app
ADD ./streamFiles/18.mp4 /app/streamFiles

EXPOSE 8084

CMD ["java", "-jar", "video-streamer-api-1.0.0-SNAPSHOT.jar"]
