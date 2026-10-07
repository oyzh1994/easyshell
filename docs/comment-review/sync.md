# easyshell 云同步（sync 包）代码审查文档

> 说明：仅新增文档，未改动任何 `.java`。
> 范围：easyshell/src/main/java/cn/oyzh/easyshell/sync/，共 8 个 .java，全部存活。

本包实现基于代码片段（gist）的配置数据云同步，支持 Gitee 与 GitHub 两种后端。加密数据统一由 `ShellSyncManager` 负责编码/解码与落库，`ShellGistSyncer` 负责与远端交互，`ShellGistOperator` 负责 HTTP 细节。

---

## ShellGistOperator

- 职责：gist 操作的抽象基类，封装访问令牌与 HTTP 客户端，定义远端片段增删改查的抽象方法。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| accessToken | `String` | 访问令牌 |
| httpClient | `CloseableHttpClient` | http客户端 |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellGistOperator(String accessToken)` | 构造gist操作器 | 保存 `accessToken`，`HttpClients.createDefault()` 创建 `httpClient` |
| `listGists()` | 抽象：获取所有代码片段列表 | 由子类实现 |
| `getGist(String gistId)` | 抽象：获取特定代码片段详情 | 由子类实现 |
| `createGist(String description, Map<String,String> files, boolean isPublic)` | 抽象：创建代码片段 | 由子类实现，返回片段id |
| `updateGist(String gistId, String description, Map<String,String> files)` | 抽象：更新代码片段 | 由子类实现 |
| `deleteGist(String gistId)` | 抽象：删除代码片段 | 由子类实现 |
| `getFileContent(String gistId)` | 获取代码片段的特定文件 | `getGist(gistId).getJSONObject("files")` |
| `gistExists(String gistId)` | 检查代码片段是否存在 | `getGist(gistId)` 后判断 `!object.isEmpty()`，异常返回 false |
| `close()` | 关闭资源 | 关闭并置空 `httpClient`，`accessToken` 置 null |

- 调用链：`getFileContent → getGist`；`gistExists → getGist`

## ShellGistSyncer

- 职责：gist 同步器抽象基类，实现 `ShellSyncer`，处理同步/清除的业务编排（创建、更新、合并、清除）。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| setting | `ShellSetting` | 设置，取自 `ShellSettingStore.SETTING` |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `sync(String snippetName)` | 同步入口 | 取操作器 → 定位片段id（为空则遍历 `listGists` 按 `description` 匹配）→ 无id则 `doCreate`；否则 `getFileContent` 读取 `data`/`syncTime`，为空或时间相同则 `doUpdate`，否则 `decodeSyncData` + `saveSyncData` 合并后再 `doUpdate`，最后 `ShellEventUtil.dataImported()`；`finally` 中 `IOUtil.close(operator)` |
| `clear(String snippetName)` | 清除入口 | 取操作器 → 定位片段id，为空则 `JulLog.warn("syncId is empty")` 并返回；否则 `doClear` |
| `doCreate(ShellGistOperator operator, String snippetName)` | 执行新增 | `getSyncData` → `encodeSyncData` → 组装 `data`/`syncTime` → `createGist(...,false)` → 回写 `setting` 的 syncId/syncTime 并 `ShellSettingStore.INSTANCE.replace` |
| `doUpdate(ShellGistOperator operator, String snippetId, String snippetName)` | 执行更新 | `getSyncData` → `encodeSyncData` → `updateGist` → 回写 `setting` 并 replace |
| `doClear(ShellGistOperator operator, String snippetId, String snippetName)` | 执行清除 | `updateGist` 写入 `data=null`、`syncTime=null`，`setting.setSyncTime(null)` 并 replace |
| `getOperator(String accessToken)` | 抽象：获取操作器 | 由子类实现 |

- 调用链：`sync → getOperator → listGists/getFileContent/doCreate/doUpdate`；`doUpdate → ShellSyncManager.encodeSyncData → getSyncData`；`sync(合并分支) → ShellSyncManager.decodeSyncData → saveSyncData → ShellEventUtil.dataImported`

## ShellGitHubGistOperator

- 职责：GitHub gist 操作器，通过 GitHub REST API 实现片段的增删改查。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| GITHUB_API_BASE | `String` | github gist接口地址，`https://api.github.com/gists`（static final） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellGitHubGistOperator(String accessToken)` | 构造github gist操作器 | 调用 `super(accessToken)` |
| `listGists()` | 获取所有代码片段列表 | `GET /gists?page=1&per_page=100`，`setAuthHeader`，解析 `JSONArray` 转 `List<JSONObject>` |
| `getGist(String gistId)` | 获取片段详情 | `GET /gists/{id}`，`setAuthHeader`，`JSON.parseObject` |
| `createGist(String description, Map<String,String> files, boolean isPublic)` | 创建片段 | `POST /gists`，请求体含 `description`/`public`/`files`，返回 `id` |
| `updateGist(String gistId, String description, Map<String,String> files)` | 更新片段 | `PATCH /gists/{id}`，状态码 `200` 为成功 |
| `deleteGist(String gistId)` | 删除片段 | `DELETE /gists/{id}`，状态码 `204` 为成功 |
| `setAuthHeader(HttpUriRequest request)` | 设置认证头 | `Authorization: token {accessToken}`（非空时）+ `User-Agent: Java-Gist-Client` |

- 调用链：`listGists/getGist/createGist/updateGist/deleteGist → setAuthHeader → httpClient.execute`

## ShellGiteeGistOperator

- 职责：Gitee gist 操作器，通过 Gitee OpenAPI v5 实现片段的增删改查。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| GITEE_API_BASE | `String` | gitee gist接口地址，`https://gitee.com/api/v5/gists`（static final） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `ShellGiteeGistOperator(String accessToken)` | 构造gitee gist操作器 | 调用 `super(accessToken)` |
| `listGists()` | 获取所有代码片段列表 | `GET {base}?access_token=...&page=1&per_page=100`，解析 `JSONArray` 转 `List<JSONObject>` |
| `getGist(String gistId)` | 获取片段详情 | `GET {base}/{id}?access_token=...`，`JSON.parseObject` |
| `createGist(String description, Map<String,String> files, boolean isPublic)` | 创建片段 | `POST {base}?access_token=...`，请求体含 `description`/`public`/`files`，返回 `id` |
| `updateGist(String gistId, String description, Map<String,String> files)` | 更新片段 | `PATCH {base}/{id}?access_token=...`，状态码 `200` 为成功 |
| `deleteGist(String gistId)` | 删除片段 | `DELETE {base}/{id}?access_token=...`，状态码 `204` 为成功 |

- 调用链：`listGists/getGist/createGist/updateGist/deleteGist → httpClient.execute`

## ShellGiteeSyncer

- 职责：Gitee 专用同步器，为 `ShellGistSyncer` 提供 Gitee 操作器实例。

- 字段：无字段

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getOperator(String accessToken)` | 获取操作器 | `new ShellGiteeGistOperator(accessToken)` |

- 调用链：`ShellGistSyncer.sync/clear → getOperator → ShellGiteeGistOperator`

## ShellGithubSyncer

- 职责：GitHub 专用同步器，为 `ShellGistSyncer` 提供 GitHub 操作器实例。

- 字段：无字段

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getOperator(String accessToken)` | 获取操作器 | `new ShellGitHubGistOperator(this.setting.getSyncToken())`（忽略入参，改取 setting 中的令牌） |

- 调用链：`ShellGistSyncer.sync/clear → getOperator → ShellGitHubGistOperator`

## ShellSyncManager

- 职责：同步管理器，负责选择同步器、编排同步/清除、配置数据的 AES 加解密以及从存储加载/写回同步数据。

- 字段：

| 字段 | 类型 | 含义 |
|---|---|---|
| AES_SECRET | `String` | 加密密钥，`"easy_shell_sync_aes_secret"`（static final） |
| SNIPPET_NAME | `String` | 片段名称，`"EasyShell_Config_Data"`（static final） |

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `getSyncer()` | 获取同步器 | 读取 `ShellSettingStore.SETTING`，`isGiteeType()` → `ShellGiteeSyncer`，`isGithubType()` → `ShellGithubSyncer`，否则 null |
| `doSync()` | 执行更新 | `syncer.sync(SNIPPET_NAME)` |
| `clearSync()` | 执行清除 | `syncer.clear(SNIPPET_NAME)` |
| `encodeSyncData(ShellDataExport export)` | 加密数据 | `JSONUtil.toJson` → `AESUtil.encrypt(json, AES_SECRET)` |
| `decodeSyncData(String data)` | 解密数据 | `AESUtil.decrypt` → `JSONUtil.toBean(json, ShellDataExport.class)` |
| `getSyncData(boolean key, boolean group, boolean snippet, boolean connect)` | 获取同步数据 | 按开关从 `ShellKeyStore`/`ShellGroupStore`/`ShellSnippetStore`/`ShellConnectStore` 装配 `ShellDataExport` |
| `saveSyncData(ShellDataExport data, boolean key, boolean group, boolean snippet, boolean connect)` | 保存同步数据 | 按开关对 keys/groups/snippets 逐条 `replace`，对 connects 逐条 `sync` |

- 调用链：`doSync → getSyncer → ShellGiteeSyncer/ShellGithubSyncer.sync`；`encodeSyncData/decodeSyncData → ShellGistSyncer.doCreate/doUpdate/merge`

## ShellSyncer

- 职责：同步器接口，定义同步与清除两项能力。

- 字段：无字段

- 方法：

| 方法 | 说明 | 关键逻辑/调用 |
|---|---|---|
| `sync(String snippetName)` | 同步 | 由实现类 `ShellGistSyncer` 实现 |
| `clear(String snippetName)` | 清除 | 由实现类 `ShellGistSyncer` 实现 |

- 调用链：`ShellSyncManager.doSync/clearSync → ShellSyncer.sync/clear → ShellGistSyncer → getOperator`
