# FortiCNAPP Composite Alert Generator

A lightweight Docker-based system for automatically generating FortiCNAPP composite alerts for testing and demonstration purposes.

## 📋 Overview

This project provides an automated way to trigger composite alerts in FortiCNAPP at regular intervals. It consists of:
- A Flask REST API server running in Docker
- A systemd service that sends periodic alert triggers
- Simple setup and cleanup scripts

## 🚀 Quick Start

### Prerequisites

- Docker installed and running
- Root/sudo access
- curl installed

### Installation & Setup

Run the setup script to deploy the complete system:
```bash
chmod +x manually_trigger_composite_alert.sh
sudo ./manually_trigger_composite_alert.sh
```

This will:
1. ✅ Create and containerize the Flask API server
2. ✅ Deploy the Docker container on port 8888
3. ✅ Install a systemd service to trigger alerts every 5 minutes
4. ✅ Start the alert generation automatically

### Cleanup

To stop and remove all components:
```bash
chmod +x cleanup.sh
sudo ./cleanup.sh
```

This will:
1. 🛑 Stop the systemd service
2. 🗑️ Remove the Docker container and image
3. 🧹 Clean up all configuration files

## 📁 Project Structure
```
.
├── README.md                              # This file
├── manually_trigger_composite_alert.sh    # Setup script
└── cleanup.sh                             # Cleanup script
```

After setup, the following components are created:
```
/opt/alert-trigger/                        # Project directory
├── app.py                                 # Flask API server
├── Dockerfile                             # Container definition
└── requirements.txt                       # Python dependencies

/usr/local/bin/trigger_alerts.sh           # Alert trigger script
/etc/systemd/system/alert-trigger.service  # Systemd service
```
