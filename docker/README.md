# Docker
###### docker启动 windows
docker-compose -f .\文件名 up -d
###### docker启动 macos
docker compose -f 文件名 up -d
###### docker启动 linux
docker compose -f .\文件名 up -d  
或  
docker-compose -f .\文件名 up -d  
###### docker构建镜像
docker build -t rlogin-server -f rlogin.dockerfile . 
###### 数据库示例
- MySQL：`mysql/mysql.txt`
- MariaDB：`mariadb/mariadb.txt`
- PostgreSQL：`postgresql/docker.txt`（测试对象初始化脚本：`postgresql/init.sql`）
- 达梦：`dameng/docker.txt`
- MongoDB：`mongo/docker.txt`
- Valkey：`valkey/README.md`（单节点、ACL、主从/哨兵、集群、TLS）
