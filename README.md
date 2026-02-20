run a process named manually_trigger_composite_alert or put that as a parameter to any other process,

---


sudo vi /usr/local/bin/manually_trigger_composite_alert.sh
#!/bin/bash
while true; do
  curl -s -X POST http://127.0.0.1:8888/api/v1/composite_alerts_trigger \
       -H "Content-Type: application/json" \
       -d '{"alert":"persistent-drill"}'
  sleep 300
done

-------
app 
------
import requests

API_URL = "http://127.0.0.1:8888/api/v1/composite_alert"
ALERT_ID = "test_composite_alert"

payload = {
    "alert_id": ALERT_ID,
    "note": "Triggered manually on localhost"
}

try:
    response = requests.post(API_URL, json=payload)
    response.raise_for_status()
    print(f"Composite alert '{ALERT_ID}' triggered successfully!")
except requests.exceptions.RequestException as e:
    print(f"Failed to trigger alert: {e}")
----
systemd service

sudo vi  /etc/systemd/system/manually_trigger_composite_alert.service

[Unit]
Description=Manually Trigger Composite Alert (FortiCNAPP Demo)
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
ExecStart=/usr/local/bin/manually_trigger_composite_alert.sh
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target

================
