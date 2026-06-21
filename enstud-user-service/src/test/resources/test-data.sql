-- user-service 预置测试数据
-- password_hash = BCrypt("Test@1234")

INSERT INTO enstud_user (id, username, email, password_hash, nickname, level, points)
VALUES (1, 'testuser', 'test@example.com',
        '$2a$10$dXJ3SW6G7P50lLzM2pzM7eO0rQvZ3Gf0D3qE5q8v8x8h8j8k8l8m',
        'TestUser', 1, 100);
