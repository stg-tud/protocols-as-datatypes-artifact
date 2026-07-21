run_local_benchmark $SYSTEM $NUM_NODES="3" $THREADS="10":
  fish scripts/run-benchmark-local.fish

run_local_benchmark_etcd $NUM_NODES="3" $THREADS="10":
  just run_local_benchmark "etcd" {{NUM_NODES}} {{THREADS}}

run_local_benchmark_prdt $NUM_NODES="3" $THREADS="10":
  just run_local_benchmark "prdt" {{NUM_NODES}} {{THREADS}}

all_benchmarks_local:
  fish scripts/all-benchmarks-local.fish
