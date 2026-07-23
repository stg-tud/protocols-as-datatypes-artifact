#! /usr/bin/env fish
# create 3 servers
ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml --extra-vars '{"nodenames": ["node1", "node2", "node3"]}' ansible/create-servers.yaml
set -x SERVERS 3

set datacenter3TimeStart (date +%Y-%m-%d-%T:%z)
fish scripts/datacenter-benchmarks.fish
set datacenter3TimeEnd (date +%Y-%m-%d-%T:%z)

touch /app/results/timing.txt
echo "datacenter benchmark (3 nodes) started at $datacenter3TimeStart, ended at $datacenter3TimeEnd" >> /app/results/timing.txt

set leaderFailureTimeStart (date +%Y-%m-%d-%T:%z)
fish scripts/leader-failure-benchmark.fish
set leaderFailureTimeEnd (date +%Y-%m-%d-%T:%z)
echo "leader failure benchmark started at $leaderFailureTimeStart, ended at $leaderFailureTimeEnd" >> /app/results/timing.txt

# create 5 servers
ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml --extra-vars '{"nodenames": ["node1", "node2", "node3", "node4", "node5"]}' ansible/create-servers.yaml
set -x SERVERS 5

set datacenter5TimeStart (date +%Y-%m-%d-%T:%z)
fish scripts/datacenter-benchmarks.fish
set datacenter5TimeEnd (date +%Y-%m-%d-%T:%z)
echo "datacenter benchmark (5 nodes) started at $datacenter5TimeStart, ended at $datacenter5TimeEnd" >> /app/results/timing.txt

# remove servers
ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml ansible/remove-all-servers.yaml

# run geo-repl benchmarks
set geoReplTimeStart (date +%Y-%m-%d-%T:%z)
fish scripts/geo-repl-benchmarks.fish
set geoReplTimeEnd (date +%Y-%m-%d-%T:%z)
echo "geo-repl benchmark started at $geoReplTimeStart, ended at $geoReplTimeEnd" >> /app/results/timing.txt

echo "datacenter benchmark (3 nodes) started at $datacenter3TimeStart, ended at $datacenter3TimeEnd"
echo "datacenter benchmark (5 nodes) started at $datacenter5TimeStart, ended at $datacenter5TimeEnd"
echo "leader failure benchmark started at $leaderFailureTimeStart, ended at $leaderFailureTimeEnd"
echo "geo-repl benchmark started at $geoReplTimeStart, ended at $geoReplTimeEnd"
