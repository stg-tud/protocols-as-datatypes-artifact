#! /usr/bin/env fish
ansible-playbook -i ansible/hCloudInventory.hcloud.yaml -i ansible/inventory.yaml ansible/remove-all-servers.yaml
