# Ansible Deployment

Install the required collections:

```bash
ansible-galaxy collection install -r infra/ansible/requirements.yml
```

Prepare a host:

```bash
ansible-playbook -i infra/ansible/inventory.ini infra/ansible/playbooks/setup-host.yml
```

Deploy the stack:

```bash
ansible-playbook -i infra/ansible/inventory.ini infra/ansible/playbooks/deploy.yml
```

Copy `inventory.example.ini` to `inventory.ini` and `group_vars/travel_hosts.example.yml` to `group_vars/travel_hosts.yml`, then adjust hostnames, users, and paths.

Do not commit real secrets. Replace `.env` on the target host with production values managed by a secrets platform such as HashiCorp Vault.
