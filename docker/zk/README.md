# Docker
###### docker启动zk(windows)
docker run -itd -p 2181:2181 zookeeper

###### docker启动zk(集群) windows
docker-compose -f .\zk-cluster-compose.yml up -d
###### docker启动zk(集群) macos
docker compose -f zk-cluster-compose.yml up -d

###### docker启动zk(sasl) windows
docker-compose -f .\zk-sasl-compose.yml up -d
###### docker启动zk(sasl) macos
docker compose -f zk-sasl-compose.yml up -d