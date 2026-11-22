# SQL Server Docker 环境

以下命令在 `docker/sqlserver` 目录执行。镜像固定为
`mcr.microsoft.com/mssql/server:2022-latest`，配置与 EasyShell SQL Server
集成测试使用的实例一致。

## 1. 启动

```bash
docker compose -f docker-compose.yml up -d
```

这个配置使用：

```text
容器名：easyshell-sqlserver
镜像：mcr.microsoft.com/mssql/server:2022-latest
版本：Developer
宿主机端口：14333
容器端口：1433
管理员账号：sa
管理员密码：easyshell@2026
持久卷：easyshell-sqlserver-data
```

Apple Silicon 会通过 Docker 的 amd64 模拟运行 SQL Server Linux 镜像。
首次启动通常需要 20～60 秒。

如果之前已经按相同参数启动了 `easyshell-sqlserver`，可以直接连接，
无需再次创建容器。Compose 适合在容器不存在时重建环境。

## 2. 初始化

SQL Server 官方镜像没有 `/docker-entrypoint-initdb.d` 自动初始化机制，
因此使用脚本等待数据库就绪后执行 `init.sql`：

```bash
bash init.sh
```

脚本会创建：

- 数据库：`EASYSHELL_SQLSERVER`
- Schema：`EASYSHELL_SRC`、`EASYSHELL_DST`
- 转储测试 Schema：`EASYSHELL_DUMP_SRC`、`EASYSHELL_DUMP_DST`
- `USERS`、`ORDERS`、`ORDER_ITEMS`、`COMPLEX_TYPES` 表及示例数据
- `COMPLEX_TYPES` 覆盖数值、Unicode、换行、二进制、日期时间、
  `DATETIMEOFFSET`、UUID、XML、NULL 和持久化计算列
- Computed Column、主键、唯一键、索引、检查约束、外键
- 视图、函数、过程、序列和触发器
- 表及字段扩展属性注释

`init.sql` 会先删除同名测试对象，可以重复执行。

## 3. EasyShell 连接

选择“添加连接 -> 数据库 -> SqlServer”，填写：

```text
协议：SqlServer
主机：127.0.0.1
端口：14333
用户名：sa
密码：easyshell@2026
数据库：EASYSHELL_SQLSERVER
SSL：关闭
```

SQL Server 的连接绑定一个数据库。连接成功后，树中展示的是该数据库内的
Schema；需要访问其他数据库时，修改连接配置中的“数据库”。

## 4. 验证

```bash
docker ps --filter name=easyshell-sqlserver

docker exec easyshell-sqlserver \
  /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'easyshell@2026' -C \
  -d EASYSHELL_SQLSERVER \
  -Q "SELECT name FROM sys.schemas WHERE name LIKE 'EASYSHELL_%';"

docker exec easyshell-sqlserver \
  /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'easyshell@2026' -C \
  -d EASYSHELL_SQLSERVER \
  -Q "SELECT COUNT(*) AS CNT FROM [EASYSHELL_SRC].[USERS];"
```

也可以直接运行 SQL Server 集成测试：

```bash
mvn -Dtest=cn.oyzh.easyshell.sqlserver.SqlServerIntegrationTest \
  -Dsqlserver.host=127.0.0.1 \
  -Dsqlserver.port=14333 \
  -Dsqlserver.user=sa \
  -Dsqlserver.password='easyshell@2026' \
  test
```

## 5. 常用运维

```bash
docker compose -f docker-compose.yml ps
docker compose -f docker-compose.yml logs -f
docker stop easyshell-sqlserver
docker start easyshell-sqlserver
```

`docker compose down` 会停止并删除容器，但保留命名卷
`easyshell-sqlserver-data`。不要执行 `docker compose down -v`，除非确认
需要删除数据库数据。

原测试容器如果有需要保留的数据，请先备份，不要在未备份时删除容器或卷。
