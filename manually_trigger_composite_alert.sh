#!/bin/bash
while true; do
  curl -s -X POST http://127.0.0.1:8888/api/v1/composite_alerts_trigger \
       -H "Content-Type: application/json" \
       -d '{"alert":"manually_trigger_composite_alert"}'
  sleep 300
done
