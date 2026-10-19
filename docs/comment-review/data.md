# 数据模块（`cn.oyzh.easyshell.data`）代码审查文档 · 索引

> 范围：`src/main/java/cn/oyzh/easyshell/data/` 递归全部 `.java` 文件，共 **122** 个。
> 按子包拆分为下列分册；整文件被注释掉的死代码不逐类展开，仅在各自分册末尾“跳过清单”登记。
> 术语用中文，类名 / 方法名 / 字段名等标识符保留原文。

## 分册

| 分册文件 | 子包 | 文件数 | 覆盖存活类 | 跳过死代码文件 |
|---|---|---|---|---|
| [data-dameng.md](./data-dameng.md) | `data/dameng/`（dto·file·handler·ui） | 36 | 20 | 16 |
| [data-db.md](./data-db.md) | `data/db/`（含 event） | 6 | 0 | 6（整包死代码） |
| [data-mongo.md](./data-mongo.md) | `data/mongo/`（config·dto·file·handler·ui） | 33 | 20 | 13 |
| [data-mysql.md](./data-mysql.md) | `data/mysql/`（config·dto·file·handler·ui） | 41 | 20 | 21 |
| [data-redis-zk.md](./data-redis-zk.md) | `data/redis/handler/`、`data/zk/handler/` | 6 | 6 | 0 |
| **合计** | — | **122** | **66** | **56** |

## 总体结构

数据模块按“数据库类型”分包，每个子包内部结构高度一致：

- `dto/`：导出列 / 导出表 / 导入文件 / 传输对象等 JavaFX 属性模型。
- `file/`：`TypeFileWriter` / `TypeFileReader` 系列，按 csv / excel / json / xml / txt / sql / html / js 等格式读写（多数已迁移到父模块 `cn.oyzh.fx.db`）。
- `handler/`：执行主体，含导出 `Export`、导入 `Import`、传输 `Transport`、导出 `Dump`、执行脚本 `RunSqlFile/RunScriptFile` 等处理器。
- `ui/`：与业务无关的视图控件（列 `ListView`、表 `TableView`、集合/表 `ComboBox` 等）。
- `config/`：导入/导出配置（多数已被 `fx.db` 的 `DBDataExportConfig` / `DBDataImportConfig` 取代）。

## 关键结论

- **大量遗留死代码**：122 个文件中约 56 个整文件被 `//` 注释，集中在各包的 `*TypeFileReader`、`config/` 与 `dto/*Transport*`，已由父模块 `cn.oyzh.fx.db` 的对应实现取代（详见各分册“跳过清单”）。
- **`db/` 子包整体为死代码**：`DBDialect`、`DBFeature`、`DBObjectList`、`DBObjectStatus`、`DBEventAlertSqlGenerator`、`DBEventCreateSqlGenerator` 六个文件均从首行起全部注释。
- **存活处理器统一继承 `cn.oyzh.fx.db.data.handler` 系列基类**，通过各类型对应 `Client` / `DataUtil` 完成数据库侧读写。
- 走查中发现的可疑点（详见分册）：`ShellMysqlDataImportHelper` 残留 `System.out.println`；redis 传输与导出 `isExclude` 行为不一致；`ShellMysqlDataRunSqlFileHandler` 分行解析依赖标志位、`-- ` 注释判定较脆弱。

> 生成说明：本索引与各分册均为新增文档，未修改任何 `.java` 文件。
