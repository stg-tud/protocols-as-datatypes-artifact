# a multi platform Dockerfile
FROM eclipse-temurin:21-jdk AS build
ARG TARGETPLATFORM

# install dependencies
RUN apt-get update && apt-get install -y wget curl just fish unzip fzf pipenv ansible python3-dateutil rsync

# install coursier
WORKDIR /coursier
RUN curl -fLo coursier https://github.com/coursier/launchers/raw/master/coursier && chmod +x coursier
RUN ./coursier setup --yes
RUN ./coursier install sbt
RUN [ "ln", "-s", "/root/.local/share/coursier/bin/sbt", "/usr/local/bin/sbt"]

# verify that installed tools are available
RUN sbt --version
#RUN etcd --version


# install python dependencies for jupyter
RUN mkdir -p /app/analysis
COPY analysis /app/analysis
WORKDIR /app/analysis
RUN ["pipenv", "install"]

# install ansible deps
RUN ansible-galaxy collection install -f hetzner.hcloud

# copy PRDT source & jars
COPY bin/jars /app/jars
COPY src/ /app/src/
# ensure sbt dependencies are downloaded
WORKDIR /app/src/bismuth
RUN ["sbt", "publishLocal"]
WORKDIR /app/src/example-project
RUN ["sbt", "test:compile"]

COPY scripts /app/scripts
COPY bin/ycsb-core.jar /app/
COPY benchConfig /app/
COPY workloads/writeonly /app/workloads/writeonly
COPY workloads/workloadb /app/workloads/workloadb
COPY bin/etcd/ /app/etcd
COPY ansible /app/ansible
COPY prdt-artifact /root/.ssh/id_ed25519
RUN chmod 600 /root/.ssh/id_ed25519
COPY .ansible.cfg /root/.ansible.cfg


COPY Justfile /app/Justfile

RUN ["fish",  "-c", "set -Ux jarspath /app/jars"]

WORKDIR /app
ENTRYPOINT [ "fish" ]
