# PRDTs: Composable Design and Verification of Consensus Protocols using Replicated Data Types (Artifact)

This is the artifact for the paper _"PRDTs: Composable Design and Verification of Consensus Protocols using Replicated Data Types"_.

It contains the following components:

- PRDT library & protocol implementations (Scala)
- Performance benchmark
  - Key/value-store implementation (Scala)
  - Benchmark scripts (ansible & fish shell scripts)
  - Analysis scripts (python jupyter notebook)

# Download, Installation, and Sanity-Testing

## **Hardware and Software Requirements:**

Our performance benchmarks are supposed to be run on cloud infrastructure.
They require at least 4 Ubuntu machines (3 for the cluster, 1 for the benchmark runner) that you have SSH access to. For the artifact evaluation period of OOPSLA26, we give the reviewers access to remote servers that can be used to run our evaluation scripts.
However, if you are reading this after artifact evaluation has ended, you will have to bring your own servers for the remote benchmarks (see the section "Additional Artifact Description" for details).

For orchestrating the benchmarks and executing the other parts of the artifact, we provide two docker images, one for linux/amd64 and one for linux/arm64. The former should work on any 64 bit x86 system while the latter should work on 64 bit arm machines such as those based on Apple silicon chips.

<!-- Within the docker image, you will find everything needed to:

- compile and execute our code
- run remote benchmarks via ansible
- analyse the benchmark results and produce the figures from the paper

The remote benchmarks require at least 4 Ubuntu machines (3 for the cluster, 1 for the benchmark runner) that you have SSH access to. For the artifact evaluation period of OOPSLA26, we give the reviewers access to the remote servers that we have used for our evaluation.

>

However, if you are reading this after artifact evaluation has ended, you will have to bring your own servers for the remote benchmarks or stick with the local benchmarks.
In order to run the benchmarks without docker (for improved performance), you will need to install java (JDK 21) and a recent version of the [fish shell](https://fishshell.com/) (we tested 4.2.1 and 4.8.1). All other dependencies are packaged with the artifact.

_Disclaimer: While we aim to support local performance benchmarks, please note that it is generally a bad idea to have the benchmark runner and the benchmarked system on the same machine and that your results will differ from the ones reported in the paper.
Additionally, local benchmark perfomance in Docker is very suboptimal, especially on non-linux hosts. For these cases, we do support running the benchmark scripts locally without docker._

--->

## Download and Installation

Download the artifact from [https://doi.org/10.5281/zenodo.21442650](https://doi.org/10.5281/zenodo.21442650).
There you will find two docker images for easy execution and a zip file containing all the sources (code and scripts) used to build the docker images. The latter can be used to change and reuse the artifact, and to run it without docker.

**TODO: explain file structure**

Make sure that [docker](https://www.docker.com/) is installed on your machine ([podman](https://podman.io/) might work as well).
Then, pick the right docker image for your architecture and load it using:

```bash
docker image load -i dockerimage_amd64.tar # for x86
```

```bash
docker image load -i dockerimage_arm64.tar # for ARM
```

## Sanity Testing (kick-the-tires)

**Visualizing the Benchmark Results:**

After loading the image, copy the `paper_dataset` folder from the zip file that came with the artifact. It contains the benchmark results that were used to produce the figures in the paper.

Then, start the docker container from the folder containing the `paper_dataset` folder:

```bash
docker run --rm -it -v ./paper_dataset:/app/results -p 8888:8888  prdt-artifact
```

This should open an interactive shell from which you can run various scripts.

Try running the following command to start a jupyter notebook to analyze the pre-supplied benchmark results.

```bash
scripts/run-jupyter.fish
```

This should print a URL like `http://127.0.0.1:8888/tree?token=...` to your terminal. Copy this URL and paste it into a webbrowser.
Inside the jupyter menu, select the "Data_Visualization.ipynb" notebook and run it via "Kernel > Restart Kernel and Run All Cells". This should produce several tables and plots.

**Running Cloud Benchmarks:**

Next, we are going to perform a benchmark run on our supplied cloud-infrastructure.

> _Disclaimer: These scripts should only be run by one reviewer at a time since they run on the same infrastructure and running multiple benchmarks at once will affect the results._

Go back to the terminal running the jupyter notebook and stop it with Ctrl-C. Then, stop the running docker container (e.g. via Ctrl-D).

Create a folder to hold your benchmark results, e.g. `myresults`. Then mount it into the docker container using

```bash
docker run --rm -it -v ./myresults:/app/results -p 8888:8888  prdt-artifact
```

Try to run a benchmark on our supplied cloud-infrastructure:

```bash
scripts/test-benchmark-cloud.fish
```

This should run two simple benchmarks which takes around 7 minutes.
There might be ansible deprecation warnings but the script should still run and in the end the results should show up in your `myresults` folder.
After the benchmark has finished, you can start up the jupyter notebook again with the `scripts/run-jupyter.fish` command to inspect the results.
The notebook will fail to evaluate the later half of the cells because it is missing input data for the leader failure and geo-replicated benchmark. This is expected and okay for now.

# Evaluation Instructions

The main evaluation workflow when interacting with the artifact consists of two steps:

1. mount a dataset into the docker container
2. populate/analyze the dataset with the scripts in the `scripts` folder

**Loading a dataset:**

To load a dataset, mount it into the docker container. For example, to load the `paper_dataset`, run:

```bash
docker run --rm -it -v ./paper_dataset:/app/results -p 8888:8888  prdt-artifact
```

**Analyzing a dataset:**

To analyze a dataset, run our jupyter notebook with:

```bash
scripts/run-jupyter.fish
```

In the command output, look for a line containing an URL like `http://127.0.0.1:8888/tree?token=...`, copy the URL and open it with a browser. In jupyter, select and open the `Data_Visualization.ipynb` notebook. You can run it via “Kernel > Restart Kernel and Run All Cells”. This will produce the figures from the Evaluation section of the paper.
The notebook also allows you to inspect the data interactively and create new analyses.
By default, the notebook considers the **3 newest runs** per configuration (same workload, same number of servers, same system).

**Populating a dataset:**

In order to create a new dataset `my_dataset`, restart the container (Ctrl-C, Ctrl-D):

```bash
mkdir my_dataset
docker run --rm -it -v ./my_dataset:/app/results -p 8888:8888  prdt-artifact
```

You can then use the scripts in the `scripts` folder to populate the dataset.

> _Disclaimer: the cloud scripts should only be executed one at a time (by one reviewer at a time) because they will run on the same infrastructure, which will affect the results._

You can produce a complete dataset (read/write benchmarks, leader failure, geo-replication, 3 runs per config) with the following command. **Beware: The script takes about 17h to finish.**

```bash
scripts/all-benchmarks.fish
```

For a slightly quicker option, you can run individual benchmark scripts:

```bash
scripts/datacententer-benchmarks.fish # ~10h
scripts/geo-repl-benchmarks.fish # ~5h
scripts/leader-failure-benchmark.fish # ~80min
```

For the quickest option, you can run a single cloud benchmark using:

```bash
NUM_NODES=3 THREADS=10 WORKLOAD=writeonly scripts/run-benchmark-cloud.fish
```

This will provision the needed cloud servers, run the benchmark for both systems, retrieve the results into the dataset, and remove the servers again.
`NUM_NODES` controls the amount of physical machines that will be part of the cluster, while `THREADS` controls how many client threads will be stressing the system concurrently. `WORKLOAD` sets the YCSB workload that is going to be executed. We support `workloadb` (read-mostly workload) and `writeonly` (only writes).
**Each benchmark run takes ~7 minutes including setup and teardown.**

**Interpreting the results:**

Your results will differ from the ones that we reported in the paper.
In particular, there will always be a certain randomness factor when relying on cloud infrastructure. Any consensus-based key-value store is highly latency sensitive and we cannot control the exact location of the node machines. They will be located in the same data center but their exact location in the data center and the general network load of the data center will affect performance results.
To ensure a fair comparison, our scripts always run the benchmark for both systems (etcd and ours) on the same servers befor tearing them down again.

To make things worse, our cloud provider hetzner seems to be experiencing a high load on their cloud instances right now ([source](https://status.hetzner.com/incident/0a75c7ae-3377-41dc-aabe-601063724d24)).
In our recent experiments, this lead to 2-3x higher latencies when compared to the performance at paper submission time.
However, this affects both systems equally, and thus the general trends and observations that we describe in the paper still stand: With all nodes in the same data center, both systems perform comparably for writes, and our system outperforms etcd for reads.

See the reproducibility guidelines for further discussion.

# Reusability guidelines

# Reproducibility guidelines

Our evaluation makes one central claim:

- **Practical Feasability (Section 5):** PRDTs achieve performance comparable to established consensus implementations in real-world applications.

This claim can be evaluated by running the `all-benchmarks.fish` script and comparing the results for our PRDT-based system to etcd as described in the evaluation intructions.
While the exact performance numbers are dependent on the performance of the cloud environment, this should affect both systems and should not violate our main claim of comparable performance.

See the following figures for a comparison between the performance measurements that we conducted at paper submission time (first) and during artifact preparation (second).
We expect the reviewers to observe numbers similar to the latter plots.

**Paper Results:**

![](img/through_lat_both_read.png)
![](img/through_lat_both_write.png)

**Artifact Preparation Results** (changes are due to higher overall network latency, but still show the same trends):

![](img/through_lat_both_read_artifact.png)
![](img/through_lat_both_write_artifact.png)

# Additional artifact description (file structure, guidelines for extending the tool or adding new examples, etc.)

<!-- If you also want to use the artifact without docker, make sure that you install [fish shell](https://fishshell.com/) and java (>= JDK 21). See the section "Additional Artifact Description" for more details on running the artifact without docker. -->

> In the Hardware Dependencies section, describe the hardware required to evaluate the artifact. If the artifact requires specific hardware (e.g., many cores, disk space, GPUs, specific processors), please provide instructions on how to gain access to the hardware. Keep in mind that reviewers must remain anonymous.

If you are on Linux, also supply the `--tmpfs /tmp` flag to greatly improve benchmark performance:

```bash
docker run --rm -it -v ./results:/app/results -p 8888:8888 --entrypoint=fish  --tmpfs /tmp prdt-artifact
```

Afterwards, try running the following commands to run a local prdt/etcd benchmark respectively. They should take no more than 2 minutes each and should print a summary in the end.
The benchmark results should also appear in the previously created folder `results`.

```bash
NUM_NODES=3 THREADS=10 SYSTEM=prdt scripts/run-benchmark-local.fish
```

```bash
NUM_NODES=3 THREADS=10 SYSTEM=etcd scripts/run-benchmark-local.fish
```
