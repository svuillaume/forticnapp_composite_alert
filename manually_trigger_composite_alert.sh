#!/bin/bash
set -e

echo "=== Setting up Web App ==="

# 1. Create project directory
PROJECT_DIR="/opt/manually_trigger_composite_alert"
mkdir -p "$PROJECT_DIR"
cd "$PROJECT_DIR"

# 2. Create Flask app
cat > app.py << 'EOF'
from flask import Flask, request

app = Flask(__name__)

@app.route("/health")
def health():
    return {"status": "ok"}

@app.route("/api/v1/manually_trigger_composite_alert", methods=["POST"])
def trigger():
    print("ALERT RECEIVED:", request.json)
    return {"result": "FortiCNAPP Demo Composite Alert"}

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8888)
EOF

# 3. Create requirements.txt
cat > requirements.txt << 'EOF'
Flask==3.0.0
EOF

# 4. Create Dockerfile
cat > Dockerfile << 'EOF'
FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY app.py .
EXPOSE 8888
CMD ["python", "app.py"]
EOF

# 5. Build Docker image
echo "Building Docker image..."
docker build -t manually_trigger_composite_alert .

# 6. Stop and remove existing container if running
docker stop manually_trigger_composite_alert 2>/dev/null || true
docker rm manually_trigger_composite_alert 2>/dev/null || true

# 7. Run Docker container
echo "Starting Docker container..."
docker run -d \
  --name manually_trigger_composite_alert \
  --restart unless-stopped \
  -p 8888:8888 \
  manually_trigger_composite_alert

# 8. Wait for container to be ready
echo "Waiting for Flask app to be ready..."
sleep 5
until curl -s http://127.0.0.1:8888/health > /dev/null 2>&1; do
  echo "Waiting..."
  sleep 2
done
echo "Flask app is ready!"

# 9. Create trigger script
cat > /usr/local/bin/manually_trigger_composite_alert.sh << 'EOF'
#!/bin/bash
while true; do
  curl -s -X POST http://127.0.0.1:8888/api/v1/manually_trigger_composite_alert \
       -H "Content-Type: application/json" \
       -d '{"alert":"manually_trigger_composite_alert"}'
  echo " - Alert sent at $(date)"
  sleep 300
done
EOF

chmod +x /usr/local/bin/manually_trigger_composite_alert.sh

# 10. Create systemd service
cat > /etc/systemd/system/manually_trigger_composite_alert.service << 'EOF'
[Unit]
Description=manually_trigger_composite_alert
After=network-online.target docker.service
Wants=network-online.target
Requires=docker.service

[Service]
Type=simple
ExecStart=/usr/local/bin/manually_trigger_composite_alert.sh
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 11. Enable and start the systemd service
echo "Enabling and starting systemd service..."
systemctl daemon-reload
systemctl enable manually_trigger_composite_alert.service
systemctl start manually_trigger_composite_alert.service

# 12. Show status
echo ""
echo "=== Setup Complete ==="
echo "Docker container status:"
docker ps | grep manually_trigger_composite_alert
echo ""
echo "Systemd service status:"
systemctl status manually_trigger_composite_alert.service --no-pager
echo ""
echo "View logs with:"
echo "  - Docker logs: docker logs -f manually_trigger_composite_alert"
echo "  - Service logs: journalctl -u manually_trigger_composite_alert -f"
echo "  - SystemD status: systemctl status manually_trigger_composite_alert"
