# LLM Agent 工具作用域与优先级改进任务

> 状态：T1-T4、T6-T10 已完成；T5 按当前需求暂不处理
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

### [x] T4. 注入当前连接上下文

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

完成记录：

- 新增 `LLMSessionContext`，保存当前绑定 Session 的 id、名称、类型、主机、端口、用户和默认目标。
- Session 元数据来自当前 `LLMTabCapability` 关联的真实 `ShellConnect`，不包含密码、私钥、令牌或 Cookie。
- 普通助手系统提示会包含同一份绑定 Session 上下文。
- Planner 任务提示会包含绑定 Session 上下文和默认目标。
- connection、internal、local、mcp Worker 均会收到同一份绑定 Session 上下文。
- 最终汇总 Agent 同样会收到该上下文，避免最终答案偏离当前目标。
- Prompt 明确要求“当前服务器、这个连接、这个 Tab”等表达按绑定上下文解析，禁止通过时区、语言、DNS、公网 IP 或历史对话推断目标。

### [ ] T5. 增加运行时调用拦截

> 暂不处理：当前工具选择路径已不会发生该场景。

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

### [x] T6. 调整 Agent Planner 的工具目录

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
- EasyShell 自身诊断在 `INTERNAL` 可用时使用 `INTERNAL`。
- 本机命令使用独立的 `LOCAL_EXECUTION` 分组。
- MCP 使用独立分组，仅在已启用且连接成功时出现。
- 不同作用域工具不可互相替代。

验收标准：

- “安装 Docker”不会规划成本机内部任务。
- “查看 EasyShell 版本”不会选择 SSH 命令。
- “查看当前 Redis 版本”选择 Redis 服务器工具。

完成记录：

- `LLMAgentType` 新增 `LOCAL`，Planner 支持 `connection|internal|local|mcp`。
- `toolCatalog` 按实际作用域分别输出工具，不再把 LOCAL_EXECUTION 混入 connection 或 internal。
- `connection` Worker 只收到当前 SESSION 工具。
- `internal` Worker 只收到 EasyShell INTERNAL 工具。
- `local` Worker 只收到 LOCAL_EXECUTION 工具。
- MCP 仅在实际有已连接客户端时加入 Planner 分组。
- Planner 系统提示增加强制规则：不得用一个作用域替代另一个作用域，服务器操作默认使用当前 SESSION。
- 过期的 Planner 分组会按当前实际可用作用域纠正。

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

### [x] T8. 增加工具策略设置

在 LLM 设置中增加：

```text
工具作用域：
  (o) 会话 + 本机
  ( ) 仅当前会话
  ( ) 完整工具
```

要求：

- 默认选择“会话 + 本机”。
- “完整工具”只表示当前 SESSION + LOCAL_EXECUTION + INTERNAL。
- “完整工具”绝不包含其他 Session Tab，也不支持跨 Session 操作。
- 不使用“全部工具”这个名称。
- MCP 不属于普通工具作用域，继续独立按启用状态注入。
- 设置项持久化。
- 切换后新请求立即生效。
- 旧会话恢复时行为一致。

验收标准：

- “会话 + 本机”只暴露当前 SESSION + LOCAL_EXECUTION。
- “仅当前会话”不暴露 LOCAL_EXECUTION 或 INTERNAL。
- “完整工具”只增加当前请求的 INTERNAL，不读取其他 Session。
- 任意模式下都不会把 Redis 等其他 Tab 的工具注入当前 SSH Session。

完成记录：

- 新增 `LLMToolAccessMode`，包含 `SESSION_WITH_LOCAL`、`SESSION_ONLY`、`COMPLETE`。
- `ShellSetting` 持久化 `llmToolAccessMode`。
- LLM 设置菜单新增“工具范围”。
- `LLMToolSelection` 根据模式组合工具和提示词。
- “完整工具”最多只组合当前 Session、LOCAL_EXECUTION、INTERNAL 三类普通工具。
- MCP 仍由启用状态独立注入，不参与普通工具模式组合。

### [x] T9. 补充审计信息

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

完成记录：

- 审计记录增加 `toolScope`、`sessionTabType`、`connectionHost`、`connectionUser`。
- 审计记录增加普通工具 `toolAccessMode`。
- 审计记录增加 `blockedByScope=false`。
- 新增 `recordBlocked`，用于记录执行前被作用域策略拒绝的调用，标记 `blockedByScope=true` 和 `blockReason`。
- MCP 工具来源继续记录为独立 `MCP` 作用域。
- 会话元数据只包含类型、主机和用户，不包含密码、密钥、令牌或 Cookie。

### [x] T10. 增加回归测试

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

完成记录：

- 工具作用域、Local Tab、无 Session、三类访问模式均使用 `FakeTools` 验证，不依赖真实 SSH。
- Planner 与 Worker 使用同一绑定 Session 上下文，测试覆盖主机、用户和默认目标，并确认提示中不含密码字段。
- Planner 工具目录分别覆盖 `connection`、`internal`、`local`、`mcp`，过期分组会纠正到实际可用作用域。
- 工具名称全局唯一性、运行时重复名称拒绝、MCP 清洗后名称碰撞消歧和原始重复名称拒绝均已覆盖。
- MCP 集成测试覆盖 Resources 能力探测、Tools-only Server、混合 Server 的作用域隔离、单 Server 故障隔离和工具执行。
- LLM 参数测试覆盖选中模型真正进入 Chat/Streaming 模型、最大输出 Token、各供应商思考参数映射、非法枚举值和设置持久化字段。
- Agent 测试覆盖流式思考/结果、内联轨迹、取消、工具作用域隔离、绑定上下文和可配置 Worker 消息窗口。
- Session 测试覆盖异步保存使用的不可变快照。
- 第 7、8 项依赖 T5 的运行时拦截；按用户要求 T5 暂不处理，因此本轮不实现、不伪造这两项测试，待 T5 恢复时补齐。
- `mvn -Dtest='*LLM*Test,I18nResourceIntegrityTest#easyShellLocalesHaveSameKeysAndNoEmptyValues+placeholdersAreConsistentAcrossLocales+onlySupportedLocalesRemainInRuntimeResources' test`
  通过 168 项，0 失败，2 项外部环境测试跳过。

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

## 7. LLM 模块集中审查收尾记录

本轮在 T10 基础上完成了跨模块参数和生命周期收口：

- 聊天框选中的模型此前只保存在界面状态，实际模型构造仍使用供应商默认模型；现已贯穿 Chat、Streaming、Agent 和工具审计。
- 拉取模型列表此前只更新候选项，保存逻辑却读取单模型文本，导致数据库仍只有默认模型；现模型列表保存到 `modelsText`，当前选择单独保存到已有的 `defaultModel` 字段，文本框只显示默认模型，下拉列表保留完整模型集合；默认模型为空或不在列表中时回退到列表第一项。
- 最大输出 Token 此前固定为 8192；现增加设置、持久化、菜单入口、边界归一化和请求快照。
- 请求超时、消息窗口和最大输出 Token 在发送时一起形成请求快照，避免请求执行期间修改设置导致同一请求参数不一致。
- 思考强度按供应商转换为合法参数；Anthropic 思考预算始终小于输出 Token 上限，无法容纳思考预算时不会发送非法配置。
- 新增独立于思考强度的“思考过程显示”设置。关闭时模型仍按原思考强度运行并继续生成/保存思考数据，仅在 UI 隐藏普通消息思考与 Agent 轨迹中的思考段，执行目标、耗时、结果和错误继续显示。
- 新增单 Agent / 多 Agent 模式设置，默认多 Agent。单 Agent 跳过 Planner、分步 Worker 编排和最终汇总 Agent，直接使用当前请求选择的普通工具与独立 MCP 工具执行，降低简单任务的模型调用和 Token 消耗；多 Agent 保持原有规划、作用域隔离和汇总验证流程。
- 新增独立的 Agent 编排配置：默认“串行主从”，并提供并行、DAG、动态主从。并行模式按最大并发数调度独立 Worker；DAG 模式按步骤依赖调度；动态主从模式在每一步完成后重新询问 Supervisor 下一步。同一工具作用域最多保留一个运行中的 Worker，避免共享工具实例并发调用。
- Agent 最大并发支持固定值 `2/4/8/12/20`、`0`（物理核心，默认）和 `-1`（无上限，实际上限为当前计划的步骤数）。物理核心数按操作系统探测，无法探测时回退到 JVM 可用处理器数。
- Agent 最大步骤数与并发数独立配置，默认 10，支持 `6/10/15/20/30`。Planner、并行、DAG 和动态主从统一使用该上限，因此提高并发档位后可以规划超过 6 个 Agent 步骤。
- Agent 失败策略支持继续执行（默认）、快速失败、失败重试一次。DAG 中依赖失败步骤的下游步骤会标记为跳过；并发轨迹按开始时间稳定排序，并使用线程安全的 Trace 集合。
- LLM 模型调用增加连接异常专用重试：普通聊天和 Agent 的同步/流式调用在检测到连接重置、连接中止、断管、异常 EOF、`NoHttpResponseException` 等连接提前断开时，最多自动重试 3 次，采用 250ms、500ms、1000ms 退避；同时识别 Winsock 10052/10053/10054、中英文本地化错误消息及常见 HTTP/2 `GOAWAY`/`REFUSED_STREAM` 变体。认证失败、超时、主动取消、TLS 配置错误等业务或配置异常不重试。通用异常识别由 `base-common` 的 `NetworkUtil` 统一提供。
- Agent Worker 不再硬编码 30 条消息，改为使用 LLM 消息窗口设置。
- 多组工具提示词合并时去除重复的语言和执行规则，避免 SESSION、LOCAL_EXECUTION、INTERNAL 组合导致提示词重复膨胀。
- MCP 工具名按 Server 预先生成映射，清洗碰撞时稳定加哈希消歧；原始重复名称和跨 Server 最终名称冲突会在注册前拒绝。
- 单个 MCP Server 的配置/名称异常会作为该 Server 的错误隔离，不阻止其他已连接 Server 继续工作。
- MCP Server 指令明确标记为不可覆盖 EasyShell 作用域、执行模式和安全规则的配置数据。
- 会话异步保存改为保存不可变快照并通过版本和锁避免旧快照覆盖新快照；保存前切回 FX 线程，避免后台读取 JavaFX 属性。
- 请求执行期间禁止切换会话或新建/打开其他会话，避免旧请求结果保存到新会话。
- 请求资源改为在请求完成且处理线程退出后关闭，兼容流式回调和取消竞态。
