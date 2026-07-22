#! /usr/bin/env fish
# run geo-replicated benchmarks 3 nodes
function run_benchmarks
    for workload in writeonly
        for system in pb etcd pb etcd pb etcd
            for threads in 1 20 50 75 100 200
                echo $workload $system $threads threads
                ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml --extra-vars "{\"workload\": \"$workload\", \"threads\": $threads, \"operations\": 1000, \"recordcount\": 100, \"type\": \"$system\", \"measurementtype\": \"raw\", \"max_batch_size\": 1, \"leader\": \"00leader-fsn1-node1\", \"benchmark_timeout\": 500, \"leader_timeout\": 50000, \"comment\": \"artifact-eval\", \"commit_reads\": \"false\", \"op_timeout\": \"60\"}" ansible/run-benchmark.yaml
            end
        end
    end
end

ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml --extra-vars '{"nodenames": ["node1"]}' ansible/setup-servers-distributed.yaml

run_benchmarks

# run geo-replicated benchmarks 9 nodes
ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml --extra-vars '{"nodenames": ["node1", "node2", "node3"]}' ansible/setup-servers-distributed.yaml

run_benchmarks

ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml ansible/remove-all-servers.yaml
