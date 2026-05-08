-- H2 兼容版数据库初始化脚本
-- 用于开发环境（免安装 MySQL）

CREATE TABLE IF NOT EXISTS blog_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS blog_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    sort_order INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS blog_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS blog_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500),
    content CLOB,
    cover_image VARCHAR(255),
    category_id BIGINT,
    author_id BIGINT,
    view_count INT DEFAULT 0,
    status INT DEFAULT 0,
    is_top INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES blog_category(id),
    FOREIGN KEY (author_id) REFERENCES blog_user(id)
);

CREATE TABLE IF NOT EXISTS blog_article_tag (
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    FOREIGN KEY (article_id) REFERENCES blog_article(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES blog_tag(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS blog_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    author_name VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES blog_article(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES blog_comment(id) ON DELETE CASCADE
);

-- 默认数据
MERGE INTO blog_category (id, name, sort_order) KEY(id) VALUES (1, '技术', 1);
MERGE INTO blog_category (id, name, sort_order) KEY(id) VALUES (2, '生活', 2);
MERGE INTO blog_category (id, name, sort_order) KEY(id) VALUES (3, '笔记', 3);

MERGE INTO blog_tag (id, name) KEY(id) VALUES (1, 'Java');
MERGE INTO blog_tag (id, name) KEY(id) VALUES (2, 'Spring Boot');
MERGE INTO blog_tag (id, name) KEY(id) VALUES (3, 'Vue');
MERGE INTO blog_tag (id, name) KEY(id) VALUES (4, 'MySQL');
MERGE INTO blog_tag (id, name) KEY(id) VALUES (5, '前端');
MERGE INTO blog_tag (id, name) KEY(id) VALUES (6, '后端');

MERGE INTO blog_user (id, username, password, nickname) KEY(id) VALUES (1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '站长');
