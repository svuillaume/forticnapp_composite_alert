
The objective is this project is generate easy FortiCNAPP Composite Alerts

### Step 1 run app.py

docker-compose up

### Step 2 Create a new systemd service unit 

sudo vi /etc/systemd/system/manually_trigger_composite_alert.service

###COPY manually_trigger_composite_alert.service### 

###Step 3 Trigger Alerts### 

sudo vi /usr/local/bin/manually_trigger_composite_alert.sh

Copy manually_trigger_composite_alert.sh to sudo vi /usr/local/bin/manually_trigger_composite_alert.sh
