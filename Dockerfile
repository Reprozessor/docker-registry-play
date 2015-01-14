FROM zalando/ubuntu:14.04.1-1
MAINTAINER Henning Jacobs <henning.jacobs@zalando.de>

RUN apt-get update
RUN apt-get install -y ca-certificates-java java-common libcups2 liblcms2-2 libjpeg8 libnss3 libfreetype6 libpcsclite1 libx11-6 libxext6 libxi6 libxrender1 libxtst6

RUN curl -o /tmp/openjdk.deb http://de.archive.ubuntu.com/ubuntu/pool/universe/o/openjdk-8/openjdk-8-jre-headless_8u40~b09-1_amd64.deb
RUN dpkg -i /tmp/openjdk.deb && rm /tmp/openjdk.deb

ADD target/scala-2.11/docker-registry-play_2.11-0.1-SNAPSHOT.jar /docker-registry-play.jar

CMD ["java", "-jar", "/docker-registry-play.jar"]
