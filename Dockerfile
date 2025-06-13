FROM docker.io/sbtscala/scala-sbt:eclipse-temurin-24.0.1_9_1.11.2_3.7.1
RUN apt-get update && apt-get install -y wget just fish
RUN wget https://github.com/etcd-io/etcd/releases/download/v3.6.0/etcd-v3.6.0-linux-amd64.tar.gz
RUN mkdir /etcd
RUN tar -xvzf etcd-v3.6.0-linux-amd64.tar.gz -C /etcd
RUN rm etcd-v3.6.0-linux-amd64.tar.gz