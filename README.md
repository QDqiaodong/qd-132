# qd-132 航天科普馆互动实验设备研学团参观时段配对台账系统

## 项目简介

航天科普馆互动实验设备、研学团与参观时段配对台账系统。项目包含 Vue/Vite 前端、Spring Boot 后端、MySQL 与 Redis，已按依赖层缓存和固定端口交付链路规范整理。

## 访问地址

- 前端地址: [http://localhost:8202](http://localhost:8202)
- 127.0.0.1 地址: [http://127.0.0.1:8202](http://127.0.0.1:8202)
- 后端 API: http://localhost:8212/api

## 端口

- 前端: 8202
- 后端: 8212
- MySQL: 3368
- Redis: 6441

## 编译与启动

```bash
cd backend
mvn compile -q

cd ../frontend
npm ci
npm run build

cd ..
docker compose up -d --build
```

Docker Compose 端口均绑定到 `127.0.0.1`，镜像基础地址通过 `.env` 中的 `DOCKER_REGISTRY` 统一控制。
