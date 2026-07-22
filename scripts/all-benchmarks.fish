#! /usr/bin/env fish
# create 3 servers
ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml --extra-vars '{"bismuthdir": "/home/julian/Nextcloud/Uni/phd/Darmstadt/REScala", "nodenames": ["node1", "node2", "node3"]}' ansible/create-servers.yaml
set -x SERVERS 3

fish scripts/datacenter-benchmarks.fish

fish scripts/leader-failure-benchmark.fish

# create 5 servers
ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml --extra-vars '{"bismuthdir": "/home/julian/Nextcloud/Uni/phd/Darmstadt/REScala", "nodenames": ["node1", "node2", "node3", "node4", "node5"]}' ansible/create-servers.yaml
set -x SERVERS 5

fish scripts/datacenter-benchmarks.fish

# remove servers
ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml ansible/remove-all-servers.yaml

# run geo-repl benchmarks
fish scripts/geo-repl-benchmarks.fish
