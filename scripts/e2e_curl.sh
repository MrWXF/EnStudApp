#!/bin/bash
set -e
BASE_URL="http://172.20.0.6:8080"

echo "=== Step 1: Login ==="
LOGIN_RESP=$(curl -s -X POST "$BASE_URL/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"e2e_test4","password":"Test123456!"}')
echo "$LOGIN_RESP" | python3 -m json.tool 2>/dev/null || echo "$LOGIN_RESP"

# Extract token
TOKEN_RAW=$(echo "$LOGIN_RESP" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
echo "Token obtained: ${TOKEN_RAW:0:30}..."

echo ""
echo "=== Step 2: User Info ==="
curl -s -X GET "$BASE_URL/user/info" -H "Authorization: Bearer *** -w "\nHTTP_CODE:%{http_code}"

echo ""
echo "=== Step 3: Forum Posts ==="
curl -s -X GET "$BASE_URL/forum/posts?page=1&size=10" -w "\nHTTP_CODE:%{http_code}"

echo ""
echo "=== Step 4: Create Forum Post ==="
curl -s -X POST "$BASE_URL/forum/posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer *** \
  -d '{"title":"E2E Post","content":"Test content","category":"general"}' \
  -w "\nHTTP_CODE:%{http_code}"

echo ""
echo "=== Step 5: Word Books ==="
curl -s -X GET "$BASE_URL/word/wordbooks?userId=1" -w "\nHTTP_CODE:%{http_code}"

echo ""
echo "=== Step 6: Writing Essays ==="
curl -s -X GET "$BASE_URL/writing/essays?page=1&size=10" \
  -H "Authorization: Bearer *** -w "\nHTTP_CODE:%{http_code}"

echo ""
echo "=== Step 7: Chat Messages ==="
curl -s -X POST "$BASE_URL/chat/messages" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer *** \
  -d '{"content":"Hello, E2E test"}' \
  -w "\nHTTP_CODE:%{http_code}"

echo ""
echo "=== Step 8: Translate ==="
curl -s -X POST "$BASE_URL/translate/text" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer *** \
  -d '{"text":"Hello world","source":"en","target":"zh"}' \
  -w "\nHTTP_CODE:%{http_code}"

echo ""
echo "=== Step 9: Read Articles ==="
curl -s -X GET "$BASE_URL/read/articles?page=1&size=10" \
  -H "Authorization: Bearer *** -w "\nHTTP_CODE:%{http_code}"

echo ""
echo "=== Step 10: Word Stats ==="
curl -s -X GET "$BASE_URL/word/stats/3" \
  -H "Authorization: Bearer *** -w "\nHTTP_CODE:%{http_code}"
