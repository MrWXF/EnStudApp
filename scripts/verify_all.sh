#!/bin/bash
# EnStudApp 全量功能巡检 — 覆盖用户/单词/对话/写作/翻译/阅读/论坛 + 前端资产
BASE=http://localhost/api
PASS=0; FAIL=0; LAST=""

api() { # name method path [json_body]
  local name="$1" method="$2" path="$3" data="$4"
  local args=(-s -X "$method" --max-time 120 -H "Authorization: Bearer $TOKEN" "$BASE$path")
  [ -n "$data" ] && args+=(-H "Content-Type: application/json" -d "$data")
  local resp; resp=$(curl "${args[@]}")
  local code; code=$(echo "$resp" | head -c 200 | grep -o '"code":[0-9-]*' | head -1 | cut -d: -f2)
  if [ "$code" = "0" ]; then
    echo "PASS  $name"; PASS=$((PASS+1)); LAST="$resp"
  else
    echo "FAIL  $name → $(echo "$resp" | head -c 200)"; FAIL=$((FAIL+1)); LAST="$resp"
  fi
}

extract() { # regex from $LAST
  echo "$LAST" | sed -n "s/.*$1.*/\1/p" | head -1
}

echo "========== 0. 登录 =========="
LOGIN=$(curl -s -X POST "$BASE/user/login" -H "Content-Type: application/json" -d '{"username":"survey_1792900121","password":"test123456"}')
TOKEN=$(echo "$LOGIN" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
if [ -n "$TOKEN" ]; then echo "PASS  login (token ${#TOKEN} chars)"; PASS=$((PASS+1)); else echo "FAIL  login"; FAIL=$((FAIL+1)); fi

echo "========== 1. 用户模块 =========="
api "GET /user/profile" GET /user/profile
api "GET /user/stats" GET /user/stats
api "GET /user/daily-journey" GET /user/daily-journey
api "GET /user/goal" GET /user/goal
api "GET /user/goal/status" GET /user/goal/status
api "POST /user/goal/checkin" POST /user/goal/checkin

# 关注流程：注册用户 B → A 关注 B → 查列表/状态 → 取关
SUFX=$RANDOM
REG=$(curl -s -X POST "$BASE/user/register" -H "Content-Type: application/json" -d "{\"username\":\"verify_$SUFX\",\"password\":\"test123456\",\"nickname\":\"verifier$SUFX\",\"email\":\"v$SUFX@test.com\"}")
# 注册接口不返回 userId，从数据库取
BID=$(docker exec enstud-mysql mysql -uroot -proot enstud_app -N -e "SELECT id FROM enstud_user WHERE username='verify_$SUFX';" 2>/dev/null)
if [ -n "$BID" ]; then echo "PASS  register user B (id=$BID)"; PASS=$((PASS+1)); else echo "FAIL  register user B → $(echo "$REG" | head -c 150)"; FAIL=$((FAIL+1)); fi
if [ -n "$BID" ]; then
  api "POST follow B" POST "/user/follow/$BID"
  api "GET /user/following" GET /user/following
  api "GET follow-status" GET "/user/$BID/follow-status"
  api "DELETE follow B" DELETE "/user/follow/$BID"
fi

echo "========== 2. 单词模块 =========="
api "GET /word/wordbooks" GET /word/wordbooks
api "GET wordbook words" GET "/word/wordbooks/2/words?limit=5"
api "POST /word/study" POST "/word/study?wordbookId=2&limit=3"
WID=$(extract '"id":\([0-9]*\)')
if [ -n "$WID" ]; then
  api "POST /word/review (quality=4)" POST "/word/review?wordId=$WID&quality=4"
  api "PUT /word/memory-level/adjust" PUT /word/memory-level/adjust "{\"wordId\":$WID,\"targetLevel\":2}"
fi
api "GET memory-level/distribution" GET /word/memory-level/distribution
api "GET memory-level/words" GET "/word/memory-level/words?memoryLevel=2&limit=3"
api "GET /word/learning-words" GET /word/learning-words
api "GET /word/wrong-words" GET /word/wrong-words
api "POST /word/reading/add" POST /word/reading/add '{"word":"verifyword1","definitionCn":"vt. to check","partOfSpeech":"v.","contextSentence":"We verify every feature carefully."}'
api "GET word recommendations" GET /word/recommendations/words
api "GET topic recommendations" GET /word/recommendations/topics
api "GET vocab-test/words" GET /word/vocab-test/words
api "GET vocab-test/latest" GET /word/vocab-test/latest

echo "========== 3. AI 对话模块（真实 DeepSeek）=========="
api "POST create session" POST /chat/sessions
SID=$(extract '"id":\([0-9]*\)')
if [ -n "$SID" ]; then
  api "POST send message (grammar test)" POST "/chat/sessions/$SID/messages?content=I%20goed%20to%20school%20yesterday%20and%20eats%20an%20apple."
  api "GET session messages" GET "/chat/sessions/$SID/messages"
  api "DELETE session" DELETE "/chat/sessions/$SID"
fi
api "GET /chat/sessions" GET /chat/sessions
api "POST review-session (AI)" POST /chat/review-session
api "GET chat topic recommendations" GET /chat/recommendations/topics
if [ -n "$WID" ]; then api "POST word-practice (AI)" POST "/chat/word-practice/$WID"; fi

echo "========== 4. 写作模块（真实 DeepSeek）=========="
api "GET /writing/models" GET /writing/models
api "POST /writing/submit (AI 批改)" POST /writing/submit '{"title":"My Weekend","content":"Yesterday I goed to the park with my friend. We has a very good time and eat many delicious apples. The weather are sunny and we enjoy ourself very much. I hope I can go there again next week.","topicType":"DAILY"}'
api "GET /writing/history" GET /writing/history

echo "========== 5. 翻译模块（真实 DeepSeek）=========="
api "POST /translate/text" POST /translate/text '{"text":"Hello, how are you today? Would you like to learn English with me?","from":"en","to":"zh"}'

echo "========== 6. 阅读模块 =========="
api "GET /read/hot" GET /read/hot
AID=$(extract '"id":\([0-9]*\)')
if [ -n "$AID" ]; then
  api "GET article detail" GET "/read/$AID"
  api "GET article translate (cached)" GET "/read/$AID/translate"
  api "POST bookmark" POST "/read/$AID/bookmark"
fi
api "GET /read/recommended" GET /read/recommended
api "GET /read/bookmarks" GET /read/bookmarks
api "GET /read/sources" GET /read/sources
if [ -n "$AID" ]; then
  api "POST word-lookup (词典命中)" POST /read/word-lookup "{\"selectedText\":\"abandon\",\"contextSentence\":\"Do not abandon your dreams.\",\"articleId\":$AID}"
fi

echo "========== 7. 论坛模块 =========="
api "GET /forum/categories" GET /forum/categories
CID=$(extract '"id":\([0-9]*\)')
api "GET /forum/posts" GET /forum/posts
if [ -n "$CID" ]; then
  api "POST create post" POST /forum/posts "{\"title\":\"Feature Verification Post $SUFX\",\"content\":\"This is an automated verification post. It will be deleted soon.\",\"categoryId\":$CID}"
  PID=$(extract '"id":\([0-9]*\)')
  if [ -n "$PID" ]; then
    api "POST reply post" POST "/forum/posts/$PID/reply" '{"content":"Automated verification reply."}'
    api "POST like post" POST "/forum/like?targetType=POST&targetId=$PID"
    api "GET post detail" GET "/forum/posts/$PID"
    api "DELETE post (cleanup)" DELETE "/forum/posts/$PID"
  fi
fi
api "GET tag posts (tagId=1)" GET /forum/tags/1/posts

# 标签闭环：发带标签的帖 → 按标签查 → 删帖清理
if [ -n "$CID" ]; then
  api "POST create post with tags" POST /forum/posts "{\"title\":\"Tag E2E $SUFX\",\"content\":\"Automated tag verification post.\",\"categoryId\":$CID,\"tags\":\"verification,english\"}"
  TPID=$(extract '"id":\([0-9]*\)')
  TAGID=$(docker exec enstud-mysql mysql -uroot -proot enstud_app -N -e "SELECT id FROM enstud_forum_tag WHERE name='verification';" 2>/dev/null)
  if [ -n "$TAGID" ]; then
    api "GET posts by tag" GET "/forum/tags/$TAGID/posts"
  else
    echo "FAIL  tag was not created on post"; FAIL=$((FAIL+1))
  fi
  if [ -n "$TPID" ]; then api "DELETE tagged post (cleanup)" DELETE "/forum/posts/$TPID"; fi
fi

echo "========== 8. 前端资产 =========="
HTML=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/)
SW=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/sw.js)
JS=$(curl -s http://localhost/ | grep -o 'assets/[^"]*\.js' | head -1)
JSC=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost/$JS")
for pair in "index.html:$HTML" "sw.js:$SW" "js-bundle:$JSC"; do
  n="${pair%%:*}"; c="${pair##*:}"
  if [ "$c" = "200" ]; then echo "PASS  frontend $n (200)"; PASS=$((PASS+1)); else echo "FAIL  frontend $n ($c)"; FAIL=$((FAIL+1)); fi
done

echo ""
echo "========== 汇总: PASS=$PASS FAIL=$FAIL =========="
