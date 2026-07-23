#! /usr/bin/env fish
if not set -q RUNS
    set RUNS 1
end

function run_bench
    for run in (seq 1 $RUNS)
        for workload in workloadb writeonly
            for system in pb etcd
                for threads in 1 2 3 10 20 50 100 200
                    echo run $run $workload $system $threads threads
                    ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml --extra-vars "{\"workload\": \"$workload\", \"threads\": $threads, \"operations\": 100000, \"recordcount\": 1000, \"type\": \"$system\", \"measurementtype\": \"raw\", \"max_batch_size\": 1, \"leader\": \"fsn-node1\", \"benchmark_timeout\": 500, \"leader_timeout\": 50000, \"comment\": \"artifact-eval\", \"commit_reads\": \"false\", \"op_timeout\": \"1\"}" ansible/run-benchmark.yaml
                end
            end
        end
    end
end

if not set -q SERVERS
	# run datacenter local benchmarks 3 nodes
	# create servers
	ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml --extra-vars '{"bismuthdir": "/home/julian/Nextcloud/Uni/phd/Darmstadt/REScala", "nodenames": ["node1", "node2", "node3"]}' ansible/create-servers.yaml
	run_bench

	# run datacenter local benchmarks 5 nodes
	ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml --extra-vars '{"bismuthdir": "/home/julian/Nextcloud/Uni/phd/Darmstadt/REScala", "nodenames": ["node1", "node2", "node3", "node4", "node5"]}' ansible/create-servers.yaml
	run_bench

	ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml ansible/remove-all-servers.yaml
else
	run_bench
end
