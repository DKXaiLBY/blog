-- H2 兼容版数据库初始化脚本
-- 用于开发环境（免安装 MySQL）

CREATE TABLE IF NOT EXISTS blog_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    tagline VARCHAR(200),
    bio CLOB,
    skills VARCHAR(500),
    contacts VARCHAR(500),
    projects TEXT,
    announcement TEXT,
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
    like_count INT DEFAULT 0,
    status INT DEFAULT 0,
    is_top INT DEFAULT 0,
    series VARCHAR(200),
    scheduled_publish_at TIMESTAMP,
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
    email VARCHAR(100),
    content TEXT NOT NULL,
    status INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES blog_article(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES blog_comment(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS blog_friend_link (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    description VARCHAR(200),
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS blog_subscriber (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(200) NOT NULL,
    verified INT DEFAULT 0,
    verify_token VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_article_category ON blog_article(category_id);
CREATE INDEX IF NOT EXISTS idx_article_author ON blog_article(author_id);
CREATE INDEX IF NOT EXISTS idx_article_status ON blog_article(status);
CREATE INDEX IF NOT EXISTS idx_article_created ON blog_article(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comment_article ON blog_comment(article_id);
CREATE INDEX IF NOT EXISTS idx_comment_parent ON blog_comment(parent_id);
CREATE INDEX IF NOT EXISTS idx_comment_status ON blog_comment(status);
CREATE INDEX IF NOT EXISTS idx_article_tag_article ON blog_article_tag(article_id);
CREATE INDEX IF NOT EXISTS idx_article_tag_tag ON blog_article_tag(tag_id);

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

MERGE INTO blog_user (id, username, password, nickname, tagline, bio, skills, contacts) KEY(id) VALUES (1, 'admin', '$2a$10$qcesUgTew6FqIM10rXP6zOAzFLhTsMHRRnT2EkX9U7jpebsVA2nB.', '站长', '热爱技术，专注 Java 与前端开发', '你好，我是一名热爱编程的开发者。目前专注于后端 Java 开发与前端 Vue 技术栈。这个博客是我记录学习过程、分享技术心得的地方。我相信技术的力量可以改变世界，也相信持续学习是成长的唯一途径。', '[{"name":"Java / Spring Boot","color":"var(--accent)"},{"name":"Vue 3 / Element Plus","color":"#4ade80"},{"name":"MySQL / H2","color":"#fbbf24"},{"name":"Docker / 运维","color":"#60a5fa"},{"name":"Git / GitHub","color":"#f472b6"},{"name":"MyBatis Plus","color":"#a78bfa"}]', '[{"label":"GitHub","icon":"github","href":"https://github.com/DKXaiLBY"},{"label":"QQ 邮箱","icon":"email","href":"https://mail.qq.com/"},{"label":"RSS 订阅","icon":"rss","href":"/api/rss"}]');
