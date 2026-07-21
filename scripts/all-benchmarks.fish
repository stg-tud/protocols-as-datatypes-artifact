# create servers
ansible-playbook -i hCloudInventory.hcloud.yaml -i inventory.yaml --extra-vars '{"bismuthdir": "/home/julian/Nextcloud/Uni/phd/Darmstadt/REScala", "nodenames": ["node1", "node2", "node3"]}' create-servers.yaml

# run datacenter local benchmarks 3 nodes
for workload in workloadb writeonly
    for system in pb etcd pb etcd pb etcd
        for threads in 1 2 3 4 5 10 20 50 100 200
            echo $workload $system $threads threads
            ansible-playbook -i hCloudInventory.hcloud.yaml -i inventory.yaml --extra-vars "{\"workload\": \"$workload\", \"threads\": $threads, \"operations\": 10000, \"recordcount\": 1000, \"type\": \"$system\", \"measurementtype\": \"raw\", \"max_batch_size\": 1, \"leader\": \"fsn-node1\", \"benchmark_timeout\": 500, \"leader_timeout\": 50000, \"comment\": \"artifact-eval\", \"commit_reads\": \"false\", \"op_timeout\": \"30\"}" run-benchmark.yaml
        end
    end
end

ansible-playbook -i hCloudInventory.hcloud.yaml -i inventory.yaml --extra-vars '{"bismuthdir": "/home/julian/Nextcloud/Uni/phd/Darmstadt/REScala", "nodenames": ["node1", "node2", "node3", "node4", "node5"]}' create-servers.yaml

# run datacenter local benchmarks 5 nodes
for workload in workloadb writeonly
    for system in pb etcd pb etcd pb etcd
        for threads in 1 2 3 4 5 10 20 50 100 200
            echo $workload $system $threads threads
            ansible-playbook -i hCloudInventory.hcloud.yaml -i inventory.yaml --extra-vars "{\"workload\": \"$workload\", \"threads\": $threads, \"operations\": 10000, \"recordcount\": 1000, \"type\": \"$system\", \"measurementtype\": \"raw\", \"max_batch_size\": 1, \"leader\": \"fsn-node1\", \"benchmark_timeout\": 500, \"leader_timeout\": 50000, \"comment\": \"artifact-eval\", \"commit_reads\": \"false\", \"op_timeout\": \"30\"}" run-benchmark.yaml
        end
    end
end

# remove servers
ansible-playbook -i hCloudInventory.hcloud.yaml -i inventory.yaml remove-all-servers.yaml
