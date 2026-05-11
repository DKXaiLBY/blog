-- 博客系统数据库初始化脚本（MySQL）
-- 执行方式: mysql -u root -p < schema.sql
-- 或通过 docker-entrypoint-initdb.d 自动执行

CREATE TABLE IF NOT EXISTS blog_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    tagline VARCHAR(200),
    bio TEXT,
    skills VARCHAR(500),
    contacts VARCHAR(500),
    projects TEXT,
    announcement TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS blog_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    sort_order INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS blog_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS blog_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500),
    content MEDIUMTEXT,
    cover_image VARCHAR(255),
    category_id BIGINT,
    author_id BIGINT,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    status INT DEFAULT 0,
    is_top INT DEFAULT 0,
    series VARCHAR(200),
    scheduled_publish_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_article_category (category_id),
    INDEX idx_article_author (author_id),
    INDEX idx_article_status (status),
    INDEX idx_article_created (created_at),
    FOREIGN KEY (category_id) REFERENCES blog_category(id),
    FOREIGN KEY (author_id) REFERENCES blog_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS blog_article_tag (
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    INDEX idx_at_article (article_id),
    INDEX idx_at_tag (tag_id),
    FOREIGN KEY (article_id) REFERENCES blog_article(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES blog_tag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS blog_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    author_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    content TEXT NOT NULL,
    status INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_comment_article (article_id),
    INDEX idx_comment_parent (parent_id),
    INDEX idx_comment_status (status),
    FOREIGN KEY (article_id) REFERENCES blog_article(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES blog_comment(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS blog_subscriber (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(200) NOT NULL,
    verified INT DEFAULT 0,
    verify_token VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS blog_friend_link (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    description VARCHAR(200),
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 默认数据
INSERT IGNORE INTO blog_category (id, name, sort_order) VALUES
(1, '技术', 1), (2, '生活', 2), (3, '笔记', 3);

INSERT IGNORE INTO blog_tag (id, name) VALUES
(1, 'Java'), (2, 'Spring Boot'), (3, 'Vue'),
(4, 'MySQL'), (5, '前端'), (6, '后端');

INSERT IGNORE INTO blog_user (id, username, password, nickname, tagline, bio, skills, contacts)
VALUES (1, 'admin',
  '$2a$10$qcesUgTew6FqIM10rXP6zOAzFLhTsMHRRnT2EkX9U7jpebsVA2nB.',
  '站长', '热爱技术，专注 Java 与前端开发',
  '你好，我是一名热爱编程的开发者。',
  '[{"name":"Java / Spring Boot","color":"var(--accent)"},{"name":"Vue 3 / Element Plus","color":"#4ade80"},{"name":"MySQL","color":"#fbbf24"},{"name":"Docker","color":"#60a5fa"},{"name":"Git","color":"#f472b6"},{"name":"MyBatis Plus","color":"#a78bfa"}]',
  '[{"label":"GitHub","icon":"github","href":"https://github.com/DKXaiLBY"},{"label":"QQ 邮箱","icon":"email","href":"https://mail.qq.com/"},{"label":"RSS 订阅","icon":"rss","href":"/api/rss"}]'
);
