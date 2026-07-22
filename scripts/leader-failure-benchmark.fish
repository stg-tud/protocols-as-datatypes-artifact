#! /usr/bin/env fish
if not set -q SERVERS
    # create servers
    ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml --extra-vars '{"bismuthdir": "/home/julian/Nextcloud/Uni/phd/Darmstadt/REScala", "nodenames": ["node1", "node2", "node3"]}' ansible/create-servers.yaml
end

# run leader failure benchmarks
for workload in writeonly
    for system in pb etcd pb etcd pb etcd
        for threads in 1 2 3 20
            echo $workload $system $threads threads
            ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml --extra-vars "{\"workload\": \"$workload\", \"kill_leader\": 10, \"threads\": $threads, \"operations\": 50000, \"recordcount\": 1000, \"type\": \"$system\", \"measurementtype\": \"raw\", \"max_batch_size\": 1, \"leader\": \"fsn-node1\", \"benchmark_timeout\": 500, \"leader_timeout\": 5000, \"comment\": \"plumtree\", \"commit_reads\": \"false\", \"op_timeout\": \"30\"}" ansible/run-benchmark.yaml
        end
    end
end

if not set -q SERVERS
    # remove servers
    ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml ansible/remove-all-servers.yaml
end
