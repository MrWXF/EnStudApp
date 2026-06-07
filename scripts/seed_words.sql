DELETE FROM enstud_word;
DELETE FROM enstud_wordbook;

INSERT INTO enstud_wordbook (id, name, description, category, difficulty, sort_order, is_official) VALUES
(1, 'CET-4 四级词汇', '大学英语四级考试核心词汇', 'CET4', 1, 1, 1),
(2, 'CET-6 六级词汇', '大学英语六级考试核心词汇', 'CET6', 2, 2, 1),
(3, '考研英语词汇', '考研英语核心词汇', 'GRAD', 3, 3, 1),
(4, '雅思核心词汇', 'IELTS考试高频词汇', 'IELTS', 4, 4, 1),
(5, '日常口语高频词', '日常口语交流常用词', 'DAILY', 1, 5, 1);

INSERT INTO enstud_word (word, phonetic_us, definition_cn, part_of_speech, example_sentence, example_cn, difficulty_level, wordbook_id) VALUES
('abandon', '/əˈbændən/', 'v. 放弃；抛弃', 'VERB', 'He had to abandon his plan due to bad weather.', '由于天气恶劣，他不得不放弃计划。', 2, 1),
('ability', '/əˈbɪləti/', 'n. 能力；才能', 'NOUN', 'She has the ability to solve complex problems.', '她有能力解决复杂问题。', 1, 1),
('abroad', '/əˈbrɔːd/', 'adv. 在国外', 'ADV', 'He dreams of studying abroad next year.', '他梦想明年出国留学。', 1, 1),
('absent', '/ˈæbsənt/', 'adj. 缺席的', 'ADJ', 'Several students were absent from class yesterday.', '昨天有几个学生缺课了。', 1, 1),
('absorb', '/əbˈzɔːrb/', 'v. 吸收；吸引注意力', 'VERB', 'Plants absorb nutrients from the soil.', '植物从土壤中吸收养分。', 2, 1),
('abstract', '/ˈæbstrækt/', 'adj. 抽象的', 'ADJ', 'The concept of love is too abstract to define easily.', '爱的概念太抽象，不容易定义。', 3, 1),
('abundant', '/əˈbʌndənt/', 'adj. 丰富的；充裕的', 'ADJ', 'The region has abundant natural resources.', '该地区拥有丰富的自然资源。', 2, 1),
('abuse', '/əˈbjuːz/', 'v./n. 滥用；虐待', 'VERB', 'We should never abuse our power or authority.', '我们绝不应滥用权力。', 2, 1),
('academic', '/ˌækəˈdemɪk/', 'adj. 学术的；学院的', 'ADJ', 'She published several academic papers last year.', '她去年发表了几篇学术论文。', 2, 1),
('accelerate', '/əkˈseləreɪt/', 'v. 加速；促进', 'VERB', 'The new policy will accelerate economic growth.', '新政策将加速经济增长。', 2, 1),
('access', '/ˈækses/', 'n. 通道；访问；v. 进入', 'NOUN', 'Students have free access to the library.', '学生可以免费使用图书馆。', 1, 1),
('accommodation', '/əˌkɑːməˈdeɪʃn/', 'n. 住宿；膳宿', 'NOUN', 'The university provides accommodation for students.', '大学为学生提供住宿。', 2, 1),
('accompany', '/əˈkʌmpəni/', 'v. 陪伴；伴随', 'VERB', 'I will accompany you to the airport tomorrow.', '我明天陪你去机场。', 1, 1),
('accomplish', '/əˈkɑːmplɪʃ/', 'v. 完成；实现', 'VERB', 'We accomplished our goal ahead of schedule.', '我们提前完成了目标。', 2, 1),
('account', '/əˈkaʊnt/', 'n. 账户；说明', 'NOUN', 'Please create an account to access the service.', '请创建一个账户以访问服务。', 1, 1),
('accurate', '/ˈækjərət/', 'adj. 准确的；精确的', 'ADJ', 'The weather forecast proved to be accurate.', '天气预报证明是准确的。', 1, 1),
('achieve', '/əˈtʃiːv/', 'v. 达到；取得', 'VERB', 'She worked hard to achieve her dreams.', '她努力工作以实现梦想。', 1, 1),
('acknowledge', '/əkˈnɑːlɪdʒ/', 'v. 承认；感谢', 'VERB', 'He acknowledged his mistake and apologized.', '他承认了错误并道歉。', 2, 1),
('acquire', '/əˈkwaɪər/', 'v. 获得；学到', 'VERB', 'It takes years to acquire a new language.', '掌握一门新语言需要多年时间。', 2, 1),
('adapt', '/əˈdæpt/', 'v. 适应；改编', 'VERB', 'You need to adapt to the new environment quickly.', '你需要快速适应新环境。', 2, 1),
('adequate', '/ˈædɪkwət/', 'adj. 足够的；适当的', 'ADJ', 'Make sure you have adequate sleep before the exam.', '考试前确保充足的睡眠。', 2, 1),
('adjust', '/əˈdʒʌst/', 'v. 调整；适应', 'VERB', 'Can you adjust the volume of the speaker?', '你能调一下音响的音量吗？', 1, 1),
('admire', '/ədˈmaɪər/', 'v. 钦佩；赞赏', 'VERB', 'I really admire your courage and determination.', '我非常钦佩你的勇气和决心。', 1, 1),
('admit', '/ədˈmɪt/', 'v. 承认；准许进入', 'VERB', 'You must admit that you were wrong.', '你必须承认你错了。', 1, 1),
('adopt', '/əˈdɑːpt/', 'v. 采用；收养', 'VERB', 'The company decided to adopt a new strategy.', '公司决定采取新策略。', 2, 1),
('advance', '/ədˈvæns/', 'n./v. 前进；进步', 'VERB', 'Technology continues to advance at a rapid pace.', '技术以快速步伐不断进步。', 1, 1),
('advantage', '/ədˈvæntɪdʒ/', 'n. 优势；有利条件', 'NOUN', 'Speaking English fluently is a great advantage.', '流利说英语是一个很大的优势。', 1, 1),
('advice', '/ədˈvaɪs/', 'n. 建议；忠告', 'NOUN', 'Can you give me some advice on learning English?', '你能给我一些学习英语的建议吗？', 1, 1),
('affair', '/əˈfer/', 'n. 事情；事务', 'NOUN', 'The meeting was about international affairs.', '会议是关于国际事务的。', 2, 1),
('affect', '/əˈfekt/', 'v. 影响；感动', 'VERB', 'Climate change affects everyone on the planet.', '气候变化影响地球上的每一个人。', 2, 1),
('afford', '/əˈfɔːrd/', 'v. 负担得起；提供', 'VERB', 'I cannot afford to buy a new car right now.', '我现在买不起新车。', 1, 1),
('aggressive', '/əˈɡresɪv/', 'adj. 侵略的；好斗的', 'ADJ', 'Some people become aggressive when stressed.', '有些人在压力下会变得有攻击性。', 2, 1),
('agreement', '/əˈɡriːmənt/', 'n. 协议；一致', 'NOUN', 'The two companies signed a partnership agreement.', '两家公司签署了合作协议。', 1, 1),
('alarm', '/əˈlɑːrm/', 'n. 警报；闹钟', 'NOUN', 'I set my alarm for 6:30 every morning.', '我每天早上设6:30的闹钟。', 1, 1),
('alcohol', '/ˈælkəhɔːl/', 'n. 酒精；酒', 'NOUN', 'Drinking too much alcohol is harmful to health.', '过量饮酒有害健康。', 1, 1),
('alternative', '/ɔːlˈtɜːrnətɪv/', 'n./adj. 替代方案；替代的', 'ADJ', 'We need to find an alternative solution.', '我们需要找到一个替代方案。', 2, 1),
('amaze', '/əˈmeɪz/', 'v. 使惊奇；使吃惊', 'VERB', 'His performance on stage amazed everyone.', '他在舞台上的表现让所有人惊叹。', 1, 1),
('ambition', '/æmˈbɪʃn/', 'n. 雄心；野心', 'NOUN', 'She has the ambition to become a CEO one day.', '她有朝一日成为CEO的雄心。', 2, 1),
('amount', '/əˈmaʊnt/', 'n. 数量；总额', 'NOUN', 'A small amount of money can make a big difference.', '少量资金也能产生很大影响。', 1, 1),
('ancient', '/ˈeɪnʃənt/', 'adj. 古代的；古老的', 'ADJ', 'The museum displays many ancient artifacts.', '博物馆展示了许多古代文物。', 1, 1),
('announce', '/əˈnaʊns/', 'v. 宣布；通告', 'VERB', 'The principal will announce the results tomorrow.', '校长明天将公布结果。', 1, 1),
('annual', '/ˈænjuəl/', 'adj. 每年的；年度的', 'ADJ', 'The annual conference will be held in Shanghai.', '年度会议将在上海举行。', 1, 1);

INSERT INTO enstud_word (word, phonetic_us, definition_cn, part_of_speech, example_sentence, example_cn, difficulty_level, wordbook_id) VALUES
('abolish', '/əˈbɑːlɪʃ/', 'v. 废除；取消', 'VERB', 'Many countries have abolished the death penalty.', '许多国家已经废除了死刑。', 3, 2),
('absurd', '/əbˈsɜːrd/', 'adj. 荒谬的；荒唐的', 'ADJ', 'It is absurd to judge a person by appearance alone.', '仅凭外表判断一个人是荒谬的。', 3, 2),
('abundance', '/əˈbʌndəns/', 'n. 丰富；充裕', 'NOUN', 'The garden has an abundance of flowers in spring.', '春天花园里有丰富的花朵。', 2, 2),
('accessory', '/əkˈsesəri/', 'n. 配件；附件', 'NOUN', 'She bought some accessories for her new phone.', '她为新手机买了一些配件。', 2, 2),
('accommodate', '/əˈkɑːmədeɪt/', 'v. 容纳；提供住宿', 'VERB', 'The hotel can accommodate up to 500 guests.', '这家酒店可容纳多达500位客人。', 3, 2),
('acquaint', '/əˈkweɪnt/', 'v. 使熟悉；使认识', 'VERB', 'You should acquaint yourself with the new rules.', '你应该熟悉新规则。', 3, 2),
('activate', '/ˈæktɪveɪt/', 'v. 激活；启动', 'VERB', 'Please activate your account via email.', '请通过电子邮件激活您的账户。', 2, 2),
('adhere', '/ədˈhɪr/', 'v. 粘附；遵守', 'VERB', 'All members must adhere to the community rules.', '所有成员必须遵守社区规则。', 3, 2),
('adjacent', '/əˈdʒeɪsnt/', 'adj. 邻近的；毗邻的', 'ADJ', 'The library is adjacent to the main building.', '图书馆与主楼相邻。', 3, 2),
('adolescent', '/ˌædəˈlesnt/', 'n./adj. 青少年；青春期的', 'NOUN', 'Adolescents often face peer pressure at school.', '青少年在学校经常面临同伴压力。', 2, 2),
('advent', '/ˈædvent/', 'n. 到来；出现', 'NOUN', 'The advent of the Internet changed everything.', '互联网的出现改变了一切。', 3, 2),
('adverse', '/ædˈvɜːrs/', 'adj. 不利的；相反的', 'ADJ', 'The medicine may have some adverse effects.', '这种药可能有一些副作用。', 3, 2),
('advocate', '/ˈædvəkeɪt/', 'v./n. 提倡；倡导者', 'VERB', 'He advocates for better education for all children.', '他倡导为所有儿童提供更好的教育。', 3, 2);

INSERT INTO enstud_word (word, phonetic_us, definition_cn, part_of_speech, example_sentence, example_cn, difficulty_level, wordbook_id) VALUES
('acclaim', '/əˈkleɪm/', 'v./n. 称赞；欢呼', 'VERB', 'The film was acclaimed by critics worldwide.', '这部电影受到全球评论家的赞誉。', 3, 3),
('aesthetic', '/esˈθetɪk/', 'adj. 美学的；审美的', 'ADJ', 'The design has great aesthetic appeal.', '这个设计具有极大的美学吸引力。', 3, 3),
('affiliate', '/əˈfɪlieɪt/', 'v. 使附属；n. 附属机构', 'VERB', 'The hospital is affiliated with the university.', '这家医院附属于大学。', 3, 3),
('agitate', '/ˈædʒɪteɪt/', 'v. 搅动；煽动', 'VERB', 'The protesters agitated for political reform.', '抗议者鼓动政治改革。', 4, 3),
('alienate', '/ˈeɪliəneɪt/', 'v. 疏远；离间', 'VERB', 'His rude behavior alienated all his friends.', '他的粗鲁行为疏远了所有朋友。', 4, 3);

INSERT INTO enstud_word (word, phonetic_us, definition_cn, part_of_speech, example_sentence, example_cn, difficulty_level, wordbook_id) VALUES
('acquisition', '/ˌækwɪˈzɪʃn/', 'n. 获得；收购', 'NOUN', 'Language acquisition takes time and practice.', '语言习得需要时间和练习。', 3, 4),
('agenda', '/əˈdʒendə/', 'n. 议程；议事日程', 'NOUN', 'The first item on the agenda is the budget review.', '议程第一项是预算审查。', 2, 4),
('allocation', '/ˌæləˈkeɪʃn/', 'n. 分配；配置', 'NOUN', 'The allocation of resources must be fair and efficient.', '资源分配必须公平高效。', 3, 4),
('analogy', '/əˈnælədʒi/', 'n. 类比；相似', 'NOUN', 'He used an analogy to explain the complex theory.', '他用类比来解释复杂理论。', 3, 4),
('anonymous', '/əˈnɑːnɪməs/', 'adj. 匿名的', 'ADJ', 'The donation was made by an anonymous benefactor.', '这笔捐款来自一位匿名捐助者。', 2, 4),
('apparatus', '/ˌæpəˈrætəs/', 'n. 仪器；设备', 'NOUN', 'The laboratory has all the necessary apparatus.', '实验室配备了所有必要设备。', 3, 4),
('appendix', '/əˈpendɪks/', 'n. 附录；阑尾', 'NOUN', 'Please refer to the appendix for more data.', '请参考附录获取更多数据。', 3, 4),
('arbitrary', '/ˈɑːrbɪtreri/', 'adj. 任意的；武断的', 'ADJ', 'The rules seemed arbitrary and unreasonable.', '这些规则似乎武断且不合理。', 3, 4),
('articulate', '/ɑːrˈtɪkjuleɪt/', 'v. 清晰表达；adj. 善于表达的', 'VERB', 'She articulated her thoughts very clearly.', '她非常清晰地表达了自己的想法。', 3, 4);

INSERT INTO enstud_word (word, phonetic_us, definition_cn, part_of_speech, example_sentence, example_cn, difficulty_level, wordbook_id) VALUES
('beautiful', '/ˈbjuːtɪfl/', 'adj. 美丽的；美好的', 'ADJ', 'What a beautiful sunset! Let is take a picture.', '多么美丽的日落！我们拍张照吧。', 1, 5),
('delicious', '/dɪˈlɪʃəs/', 'adj. 美味的', 'ADJ', 'This homemade pizza is absolutely delicious!', '这个自制披萨太美味了！', 1, 5),
('important', '/ɪmˈpɔːrtnt/', 'adj. 重要的', 'ADJ', 'It is important to drink enough water every day.', '每天喝足够的水很重要。', 1, 5),
('wonderful', '/ˈwʌndərfl/', 'adj. 奇妙的；极好的', 'ADJ', 'We had a wonderful time at the beach yesterday.', '我们昨天在海滩度过了美好的时光。', 1, 5),
('interesting', '/ˈɪntrəstɪŋ/', 'adj. 有趣的', 'ADJ', 'This book is very interesting. I cannot put it down.', '这本书很有趣。我放不下来。', 1, 5),
('comfortable', '/ˈkʌmfərtəbl/', 'adj. 舒适的', 'ADJ', 'Make yourself comfortable while I prepare tea.', '请随意坐，我去泡茶。', 1, 5),
('fantastic', '/fænˈtæstɪk/', 'adj. 极好的；了不起的', 'ADJ', 'You did a fantastic job on the presentation!', '你的演示太棒了！', 1, 5),
('brilliant', '/ˈbrɪliənt/', 'adj. 杰出的；明亮的', 'ADJ', 'That is a brilliant idea! Let us do it.', '这是个绝妙的主意！我们来做吧。', 1, 5),
('favorite', '/ˈfeɪvərɪt/', 'adj./n. 最喜欢的；最爱', 'ADJ', 'Chocolate is my favorite dessert in the world.', '巧克力是我全世界最喜欢的甜点。', 1, 5),
('necessary', '/ˈnesəseri/', 'adj. 必要的', 'ADJ', 'Good sleep is necessary for a healthy lifestyle.', '良好的睡眠是健康生活方式的必要条件。', 1, 5),
('convenient', '/kənˈviːniənt/', 'adj. 方便的', 'ADJ', 'Online shopping is very convenient these days.', '现在网购非常方便。', 1, 5),
('different', '/ˈdɪfrənt/', 'adj. 不同的', 'ADJ', 'Every person has a different learning style.', '每个人都有不同的学习方式。', 1, 5);

UPDATE enstud_wordbook SET word_count = 42 WHERE id = 1;
UPDATE enstud_wordbook SET word_count = 13 WHERE id = 2;
UPDATE enstud_wordbook SET word_count = 5 WHERE id = 3;
UPDATE enstud_wordbook SET word_count = 9 WHERE id = 4;
UPDATE enstud_wordbook SET word_count = 12 WHERE id = 5;
