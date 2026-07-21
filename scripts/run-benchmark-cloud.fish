#! /usr/bin/env fish
# variables

if not set -q NUM_NODES
    set -x NUM_NODES 3
end
if not set -q THREADS
    set -x THREADS 50
end

if not set -q WORKLOAD
    set -x WORKLOAD workloadb
end

set nodenames [(string join ", " (string replace -r '(\d+)' '"node${1}"' (seq 1 $NUM_NODES)))]

# create servers
ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml --extra-vars "{\"nodenames\": $nodenames}" ansible/create-servers.yaml

for system in etcd pb
    ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml --extra-vars "{\"workload\": \"$WORKLOAD\", \"threads\": $THREADS, \"operations\": 100000, \"recordcount\": 1000, \"type\": \"$system\", \"measurementtype\": \"raw\", \"max_batch_size\": 1, \"leader\": \"fsn-node1\", \"benchmark_timeout\": 500, \"leader_timeout\": 50000, \"comment\": \"artifact-eval\", \"commit_reads\": \"false\", \"op_timeout\": \"30\"}" ansible/run-benchmark.yaml
end

# remove servers
ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml ansible/remove-all-servers.yaml
