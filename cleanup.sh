#!/bin/bash
set -e

echo "=== Cleaning up Flask Alert Trigger System ==="

# 1. Stop and disable systemd service
echo "Stopping systemd service..."
systemctl stop alert-trigger.service 2>/dev/null || true
systemctl disable alert-trigger.service 2>/dev/null || true

# 2. Remove systemd service file
echo "Removing systemd service file..."
rm -f /etc/systemd/system/alert-trigger.service
systemctl daemon-reload

# 3. Remove trigger script
echo "Removing trigger script..."
rm -f /usr/local/bin/trigger_alerts.sh

# 4. Stop and remove Docker container
echo "Stopping and removing Docker container..."
docker stop alert-trigger 2>/dev/null || true
docker rm alert-trigger 2>/dev/null || true

# 5. Remove Docker image
echo "Removing Docker image..."
docker rmi alert-trigger-app 2>/dev/null || true

# 6. Remove project directory
echo "Removing project directory..."
rm -rf /opt/alert-trigger

echo ""
echo "=== Cleanup Complete ==="
echo "All components have been removed."
