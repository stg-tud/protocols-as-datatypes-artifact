#!/usr/bin/env fish
# run local benchmarks 3 nodes
for workload in workloadb writeonly
    for system in pb etcd
        for threads in 1 2 3 4 5 10 20 50 100 200
            echo $workload $system $threads threads
            set -x WORKLOAD $workload
            set -x SYSTEM $system
            set -x THREADS $threads
            scripts/run-benchmark-local.fish
            sleep 30
        end
    end
end

## run datacenter local benchmarks 5 nodes
#for workload in workloadb writeonly
#    for system in pb etcd pb etcd pb etcd
#        for threads in 1 2 3 4 5 10 20 50 100 200
#            echo $workload $system $threads threads
#            ansible-playbook -i hCloudInventory.hcloud.yaml -i inventory.yaml --extra-vars "{\"workload\": \"$workload\", \"threads\": $threads, \"operations\": 10000, \"recordcount\": 1000, \"type\": \"$system\", \"measurementtype\": \"raw\", \"max_batch_size\": 1, \"leader\": \"fsn-node1\", \"benchmark_timeout\": 500, \"leader_timeout\": 50000, \"comment\": \"artifact-eval\", \"commit_reads\": \"false\", \"op_timeout\": \"30\"}" run-benchmark.yaml
#        end
#    end
#end
