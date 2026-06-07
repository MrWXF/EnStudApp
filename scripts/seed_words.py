#!/usr/bin/env python3
"""导入真实英语单词数据到 EnStudApp 数据库"""
import mysql.connector, sys

DB = {
    'host': '127.0.0.1', 'user': 'root', 'password': 'root',
    'database': 'enstud_app', 'charset': 'utf8mb4'
}

# ===== 词库定义 =====
WORD_BOOKS = [
    (1, 'CET-4 四级词汇', '大学英语四级考试核心词汇', 'CET4', 1, 1),
    (2, 'CET-6 六级词汇', '大学英语六级考试核心词汇', 'CET6', 2, 2),
    (3, '考研英语词汇', '全国硕士研究生入学考试英语核心词汇', 'GRAD', 3, 3),
    (4, '雅思核心词汇', 'IELTS 考试高频词汇', 'IELTS', 4, 4),
    (5, '日常口语高频词', '日常生活和口语交流最常用词汇', 'DAILY', 1, 5),
]

# ===== 真实单词数据（含音标、释义、例句）=====
WORDS = {
    1: [  # CET-4
        ('abandon', '/əˈbændən/', '/əˈbændən/', 'v. 放弃；抛弃', 'VERB', 'He had to abandon his plan due to bad weather.',
         '由于天气恶劣，他不得不放弃计划。', 2),
        ('ability', '/əˈbɪləti/', '/əˈbɪləti/', 'n. 能力；才能', 'NOUN', 'She has the ability to solve complex problems.',
         '她有能力解决复杂问题。', 1),
        ('abroad', '/əˈbrɔːd/', '/əˈbrɔːd/', 'adv. 在国外；到国外', 'ADV', 'He dreams of studying abroad next year.',
         '他梦想明年出国留学。', 1),
        ('absent', '/ˈæbsənt/', '/ˈæbsənt/', 'adj. 缺席的；不在的', 'ADJ', 'Several students were absent from class yesterday.',
         '昨天有几个学生缺课了。', 1),
        ('absorb', '/əbˈzɔːb/', '/əbˈzɔːrb/', 'v. 吸收；吸引注意力', 'VERB', 'Plants absorb nutrients from the soil.',
         '植物从土壤中吸收养分。', 2),
        ('abstract', '/ˈæbstrækt/', '/ˈæbstrækt/', 'adj. 抽象的；n. 摘要', 'ADJ', 'The concept of love is too abstract to define easily.',
         '爱的概念太抽象，不容易定义。', 3),
        ('abundant', '/əˈbʌndənt/', '/əˈbʌndənt/', 'adj. 丰富的；充裕的', 'ADJ', 'The region has abundant natural resources.',
         '该地区拥有丰富的自然资源。', 2),
        ('abuse', '/əˈbjuːz/', '/əˈbjuːz/', 'v./n. 滥用；虐待', 'VERB', 'We should never abuse our power or authority.',
         '我们绝不应滥用权力。', 2),
        ('academic', '/ˌækəˈdemɪk/', '/ˌækəˈdemɪk/', 'adj. 学术的；学院的', 'ADJ', 'She published several academic papers last year.',
         '她去年发表了几篇学术论文。', 2),
        ('accelerate', '/əkˈseləreɪt/', '/əkˈseləreɪt/', 'v. 加速；促进', 'VERB', 'The new policy will accelerate economic growth.',
         '新政策将加速经济增长。', 2),
        ('access', '/ˈækses/', '/ˈækses/', 'n. 通道；访问；v. 进入', 'NOUN', 'Students have free access to the library.',
         '学生可以免费使用图书馆。', 1),
        ('accommodation', '/əˌkɒməˈdeɪʃn/', '/əˌkɑːməˈdeɪʃn/', 'n. 住宿；膳宿', 'NOUN', 'The university provides accommodation for international students.',
         '大学为国际学生提供住宿。', 2),
        ('accompany', '/əˈkʌmpəni/', '/əˈkʌmpəni/', 'v. 陪伴；伴随', 'VERB', 'I will accompany you to the airport tomorrow.',
         '我明天陪你去机场。', 1),
        ('accomplish', '/əˈkʌmplɪʃ/', '/əˈkɑːmplɪʃ/', 'v. 完成；实现', 'VERB', 'We accomplished our goal ahead of schedule.',
         '我们提前完成了目标。', 2),
        ('account', '/əˈkaʊnt/', '/əˈkaʊnt/', 'n. 账户；说明；v. 解释', 'NOUN', 'Please create an account to access the service.',
         '请创建一个账户以访问服务。', 1),
        ('accurate', '/ˈækjərət/', '/ˈækjərət/', 'adj. 准确的；精确的', 'ADJ', 'The weather forecast proved to be accurate.',
         '天气预报证明是准确的。', 1),
        ('achieve', '/əˈtʃiːv/', '/əˈtʃiːv/', 'v. 达到；取得', 'VERB', 'She worked hard to achieve her dreams.',
         '她努力工作以实现梦想。', 1),
        ('acknowledge', '/əkˈnɒlɪdʒ/', '/əkˈnɑːlɪdʒ/', 'v. 承认；感谢', 'VERB', 'He acknowledged his mistake and apologized.',
         '他承认了错误并道歉。', 2),
        ('acquire', '/əˈkwaɪə(r)/', '/əˈkwaɪər/', 'v. 获得；学到', 'VERB', 'It takes years to acquire a new language.',
         '掌握一门新语言需要多年时间。', 2),
        ('adapt', '/əˈdæpt/', '/əˈdæpt/', 'v. 适应；改编', 'VERB', 'You need to adapt to the new environment quickly.',
         '你需要快速适应新环境。', 2),
        ('adequate', '/ˈædɪkwət/', '/ˈædɪkwət/', 'adj. 足够的；适当的', 'ADJ', 'Make sure you have adequate sleep before the exam.',
         '考试前确保充足的睡眠。', 2),
        ('adjust', '/əˈdʒʌst/', '/əˈdʒʌst/', 'v. 调整；适应', 'VERB', 'Can you adjust the volume of the speaker?',
         '你能调一下音响的音量吗？', 1),
        ('administration', '/ədˌmɪnɪˈstreɪʃn/', '/ədˌmɪnɪˈstreɪʃn/', 'n. 管理；行政', 'NOUN', 'The school administration approved the new policy.',
         '学校管理层批准了新政策。', 2),
        ('admire', '/ədˈmaɪə(r)/', '/ədˈmaɪər/', 'v. 钦佩；赞赏', 'VERB', 'I really admire your courage and determination.',
         '我非常钦佩你的勇气和决心。', 1),
        ('admit', '/ədˈmɪt/', '/ədˈmɪt/', 'v. 承认；准许进入', 'VERB', 'You must admit that you were wrong.',
         '你必须承认你错了。', 1),
        ('adopt', '/əˈdɒpt/', '/əˈdɑːpt/', 'v. 采用；收养', 'VERB', 'The company decided to adopt a new strategy.',
         '公司决定采取新策略。', 2),
        ('advance', '/ədˈvɑːns/', '/ədˈvæns/', 'n./v. 前进；进步', 'VERB', 'Technology continues to advance at a rapid pace.',
         '技术以快速步伐不断进步。', 1),
        ('advantage', '/ədˈvɑːntɪdʒ/', '/ədˈvæntɪdʒ/', 'n. 优势；有利条件', 'NOUN', 'Speaking English fluently is a great advantage.',
         '流利说英语是一个很大的优势。', 1),
        ('advertise', '/ˈædvətaɪz/', '/ˈædvərtaɪz/', 'v. 做广告；宣传', 'VERB', 'They plan to advertise the new product online.',
         '他们计划在网上宣传新产品。', 1),
        ('advice', '/ədˈvaɪs/', '/ədˈvaɪs/', 'n. 建议；忠告', 'NOUN', 'Can you give me some advice on learning English?',
         '你能给我一些学习英语的建议吗？', 1),
        ('affair', '/əˈfeə(r)/', '/əˈfer/', 'n. 事情；事务', 'NOUN', 'The meeting was about international affairs.',
         '会议是关于国际事务的。', 2),
        ('affect', '/əˈfekt/', '/əˈfekt/', 'v. 影响；感动', 'VERB', 'Climate change affects everyone on the planet.',
         '气候变化影响地球上的每一个人。', 2),
        ('afford', '/əˈfɔːd/', '/əˈfɔːrd/', 'v. 负担得起；提供', 'VERB', 'I cannot afford to buy a new car right now.',
         '我现在买不起新车。', 1),
        ('aggressive', '/əˈɡresɪv/', '/əˈɡresɪv/', 'adj. 侵略的；好斗的', 'ADJ', 'Some people become aggressive when stressed.',
         '有些人在压力下会变得有攻击性。', 2),
        ('agreement', '/əˈɡriːmənt/', '/əˈɡriːmənt/', 'n. 协议；一致', 'NOUN', 'The two companies signed a partnership agreement.',
         '两家公司签署了合作协议。', 1),
        ('agriculture', '/ˈæɡrɪkʌltʃə(r)/', '/ˈæɡrɪkʌltʃər/', 'n. 农业', 'NOUN', 'Agriculture plays a vital role in the economy.',
         '农业在经济中起着至关重要的作用。', 2),
        ('alarm', '/əˈlɑːm/', '/əˈlɑːrm/', 'n. 警报；闹钟', 'NOUN', 'I set my alarm for 6:30 every morning.',
         '我每天早上设6:30的闹钟。', 1),
        ('alcohol', '/ˈælkəhɒl/', '/ˈælkəhɔːl/', 'n. 酒精；酒', 'NOUN', 'Drinking too much alcohol is harmful to health.',
         '过量饮酒有害健康。', 1),
        ('allow', '/əˈlaʊ/', '/əˈlaʊ/', 'v. 允许；准许', 'VERB', 'My parents do not allow me to stay out late.',
         '我父母不允许我在外待太晚。', 1),
        ('alternative', '/ɔːlˈtɜːnətɪv/', '/ɔːlˈtɜːrnətɪv/', 'n./adj. 替代方案；替代的', 'ADJ', 'We need to find an alternative solution.',
         '我们需要找到一个替代方案。', 2),
        ('amaze', '/əˈmeɪz/', '/əˈmeɪz/', 'v. 使惊奇；使吃惊', 'VERB', 'His performance on stage amazed everyone.',
         '他在舞台上的表现让所有人惊叹。', 1),
        ('ambition', '/æmˈbɪʃn/', '/æmˈbɪʃn/', 'n. 雄心；野心', 'NOUN', 'She has the ambition to become a CEO one day.',
         '她有朝一日成为CEO的雄心。', 2),
        ('amount', '/əˈmaʊnt/', '/əˈmaʊnt/', 'n. 数量；总额', 'NOUN', 'A small amount of money can make a big difference.',
         '少量资金也能产生很大影响。', 1),
        ('amuse', '/əˈmjuːz/', '/əˈmjuːz/', 'v. 使娱乐；逗笑', 'VERB', 'The clown managed to amuse all the children.',
         '小丑成功逗笑了所有孩子。', 1),
        ('analyze', '/ˈænəlaɪz/', '/ˈænəlaɪz/', 'v. 分析', 'VERB', 'Scientists analyze data to draw conclusions.',
         '科学家分析数据得出结论。', 2),
        ('ancestor', '/ˈænsestə(r)/', '/ˈænsestər/', 'n. 祖先', 'NOUN', 'Our ancestors lived in this land thousands of years ago.',
         '我们的祖先几千年前就住在这片土地上。', 2),
        ('ancient', '/ˈeɪnʃənt/', '/ˈeɪnʃənt/', 'adj. 古代的；古老的', 'ADJ', 'The museum displays many ancient artifacts.',
         '博物馆展示了许多古代文物。', 1),
        ('anniversary', '/ˌænɪˈvɜːsəri/', '/ˌænɪˈvɜːrsəri/', 'n. 周年纪念日', 'NOUN', 'Today is our company\'s tenth anniversary.',
         '今天是我们公司十周年纪念日。', 2),
        ('announce', '/əˈnaʊns/', '/əˈnaʊns/', 'v. 宣布；通告', 'VERB', 'The principal will announce the results tomorrow.',
         '校长明天将公布结果。', 1),
        ('annual', '/ˈænjuəl/', '/ˈænjuəl/', 'adj. 每年的；年度的', 'ADJ', 'The annual conference will be held in Shanghai.',
         '年度会议将在上海举行。', 1),
    ],
    2: [  # CET-6
        ('abolish', '/əˈbɒlɪʃ/', '/əˈbɑːlɪʃ/', 'v. 废除；取消', 'VERB', 'Many countries have abolished the death penalty.',
         '许多国家已经废除了死刑。', 3),
        ('absurd', '/əbˈsɜːd/', '/əbˈsɜːrd/', 'adj. 荒谬的；荒唐的', 'ADJ', 'It is absurd to judge a person by appearance alone.',
         '仅凭外表判断一个人是荒谬的。', 3),
        ('abundance', '/əˈbʌndəns/', '/əˈbʌndəns/', 'n. 丰富；充裕', 'NOUN', 'The garden has an abundance of flowers in spring.',
         '春天花园里有丰富的花朵。', 2),
        ('accessory', '/əkˈsesəri/', '/əkˈsesəri/', 'n. 配件；附件', 'NOUN', 'She bought some accessories for her new phone.',
         '她为新手机买了一些配件。', 2),
        ('accommodate', '/əˈkɒmədeɪt/', '/əˈkɑːmədeɪt/', 'v. 容纳；提供住宿', 'VERB', 'The hotel can accommodate up to 500 guests.',
         '这家酒店可容纳多达500位客人。', 3),
        ('acquaint', '/əˈkweɪnt/', '/əˈkweɪnt/', 'v. 使熟悉；使认识', 'VERB', 'You should acquaint yourself with the new rules.',
         '你应该熟悉新规则。', 3),
        ('activate', '/ˈæktɪveɪt/', '/ˈæktɪveɪt/', 'v. 激活；启动', 'VERB', 'Please activate your account via email.',
         '请通过电子邮件激活您的账户。', 2),
        ('addict', '/ˈædɪkt/', '/ˈædɪkt/', 'n. 上瘾者；v. 使上瘾', 'NOUN', 'He became a social media addict in college.',
         '他在大学时成了社交媒体上瘾者。', 2),
        ('adhere', '/ədˈhɪə(r)/', '/ədˈhɪr/', 'v. 粘附；遵守', 'VERB', 'All members must adhere to the community rules.',
         '所有成员必须遵守社区规则。', 3),
        ('adjacent', '/əˈdʒeɪsnt/', '/əˈdʒeɪsnt/', 'adj. 邻近的；毗邻的', 'ADJ', 'The library is adjacent to the main building.',
         '图书馆与主楼相邻。', 3),
        ('administer', '/ədˈmɪnɪstə(r)/', '/ədˈmɪnɪstər/', 'v. 管理；执行', 'VERB', 'The nurse will administer the medicine to the patient.',
         '护士将给病人用药。', 3),
        ('adolescent', '/ˌædəˈlesnt/', '/ˌædəˈlesnt/', 'n./adj. 青少年；青春期的', 'NOUN', 'Adolescents often face peer pressure at school.',
         '青少年在学校经常面临同伴压力。', 2),
        ('advent', '/ˈædvent/', '/ˈædvent/', 'n. 到来；出现', 'NOUN', 'The advent of the Internet changed everything.',
         '互联网的出现改变了一切。', 3),
        ('adverse', '/ˈædvɜːs/', '/ædˈvɜːrs/', 'adj. 不利的；相反的', 'ADJ', 'The medicine may have some adverse effects.',
         '这种药可能有一些副作用。', 3),
        ('advocate', '/ˈædvəkeɪt/', '/ˈædvəkeɪt/', 'v./n. 提倡；倡导者', 'VERB', 'He advocates for better education for all children.',
         '他倡导为所有儿童提供更好的教育。', 3),
    ],
    3: [  # 考研
        ('aberration', '/ˌæbəˈreɪʃn/', '/ˌæbəˈreɪʃn/', 'n. 异常；偏差', 'NOUN', 'The sudden drop in temperature was an aberration.',
         '气温骤降是一种异常现象。', 4),
        ('abstain', '/əbˈsteɪn/', '/əbˈsteɪn/', 'v. 弃权；戒除', 'VERB', 'Several countries chose to abstain from the vote.',
         '几个国家选择在投票中弃权。', 4),
        ('acclaim', '/əˈkleɪm/', '/əˈkleɪm/', 'v./n. 称赞；欢呼', 'VERB', 'The film was acclaimed by critics worldwide.',
         '这部电影受到全球评论家的赞誉。', 3),
        ('admonish', '/ədˈmɒnɪʃ/', '/ədˈmɑːnɪʃ/', 'v. 告诫；警告', 'VERB', 'The teacher admonished the students for cheating.',
         '老师告诫学生不要作弊。', 4),
        ('aesthetic', '/iːsˈθetɪk/', '/esˈθetɪk/', 'adj. 美学的；审美的', 'ADJ', 'The design has great aesthetic appeal.',
         '这个设计具有极大的美学吸引力。', 3),
        ('affiliate', '/əˈfɪlieɪt/', '/əˈfɪlieɪt/', 'v. 使附属；n. 附属机构', 'VERB', 'The hospital is affiliated with the university.',
         '这家医院附属于大学。', 3),
        ('afflict', '/əˈflɪkt/', '/əˈflɪkt/', 'v. 使痛苦；折磨', 'VERB', 'The disease afflicts millions of people worldwide.',
         '这种疾病折磨着全球数百万人。', 4),
        ('aggregate', '/ˈæɡrɪɡət/', '/ˈæɡrɪɡət/', 'v./n. 聚集；总计', 'VERB', 'The total cost aggregates to over one million dollars.',
         '总成本合计超过一百万美元。', 4),
        ('agitate', '/ˈædʒɪteɪt/', '/ˈædʒɪteɪt/', 'v. 搅动；煽动', 'VERB', 'The protesters agitated for political reform.',
         '抗议者鼓动政治改革。', 4),
        ('alienate', '/ˈeɪliəneɪt/', '/ˈeɪliəneɪt/', 'v. 疏远；离间', 'VERB', 'His rude behavior alienated all his friends.',
         '他的粗鲁行为疏远了所有朋友。', 4),
    ],
    4: [  # 雅思
        ('acquisition', '/ˌækwɪˈzɪʃn/', '/ˌækwɪˈzɪʃn/', 'n. 获得；收购', 'NOUN', 'Language acquisition takes time and practice.',
         '语言习得需要时间和练习。', 3),
        ('adolescence', '/ˌædəˈlesns/', '/ˌædəˈlesns/', 'n. 青春期', 'NOUN', 'Adolescence is a critical period of development.',
         '青春期是一个关键的发展阶段。', 3),
        ('agenda', '/əˈdʒendə/', '/əˈdʒendə/', 'n. 议程；议事日程', 'NOUN', 'The first item on the agenda is the budget review.',
         '议程第一项是预算审查。', 2),
        ('allocation', '/ˌæləˈkeɪʃn/', '/ˌæləˈkeɪʃn/', 'n. 分配；配置', 'NOUN', 'The allocation of resources must be fair and efficient.',
         '资源分配必须公平高效。', 3),
        ('analogy', '/əˈnælədʒi/', '/əˈnælədʒi/', 'n. 类比；相似', 'NOUN', 'He used an analogy to explain the complex theory.',
         '他用类比来解释复杂理论。', 3),
        ('anonymous', '/əˈnɒnɪməs/', '/əˈnɑːnɪməs/', 'adj. 匿名的', 'ADJ', 'The donation was made by an anonymous benefactor.',
         '这笔捐款来自一位匿名捐助者。', 2),
        ('apparatus', '/ˌæpəˈreɪtəs/', '/ˌæpəˈrætəs/', 'n. 仪器；设备', 'NOUN', 'The laboratory has all the necessary apparatus.',
         '实验室配备了所有必要设备。', 3),
        ('appendix', '/əˈpendɪks/', '/əˈpendɪks/', 'n. 附录；阑尾', 'NOUN', 'Please refer to the appendix for more data.',
         '请参考附录获取更多数据。', 3),
        ('arbitrary', '/ˈɑːbɪtrəri/', '/ˈɑːrbɪtreri/', 'adj. 任意的；武断的', 'ADJ', 'The rules seemed arbitrary and unreasonable.',
         '这些规则似乎武断且不合理。', 3),
        ('articulate', '/ɑːˈtɪkjuleɪt/', '/ɑːrˈtɪkjuleɪt/', 'v. 清晰表达；adj. 善于表达的', 'VERB', 'She articulated her thoughts very clearly.',
         '她非常清晰地表达了自己的想法。', 3),
    ],
    5: [  # 日常口语
        ('beautiful', '/ˈbjuːtɪfl/', '/ˈbjuːtɪfl/', 'adj. 美丽的；美好的', 'ADJ', 'What a beautiful sunset! Let\'s take a picture.',
         '多么美丽的日落！我们拍张照吧。', 1),
        ('delicious', '/dɪˈlɪʃəs/', '/dɪˈlɪʃəs/', 'adj. 美味的', 'ADJ', 'This homemade pizza is absolutely delicious!',
         '这个自制披萨太美味了！', 1),
        ('important', '/ɪmˈpɔːtnt/', '/ɪmˈpɔːrtnt/', 'adj. 重要的', 'ADJ', 'It is important to drink enough water every day.',
         '每天喝足够的水很重要。', 1),
        ('wonderful', '/ˈwʌndəfl/', '/ˈwʌndərfl/', 'adj. 奇妙的；极好的', 'ADJ', 'We had a wonderful time at the beach yesterday.',
         '我们昨天在海滩度过了美好的时光。', 1),
        ('interesting', '/ˈɪntrəstɪŋ/', '/ˈɪntrəstɪŋ/', 'adj. 有趣的', 'ADJ', 'This book is very interesting. I can\'t put it down.',
         '这本书很有趣。我放不下来。', 1),
        ('comfortable', '/ˈkʌmftəbl/', '/ˈkʌmfərtəbl/', 'adj. 舒适的', 'ADJ', 'Make yourself comfortable while I prepare tea.',
         '请随意坐，我去泡茶。', 1),
        ('fantastic', '/fænˈtæstɪk/', '/fænˈtæstɪk/', 'adj. 极好的；了不起的', 'ADJ', 'You did a fantastic job on the presentation!',
         '你的演示太棒了！', 1),
        ('terrible', '/ˈterəbl/', '/ˈterəbl/', 'adj. 糟糕的；可怕的', 'ADJ', 'The traffic was terrible this morning.',
         '今天早上的交通太糟糕了。', 1),
        ('brilliant', '/ˈbrɪliənt/', '/ˈbrɪliənt/', 'adj. 杰出的；明亮的', 'ADJ', 'That\'s a brilliant idea! Let\'s do it.',
         '这是个绝妙的主意！我们来做吧。', 1),
        ('favorite', '/ˈfeɪvərɪt/', '/ˈfeɪvərɪt/', 'adj./n. 最喜欢的；最爱', 'ADJ', 'Chocolate is my favorite dessert in the world.',
         '巧克力是我全世界最喜欢的甜点。', 1),
        ('necessary', '/ˈnesəsəri/', '/ˈnesəseri/', 'adj. 必要的', 'ADJ', 'Is it necessary to bring an umbrella today?',
         '今天需要带伞吗？', 1),
        ('convenient', '/kənˈviːniənt/', '/kənˈviːniənt/', 'adj. 方便的', 'ADJ', 'Online shopping is very convenient these days.',
         '现在网购非常方便。', 1),
        ('expensive', '/ɪkˈspensɪv/', '/ɪkˈspensɪv/', 'adj. 昂贵的', 'ADJ', 'This restaurant is too expensive for a daily meal.',
         '这家餐厅太贵了不适合日常用餐。', 1),
        ('necessary', '/ˈnesəsəri/', '/ˈnesəseri/', 'adj. 必要的；必需的', 'ADJ', 'Good sleep is necessary for a healthy lifestyle.',
         '良好的睡眠是健康生活方式的必要条件。', 1),
        ('different', '/ˈdɪfrənt/', '/ˈdɪfrənt/', 'adj. 不同的', 'ADJ', 'Every person has a different learning style.',
         '每个人都有不同的学习方式。', 1),
    ],
}


def main():
    conn = mysql.connector.connect(**DB)
    cur = conn.cursor()

    # 清空已有数据
    cur.execute("DELETE FROM enstud_word WHERE 1=1")
    cur.execute("DELETE FROM enstud_wordbook WHERE 1=1")
    conn.commit()

    # 插入词库
    for bid, name, desc, cat, diff, sort in WORD_BOOKS:
        cur.execute(
            "INSERT INTO enstud_wordbook (id, name, description, category, difficulty, sort_order, is_official) "
            "VALUES (%s,%s,%s,%s,%s,%s,1)", (bid, name, desc, cat, diff, sort))
    conn.commit()
    print(f"创建 {len(WORD_BOOKS)} 个词库")

    # 插入单词
    total = 0
    for book_id, word_list in WORDS.items():
        count = 0
        for word, ph_uk, ph_us, def_cn, pos, ex_en, ex_cn, diff in word_list:
            cur.execute(
                "INSERT INTO enstud_word (word, phonetic_uk, phonetic_us, definition_cn, part_of_speech, "
                "example_sentence, example_cn, difficulty_level, wordbook_id) "
                "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)",
                (word, ph_uk, ph_us, def_cn, pos, ex_en, ex_cn, diff, book_id))
            count += 1
        cur.execute("UPDATE enstud_wordbook SET word_count = %s WHERE id = %s", (count, book_id))
        conn.commit()
        total += count
        print(f"词库 {book_id}: {count} 个单词")

    cur.close()
    conn.close()
    print(f"\n总计导入 {total} 个单词到 5 个词库 ✅")


if __name__ == '__main__':
    main()
