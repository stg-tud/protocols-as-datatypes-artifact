sudo tc qdisc add dev lo root handle 1: prio
sudo tc qdisc add dev lo parent 1:3 handle 30: netem delay 120ms
sudo tc filter add dev lo protocol ip parent 1:0 prio 3 u32 match ip dport 8111 0xffff flowid 1:3
sudo tc filter add dev lo protocol ip parent 1:0 prio 3 u32 match ip dport 8211 0xffff flowid 1:3
sudo tc filter add dev lo protocol ip parent 1:0 prio 3 u32 match ip dport 8311 0xffff flowid 1:3
