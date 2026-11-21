# LLM 模块完整性审查与待办

> 状态：主功能基本完整，进入逐项收尾阶段
> 审查日期：2026-11-21
> 使用方式：每次只处理一个条目，完成后更新状态并补充验证记录。

## 1. 总体结论

当前 LLM 模块已经具备日常使用所需的主要能力：

- OpenAI 兼容、Anthropic、Gemini、Ollama 供应商及模型列表管理。
- 流式/非流式聊天、思考等级、思考显示、输出 Token、消息窗口、超时和连接重试。
- Prompt 管理、会话历史、附件、消息折叠和手动滚动。
- 单/多 Agent，以及串行主从、并行、DAG、动态主从编排。
- Agent 并发、最大步骤、失败策略和请求取消。
- 工具作用域、工具命名去重、MCP Tools/Resources、Skill 和脚本模式。
- 工具审计、敏感参数脱敏和 Agent Trace。
- 完整 LLM 回归：`205` 项通过，`0` 失败，`2` 项外部环境测试跳过。

当前还不建议直接定义为“生产级功能完整”。剩余问题主要集中在上下文管理、
模型能力识别、审计凭据安全和少量通用聊天能力。

## 2. 待办清单

### [x] P0-1. 将执行模式和作用域变为运行时硬约束

现状：

- 执行模式通过系统提示词描述，没有在实际工具执行前强制执行。
- “建议模式”下执行类工具仍然暴露给模型，只能依赖模型遵循提示。
- 默认 SSH 场景为 `SESSION_WITH_LOCAL`，仍暴露本机执行工具。
- 运行时工具作用域拦截 T5 尚未实现。

证据：

- `src/main/java/cn/oyzh/easyshell/llm/LLMUtil.java`
- `src/main/java/cn/oyzh/easyshell/llm/tool/LLMToolAccessMode.java`
- `docs/llm-agent-tool-scope-improvement-tasks.md` 中 T5

目标：

- 在工具真正执行前检查执行模式、工具作用域和当前绑定 Session。
- 建议模式禁止执行或改变状态的工具实际执行。
- 非 Local Session 在用户未明确要求本机操作时，拒绝本地命令工具。
- 拒绝结果作为可纠正的工具结果返回给模型，并记录到审计日志。
- 高风险或不可逆操作可按配置要求用户确认。

验收：

- SSH 会话中模型误调用本地命令时，不产生任何本地进程。
- 拒绝结果包含当前目标、拒绝原因和建议的正确工具。
- 审计日志记录 `blockedByScope=true` 和拒绝原因。
- 不依赖提示词是否被模型正确遵循。

完成记录：

- 新增 `LLMToolRuntimePolicy`，统一执行模式、工具访问模式、绑定 Session、
  当前用户任务和工具作用域。
- 新增 `LLMToolRegistry`，普通工具、MCP 工具和 Skill 工具统一经过包装后的
  `ToolProvider` 与 `ToolExecutor` 注册。
- `SUGGEST` 模式不再向模型暴露任何工具；如果模型仍调用已知工具名，会返回
  `[EASYSHELL_POLICY_BLOCKED]` 错误结果，而不是执行工具。
- 非 Local Session 默认隐藏 `LOCAL_EXECUTION` 工具。只有当前用户任务明确出现
  “本机”、“本地机器”、“local machine”等本地意图时才会暴露。
- Runtime Executor 会再次校验工具作用域，确保即使工具被错误注册也不会执行。
- 隐藏工具被模型误调用时由工具名保护策略返回可纠正结果，并写入
  `blockedByScope=true` 和 `blockReason`。
- Agent Planner 的可用分组和 Worker 工具同步使用同一策略；Local Worker 在无明确
  本地意图时不会出现在计划中。
- `SUGGEST`、`SESSION_ONLY`、非 Local Session 本地工具隔离、真实本地工具解析、
  Local Session、MCP/Skill 独立作用域、AiServices 静态工具注册、Runtime Executor
  拒绝和审计脱敏均补充测试。

### [ ] P1-1. 接入真实 Token Usage 和上下文占用

现状：

- `LLMView.setContextUsage(...)` 只有界面实现和演示代码调用，生产请求链路没有调用方。
- 当前界面的上下文占用指标不会随真实对话更新。
- 请求和响应中的 Token Usage 没有形成会话统计。

证据：

- `src/main/java/cn/oyzh/easyshell/fx/llm/LLMView.java`
- `src/main/java/cn/oyzh/easyshell/controller/llm/ChatController.java`

目标：

- 读取各供应商响应中的输入、输出和总 Token。
- 更新当前上下文占用、累计会话 Token 和最近一次请求耗时。
- 对不支持 Usage 的供应商提供明确未知状态，不伪造数据。
- 为后续 token-aware 截断提供统一数据模型。

验收：

- 一次请求完成后，上下文占用和累计 Token 正确更新。
- 切换会话后显示该会话自己的统计。
- 不支持 Token Usage 的模型显示未知或估算状态。

### [ ] P1-2. 将消息窗口升级为 Token-aware 上下文管理

现状：

- `MessageWindowChatMemory` 按消息条数截断，默认 20 条。
- 长文本、附件、工具调用和工具结果可能让单条消息占用大量 Token。
- 尚未实现按 Token 预算裁剪、摘要压缩或工具结果归档。

证据：

- `src/main/java/cn/oyzh/easyshell/controller/llm/ChatController.java`
- `src/main/java/cn/oyzh/easyshell/llm/LLMParameterUtil.java`

目标：

- 同时支持最大消息数和最大上下文 Token 预算。
- 优先保留 System、最新用户消息和最近必要工具结果。
- 超限时按策略裁剪旧消息或生成可追踪摘要。
- 无法精确计算 Token 时明确使用估算策略。

验收：

- 连续长对话不会因为固定条数策略突然超出模型上下文。
- 裁剪后对话仍保留当前任务、关键结论和必要工具结果。
- 上下文超限有可理解的日志和界面提示。

### [ ] P1-3. 增加模型能力元数据与请求前校验

现状：

- 供应商配置主要保存 API 类型、地址、密钥、模型列表和默认模型。
- 没有模型上下文长度、图片、音频、PDF、工具调用、思考等能力元数据。
- 附件、工具和思考参数是否支持只能在请求失败后由供应商报错。

证据：

- `src/main/java/cn/oyzh/easyshell/llm/provider/LLMProvider.java`
- `src/main/java/cn/oyzh/easyshell/llm/LLMFileContentUtil.java`
- `src/main/java/cn/oyzh/easyshell/llm/LLMUtil.java`

目标：

- 建立可扩展的模型能力描述。
- 支持内置能力和用户覆盖配置。
- 发送前校验附件类型、工具调用、思考等级和上下文预算。
- 根据能力关闭无效 UI 项或给出明确提示。

验收：

- 不支持的附件类型不会发送到供应商后才失败。
- 不支持工具调用的模型不会注册工具并产生不可解释错误。
- 思考等级会按模型能力转换或禁用。

### [ ] P2-1. 完善审计结果脱敏与凭据存储

现状：

- 工具参数已按敏感字段脱敏。
- 工具返回结果只截断，没有统一脱敏，可能记录命令输出中的密码或 Token。
- 供应商 API Key 当前保存在普通数据库字段中。

证据：

- `src/main/java/cn/oyzh/easyshell/llm/tool/LLMToolAudit.java`
- `src/main/java/cn/oyzh/easyshell/llm/provider/LLMProvider.java`

目标：

- 对工具结果执行 JSON 和纯文本敏感信息脱敏。
- 评估使用系统钥匙串或本机安全存储保存 API Key。
- 若暂不迁移密钥，至少明确风险并避免在日志、导出和错误信息中暴露。

验收：

- 包含密码、Token、Authorization、Cookie 的工具结果写入审计后已脱敏。
- API Key 不出现在普通日志和会话导出中。

### [ ] P2-2. 补充通用聊天操作

现状：

- 已具备发送、停止、会话恢复、重命名和删除。
- 尚未提供重新生成、编辑上一条用户消息后重发、从指定消息分叉会话、会话导出和消息搜索。

目标：

- 优先实现重新生成和编辑后重发。
- 再评估消息分叉、会话导出和搜索。

验收：

- 重新生成不会重复追加原用户消息。
- 编辑历史消息后可创建清晰的新分支或明确覆盖旧回答。
- 原会话数据不会因重新生成或编辑被无意破坏。

### [x] P2-3. 优化 MCP 连接生命周期

现状：

- 每次 LLM 请求重新连接启用的 MCP Server 并重新获取工具列表。
- 功能正确，但多 Server 场景下存在首包延迟和重复连接开销。

优点：

- 当前隔离方式简单，单个 Server 故障不会污染全局状态。
- 修改配置后下一次请求立即生效。

可选目标：

- 增加短期连接复用或工具列表缓存。
- 配置变化、网络断开或认证失效时可靠失效缓存。
- 保持单个 Server 故障隔离语义不变。

验收：

- 缓存不会继续使用已禁用或配置已变更的 Server。
- 单 Server 故障不会阻塞其他 Server。
- 重连和缓存失效行为有日志和测试。

完成记录：

- 新增 `LLMMcpConnectionManager`，按 Server 连接键缓存短期 MCP 连接和工具列表。
- 工具与 Resources 能力缓存有效期为 2 分钟。
- 复用缓存时按 15 秒间隔执行 `checkHealth()`，健康检查失败会重建连接。
- Server 配置通过 SHA-256 指纹校验；名称、传输方式、命令、环境变量、URL、Header、
  工具白名单和超时任一变化都会淘汰旧连接。
- 禁用或删除 Server 后，下一次请求会清理对应缓存。
- 连接使用租约引用计数：配置变化时旧请求可以继续使用旧连接，最后一个租约释放后再关闭。
- ChatController 销毁时关闭全部 MCP 连接缓存。
- `LLMMcpClientIntegrationTest` 新增连接复用、配置变化重建、Server 禁用淘汰缓存测试。

## 3. 建议处理顺序

1. `P0-1`：运行时安全边界。
2. `P1-1`：Token Usage 和上下文占用。
3. `P1-2`：Token-aware 上下文管理。
4. `P1-3`：模型能力元数据。
5. `P2-1`：审计和凭据安全。
6. `P2-2`：通用聊天操作。
7. `P2-3`：MCP 连接生命周期优化。

## 4. 完成定义

当以下条件全部满足时，可以将 LLM 模块标记为“功能完整”：

- 执行模式和工具作用域不能仅靠模型自觉遵守。
- 上下文占用和 Token 使用来自真实请求，不使用伪造数据。
- 长会话有明确的 token-aware 控制策略。
- 模型能力在请求前可知，不支持的输入不会静默发送。
- 工具审计不会记录明文敏感结果。
- 关键聊天操作、MCP、Skill 和 Agent 能力有自动化回归覆盖。
