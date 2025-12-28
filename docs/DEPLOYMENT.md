# 面试知识库系统部署说明

## 目录

- [项目架构](#项目架构)
- [环境要求](#环境要求)
- [开发环境部署](#开发环境部署)
- [生产环境部署](#生产环境部署)
- [Docker部署](#docker部署)
- [常见问题](#常见问题)

---

## 项目架构

```
interview-system/
├── backend/                  # 后端项目 (Spring Boot)
│   ├── src/main/java/com/interviewkb/
│   │   ├── config/          # 配置类
│   │   ├── controller/      # 控制器层
│   │   ├── service/         # 服务层
│   │   ├── mapper/          # MyBatis Mapper
│   │   ├── entity/          # 实体类
│   │   ├── dto/             # 数据传输对象
│   │   ├── common/          # 公共模块
│   │   └── security/        # 安全模块
│   └── src/main/resources/
│       ├── application.yml  # 配置文件
│       └── mapper/          # MyBatis XML
├── frontend/                 # 前端项目 (Vue 3)
│   ├── src/
│   │   ├── api/             # API接口
│   │   ├── components/      # 组件
│   │   ├── views/           # 页面
│   │   ├── stores/          # Pinia状态管理
│   │   ├── router/          # 路由
│   │   ├── types/           # TypeScript类型
│   │   └── utils/           # 工具类
│   └── package.json
├── sql/                      # 数据库脚本
│   └── schema.sql
└── docs/                     # 文档
    └── DEPLOYMENT.md
```

---

## 环境要求

### 基础环境

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | 后端运行环境 |
| Node.js | 18+ | 前端构建环境 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存 |
| Maven | 3.8+ | 后端构建工具 |

### 推荐配置

- **开发环境**: 8GB内存, 4核CPU
- **生产环境**: 16GB内存, 8核CPU, SSD存储

---

## 开发环境部署

### 1. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行SQL脚本
source /path/to/interview-system/sql/schema.sql
```

### 2. 配置Redis

确保Redis服务已启动：
```bash
redis-server
```

### 3. 后端启动

```bash
cd backend

# 修改配置文件 (可选)
# vim src/main/resources/application.yml

# 构建并启动
mvn clean package -DskipTests
java -jar target/interview-kb-backend-1.0.0.jar

# 或使用Maven直接运行
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 4. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:5173` 启动

---

## 生产环境部署

### 1. 后端部署

#### 修改配置

编辑 `application.yml`，设置生产环境配置：

```yaml
spring:
  profiles:
    active: prod
```

或通过环境变量：

```bash
export DB_HOST=your-mysql-host
export DB_PORT=3306
export DB_NAME=interview_kb
export DB_USERNAME=your-username
export DB_PASSWORD=your-password
export REDIS_HOST=your-redis-host
export REDIS_PORT=6379
export REDIS_PASSWORD=your-redis-password
```

#### 构建和运行

```bash
cd backend

# 构建
mvn clean package -DskipTests -Pprod

# 运行
nohup java -jar \
  -Xms512m -Xmx1024m \
  -Dspring.profiles.active=prod \
  target/interview-kb-backend-1.0.0.jar \
  > app.log 2>&1 &
```

#### 使用Systemd管理服务

创建服务文件 `/etc/systemd/system/interview-kb.service`:

```ini
[Unit]
Description=Interview Knowledge Base Backend
After=syslog.target network.target

[Service]
User=app
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar /opt/interview-kb/interview-kb-backend.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl enable interview-kb
sudo systemctl start interview-kb
```

### 2. 前端部署

#### 构建

```bash
cd frontend

# 安装依赖
npm install

# 构建生产版本
npm run build
```

生成的文件在 `dist/` 目录

#### Nginx配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /var/www/interview-kb;
    index index.html;

    # 前端静态资源
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 上传文件
    location /files {
        alias /data/upload;
    }

    # Gzip压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
}
```

---

## Docker部署

### Docker Compose方式

创建 `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: interview-kb-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123456
      MYSQL_DATABASE: interview_kb
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/schema.sql:/docker-entrypoint-initdb.d/init.sql
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

  redis:
    image: redis:7-alpine
    container_name: interview-kb-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: interview-kb-backend
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=mysql
      - DB_PORT=3306
      - DB_NAME=interview_kb
      - DB_USERNAME=root
      - DB_PASSWORD=root123456
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    depends_on:
      - mysql
      - redis

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: interview-kb-frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
  redis_data:
```

### 后端Dockerfile

```dockerfile
# backend/Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 前端Dockerfile

```dockerfile
# frontend/Dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 启动服务

```bash
docker-compose up -d
```

---

## 常见问题

### 1. 数据库连接失败

检查MySQL服务状态和连接配置：
```bash
mysql -h localhost -u root -p -P 3306
```

### 2. Redis连接失败

确认Redis服务运行：
```bash
redis-cli ping
```

### 3. 前端无法访问后端API

- 检查后端服务是否正常运行
- 确认Nginx代理配置正确
- 检查CORS配置

### 4. 文件上传失败

- 检查上传目录权限
- 确认文件大小限制配置

### 5. JWT Token无效

- 检查JWT密钥配置
- 确认Token过期时间设置

---

## 维护建议

1. **日志管理**: 配置日志轮转，避免磁盘占用过大
2. **数据备份**: 定期备份MySQL数据库
3. **监控告警**: 配置服务健康检查和告警
4. **安全更新**: 定期更新依赖包，修复安全漏洞

---

## 技术支持

如有问题，请提交Issue或联系技术支持。
