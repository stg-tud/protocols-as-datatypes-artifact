FROM docker.io/sbtscala/scala-sbt:eclipse-temurin-21.0.7_6_1.11.2_3.7.1

# install etcd
RUN apt-get update && apt-get install -y wget just fish unzip fzf python3-poetry
RUN wget https://github.com/etcd-io/etcd/releases/download/v3.6.0/etcd-v3.6.0-linux-amd64.tar.gz
RUN mkdir /etcd
RUN tar -xvzf etcd-v3.6.0-linux-amd64.tar.gz -C /etcd
RUN cp /etcd/etcd-v3.6.0-linux-amd64/etcd* /bin/
RUN rm etcd-v3.6.0-linux-amd64.tar.gz

# install python dependencies for jupyter
COPY data-analysis /app/data-analysis
WORKDIR /app/data-analysis
RUN ["poetry", "install"]

# build scala sources
COPY src/ /app/
WORKDIR /app
RUN ["just", "package-bin"]

ENTRYPOINT [ "just" ]
CMD [ "--choose" ]