# 数据模块 · db 包

> 范围：`cn/oyzh/easyshell/data/db/`（含 `event/` 子包），递归共 6 个 `.java` 文件。
> 说明：本包全部 6 个文件均为**整文件被注释掉的死代码**（每行以 `//` 开头，包含 package、import、类体全部被注释），无可审查的存活类。依据「整文件被注释掉的死代码不列出」的规则，下文不展开任何类段落，仅在文末“跳过清单”逐文件登记。

## 跳过清单

以下文件整个文件均为注释（`//package ...` 起全部注释），属于死代码，未在正文列出：

| 文件 | 原注释中声明的类型 | 备注 |
|---|---|---|
| `DBDialect.java` | `public enum DBDialect` | 数据库类型（方言）枚举，仅 `MYSQL`、`MONGODB` 两个常量；注释内的方法 `dbType()`（返回 druid `DbType`，仅 `MYSQL` 有映射）、`valueList()`、`of(String)`（依据 `ShellPrototype.MYSQL` 匹配，否则默认返回 `MYSQL`）。 |
| `DBFeature.java` | `public enum DBFeature` | 数据库特性枚举，仅 `CHECK`、`EVENT` 两个常量，无字段无方法。 |
| `DBObjectList.java` | `public abstract class DBObjectList<S extends DBObjectStatus> extends ArrayList<S>` | 数据库对象状态列表，含状态类型常量 `TYPE_NORMAL=0`/`TYPE_DELETED=1`/`TYPE_CREATED=2`/`TYPE_CHANGED=3` 及按状态过滤/判定的方法（`createdList()`/`changedList()`/`deletedList()`/`normalList()`/`filterList(byte...)`/`isChanged`/`isCreated`/`isDeleted`/`isNormal` 等）。 |
| `DBObjectStatus.java` | `public class DBObjectStatus implements Destroyable` | 数据库对象变更状态模型，使用 JavaFX 属性 `changedProperty`/`deletedProperty`/`createdProperty`/`statusProperty`，配套 `changedFlag`/`originalData` 映射与变更判定、状态符号 `+`/`*`/空、`destroy()` 释放等逻辑。 |
| `event/DBEventAlertSqlGenerator.java` | `public abstract class DBEventAlertSqlGenerator` | 事件告警 SQL 生成器抽象类，构造持有 `DBDialect`，抽象方法 `generate(MysqlEvent)`，静态 `generate(DBDialect, MysqlEvent)` 按方言分发（仅 `MYSQL` 走 `MysqlEventAlertSqlGenerator`）。 |
| `event/DBEventCreateSqlGenerator.java` | `public abstract class DBEventCreateSqlGenerator` | 事件创建 SQL 生成器抽象类，结构同上，仅 `MYSQL` 走 `MysqlEventCreateSqlGenerator`。 |

- 覆盖存活的类数：**0**
- 跳过的死代码文件数：**6**（即本包全部文件）
