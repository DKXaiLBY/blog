# 部署指南

## 环境要求

- **后端**: JDK 17 / Maven 3.8+ / MySQL 8.0
- **前端**: Node.js 18+ / Nginx
- **可选**: Docker & Docker Compose

---

## 方式一：Docker Compose 一键部署（推荐）

适合任何有 Docker 的机器，一条命令全搞定。

```bash
# 1. 安装 Docker
# Windows/Mac: https://docker.com/products/docker-desktop
# Linux: curl -fsSL https://get.docker.com | bash

# 2. 克隆项目
git clone https://github.com/DKXaiLBY/blog.git
cd blog

# 3. 配置环境变量（可选，不改也能跑）
cp .env.example .env
nano .env   # 修改密码和 JWT 密钥

# 4. 启动
docker compose up -d

# 5. 浏览器打开 http://你的服务器IP
# 默认后台: admin / admin123
```

**持久化**: MySQL 数据在 `mysql_data` 卷，上传图片在 `uploads_data` 卷，重装不影响数据。

**更新**: `git pull && docker compose up -d --build`

---

## 方式二：宝塔面板部署

适合国内 Linux 服务器，网页操作，不需要记命令。

### 安装宝塔

```bash
# CentOS/Ubuntu/Debian
curl -sSO https://download.bt.cn/install/install_lts.sh && bash install_lts.sh
```

安装完后浏览器打开面板地址，登录。

### 安装环境

在宝塔面板的「软件商店」安装：
- **MySQL 8.0**
- **Nginx**（1.20+）
- **Java 项目管理器** 或 **Docker 管理器**
- **PM2 管理器**（运行 JAR）

### 部署步骤

**1. 创建数据库**

宝塔面板 → 数据库 → 添加数据库：
```
数据库名: blog
用户名: blog
密码: (自己设置)
```

导入 `blog-server/src/main/resources/schema.sql`。

**2. 打包后端**

```bash
cd blog-server
./mvnw package -DskipTests
# 得到 target/blog-server-1.0.0.jar
```

**3. 运行后端**

宝塔 → Java 项目管理器 → 添加项目：
- 项目路径: `/www/wwwroot/blog/blog-server/target/blog-server-1.0.0.jar`
- 端口: `8080`
- 环境变量: `MYSQL_HOST=127.0.0.1;MYSQL_USER=blog;MYSQL_PASSWORD=你的数据库密码;JWT_SECRET=随机字符串32位;SPRING_PROFILES_ACTIVE=mysql`

或者用 PM2 管理器直接启动：

```bash
cd /www/wwwroot/blog/blog-server
MYSQL_HOST=127.0.0.1 \
MYSQL_USER=blog \
MYSQL_PASSWORD=你的密码 \
JWT_SECRET=随机字符串 \
java -jar target/blog-server-1.0.0.jar --spring.profiles.active=mysql
```

**4. 打包并部署前端**

```bash
cd blog-web
npm install
npm run build
# 得到 dist/ 目录
```

**5. 配置 Nginx**

宝塔 → 网站 → 添加站点 → 输入你的域名或 IP。

站点设置 → 配置文件 → 粘贴以下内容：

```nginx
server {
    listen 80;
    server_name localhost;  # 改为你的域名

    # 前端
    location / {
        root /www/wwwroot/blog/blog-web/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        client_max_body_size 10m;
    }

    # 上传文件
    location /uploads/ {
        proxy_pass http://127.0.0.1:8080;
    }
}
```

保存后 Nginx 重载，访问 `http://你的IP` 即可。

---

## 方式三：手动部署（无 Docker）

适合想完全掌控的 Linux 服务器。

### 1. 安装依赖

```bash
# Ubuntu/Debian
apt update && apt install -y openjdk-17-jdk mysql-server nginx nodejs npm maven

# CentOS 7/8
yum install -y java-17-openjdk mysql-server nginx nodejs npm maven
```

### 2. 创建数据库

```sql
mysql -u root -p
CREATE DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'blog'@'localhost' IDENTIFIED BY '你的密码';
GRANT ALL ON blog.* TO 'blog'@'localhost';
FLUSH PRIVILEGES;
USE blog;
SOURCE /path/to/blog/blog-server/src/main/resources/schema.sql;
EXIT;
```

### 3. 打包运行后端

```bash
cd blog-server
./mvnw package -DskipTests

# 启动（Systemd 守护进程）
cat > /etc/systemd/system/blog-server.service << 'EOF'
[Unit]
Description=Blog Server
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/blog/blog-server
Environment="SPRING_PROFILES_ACTIVE=mysql"
Environment="MYSQL_HOST=127.0.0.1"
Environment="MYSQL_USER=blog"
Environment="MYSQL_PASSWORD=你的密码"
Environment="JWT_SECRET=随机字符串"
ExecStart=/usr/bin/java -jar /opt/blog/blog-server/target/blog-server-1.0.0.jar
Restart=always

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable --now blog-server
```

### 4. 打包部署前端

```bash
cd blog-web
npm install && npm run build
cp -r dist /opt/blog/blog-web/dist
```

### 5. 配置 Nginx

```bash
cat > /etc/nginx/conf.d/blog.conf << 'EOF'
server {
    listen 80;
    server_name _;

    root /opt/blog/blog-web/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        client_max_body_size 10m;
    }

    location /uploads/ {
        proxy_pass http://127.0.0.1:8080;
    }
}
EOF

systemctl restart nginx
```

---

## 方式四：阿里云/腾讯云 + 宝塔

适合面向国内用户、需要备案域名的场景。

### 阿里云学生机

1. 阿里云搜索「开发者计划」→ 学生认证 → 购买轻量应用服务器（约 ¥68/年）
2. 镜像选 **CentOS 7.9** 或 **Ubuntu 22.04**
3. 防火墙/安全组开放端口: **22(SSH), 80(HTTP), 443(HTTPS), 8888(宝塔)**
4. SSH 登录后安装宝塔，然后按「方式二」操作

### 接入 Cloudflare（免费 CDN + HTTPS）

1. 在 Cloudflare 添加你的域名
2. DNS 添加 A 记录指向服务器 IP（开启代理，小橙云）
3. SSL/TLS 选 **Full (strict)**
4. 宝塔 Nginx 配置 `server_name` 改为你的域名
5. 宝塔 → 网站 → SSL → 申请 Let's Encrypt 免费证书

---

## 方式五：Vercel（免费前端托管）

如果只想展示前端，后端暂时用 H2 内存数据库在本地跑：

```bash
# 1. 安装 Vercel CLI
npm i -g vercel

# 2. 进入前端目录部署
cd blog-web
vercel --prod

# 3. 在 Vercel 后台设置环境变量
# VITE_API_BASE_URL = 你的后端地址
```

前端免费托管在 `xxx.vercel.app`，全球 CDN 秒开。

---

## 故障排查

| 问题 | 解决 |
|------|------|
| 启动后页面 502 | 检查后端是否启动: `docker logs blog-server` 或 `journalctl -u blog-server` |
| MySQL 连接失败 | 确认数据库已创建、密码正确、防火墙放行 |
| 上传图片失败 | 确认 `uploads/` 目录有写入权限，Nginx `client_max_body_size` 够大 |
| Docker 内存不足 | `docker system prune -a` 清理，或增大 Docker 内存限制 |
| 端口被占用 | `lsof -i :80` 查看，修改 Nginx 或 Docker Compose 端口映射 |

---

## 生产环境建议

1. **修改默认密码**: 登录后台 `/admin` → 个人设置 → 修改密码
2. **配置邮件**: `.env` 中填写 SMTP 信息，开启评论邮件通知
3. **开启 HTTPS**: 用 Cloudflare CDN 或申请 Let's Encrypt 证书
4. **定期备份**: 数据库 `mysqldump` + Docker 卷备份
5. **设置 JWT 密钥**: 生成随机 64 位字符串替换默认值

```bash
# 生成随机密钥
openssl rand -hex 32
```
