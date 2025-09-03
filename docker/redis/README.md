# Docker
###### docker启动redis(单个)
docker run -itd -p 6379:6379 redis
docker run -itd -p 6379:6379 redis --requirepass 123456

###### docker启动redis(windows)
docker-compose -f .\redis-cluster-compose.yml up -d
docker-compose -f .\redis-example-compose.yml up -d
docker-compose -f .\redis-master-compose.yml up -d
docker-compose -f .\redis-ssl-compose.yml up -d

###### docker启动redis(macos)
docker compose -f redis-cluster-compose.yml up -d
docker compose -f redis-example-compose.yml up -d
docker compose -f redis-master-compose.yml up -d
docker compose -f redis-ssl-compose.yml up -d