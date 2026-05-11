# 前端 Vue 构建 + Nginx 部署
# 构建：npm run build（在 blog-web 目录）
# 镜像：docker build -t blog-web .

FROM nginx:alpine
COPY dist/ /usr/share/nginx/html/
COPY ../nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
