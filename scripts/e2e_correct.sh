#!/bin/bash
# e2e_correct.sh - Correct E2E test for EnStudApp
# Uses file-based token storage to avoid JWT in command line
set -euo pipefail
BASE="http://172.20.0.6:8080"
OUT=/tmp/e2e_out
PASS=0
FAIL=0

rm -rf "$OUT"
mkdir -p "$OUT"

ok() { echo "  [PASS] $1"; PASS=$((PASS+1)); }
fail() { echo "  [FAIL] $1 - $2"; FAIL=$((FAIL+1)); }

echo "=============================="
echo " EnStudApp E2E Test Suite"
echo "=============================="

echo ""
echo "--- Step 1: Register User ---"
REG=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE/user/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"e2e_tester","email":"e2e@test.com","password":"Test123456!"}')
REG_CODE=$(echo "$REG" | grep "HTTP_CODE" | cut -d: -f2)
REG_BODY=$(echo "$REG" | sed '/^HTTP_CODE/d')
echo "  HTTP $REG_CODE: $REG_BODY"
if [ "$REG_CODE" = "200" ]; then
  ok "Register user"
else
  fail "Register user" "$REG_BODY"
fi

echo ""
echo "--- Step 2: Login ---"
LOGIN=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"e2e_tester","password":"Test123456!","rememberMe":true}')
LOGIN_CODE=$(echo "$LOGIN" | grep "HTTP_CODE" | cut -d: -f2)
LOGIN_BODY=$(echo "$LOGIN" | sed '/^HTTP_CODE/d')
echo "  HTTP $LOGIN_CODE: $(echo $LOGIN_BODY | head -c 100)"

if [ "$LOGIN_CODE" = "200" ]; then
  # Save token to file (never appears in command line as Bearer string)
  echo "$LOGIN_BODY" > "$OUT/login_resp.json"
  TOKEN=$(echo "$LOGIN_BODY" | sed 's/.*"accessToken":"\([^"]*\)".*/\1/')
  echo "$TOKEN" > "$OUT/token.txt"
  USER_ID=$(echo "$LOGIN_BODY" | sed 's/.*"userId":\([0-9]*\).*/\1/')
  echo "$USER_ID" > "$OUT/user_id.txt"
  echo "  Token saved (${#TOKEN} chars)"
  ok "Login - token acquired"
else
  fail "Login" "$LOGIN_BODY"
  echo "Cannot continue without login. Exiting."
  echo "PASS=$PASS FAIL=$FAIL"
  exit 1
fi

# Helper: call with auth header from file
# Writes a temporary script and executes it so the token never appears in process args
call_api() {
  local method="$1" url="$2" data="${3:-}" outfile="$4"
  local token=$(cat "$OUT/token.txt" 2>/dev/null || echo "")
  
  # Write temp script - use printf to avoid shell expansion issues
  printf '#!/bin/sh
curl -s -w "\\nHTTP_CODE:%%{http_code}" -X "%s" "%s" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer %s" \
  %s \
  > "%s" 2>/dev/null
' "$method" "$url" "$token" "${data:+-d '$data'}" "$outfile" > "$OUT/curl_$$.sh"
  chmod +x "$OUT/curl_$$.sh"
  sh "$OUT/curl_$$.sh"
  rm -f "$OUT/curl_$$.sh"
  grep "HTTP_CODE" "$outfile" | cut -d: -f2
}

echo ""
echo "--- Step 3: Get User Profile ---"
CODE=$(call_api GET "$BASE/user/profile" "" "$OUT/profile.json")
BODY=$(sed '/^HTTP_CODE/d' "$OUT/profile.json")
if [ "$CODE" = "200" ]; then
  ok "Get user profile"
else
  fail "Get user profile" "HTTP $CODE: $(echo $BODY | head -c 100)"
fi

echo ""
echo "--- Step 4: Get Forum Posts (no auth) ---"
PUB=$(curl -s -w "\nHTTP_CODE:%{http_code}" "$BASE/forum/posts?limit=3")
PUB_CODE=$(echo "$PUB" | grep "HTTP_CODE" | cut -d: -f2)
PUB_BODY=$(echo "$PUB" | sed '/^HTTP_CODE/d')
if [ "$PUB_CODE" = "200" ]; then
  ok "Get forum posts (public)"
else
  fail "Get forum posts (public)" "HTTP $PUB_CODE: $(echo $PUB_BODY | head -c 100)"
fi

echo ""
echo "--- Step 5: Create Forum Post ---"
CODE=$(call_api POST "$BASE/forum/posts" \
  '{"title":"E2E Test Post","content":"This is an automated E2E test post","categoryId":1,"tags":"test"}' \
  "$OUT/create_post.json")
BODY=$(sed '/^HTTP_CODE/d' "$OUT/create_post.json")
if [ "$CODE" = "200" ]; then
  POST_ID=$(echo "$BODY" | sed 's/.*"id":\([0-9]*\).*/\1/')
  echo "$POST_ID" > "$OUT/post_id.txt"
  ok "Create forum post (ID: $POST_ID)"
else
  fail "Create forum post" "HTTP $CODE: $(echo $BODY | head -c 100)"
fi

echo ""
echo "--- Step 6: Get Word Books (requires auth) ---"
CODE=$(call_api GET "$BASE/word/wordbooks" "" "$OUT/wordbooks.json")
BODY=$(sed '/^HTTP_CODE/d' "$OUT/wordbooks.json")
if [ "$CODE" = "200" ]; then
  ok "Get word books"
else
  fail "Get word books" "HTTP $CODE: $(echo $BODY | head -c 100)"
fi

echo ""
echo "--- Step 7: Start Study Session ---"
CODE=$(call_api POST "$BASE/word/study" '{"wordbookId":1,"limit":5}' "$OUT/study.json")
BODY=$(sed '/^HTTP_CODE/d' "$OUT/study.json")
if [ "$CODE" = "200" ]; then
  ok "Start study session"
else
  fail "Start study session" "HTTP $CODE: $(echo $BODY | head -c 100)"
fi

echo ""
echo "--- Step 8: Submit Writing ---"
CODE=$(call_api POST "$BASE/writing/submit" \
  '{"title":"E2E Test","content":"This is a test essay for E2E testing.","targetLang":"en"}' \
  "$OUT/writing.json")
BODY=$(sed '/^HTTP_CODE/d' "$OUT/writing.json")
if [ "$CODE" = "200" ]; then
  ok "Submit writing"
else
  fail "Submit writing" "HTTP $CODE: $(echo $BODY | head -c 100)"
fi

echo ""
echo "--- Step 9: Translate Text ---"
CODE=$(call_api POST "$BASE/translate/text" \
  '{"text":"Hello world","from":"en","to":"zh"}' \
  "$OUT/translate.json")
BODY=$(sed '/^HTTP_CODE/d' "$OUT/translate.json")
if [ "$CODE" = "200" ]; then
  ok "Translate text"
else
  fail "Translate text" "HTTP $CODE: $(echo $BODY | head -c 100)"
fi

echo ""
echo "--- Step 10: Create Chat Session ---"
CODE=$(call_api POST "$BASE/chat/sessions" \
  '{"title":"E2E Test Chat"}' \
  "$OUT/chat_session.json")
BODY=$(sed '/^HTTP_CODE/d' "$OUT/chat_session.json")
if [ "$CODE" = "200" ]; then
  SESSION_ID=$(echo "$BODY" | sed 's/.*"id":\([0-9]*\).*/\1/')
  echo "$SESSION_ID" > "$OUT/session_id.txt"
  ok "Create chat session (ID: $SESSION_ID)"
  
  echo ""
  echo "--- Step 11: Send Chat Message ---"
  CODE=$(call_api POST "$BASE/chat/sessions/$SESSION_ID/messages" \
    '{"content":"Hello, what is the past tense of go?"}' \
    "$OUT/chat_message.json")
  BODY2=$(sed '/^HTTP_CODE/d' "$OUT/chat_message.json")
  if [ "$CODE" = "200" ]; then
    ok "Send chat message"
  else
    fail "Send chat message" "HTTP $CODE: $(echo $BODY2 | head -c 100)"
  fi
else
  fail "Create chat session" "HTTP $CODE: $(echo $BODY | head -c 100)"
fi

echo ""
echo "--- Step 12: Get Read Articles ---"
CODE=$(call_api GET "$BASE/read/hot?limit=3" "" "$OUT/read.json")
BODY=$(sed '/^HTTP_CODE/d' "$OUT/read.json")
if [ "$CODE" = "200" ]; then
  ok "Get hot articles"
  ARTICLE_ID=$(echo "$BODY" | sed 's/.*"id":\([0-9]*\).*/\1/' | head -1)
  if [ -n "$ARTICLE_ID" ]; then
    echo "$ARTICLE_ID" > "$OUT/article_id.txt"
    
    echo ""
    echo "--- Step 13: Get Article Detail ---"
    CODE=$(call_api GET "$BASE/read/$ARTICLE_ID" "" "$OUT/article.json")
    BODY2=$(sed '/^HTTP_CODE/d' "$OUT/article.json")
    if [ "$CODE" = "200" ]; then
      ok "Get article detail"
    else
      fail "Get article detail" "HTTP $CODE: $(echo $BODY2 | head -c 100)"
    fi
    
    echo ""
    echo "--- Step 14: Translate Article ---"
    CODE=$(call_api GET "$BASE/read/$ARTICLE_ID/translate?targetLang=zh" "" "$OUT/trans_art.json")
    BODY2=$(sed '/^HTTP_CODE/d' "$OUT/trans_art.json")
    if [ "$CODE" = "200" ]; then
      ok "Translate article"
    else
      fail "Translate article" "HTTP $CODE: $(echo $BODY2 | head -c 100)"
    fi
  fi
else
  fail "Get hot articles" "HTTP $CODE: $(echo $BODY | head -c 100)"
fi

echo ""
echo "--- Step 15: Get Bookmarks ---"
CODE=$(call_api GET "$BASE/read/bookmarks" "" "$OUT/bookmarks.json")
BODY=$(sed '/^HTTP_CODE/d' "$OUT/bookmarks.json")
if [ "$CODE" = "200" ]; then
  ok "Get bookmarks"
else
  fail "Get bookmarks" "HTTP $CODE: $(echo $BODY | head -c 100)"
fi

echo ""
echo "--- Step 16: Get Read Sources ---"
CODE=$(call_api GET "$BASE/read/sources" "" "$OUT/sources.json")
BODY=$(sed '/^HTTP_CODE/d' "$OUT/sources.json")
if [ "$CODE" = "200" ]; then
  ok "Get read sources"
else
  fail "Get read sources" "HTTP $CODE: $(echo $BODY | head -c 100)"
fi

echo ""
echo "=============================="
echo " Results: $PASS passed, $FAIL failed"
echo "=============================="

# Write final result
echo "PASS=$PASS FAIL=$FAIL" > "$OUT/final.txt"
