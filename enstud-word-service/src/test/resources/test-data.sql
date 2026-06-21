-- word-service test data

INSERT INTO enstud_wordbook (id, name, description, category, difficulty, word_count, sort_order, is_official)
VALUES (1, 'Test Wordbook', 'Test wordbook for integration testing', 'CET4', 1, 10, 1, 1);

INSERT INTO enstud_word (id, word, phonetic_uk, phonetic_us, definition_cn, definition_en, example_sentence, part_of_speech, difficulty_level, wordbook_id)
VALUES
(1, 'abandon', '/əˈbændən/', '/əˈbændən/', '放弃，抛弃', 'to leave someone or something forever', 'They had to abandon the sinking ship.', 'v', 1, 1),
(2, 'brilliant', '/ˈbrɪliənt/', '/ˈbrɪliənt/', '杰出的，明亮的', 'very bright, intelligent, or successful', 'She has a brilliant future ahead of her.', 'adj', 1, 1),
(3, 'curious', '/ˈkjʊəriəs/', '/ˈkjʊriəs/', '好奇的', 'eager to know or learn something', 'The curious child asked many questions.', 'adj', 1, 1),
(4, 'deliberate', '/dɪˈlɪbərət/', '/dɪˈlɪbərət/', '故意的，深思熟虑的', 'done consciously and intentionally', 'It was a deliberate attempt to mislead.', 'adj', 2, 1),
(5, 'eloquent', '/ˈeləkwənt/', '/ˈeləkwənt/', '雄辩的，有口才的', 'fluent or persuasive in speaking or writing', 'She gave an eloquent speech at the ceremony.', 'adj', 2, 1);
