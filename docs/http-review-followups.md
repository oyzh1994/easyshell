# HTTP 模块审查待办

> 审查日期：2026-11-09
> 说明：本文件只记录尚未处理的问题，已修复项不再重复。

## P1：资源生命周期

- [x] **二进制响应临时文件未清理**
  - 已改为写入项目缓存目录 `ShellConst.getCachePath()`，通过 `ShellFileUtil.getTempFile()` 生成路径。
  - 应用退出时由 `EasyShellApp.stop()` 统一清理缓存目录。
  - HTML 预览文件也改为使用同一缓存目录。
  - 涉及：`src/main/java/cn/oyzh/easyshell/http/ShellHttpClient.java`、`src/main/java/cn/oyzh/easyshell/http/ShellHttpUtil.java`
  - 验证：集成测试确认二进制响应临时文件位于项目缓存目录。

- [x] **大响应缺少大小上限**
  - 已新增全局设置 `Maximum response size`，默认 50 MB，单位 MB。
  - 客户端改为流式读取，超过限制时停止读取，不进入正文/Base64/HEX/临时文件流程。
  - 响应只保留状态码、响应头、Cookie、大小和耗时；正文区域显示超限提示。
  - gzip/deflate 解压后也会再次受限制，避免解压后膨胀绕过限制。
  - 涉及：`ShellHttpSetting.java`、`ShellHttpSettingsTabController.java`、`ShellHttpClient.java`、`ShellHttpUtil.java`、`ShellHttpResponse.java`
  - 验证：Mock Server 新增 `/api/large`；集成测试覆盖 1 MB 限制下返回 2 MB 响应。

## P1：协议能力

- [x] **Brotli（Content-Encoding: br）未支持**
  - 已引入 `org.brotli:dec` 纯 Java 解码器。
  - `ShellHttpUtil.decompress` 支持 `Content-Encoding: br`，并继续遵守最大响应大小限制。
  - Mock Server 新增 `/brotli` 接口。
  - 涉及：`pom.xml`、`ShellHttpUtil.java`、`ShellHttpMockServer.java`
  - 验证：集成测试请求 `/brotli`，确认正文正确解压。

- [x] **常见认证类型补充**
  - 已新增 Digest 认证：支持 MD5、SHA-256、`-sess` 算法和 `qop=auth`。
  - Digest 通过 401 质询后自动计算 Authorization 并重试。
  - 已新增 OAuth 2.0 Client Credentials：Token URL、Client ID、Client Secret、Scope，自动获取并缓存 token。
  - 暂不支持 OAuth 2.0 Authorization Code/Refresh Token。
  - NTLM、AWS Signature、Hawk、OAuth 1.0 仍未实现，需要额外协议/签名实现，暂缓。
  - 涉及：`ShellHttpRequest.java`、`ShellHttpDigest.java`、`ShellHttpClient.java`、请求 FXML、Mock Server。
  - 验证：Mock Server `/auth/digest`、`/oauth/token`、`/auth/oauth2` 均有集成测试。

- [x] **HTTP/2 只有配置入口，缺少集成验证**
  - 已新增基于 TLS + ALPN 的 HTTP/2 集成测试。
  - 使用 MockWebServer3 + okhttp-tls 测试证书，验证请求级 HTTP/2 覆盖全局 HTTP/1.1 设置。
  - 涉及：`pom.xml`、`src/test/java/cn/oyzh/easyshell/http/ShellHttp2IntegrationTest.java`
  - 验证：服务端记录到的协议版本为 `HTTP/2`，响应状态码和正文正确。

## P2：请求脚本

- [x] **Nashorn 脚本缺少沙箱（不处理）**
  - 结论：EasyShell 是本地客户端，脚本属于本地可信能力，不引入沙箱限制。
  - 保留现有 Nashorn 直接执行方式。

- [x] **Postman 脚本 API 覆盖不完整（已补充常用子集）**
  - 已补充 `pm.request.url.query`、`addQueryParams`、`pm.request.body.raw`、`urlencoded/formdata`。
  - 已补充请求头/响应头 `has`、`toObject`，响应 Cookie `get/has/toObject`。
  - 已补充 `pm.response.responseSize`、`contentType`。
  - 已补充 `pm.response.to.be.ok/success/clientError/serverError/error/redirect`。
  - 已扩充 `pm.expect`：`equal/eql/a/an/include/contain/property/lengthOf/above/below/match/not/true/false/null/ok`。
  - 仍未覆盖：`pm.sendRequest`、`pm.collectionVariables`、`pm.globals`、完整 Postman Sandbox。
  - 涉及：`src/main/java/cn/oyzh/easyshell/http/script/ShellHttpScriptApi.java`
  - 验证：脚本引擎测试覆盖新增 API 和断言链。

## P2：UI 统一

- [x] **Cookie 表头 i18n**
  - Domain、Expires、Path、HttpOnly、Secure、SameSite 均已改为 i18n 资源。
  - 涉及：`ShellHttpCookieTableView.java`、`ShellI18nHelper.java`、`i18n_*.properties`

- [ ] **完整 UI 手工回归**
  - 自动化测试只覆盖数据、FXML 加载和客户端行为，未覆盖真实交互。
  - 建议逐项验证：
    - [ ] URL 与参数双向联动、描述保留
    - [ ] 请求头/参数/form-data/x-www-form-urlencoded 描述列宽度一致
    - [ ] 请求设置最后 Tab、选项单行、默认值来自全局
    - [ ] 响应 Body/Base64/HEX/Cookies/Headers/Test Results
    - [ ] 二进制响应的「打开文件」按钮
    - [ ] 测试脚本、测试结果、Pre-request Script
    - [ ] 克隆请求后自动打开新 Tab
    - [ ] 集合/集合分类数量显示

## P2：客户端缓存

- [x] **HttpClient 缓存没有上限**
  - 缓存上限设为 8。
  - 使用引用计数避免淘汰正在请求中的客户端；超过上限时只清理空闲客户端。
  - 使用访问顺序 `LinkedHashMap`，优先淘汰最久未使用的空闲客户端。
  - 涉及：`src/main/java/cn/oyzh/easyshell/http/ShellHttpClient.java`
  - 验证：集成测试连续使用 16 个不同超时设置，缓存数量保持在 8 以内。

## P3：暂缓功能

- [ ] **请求历史**
  - 当前决定暂不实现。
  - 如后续实现，建议方案：
    - 保存请求快照 JSON
    - 保存响应元数据
    - 响应正文默认不保存；用户主动保存时才落盘
    - 历史列表只做恢复/重新发送，不默认持久化响应正文
  - 相关评估：快照方案可行，但历史列表、恢复 Tab、缓存生命周期仍需独立设计。

## 已修复记录

以下问题在本次审查中已处理，保留记录便于回溯：

- [x] form-data 已有行未绑定变更监听
- [x] 参数描述修改不触发未保存状态
- [x] URL 变化丢失参数描述
- [x] 请求级设置并发时共用/关闭 HttpClient
- [x] 测试脚本异常结果被响应刷新覆盖
- [x] Base64 大响应在 FX 线程同步处理
