# SQLite 查询提示弹窗 UI 像素对齐测试报告

## 1. 测试结论

- 测试日期：2026-11-04。
- 测试对象：SQLite、MySQL、达梦查询提示弹窗的列表内容。
- 测试环境：JDK 27.0.1、JavaFX 27.0.1、macOS aarch64。
- 测试结果：三套实现使用同一组 7 类提示项渲染，SQLite 与 MySQL、达梦逐像素比较均为 0 差异。
- 结论：当前 SQLite 提示弹窗的列表结构、图标、宽度、扩展信息、颜色和弹窗尺寸已与 MySQL、达梦对齐。

自动生成的截图和测试数据：

- `target/ui-test-report/sqlite-query-prompt/mysql.png`
- `target/ui-test-report/sqlite-query-prompt/dameng.png`
- `target/ui-test-report/sqlite-query-prompt/sqlite.png`
- `target/ui-test-report/sqlite-query-prompt/sqlite-vs-mysql-diff.png`
- `target/ui-test-report/sqlite-query-prompt/sqlite-vs-dameng-diff.png`
- `target/ui-test-report/sqlite-query-prompt/report.md`

## 2. 测试方法

新增 `ShellSqliteQueryPromptUiParityTest`，在 JavaFX Application Thread 中分别创建 SQLite、MySQL、达梦提示列表，并注入相同的 7 类提示项：

1. 数据库：`main`。
2. 关键字：`SELECT`。
3. 表：`prompt_item`，扩展信息 `main`。
4. 字段：`name`，扩展信息 `"main"."prompt_item"`。
5. 视图：`prompt_view`，扩展信息 `main`。
6. 函数：`length`。
7. 过程：`refresh_cache`。

结构断言覆盖：

- 实际弹窗子类均装配对应列表实现。
- 列表尺寸固定为 `360 x 240`。
- 数据库、关键字、表、字段、视图、函数、过程分别使用对应 SVG 图标。
- 表、字段、函数、过程标签宽度为 `240`；视图、数据库、关键字不设置固定 `240` 宽度。
- 表和视图显示扩展信息 `main`，字段显示 `"main"."prompt_item"`，关键字不显示扩展信息。
- 扩展文案颜色为 `#D3D3D3`。
- 提示文本和 Tooltip 与完整对象名一致。

像素断言覆盖：

- 将列表放入同尺寸、同背景、同主题的 JavaFX Scene。
- 强制 CSS 与布局后对完整 Scene 截图。
- 逐像素比较 ARGB 值，并输出差异像素数、最大通道差和差异边界。

## 3. 像素对比结果

| 对比 | 差异像素 | 总像素 | 最大通道差 | 差异边界 | 结果 |
| --- | ---: | ---: | ---: | --- | --- |
| MySQL vs Dameng | 0 | 120000 | 0 | none | PASS |
| SQLite vs MySQL | 0 | 120000 | 0 | none | PASS |
| SQLite vs Dameng | 0 | 120000 | 0 | none | PASS |

测试连续执行两次，结果一致。

## 4. 执行命令

```bash
mvn -q -Dtest=ShellSqliteQueryPromptUiParityTest test
mvn -q -Dtest='SQLiteDesignAndDataTest#testSqliteQueryPromptAnalysis+testQueryIndexSkipsInvalidView+testDesignBrokenView+testRenameTableWithBrokenView' test
```

## 5. 修复与防回归方案

当前 SQLite 提示列表已经通过像素级对比，不需要继续调整列表视觉参数。后续按以下方案防止回退：

1. 类型分类统一：SQLite 的 database/table/column/keyword/view/function/procedure 类型判断必须与 MySQL、达梦保持一一对应，禁止再用“表或视图”合并类型。
2. 渲染统一：新增提示类型时，必须同时在 SQLite 与 MySQL、达梦列表中使用相同 SVG 图标、宽度和扩展标签规则。
3. 数据统一：表和视图扩展信息使用库名，字段扩展信息使用 `库名.表名` 并调用对应方言的标识符包裹逻辑。
4. 候选统一：Token 分析和候选生成按当前 SQL 上下文过滤，避免关键字、表、视图、字段全部混入。
5. 回归门禁：任何提示弹窗 UI 改动都必须运行 `ShellSqliteQueryPromptUiParityTest`；若出现像素差异，差异图会输出到 `target/ui-test-report/sqlite-query-prompt`。

## 6. 环境注意事项

- 该测试需要 JavaFX Toolkit。无桌面 CI 环境应使用 Xvfb，或补充 Monocle/TestFX headless 配置。
- 测试使用同一次运行中的 MySQL、达梦截图作为参照，不依赖仓库内预存的金标准截图，避免不同操作系统字体和主题导致的固定基线漂移。
- 本次比对使用默认主题和 JavaFX 字体渲染；跨操作系统发布前建议在目标系统 CI 上再执行一次相同测试。
