# Bad Outbound IP Connection Simulation

This document provides a **simulated demonstration of detecting bad outbound IP connections** on a Linux system using `nc` and `lsof`.

---

## **1. Simulate Outbound Connection**

Using `nc` (netcat) to connect to a suspicious external IP:

```bash
ubuntu@ecom-web:~$ nc -v 92.118.39.212 3389
Connection to 92.118.39.212 3389 port [tcp/ms-wbt-server] succeeded!
```

* `92.118.39.212` → example of a **potentially malicious outbound IP**
* `3389` → Remote Desktop Protocol port (ms-wbt-server)
* `-v` → verbose mode

**Note:** This is a **simulation for testing purposes only**. Do not perform on production systems.

---

## **2. List Active TCP Connections**

Use `lsof` to view all active TCP connections and identify processes responsible:

```bash
sudo lsof -i -nP | grep TCP
```

### **Command Breakdown:**

| Option | Description                                |                             |
| ------ | ------------------------------------------ | --------------------------- |
| `sudo` | Run as root to see all processes           |                             |
| `lsof` | List open files (network sockets included) |                             |
| `-i`   | Filter for network sockets (TCP/UDP)       |                             |
| `-n`   | Disable DNS lookup (show numeric IP)       |                             |
| `-P`   | Show numeric port numbers                  |                             |
| `      | grep TCP`                                  | Filter only TCP connections |

### **Example Output:**

```text
COMMAND     PID USER   FD   TYPE DEVICE SIZE/OFF NODE NAME
nc       12345 ubuntu  3u  IPv4 654321      0t0  TCP 192.168.1.10:56789->92.118.39.212:3389 (ESTABLISHED)
```

* **COMMAND** → Process making the connection (`nc`)
* **PID** → Process ID
* **USER** → Owner
* **FD** → File descriptor
* **NAME** → Local IP:Port → Remote IP:Port, with connection state

---

## **3. Analysis & Notes**

* Outbound connections to unknown or suspicious IPs (like `92.118.39.212`) on uncommon ports may indicate **malware, data exfiltration, or misconfigurations**.
* `lsof` allows you to **identify the exact process** responsible.
* Combine with firewall, IDS/IPS, or WAF logs for **full security monitoring**.
* Use this in a **lab/testing environment** only for learning or security validation.

---

**References / Tools:**

* `nc` (Netcat) — [https://linux.die.net/man/1/nc](https://linux.die.net/man/1/nc)
* `lsof` — [https://linux.die.net/man/8/lsof](https://linux.die.net/man/8/lsof)
* `grep` — For filtering output

---

**Next Steps:**

* Integrate detection into a **monitoring or alerting workflow** (SIEM, FortiSIEM, or Lacework).
* Document **all outbound connections** and flag unknown IPs for further investigation.
