# a multi platform Dockerfile
FROM eclipse-temurin:21-jdk AS build
ARG TARGETPLATFORM

# install dependencies
RUN apt-get update && apt-get install -y wget curl just fish unzip fzf pipenv ansible python3-dateutil rsync

# install etcd
# RUN mkdir /etcd
# RUN if [ "$TARGETPLATFORM" = "linux/amd64" ]; then \
#     wget https://github.com/etcd-io/etcd/releases/download/v3.6.7/etcd-v3.6.7-linux-amd64.tar.gz; \
#     tar -xvzf etcd-v3.6.7-linux-amd64.tar.gz -C /etcd; \
#     cp /etcd/etcd-v3.6.7-linux-amd64/etcd* /bin/; \
#     rm etcd-v3.6.7-linux-amd64.tar.gz; \
#     fi
# RUN if [ "$TARGETPLATFORM" = "linux/arm64" ]; then \
#     wget https://github.com/etcd-io/etcd/releases/download/v3.6.7/etcd-v3.6.7-linux-arm64.tar.gz; \
#     tar -xvzf etcd-v3.6.7-linux-arm64.tar.gz -C /etcd; \
#     cp /etcd/etcd-v3.6.7-linux-arm64/etcd* /bin/; \
#     rm etcd-v3.6.7-linux-arm64.tar.gz; \
#     fi
#
# RUN rm -rf /etcd


# install coursier
WORKDIR /coursier
RUN curl -fLo coursier https://github.com/coursier/launchers/raw/master/coursier && chmod +x coursier
RUN ./coursier setup --yes
RUN ./coursier install sbt
RUN [ "ln", "-s", "/root/.local/share/coursier/bin/sbt", "/usr/local/bin/sbt"]

# build scala sources
# COPY src/ /app/
# WORKDIR /app
# RUN ["just", "package-bin"]
# verify that installed tools are available
RUN sbt --version
#RUN etcd --version

# RUN useradd -m -s /bin/bash artifact
# RUN mkdir -p /app && chown -R artifact:artifact /app
# USER artifact

# install python dependencies for jupyter
RUN mkdir -p /app/analysis
COPY analysis/Data_Visualization.ipynb /app/analysis/
COPY analysis/Pipfile /app/analysis/
COPY analysis/Pipfile.lock /app/analysis/
WORKDIR /app/analysis
RUN ["pipenv", "install"]

# install ansible deps
RUN ansible-galaxy collection install -f hetzner.hcloud

# copy PRDT source
# TODO: maybe actually compile in here
COPY bin/jars /app/jars

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
#CMD [ "--choose" ]
