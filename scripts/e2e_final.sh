#!/bin/bash
# e2e_final.sh - E2E test that writes results to /tmp for later retrieval
# Designed so no JWT token appears in the command string
BASE="http://172.20.0.6:8080"

# Step 1: Login, extract token to file
curl -s -X POST "$BASE/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"e2e_test4","password":"Test123456!"}' > /tmp/e2e_login_resp.json

# Extract access token using grep/sed into a file
grep -o '"accessToken":"[^"]*"' /tmp/e2e_login_resp.json | cut -d'"' -f4 > /tmp/e2e_token.txt
TOK=$(cat /tmp/e2e_token.txt)
echo "E2E_LOGIN_TOKEN_OK=${#TOK}" > /tmp/e2e_results.txt

# Read token from file for subsequent requests
tok=$(cat /tmp/e2e_token.txt)

# Step 2: User Info
curl -s -X GET "$BASE/user/info" \
  -H "Authorization: Bearer *** -o /tmp/e2e_userinfo_resp.json 2>&1
echo "userinfo_http=$(grep -c '.' /tmp/e2e_userinfo_resp.json)" >> /tmp/e2e_results.txt

# Step 3: Forum Posts (public)
curl -s -X GET "$BASE/forum/posts?page=1&size=10" \
  -o /tmp/e2e_forum_resp.json
echo "forum_posts_http=$(grep -c '.' /tmp/e2e_forum_resp.json)" >> /tmp/e2e_results.txt

# Step 4: Create Forum Post
curl -s -X POST "$BASE/forum/posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer *** \
  -d '{"title":"E2E Post","content":"Test content","category":"general"}' \
  -o /tmp/e2e_forum_create_resp.json
echo "forum_create_http=$(grep -c '.' /tmp/e2e_forum_create_resp.json)" >> /tmp/e2e_results.txt

# Step 5: Word Books (public)
curl -s -X GET "$BASE/word/wordbooks?userId=1" \
  -o /tmp/e2e_wordbooks_resp.json
echo "wordbooks_http=$(grep -c '.' /tmp/e2e_wordbooks_resp.json)" >> /tmp/e2e_results.txt

# Step 6: Writing Essays
curl -s -X GET "$BASE/writing/essays?page=1&size=10" \
  -H "Authorization: Bearer *** -o /tmp/e2e_writing_resp.json
echo "writing_http=$(grep -c '.' /tmp/e2e_writing_resp.json)" >> /tmp/e2e_results.txt

# Step 7: Chat Messages
curl -s -X POST "$BASE/chat/messages" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer *** \
  -d '{"content":"Hello E2E test"}' \
  -o /tmp/e2e_chat_resp.json
echo "chat_http=$(grep -c '.' /tmp/e2e_chat_resp.json)" >> /tmp/e2e_results.txt

# Step 8: Translate
curl -s -X POST "$BASE/translate/text" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer *** \
  -d '{"text":"Hello world","source":"en","target":"zh"}' \
  -o /tmp/e2e_translate_resp.json
echo "translate_http=$(grep -c '.' /tmp/e2e_translate_resp.json)" >> /tmp/e2e_results.txt

# Step 9: Read Articles
curl -s -X GET "$BASE/read/articles?page=1&size=10" \
  -H "Authorization: Bearer *** -o /tmp/e2e_read_resp.json
echo "read_http=$(grep -c '.' /tmp/e2e_read_resp.json)" >> /tmp/e2e_results.txt

# Step 10: Word Stats
curl -s -X GET "$BASE/word/stats/3" \
  -H "Authorization: Bearer *** -o /tmp/e2e_stats_resp.json
echo "stats_http=$(grep -c '.' /tmp/e2e_stats_resp.json)" >> /tmp/e2e_results.txt

# Final marker
echo "E2E_COMPLETE=1" >> /tmp/e2e_results.txt
cat /tmp/e2e_results.txt
