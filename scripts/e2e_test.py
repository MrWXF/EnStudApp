#!/usr/bin/env python3
"""EnStudApp E2E Test - runs from WSL against docker container"""
import urllib.request, json, time, sys

BASE = "http://172.20.0.6:8080"
results = []

TOK_HEADER = "Bearer "
STAR_STAR_STAR = "***"" *"
TOK_HEADER = "Bearer " + "***"

def api(method, path, data=None, token=None):
    url = BASE + path
    req = urllib.request.Request(url, method=method)
    if data is not None:
        body = json.dumps(data).encode("utf-8")
        req.add_header("Content-Type", "application/json")
    else:
        body = None
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        r = urllib.request.urlopen(req, data=body, timeout=10)
        raw = r.read().decode()
        return str(r.status), json.loads(raw), raw
    except urllib.request.HTTPError as e:
        raw = e.read().decode()
        try:
            j = json.loads(raw)
        except:
            j = {"raw": raw[:200]}
        return str(e.code), j, raw
    except Exception as e:
        return "ERROR", {"error": str(e)}, str(e)

def check(name, ok, detail=""):
    results.append((name, ok, detail))

def rpt():
    icon = "PASS" if r[1] else "FAIL"
    print("  [{0}] {1} — {2}".format(icon, r[0], r[2][:120]))

print("=" * 50)
print("  EnStudApp End-to-End Test")
print("  " + time.strftime('%Y-%m-%d %H:%M:%S UTC', time.gmtime()))
print("=" * 50)

# Step 1: Login
print("\n=== Step 1: Login ===")
code, data, raw = api("POST", "/user/login", {"username": "e2e_test4", "password": "Test123456!"})
token = None
if code == "200" and data.get("code") == 0 and data.get("data"):
    d = data["data"]
    token = d.get("accessToken", "")
    check("Login (e2e_test4)", True, "userId=" + str(d.get('userId')))
    print("  Token: " + token[:40] + "...")
else:
    check("Login (e2e_test4)", False, "HTTP " + code + " — " + str(data)[:200])

# Step 2: Register
print("\n=== Step 2: Register ===")
ts = int(time.time())
code, data, raw = api("POST", "/user/register", {"username": "e2e_" + str(ts), "password": "Test123456!", "email": "e2e_" + str(ts) + "@test.com"})
check("Register User", code == "200" and data.get("code") == 0, "HTTP " + code + " — " + str(data)[:100])

# Step 3: Gateway reachable
check("Gateway Reachable", bool(token), "login worked, gateway routing OK")

# Step 4: User Info
print("\n=== Step 4: User Info ===")
if token:
    code, data, raw = api("GET", "/user/info", token=token)
    check("User Info", code == "200" and data.get("code") == 0, "HTTP " + code + " — " + str(data)[:150])
else:
    check("User Info", False, "no token")

# Step 5: Forum Posts (public)
print("\n=== Step 5: Forum Posts ===")
code, data, raw = api("GET", "/forum/posts?page=1&size=10")
check("Forum Posts (public)", code == "200", "HTTP " + code + " — " + str(data)[:100])

# Step 6: Create Forum Post
print("\n=== Step 6: Create Forum Post ===")
if token:
    code, data, raw = api("POST", "/forum/posts", {"title": "E2E Test Post", "content": "Test content", "category": "general"}, token=token)
    check("Create Forum Post", code == "200" and data.get("code") == 0, "HTTP " + code + " — " + str(data)[:150])
else:
    check("Create Forum Post", False, "no token")

# Step 7: Word Books
print("\n=== Step 7: Word Books ===")
code, data, raw = api("GET", "/word/wordbooks?userId=1")
check("Word Books (public)", code == "200", "HTTP " + code + " — " + str(data)[:100])

# Step 8: Writing Essays
print("\n=== Step 8: Writing Essays ===")
if token:
    code, data, raw = api("GET", "/writing/essays?page=1&size=10", token=token)
    check("Writing Essays", code == "200" and data.get("code") == 0, "HTTP " + code + " — " + str(data)[:150])
else:
    check("Writing Essays", False, "no token")

# Step 9: Chat Messages
print("\n=== Step 9: Chat Messages ===")
if token:
    code, data, raw = api("POST", "/chat/messages", {"content": "Hello, E2E test message"}, token=token)
    check("Chat Messages", code == "200" and data.get("code") == 0, "HTTP " + code + " — " + str(data)[:150])
else:
    check("Chat Messages", False, "no token")

# Step 10: Translate
print("\n=== Step 10: Translate ===")
if token:
    code, data, raw = api("POST", "/translate/text", {"text": "Hello world", "source": "en", "target": "zh"}, token=token)
    check("Translate Text", code == "200" and data.get("code") == 0, "HTTP " + code + " — " + str(data)[:150])
else:
    check("Translate Text", False, "no token")

# Step 11: Read Articles
print("\n=== Step 11: Read Articles ===")
if token:
    code, data, raw = api("GET", "/read/articles?page=1&size=10", token=token)
    check("Read Articles", code == "200" and data.get("code") == 0, "HTTP " + code + " — " + str(data)[:150])
else:
    check("Read Articles", False, "no token")

# Step 12: Word Stats (Feign)
print("\n=== Step 12: Word Stats ===")
if token:
    code, data, raw = api("GET", "/word/stats/3", token=token)
    check("Word Stats", code == "200" and data.get("code") == 0, "HTTP " + code + " — " + str(data)[:150])
else:
    check("Word Stats", False, "no token")

# Summary
print("\n" + "=" * 50)
print("  Result Summary")
print("=" * 50)
passed = sum(1 for r in results if r[1])
for r in results:
    icon = "PASS" if r[1] else "FAIL"
    print("  [{0}] {1} — {2}".format(icon, r[0], r[2][:120]))
print()
print("  PASS: " + str(passed) + "/" + str(len(results)))
print("=" * 50)
sys.exit(0 if passed == len(results) else 1)
