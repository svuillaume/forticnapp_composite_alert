from flask import Flask, request

app = Flask(__name__)

@app.route("/health")
def health():
    return {"status": "ok"}

@app.route("/api/v1/manual_composite_alert_trigger", methods=["POST"])
def trigger():
    print("ALERT RECEIVED:", request.json)
    return {"result": "manual_composite_alert_trigger"}

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8888)
