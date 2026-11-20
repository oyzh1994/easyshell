# LLM Agent 工具作用域与优先级改进任务

> 状态：待逐项处理  
> 创建日期：2026-11-21  
> 处理方式：每一项完成后勾选，并补充实际提交或验证记录。

## 1. 问题背景

当用户打开 SSH Tab 并进入 LLM 会话时，当前会话会同时向模型暴露：

- EasyShell 内置工具，包括本机命令执行能力。
- 当前 SSH Tab 的远端命令工具。

用户输入“安装 Docker”时，模型可能选择本机命令工具，把 Docker 安装到运行 EasyShell 的电脑，而不是当前 SSH 服务器。

这不是单纯的概率问题，而是当前工具注册和提示词没有明确表达“默认操作目标”。

## 2. 当前实现证据

### 2.1 工具同时注册

`ChatController.initAssistant` 总是先加入 `ShellLLMTools`，再加入当前会话工具：

- `src/main/java/cn/oyzh/easyshell/controller/llm/ChatController.java`
- 初始化位置约在 `initAssistant` 的 `ShellLLMTools internalTools = new ShellLLMTools(this.tabPane);`
- 之后执行 `tools.add(internalTools);` 和 `tools.add(request.sessionTools);`

普通 AI Service 和 Agent Runtime 最终都会收到合并后的工具列表。

### 2.2 SSH 与本地命令入口并存

SSH 远端命令入口：

- `src/main/java/cn/oyzh/easyshell/ssh/ShellSSHLLMTools.java`
- 工具名：`ssh_execute_command`

本机命令入口：

- `src/main/java/cn/oyzh/easyshell/internal/ShellLLMTools.java`
- 工具名：`execute_local_command_direct`
- 工具名：`execute_local_command_in_directory_direct`

本地 Tab 还会提供：

- `src/main/java/cn/oyzh/easyshell/local/ShellLocalLLMTools.java`
- 工具名：`execute_local_command`

因此模型能看到多个语义接近、但执行目标不同的命令工具。

### 2.3 Planner 缺少当前连接上下文

Agent 的 Planner 目前主要获取：

- 会话历史。
- 工具名称目录。
- 用户任务。

Planner 没有明确获得当前绑定 Tab 的协议、连接名称、主机和用户信息，也没有收到“当前 Tab 是默认操作目标”的强制规则。

相关代码：

- `src/main/java/cn/oyzh/easyshell/llm/agent/LLMAgentRuntime.java`
- `createPlan`
- `toolCatalog`

当前 `toolCatalog` 把 `localTools` 中的全部工具都列在 `connection` 分组下，无法清楚区分：

- 当前连接工具。
- EasyShell 内部工具。
- 本机执行工具。
- MCP 工具。

### 2.4 Worker 提示词职责重叠

Worker 的 `localSystemMessage` 当前由“内置工具说明 + 当前会话工具说明”拼接而成：

```text
internalTools.toolSystemMessage()
+
request.sessionTools.systemMessage(executionMode)
```

两份说明都可能是可执行工具说明，但没有最高优先级的作用域规则，容易出现：

- 服务器任务使用本机命令。
- 本机任务使用远端命令。
- 模型在多套近似工具之间随机选择。

### 2.5 工具名称重复

多个工具类使用同名工具：

- `get_server_version`
- `execute_command`

例如 SSH、Local、文件工具、数据库工具都可能包含 `get_server_version` 或 `execute_command`。

同名工具可能造成：

- 工具注册冲突。
- 模型无法根据名称区分作用域。
- 审计记录难以准确判断实际使用的工具来源。
- 错误调用后的自动纠正建议不稳定。

## 3. 目标策略

当前实施策略：

```text
当前绑定 Tab 是默认操作目标。
有绑定 Tab 时使用 SESSION + LOCAL_EXECUTION，不暴露 INTERNAL。
Local Tab 是 SESSION 的一种。
没有绑定 Tab 时使用 INTERNAL + LOCAL_EXECUTION。
```

示例：

| 当前 Tab | 用户表达 | 默认目标 | 优先工具 |
| --- | --- | --- | --- |
| SSH | 安装 Docker | SSH 服务器 | `ssh_execute_command` |
| SSH | 查看服务器磁盘 | SSH 服务器 | `ssh_get_disk_info` |
| SSH | 在本机安装 Docker | 本机 | `local_execute_command_direct` |
| Local | 安装 Docker | 本机 | `execute_local_command` |
| MySQL | 查询表结构 | 当前数据库 | MySQL 工具 |
| SSH | 查看 EasyShell 版本 | 无 INTERNAL 工具 | 需要解绑 Tab 后使用 INTERNAL |

当前确定的核心组合规则：

| 场景 | 暴露工具 |
| --- | --- |
| 有绑定 Tab | `SESSION + LOCAL_EXECUTION` |
| Local Tab | 属于 `SESSION + LOCAL_EXECUTION` |
| 没有绑定 Tab | `INTERNAL + LOCAL_EXECUTION` |
| MCP | 独立开关，不与上述组合替代 |

`INTERNAL` 不与 `SESSION` 同时暴露。后续可在此规则上增加用户策略设置。

“优先级”只能提高选择正确率，不能保证安全。真正可靠的做法是同时使用：

1. 工具作用域过滤。
2. Planner/Worker 提示词约束。
3. 运行时调用拦截。
4. 明确的工具命名。

## 4. 任务清单

### [x] T1. 定义工具作用域

为 `LLMTools` 增加作用域描述，例如：

```java
public enum LLMToolScope {
    SESSION,
    INTERNAL,
    LOCAL_EXECUTION,
    MCP
}
```

要求：

- 每个工具对象能够声明自己的作用域。
- 工具作用域可以被审计日志记录。
- MCP 工具保持独立作用域。
- 原始 `@Tool` 方法不需要逐个增加重复注解，优先在 `LLMTools` 实现类级别声明。

验收标准：

- 能列出一次请求实际暴露的所有工具及其作用域。
- 同名工具可以按照“作用域 + 名称”区分。

完成记录：

- 新增 `LLMToolScope`，包含 `SESSION`、`INTERNAL`、`LOCAL_EXECUTION`、`MCP`。
- `LLMTools.scope()` 默认返回 `SESSION`，因此 SSH、数据库、文件工具以及 Local Tab 的会话工具统一归为 SESSION。
- `ShellLLMTools` 返回 `INTERNAL`。
- `ShellLocalExecutionLLMTools` 返回 `LOCAL_EXECUTION`。
- `LLMToolAudit` 已记录 `toolScope`，MCP 动态注册工具固定记录为 `MCP`。

### [x] T2. 拆分内置执行能力

当前 `ShellLLMTools` 同时包含：

- EasyShell 自身诊断。
- 连接、分组、片段和查询管理。
- 主题和字体设置。
- X11 服务管理。
- 本机命令执行。

已拆分为：

- `ShellLLMTools`：只保留 EasyShell 内部能力。
- `ShellLocalExecutionLLMTools`：只保留本机命令执行能力。

验收标准：

- 非 Local Tab 可以选择不暴露本机执行工具。
- EasyShell 内部只读工具仍可正常使用。
- 不修改本机命令工具的实际执行逻辑。

完成记录：

- 从 `ShellLLMTools` 移除本机命令执行器、字段和生命周期。
- 新增 `ShellLocalExecutionLLMTools`，提供 `local_execute_command_direct` 和
  `local_execute_command_in_directory_direct`。
- 新增独立提示词 `local_execution_tool.txt`。
- 从 `internal_tool.txt` 移除本机命令执行规则，避免 INTERNAL 提示词继续描述 LOCAL_EXECUTION 能力。

### [x] T3. 按 Tab 过滤工具

已在 `ChatController.initAssistant` 构建工具列表时增加策略判断。

已实施行为：

```text
存在绑定 Tab:
  SESSION + LOCAL_EXECUTION

不存在绑定 Tab:
  INTERNAL + LOCAL_EXECUTION

Local Tab:
  作为 SESSION + LOCAL_EXECUTION

MCP:
  独立启用并追加
```

验收标准：

- SSH Tab 默认请求中不包含 `INTERNAL` 工具。
- SSH Tab 仍包含 `ssh_execute_command`。
- Local Tab 按 SESSION 处理。
- 无 SESSION 时才暴露 INTERNAL。

完成记录：

- 新增 `LLMToolSelection`，统一解析一次请求的工具组合和各类系统提示词。
- 有 SESSION 时只选择 `SESSION + LOCAL_EXECUTION`。
- 无 SESSION 时只选择 `INTERNAL + LOCAL_EXECUTION`。
- SESSION 和 INTERNAL 同时出现会被直接拒绝。
- AgentRuntime 的 Planner/Worker 分组同步使用相同作用域：
  - 有 SESSION 时只提供 `connection` 和 `mcp`。
  - 无 SESSION 时只提供 `internal` 和 `mcp`。
- Planner 返回过期分组时会自动纠正为当前可用分组。

### [ ] T4. 注入当前连接上下文

为 Planner 和 Worker 增加结构化作用域信息：

```text
当前绑定：SSH
连接名称：server-a
主机：10.0.0.12
端口：22
用户：root
默认目标：当前绑定的 SSH 服务器
```

要求：

- Planner 在创建计划时必须看到当前连接上下文。
- Worker 在执行服务器任务时必须看到相同上下文。
- 不输出密码、私钥、令牌、Cookie 等敏感信息。
- 上下文应来自当前 `LLMTabCapability` 或会话工具，而不是由模型猜测。

验收标准：

- “安装 Docker”的任务计划明确绑定当前 SSH 会话。
- Planner 的 `connection` 分组不再含糊地包含本机执行工具。

### [ ] T5. 增加运行时调用拦截

即使模型选择了错误工具，也应在执行前拦截。

当当前 Tab 不是 Local，且用户没有明确要求本机操作时，如果调用：

- `execute_local_command_direct`
- `execute_local_command_in_directory_direct`
- `execute_local_command`
- `execute_local_command_in_directory`

则返回可纠正的工具结果，例如：

```text
当前会话绑定的是 SSH 服务器 server-a。
不能使用本机命令执行该请求。
服务器操作请使用 ssh_execute_command。
如确实需要操作 EasyShell 所在本机，请明确说明“本机”，或切换到 Local Tab。
```

要求：

- 拦截发生在实际执行命令之前。
- 返回结果要包含当前作用域和正确工具建议。
- 不把拦截误报成命令执行失败。
- 审计日志记录拦截原因。

验收标准：

- SSH Tab 中误调用本机命令不会执行任何本地进程。
- 模型收到纠正结果后能够重新选择 SSH 工具。

### [ ] T6. 调整 Agent Planner 的工具目录

当前 `toolCatalog` 应改成按作用域分组，而不是把所有 `localTools` 都列在 `connection` 下。

目标结构：

```text
session/connection:
  ssh_execute_command
  ssh_get_connection_info
  ssh_get_server_summary
  ...

easyshell/internal:
  get_project_info
  list_open_connections
  ...

local machine:
  local_execute_command_direct
  local_execute_command_in_directory_direct

mcp:
  ...
```

Planner 规则至少包含：

- 服务器变更默认使用 `SESSION`。
- EasyShell 自身诊断只在没有 SESSION 时使用 `INTERNAL`。
- 本机命令使用 `LOCAL_EXECUTION`。
- 不同作用域工具不可互相替代。

验收标准：

- “安装 Docker”不会规划成本机内部任务。
- “查看 EasyShell 版本”不会选择 SSH 命令。
- “查看当前 Redis 版本”选择 Redis 服务器工具。

### [x] T7. 统一工具命名

评估并逐步消除跨作用域同名工具。

推荐命名：

```text
ssh_execute_command
local_execute_command
mysql_execute_sql
redis_execute_command
ssh_get_server_version
mysql_get_server_version
local_get_server_version
```

如果重命名成本过高，可为工具描述增加强制前缀，并在 Tool Registry 层检查名称冲突。

验收标准：

- 同一请求中不存在含义不同但名称相同的工具。
- 审计日志可以直接根据工具名识别作用域。
- 现有工具测试和提示词同步更新。

完成记录：

- 已扫描全部生产工具注解，原有重复名称数量为 22 类。
- 数据库工具统一增加协议前缀，例如 `mysql_execute_sql`、`postgresql_list_tables`、`sqlite_get_server_version`。
- Redis、Valkey、ZooKeeper、SSH 的命令入口统一增加作用域前缀，例如 `redis_execute_command`、`valkey_execute_command`、`zk_execute_command`、`ssh_execute_command`。
- Mongo、HTTP、文件和本地的冲突名称已增加作用域前缀。
- 相关系统提示词中的工具名称已同步更新。
- `LLMToolNameStrategy.create` 会在注册工具前检查重复名称，发现冲突时直接拒绝请求并列出冲突来源。
- 新增源码级全局唯一性测试和运行时重复名称测试。
- 当前生产代码中的重复 `@Tool` 名称数量为 0。

### [ ] T8. 增加工具策略设置

在 LLM 设置中增加：

```text
工具作用域：
  (o) 当前会话优先
  ( ) 仅当前会话
  ( ) 全部工具
```

可选增加：

```text
允许明确要求时跨作用域执行本机命令
[x] 开启
```

要求：

- 默认选择“当前会话优先”。
- 设置项持久化。
- 切换后新请求立即生效。
- 旧会话恢复时行为一致。

验收标准：

- 默认配置下 SSH 会话不会误用本机命令。
- “仅当前会话”模式完全不暴露本机执行工具。
- “全部工具”模式保留现有高级用户能力。

### [ ] T9. 补充审计信息

工具审计记录建议增加：

- `toolScope`
- `sessionTabId`
- `sessionTabType`
- `connectionHost`
- `connectionUser`
- `blockedByScope`

验收标准：

- 能判断一次调用属于本机、SSH、数据库还是 EasyShell 内部工具。
- 能追溯被作用域策略拦截的调用。
- 不记录敏感凭据。

### [ ] T10. 增加回归测试

至少覆盖以下场景：

1. SSH Tab 的工具列表包含 `ssh_execute_command`。
2. SSH Tab 默认工具列表不包含本机执行工具。
3. Local Tab 的工具列表包含本地执行工具。
4. Planner 收到当前连接的主机、用户和默认目标。
5. “安装 Docker”在 SSH 会话中选择 SSH 执行器。
6. “在本机安装 Docker”在允许跨作用域时选择本机执行器。
7. SSH 会话误调用本机工具时被拦截。
8. 拦截结果建议模型改用 `ssh_execute_command`。
9. 工具名称冲突检查能够发现同名工具。
10. 设置切换后工具暴露范围正确变化。

验收标准：

- 测试能够复现并锁定当前误装到本机的问题。
- 不依赖真实 SSH 服务器执行破坏性命令。
- 使用 Mock 工具客户端验证工具选择和拦截。

## 5. 建议处理顺序

建议按以下顺序逐项完成：

1. T2：拆分或可过滤内置执行能力。
2. T3：非 Local Tab 默认移除本机执行工具。
3. T4：Planner/Worker 注入当前连接上下文。
4. T5：运行时作用域拦截。
5. T6：Agent Planner 工具目录按作用域分组。
6. T10：补齐核心回归测试。
7. T1：正式引入工具作用域模型。
8. T7：逐步统一工具命名。
9. T8：增加用户设置。
10. T9：完善审计信息。

如果只想先快速降低误操作概率，可以优先处理 T2、T3、T4、T5。

如果需要彻底解决工具混淆，应完整处理 T1、T6、T7。

## 6. 完成定义

满足以下条件时，可以认为本轮工具作用域改造完成：

- 用户在 SSH Tab 中要求安装 Docker，默认只在 SSH 服务器执行。
- 非 Local Tab 不会未经明确授权执行本机命令。
- Local Tab 的本机能力保持可用。
- EasyShell 内部只读工具仍可用于应用诊断和配置查询。
- Planner、Worker、运行时拦截和审计使用同一套作用域定义。
- 工具调用结果能够明确显示实际执行目标。
- 相关回归测试全部通过。
