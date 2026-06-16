#!/bin/bash
# e2e_clean.sh - Cleans E2E test for EnStudApp
set -euo pipefail
BASE="http://172.20.0.6:8080"
OUT=/tmp/e2e_out

mkdir -p "$OUT"

# Step 1: Login, extract token
curl -s -X POST "$BASE/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"e2e_test4","password":"Test123456!"}' > "$OUT/login.json" 2>/dev/null

TOKEN=$(python3 -c "
import json
with open('$OUT/login.json') as f:
    d = json.load(f)
print(d.get('data', {}).get('accessToken', ''))
" 2>/dev/null || echo "")

if [ -z "$TOKEN" ]; then
  # fallback grep
  TOKEN=$(grep -o '"accessToken":"[^"]*"' "$OUT/login.json" | cut -d'"' -f4)
fi

echo "TOKEN_OK=$(*** $TOKEN | head -c 30)" > "$OUT/results.txt"

# Function to call API
call_api() {
  local name=$1 method=$2 url=$3 data=$4
  local file="$OUT/$name.json"
  
  if [ -n "$data" ]; then
    curl -s -X "$method" "$url" \
      -H "Content-Type: application/json" \
      -H "$AUTH" \
      -d "$data" > "$file" 2>/dev/null
  else
    curl -s -X "$method" "$url" \
      -H "$AUTH" > "$file" 2>/dev/null
  fi
  
  # Get first line of response
  head -c 200 "$file" >> "$OUT/results.txt"
  echo "" >> "$OUT/results.txt"
}

# Set auth header
AUTH="Authorization: Bearer $TOKEN"

# User Info
call_api "userinfo" "GET" "$BASE/user/info" ""

# Forum Posts (public) - no auth
curl -s -X GET "$BASE/forum/posts?page=1&size=10" > "$OUT/forum_posts.json" 2>/dev/null
echo "FORUM_POSTS=$(head -c 100 "$OUT/forum_posts.json")" >> "$OUT/results.txt"

# Create Forum Post
call_api "forum_create" "POST" "$BASE/forum/posts" '{"title":"E2E Post","content":"Test content","category":"general"}'

# Word Books (public)
curl -s -X GET "$BASE/word/wordbooks?userId=1" > "$OUT/wordbooks.json" 2>/dev/null
echo "WORDBOOKS=$(head -c 100 "$OUT/wordbooks.json")" >> "$OUT/results.txt"

# Writing Essays
call_api "writing" "GET" "$BASE/writing/essays?page=1&size=10" ""

# Chat Messages
call_api "chat" "POST" "$BASE/chat/messages" '{"content":"Hello E2E test"}'

# Translate
call_api "translate" "POST" "$BASE/translate/text" '{"text":"Hello world","source":"en","target":"zh"}'

# Read Articles
call_api "read" "GET" "$BASE/read/articles?page=1&size=10" ""

# Word Stats
call_api "stats" "GET" "$BASE/word/stats/3" ""

echo "E2E_DONE=1" >> "$OUT/results.txt"
echo "E2E_RESULTS_WRITTEN"
