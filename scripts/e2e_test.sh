#!/bin/bash
# E2E Test Script for EnStudApp
# Runs inside enstud-build container via docker exec
# Requires: java, curl, bash

BASE_URL="http://172.20.0.6:8080"
PASS=0
FAIL=0
RESULTS=""

check() {
    local name="$1"
    local status="$2"
    local detail="$3"
    if [ "$status" = "true" ]; then
        PASS=$((PASS + 1))
        RESULTS="${RESULTS}  ✅ [PASS] $name${detail:+ — $detail}\n"
    else
        FAIL=$((FAIL + 1))
        RESULTS="${RESULTS}  ❌ [FAIL] $name${detail:+ — $detail}\n"
    fi
}

echo "============================================"
echo "  EnStudApp 端到端集成测试"
echo "  $(date -u '+%Y-%m-%d %H:%M:%S UTC')"
echo "============================================"
echo ""

# ===== Step 1: Check gateway health =====
echo "=== Step 1: Gateway Health ==="
resp=$(curl -s -w "\n%{http_code}" "$BASE_URL/actuator/health" 2>/dev/null)
http_code=$(echo "$resp" | tail -1)
body=$(echo "$resp" | sed '$d')
echo "$resp" | head -3
if echo "$body" | grep -q '"status":"UP"'; then
    check "Gateway Health Check" "true" "HTTP $http_code"
else
    check "Gateway Health Check" "false" "HTTP $http_code — body: ${body:0:200}"
fi

# ===== Step 2: Register new user =====
echo ""
echo "=== Step 2: Register New User ==="
TIMESTAMP=$(date +%s)
USERNAME="e2e_${TIMESTAMP}"
resp=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/user/register" \
  -H 'Content-Type: application/json' \
  -d '{"username":"'"$USERNAME"'","password":"Test123456!","email":"'"$USERNAME"'@test.com"}' 2>/dev/null)
http_code=$(echo "$resp" | tail -1)
body=$(echo "$resp" | sed '$d')
echo "  Register: HTTP $http_code — ${body:0:100}"
if [ "$http_code" = "200" ]; then
    check "Register User ($USERNAME)" "true" "HTTP $http_code"
else
    check "Register User ($USERNAME)" "false" "HTTP $http_code — ${body:0:200}"
fi

# ===== Step 3: Login =====
echo ""
echo "=== Step 3: Login ==="
resp=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/user/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"e2e_test4","password":"Test123456!"}' 2>/dev/null)
http_code=$(echo "$resp" | tail -1)
body=$(echo "$resp" | sed '$d')
echo "  Login: HTTP $http_code"
echo "  Body: ${body:0:200}"

# Extract token using basic string operations
TOKEN=""
if echo "$body" | grep -q '"accessToken"'; then
    TOKEN=$(echo "$body" | sed 's/.*"accessToken":"\([^"]*\)".*/\1/')
fi
echo "  Token: ${TOKEN:0:40}..."

if [ -n "$TOKEN" ]; then
    check "Login (e2e_test4)" "true" "HTTP $http_code, token received"
    LOGGED_IN=true
else
    check "Login (e2e_test4)" "false" "HTTP $http_code — no token"
    LOGGED_IN=false
fi

# ===== Step 4: User Info (authenticated) =====
echo ""
echo "=== Step 4: User Info ==="
if [ "$LOGGED_IN" = "true" ]; then
    resp=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/user/info" \
      -H "Authorization: Bearer $TOKEN" 2>/dev/null)
    http_code=$(echo "$resp" | tail -1)
    body=$(echo "$resp" | sed '$d')
    echo "  User Info: HTTP $http_code — ${body:0:150}"
    if [ "$http_code" = "200" ]; then
        check "User Info" "true" "HTTP $http_code"
    else
        check "User Info" "false" "HTTP $http_code — ${body:0:200}"
    fi
else
    echo "  SKIP (no token)"
fi

# ===== Step 5: Forum Posts (public) =====
echo ""
echo "=== Step 5: Forum Posts (public) ==="
resp=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/forum/posts?page=1&size=10" 2>/dev/null)
http_code=$(echo "$resp" | tail -1)
body=$(echo "$resp" | sed '$d')
echo "  Forum Posts: HTTP $http_code — ${body:0:150}"
if [ "$http_code" = "200" ]; then
    check "Forum Posts (public)" "true" "HTTP $http_code"
else
    check "Forum Posts (public)" "false" "HTTP $http_code — ${body:0:200}"
fi

# ===== Step 6: Create Forum Post =====
echo ""
echo "=== Step 6: Create Forum Post ==="
if [ "$LOGGED_IN" = "true" ]; then
    resp=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/forum/posts" \
      -H 'Content-Type: application/json' \
      -H "Authorization: Bearer $TOKEN" \
      -d '{"title":"E2E Test Post","content":"This is an automated test","category":"general"}' 2>/dev/null)
    http_code=$(echo "$resp" | tail -1)
    body=$(echo "$resp" | sed '$d')
    echo "  Create Post: HTTP $http_code — ${body:0:150}"
    if [ "$http_code" = "200" ]; then
        check "Create Forum Post" "true" "HTTP $http_code"
    else
        check "Create Forum Post" "false" "HTTP $http_code — ${body:0:200}"
    fi
fi

# ===== Step 7: Word Books (public) =====
echo ""
echo "=== Step 7: Word Books ==="
resp=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/word/wordbooks?userId=1" 2>/dev/null)
http_code=$(echo "$resp" | tail -1)
body=$(echo "$resp" | sed '$d')
echo "  Word Books: HTTP $http_code — ${body:0:150}"
if [ "$http_code" = "200" ]; then
    check "Word Books (public)" "true" "HTTP $http_code"
else
    check "Word Books (public)" "false" "HTTP $http_code — ${body:0:200}"
fi

# ===== Step 8: Writing Essays =====
echo ""
echo "=== Step 8: Writing Essays ==="
if [ "$LOGGED_IN" = "true" ]; then
    resp=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/writing/essays?page=1&size=10" \
      -H "Authorization: Bearer $TOKEN" 2>/dev/null)
    http_code=$(echo "$resp" | tail -1)
    body=$(echo "$resp" | sed '$d')
    echo "  Writing Essays: HTTP $http_code — ${body:0:150}"
    if [ "$http_code" = "200" ]; then
        check "Writing Essays" "true" "HTTP $http_code"
    else
        check "Writing Essays" "false" "HTTP $http_code — ${body:0:200}"
    fi
fi

# ===== Step 9: Chat Messages =====
echo ""
echo "=== Step 9: Chat Messages ==="
if [ "$LOGGED_IN" = "true" ]; then
    resp=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/chat/messages" \
      -H 'Content-Type: application/json' \
      -H "Authorization: Bearer $TOKEN" \
      -d '{"content":"Hello, this is an E2E test"}' 2>/dev/null)
    http_code=$(echo "$resp" | tail -1)
    body=$(echo "$resp" | sed '$d')
    echo "  Chat Messages: HTTP $http_code — ${body:0:150}"
    if [ "$http_code" = "200" ]; then
        check "Chat Messages" "true" "HTTP $http_code"
    else
        check "Chat Messages" "false" "HTTP $http_code — ${body:0:200}"
    fi
fi

# ===== Step 10: Translate =====
echo ""
echo "=== Step 10: Translate Service ==="
if [ "$LOGGED_IN" = "true" ]; then
    resp=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/translate/text" \
      -H 'Content-Type: application/json' \
      -H "Authorization: Bearer $TOKEN" \
      -d '{"text":"Hello world","source":"en","target":"zh"}' 2>/dev/null)
    http_code=$(echo "$resp" | tail -1)
    body=$(echo "$resp" | sed '$d')
    echo "  Translate: HTTP $http_code — ${body:0:150}"
    if [ "$http_code" = "200" ]; then
        check "Translate Text" "true" "HTTP $http_code"
    else
        check "Translate Text" "false" "HTTP $http_code — ${body:0:200}"
    fi
fi

# ===== Step 11: Read Service =====
echo ""
echo "=== Step 11: Read Service ==="
if [ "$LOGGED_IN" = "true" ]; then
    resp=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/read/articles?page=1&size=10" \
      -H "Authorization: Bearer $TOKEN" 2>/dev/null)
    http_code=$(echo "$resp" | tail -1)
    body=$(echo "$resp" | sed '$d')
    echo "  Read Articles: HTTP $http_code — ${body:0:150}"
    if [ "$http_code" = "200" ]; then
        check "Read Articles" "true" "HTTP $http_code"
    else
        check "Read Articles" "false" "HTTP $http_code — ${body:0:200}"
    fi
fi

# ===== Step 12: Word Stats (Feign call user->word) =====
echo ""
echo "=== Step 12: Word Stats ==="
if [ "$LOGGED_IN" = "true" ]; then
    resp=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/word/stats/3" \
      -H "Authorization: Bearer $TOKEN" 2>/dev/null)
    http_code=$(echo "$resp" | tail -1)
    body=$(echo "$resp" | sed '$d')
    echo "  Word Stats: HTTP $http_code — ${body:0:150}"
    if [ "$http_code" = "200" ]; then
        check "Word Stats" "true" "HTTP $http_code"
    else
        check "Word Stats" "false" "HTTP $http_code — ${body:0:200}"
    fi
fi

# ===== Summary =====
echo ""
echo "============================================"
echo "  测试结果汇总"
echo "============================================"
echo ""
printf "%b" "$RESULTS"
echo ""
echo "--------------------------------------------"
echo "  PASS: $PASS"
echo "  FAIL: $FAIL"
echo "  TOTAL: $((PASS + FAIL))"
echo "--------------------------------------------"

# Exit with error if any test failed
[ "$FAIL" -eq 0 ]
exit $?
