import requests
import json
import os

# github上で管理することを踏まえて，設定ファイルに保存しています
CONFIG_FILE_PATH = os.path.join(os.path.dirname(__file__), "config.json")

# 設定の読み込み
try:
    with open(CONFIG_FILE_PATH, "r") as f:
        config = json.load(f)
    FUNCTION_URL = config.get("FUNCTION_URL")
    API_KEY = config.get("API_KEY")
    DEVICE_TOKEN_TO_TEST = config.get("DEVICE_TOKEN_TO_TEST")

    if not all([FUNCTION_URL, API_KEY, DEVICE_TOKEN_TO_TEST]):
        raise ValueError(
            "FUNCTION_URL, API_KEY, or DEVICE_TOKEN_TO_TEST is missing in config.json")

except FileNotFoundError:
    print(f"Error: Configuration file not found at {CONFIG_FILE_PATH}")
    print("Please create config.json with FUNCTION_URL, API_KEY, and DEVICE_TOKEN_TO_TEST.")
    exit(1)
except ValueError as e:
    print(f"Error in configuration file: {e}")
    exit(1)
except json.JSONDecodeError:
    print(
        f"Error: Could not decode JSON from {CONFIG_FILE_PATH}. Please ensure it is valid JSON.")
    exit(1)


# リクエストデータ
payload = {
    "processId": "integrationTest001",
    "status": "START",
    "messageTitle": "Test Title",
    "messageBody": "This message was sent from a Python script for integration testing.",
    "deviceToken": DEVICE_TOKEN_TO_TEST,
    "taskName": "Test Task"
}

headers = {
    "Content-Type": "application/json",
    "Authorization": f"Bearer {API_KEY}"
}

# HTTP POST リクエストの送信
try:
    print(f"Sending request to: {FUNCTION_URL}")
    print(
        f"Headers: {{'Content-Type': '{headers.get('Content-Type')}', 'Authorization': 'Bearer [REDACTED]'}}")
    print(f"Payload: {json.dumps(payload, indent=2)}")

    response = requests.post(
        FUNCTION_URL, headers=headers, json=payload, timeout=10)

    # レスポンスの表示
    print("\n--- Response ---")
    print(f"Status Code: {response.status_code}")
    print("Headers:")
    for key, value in response.headers.items():
        print(f"  {key}: {value}")

    print("\nBody:")
    try:
        # レスポンスボディがJSON形式であれば整形して表示
        response_json = response.json()
        print(json.dumps(response_json, indent=2, ensure_ascii=False))
    except json.JSONDecodeError:
        # JSONでなければそのまま表示
        print(response.text)

except requests.exceptions.RequestException as e:
    print(f"\nAn error occurred during the request: {e}")
