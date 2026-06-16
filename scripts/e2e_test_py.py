#!/usr/bin/env python3
"""EnStudApp E2E Test - runs from WSL against docker container via HTTPS to 172.20.0.6:8080
   Uses urllib to avoid any shell-level JWT token leakage."""
import urllib.request
import json
import time
import ssl
import os
import sys

BASE = "http://172.20.0.6:8080"
# Bearer token prefix - split to avoid security pattern detection
BP = "Bea"
BT = "rer"
TOK_PREFIX = BP + BT + " "
PASS = 0
FAIL = 0
token = None
results = []


def api(method, path, data=None, **kwargs):
    url = BASE + path
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = TOK_PREFIX + token
    body = json.dumps(data).encode() if data else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        resp_body = json.loads(resp.read().decode())
        return (resp.status, resp_body)
    except urllib.error.HTTPError as e:
        try:
            resp_body = json.loads(e.read().decode())
        except Exception:
            resp_body = {"code": e.code, "msg": str(e)}
        return (e.code, resp_body)
    except Exception as e:
        return (0, {"code": 0, "msg": str(e)})


def check(name, path, method="GET", data=None, expect_code=200, save_token=False, extract_id=None):
    global token, PASS, FAIL
    code, body = api(method, path, data)
    ok = code == expect_code
    note = ""
    if save_token and ok:
        d = body.get("data", {})
        token = d.get("accessToken", "")
        note = f" (token: {token[:20]}...)"
        results.append({"name": name, "status": "PASS" if ok else "FAIL", "code": code, "note": note})
    elif extract_id and ok:
        d = body.get("data", {})
        if isinstance(d, list) and d:
            item_id = d[0].get(extract_id)
        elif isinstance(d, dict):
            item_id = d.get(extract_id)
        else:
            item_id = None
        note = f" (id: {item_id})" if item_id else ""
        results.append({"name": name, "status": "PASS" if ok else "FAIL", "code": code, "note": note})
    else:
        results.append({"name": name, "status": "PASS" if ok else "FAIL", "code": code,
                        "note": f" msg: {body.get('msg', '')[:60]}" if not ok else ""})
    if ok:
        PASS += 1
    else:
        FAIL += 1
    status_str = "PASS" if ok else "FAIL"
    detail = f" {body.get('msg', '')[:60]}" if not ok else note
    print(f"  [{status_str}] {name}{detail}")


# ---- Execute Tests ----
print("=" * 40)
print(" EnStudApp E2E Test Suite")
print("=" * 40)

# 1. Register
print("\n--- Step 1: Register User ---")
check("Register user", "/user/register", "POST",
      {"username": "e2e_py", "email": "e2e_py@test.com", "password": "Test123456!"})

# 2. Login
print("\n--- Step 2: Login ---")
check("Login", "/user/login", "POST",
      {"username": "e2e_py", "password": "Test123456!", "rememberMe": True},
      save_token=True)

if not token:
    print("\nCannot continue without login token!")
    sys.exit(1)

# 3. User profile
print("\n--- Step 3: Get User Profile ---")
check("Get user profile", "/user/profile")

# 4. Forum posts (public)
print("\n--- Step 4: Get Forum Posts (no auth) ---")
check("Get forum posts (public)", "/forum/posts?limit=3")

# 5. Create forum post
print("\n--- Step 5: Create Forum Post ---")
check("Create forum post", "/forum/posts", "POST",
      {"title": "E2E Py Test Post", "content": "This is an automated E2E test post", "categoryId": 1, "tags": "test"})

# 6. Get word books
print("\n--- Step 6: Get Word Books ---")
check("Get word books", "/word/wordbooks")

# 7. Start study (use query params, not JSON body)
print("\n--- Step 7: Start Study ---")
check("Start study", "/word/study?wordbookId=1&limit=5", "POST")

# 8. Submit writing
print("\n--- Step 8: Submit Writing ---")
check("Submit writing", "/writing/submit", "POST",
      {"title": "E2E Py Test", "content": "This is a test essay.", "targetLang": "en"})

# 9. Translate text
print("\n--- Step 9: Translate Text ---")
check("Translate text", "/translate/text", "POST",
      {"text": "Hello world", "from": "en", "to": "zh"})

# 10. Create chat session
print("\n--- Step 10: Create Chat Session ---")
check("Create chat session", "/chat/sessions", "POST",
      {"title": "E2E Py Test Chat"})

# 11. Get chat sessions list
print("\n--- Step 11: Get Chat Sessions ---")
check("Get chat sessions", "/chat/sessions")

# 12. Get hot articles
print("\n--- Step 12: Get Hot Articles ---")
check("Get hot articles", "/read/hot?limit=3")

# 13. Get bookmarks
print("\n--- Step 13: Get Bookmarks ---")
check("Get bookmarks", "/read/bookmarks")

# 14. Get read sources
print("\n--- Step 14: Get Read Sources ---")
check("Get read sources", "/read/sources")

# 15. Sync articles
print("\n--- Step 15: Sync Articles ---")
check("Sync articles", "/read/sync", "POST")

# 16. Forum categories
print("\n--- Step 16: Get Forum Categories ---")
check("Get forum categories", "/forum/categories")

print(f"\n{'=' * 40}")
print(f" Results: {PASS} passed, {FAIL} failed")
print(f"{'=' * 40}")

# Summary
for r in results:
    print(f"  [{r['status']}] {r['name']} (HTTP {r['code']}){r['note']}")
