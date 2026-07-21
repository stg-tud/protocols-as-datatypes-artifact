This is the artifact for the paper "PRDTs: Composable Design and Verification of Consensus Protocols using Replicated Data Types".

It contains the following components:

- PRDT library & protocol implementations (Scala)
- Performance benchmark
  - Key/value-store implementation (Scala)
  - Benchmark scripts (ansible & fish shell scripts)
  - Analysis scripts (python jupyter notebook)

# Download, Installation, and Sanity-Testing

## **Hardware and Software Requirements:**

The artifact comes with two docker images, one for linux/amd64 and one for linux/arm64. The former should work on any 64 bit x86 system while the latter should work on 64 bit arm machines such as those based on Apple silicon chips.

Within the docker image, you will find everything needed to:

- compile and execute our code
- run limited local benchmarks
- run remote benchmarks via ansible
- analyse the benchmark results and produce the figures from the paper

The remote benchmarks require at least 4 Ubuntu machines (3 for the cluster, 1 for the benchmark runner) that you have SSH access to. For the artifact evaluation period of OOPSLA26, we give the reviewers access to the remote servers that we have used for our evaluation.

>

However, if you are reading this after artifact evaluation has ended, you will have to bring your own servers for the remote benchmarks or stick with the local benchmarks.
In order to run the benchmarks without docker (for improved performance), you will need to install java (JDK 21) and a recent version of the [fish shell](https://fishshell.com/) (we tested 4.2.1 and 4.8.1). All other dependencies are packaged with the artifact.

_Disclaimer: While we aim to support local performance benchmarks, please note that it is generally a bad idea to have the benchmark runner and the benchmarked system on the same machine and that your results will differ from the ones reported in the paper.
Additionally, local benchmark perfomance in Docker is very suboptimal, especially on non-linux hosts. For these cases, we do support running the benchmark scripts locally without docker._

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

If you also want to use the artifact without docker, make sure that you install [fish shell](https://fishshell.com/) and java (>= JDK 21). See the section "Additional Artifact Description" for more details on running the artifact without docker.

## Sanity Testing

After loading the image, copy the `results` folder from the zip file that came with the artifact.

Then, start the docker container from the folder containing the `results` folder:

```bash
docker run --rm -it -v ./results:/app/results -p 8888:8888 --entrypoint=fish  prdt-artifact
```

If you are on Linux, also supply the `--tmpfs /tmp` flag to greatly improve benchmark performance:

```bash
docker run --rm -it -v ./results:/app/results -p 8888:8888 --entrypoint=fish  --tmpfs /tmp prdt-artifact
```

This should open an interactive shell from which you can run various scripts.

Try running the following command to start a jupyter notebook to analyze the pre-supplied benchmark results. This should print a URL like `http://127.0.0.1:8888/tree?token=...` to your terminal. Copy this URL and paste it into a webbrowser.

```bash
scripts/run-jupyter.fish
```

Inside the jupyter menu, select the "Data_Visualization.ipynb" notebook and run it via "Kernel > Restart Kernel and Run All Cells". This should produce several tables and plots.
Go back to the terminal and press Ctrl-C to stop the jupyter notebook.

Afterwards, try running the following commands to run a local prdt/etcd benchmark respectively. They should take no more than 2 minutes each and should print a summary in the end.
The benchmark results should also appear in the previously created folder `results`.

```bash
NUM_NODES=3 THREADS=10 SYSTEM=prdt scripts/run-benchmark-local.fish
```

```bash
NUM_NODES=3 THREADS=10 SYSTEM=etcd scripts/run-benchmark-local.fish
```

Finally, try to run a benchmark on our supplied cloud-infrastructure. This should take around 7 minutes.

```bash
scripts/run-benchmark-cloud.fish
```

ENDE CHRISTIAN TEST
