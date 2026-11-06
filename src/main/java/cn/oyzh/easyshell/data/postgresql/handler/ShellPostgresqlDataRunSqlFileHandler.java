package cn.oyzh.easyshell.data.postgresql.handler;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlUtil;
import cn.oyzh.fx.db.data.handler.DBDataRunFileHandler;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Postgresql执行SQL文件处理器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlDataRunSqlFileHandler extends DBDataRunFileHandler<String> {

    /**
     * 数据库客户端
     */
    private final ShellPostgresqlClient dbClient;

    /**
     * 转储文件头中标识的源模式
     */
    private String sourceSchema;

    /**
     * 构造 Postgresql执行SQL文件处理器
     *
     * @param dbClient 数据库客户端
     * @param dbName   数据库名称
     */
    public ShellPostgresqlDataRunSqlFileHandler(ShellPostgresqlClient dbClient, String dbName) {
        super(dbName);
        this.dbClient = dbClient;
    }

    @Override
    public void runFile() throws Exception {
        this.message("Run Sql File Starting");
        // 文件读取
        try (BufferedReader reader = FileUtil.getReader(this.file, StandardCharsets.UTF_8)) {
            // 未处理完的sql
            StringBuilder builder = new StringBuilder();
            // 执行
            while (reader.ready()) {
                try {
                    // 检查中断
                    this.checkInterrupt();
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    String stripped = line.strip();
                    // 识别 EasyShell 转储文件头中的源模式，执行时自动转换为当前模式
                    if (this.sourceSchema == null && stripped.startsWith("Source Schema")) {
                        int index = stripped.indexOf(':');
                        if (index >= 0) {
                            String value = stripped.substring(index + 1).trim();
                            if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                            }
                            this.sourceSchema = value;
                        }
                    }
                    // 语句之间的注释和空行直接忽略
                    if (builder.toString().isBlank()) {
                        if (stripped.isEmpty() || stripped.startsWith("--")) {
                            continue;
                        }
                    }
                    if (!builder.isEmpty()) {
                        builder.append("\n");
                    }
                    builder.append(line);
                    // 按PostgreSQL语句结束符拆分，支持$$引用函数体、字符串、注释等
                    int end;
                    while ((end = this.indexOfStatementEnd(builder)) >= 0) {
                        String sql = builder.substring(0, end).strip();
                        builder.delete(0, end + 1);
                        if (sql.isEmpty()) {
                            continue;
                        }
                        if (!this.executeStatement(sql)) {
                            // 不继续执行后续语句
                            this.message("Run Sql File Finished");
                            return;
                        }
                    }
                } catch (InterruptedException ex) {
                    throw ex;
                } catch (Exception ex) {
                    this.exception(ex);
                    this.processedDecr();
                    if (!this.continueWithErrors) {
                        break;
                    }
                    StringUtil.clear(builder);
                    this.getInsertList().clear();
                }
            }
            // 执行剩余的sql
            String rest = builder.toString().strip();
            if (!rest.isEmpty() && this.hasExecutableSql(rest)) {
                this.executeStatement(rest);
            }
            // 收尾批量插入
            this.doBatchInsert();
        } finally {
            this.message("Run Sql File Finished");
        }
    }

    /**
     * 执行一条sql
     *
     * @param sql sql语句
     * @return 是否继续执行
     * @throws Exception 异常
     */
    private boolean executeStatement(String sql) throws Exception {
        try {
            // 转储文件的源模式与当前模式不一致时，自动重写模式限定
            if (StringUtil.isNotBlank(this.sourceSchema)
                    && !StringUtil.equalsIgnoreCase(this.sourceSchema, this.dbName)) {
                sql = ShellPostgresqlUtil.replaceSchema(sql, this.sourceSchema, this.dbName);
                // pg_get_serial_sequence 的表名是字符串字面量，需单独替换其中的模式限定
                if (sql.contains("pg_get_serial_sequence(")) {
                    sql = sql.replace("\"" + this.sourceSchema + "\".", "\"" + this.dbName + "\".");
                }
            }
            // 新增记录用批量处理
            if (StringUtil.startWithIgnoreCase(sql, "INSERT INTO ")) {
                this.addInsert(sql);
                return true;
            }
            // 执行其他语句前先落盘批量插入，保证语句顺序
            this.doBatchInsert();
            this.dbClient.executeSqlSimple(this.dbName, sql);
            this.processedIncr();
            return true;
        } catch (InterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            this.exception(ex);
            this.processedDecr();
            this.getInsertList().clear();
            return this.continueWithErrors;
        }
    }

    /**
     * 是否包含可执行sql（排除纯注释内容）
     *
     * @param sql sql内容
     * @return 结果
     */
    private boolean hasExecutableSql(String sql) {
        for (String line : sql.lines().toList()) {
            String stripped = line.strip();
            if (!stripped.isEmpty() && !stripped.startsWith("--")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找语句结束符位置
     *
     * <p>扫描时忽略字符串、双引号标识符、行注释、块注释以及PostgreSQL的$$（或$tag$）引用内容。</p>
     *
     * @param sql sql内容
     * @return 结束符位置，未结束返回-1
     */
    private int indexOfStatementEnd(CharSequence sql) {
        int len = sql.length();
        State state = State.NORMAL;
        String dollarTag = null;
        for (int i = 0; i < len; ) {
            char c = sql.charAt(i);
            switch (state) {
                case NORMAL -> {
                    if (c == '-' && i + 1 < len && sql.charAt(i + 1) == '-') {
                        state = State.LINE_COMMENT;
                        i += 2;
                    } else if (c == '/' && i + 1 < len && sql.charAt(i + 1) == '*') {
                        state = State.BLOCK_COMMENT;
                        i += 2;
                    } else if (c == '\'') {
                        state = State.SINGLE_QUOTE;
                        i++;
                    } else if (c == '"') {
                        state = State.DOUBLE_QUOTE;
                        i++;
                    } else if (c == '$') {
                        dollarTag = this.readDollarTag(sql, i);
                        if (dollarTag == null) {
                            i++;
                        } else {
                            state = State.DOLLAR_QUOTE;
                            i += dollarTag.length();
                        }
                    } else if (c == ';') {
                        return i;
                    } else {
                        i++;
                    }
                }
                case SINGLE_QUOTE -> {
                    if (c == '\'') {
                        if (i + 1 < len && sql.charAt(i + 1) == '\'') {
                            i += 2;
                        } else {
                            state = State.NORMAL;
                            i++;
                        }
                    } else {
                        i++;
                    }
                }
                case DOUBLE_QUOTE -> {
                    if (c == '"') {
                        if (i + 1 < len && sql.charAt(i + 1) == '"') {
                            i += 2;
                        } else {
                            state = State.NORMAL;
                            i++;
                        }
                    } else {
                        i++;
                    }
                }
                case DOLLAR_QUOTE -> {
                    if (this.startsWith(sql, dollarTag, i)) {
                        i += dollarTag.length();
                        dollarTag = null;
                        state = State.NORMAL;
                    } else {
                        i++;
                    }
                }
                case LINE_COMMENT -> {
                    if (c == '\n') {
                        state = State.NORMAL;
                    }
                    i++;
                }
                case BLOCK_COMMENT -> {
                    if (c == '*' && i + 1 < len && sql.charAt(i + 1) == '/') {
                        state = State.NORMAL;
                        i += 2;
                    } else {
                        i++;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 读取$$引用标签
     *
     * @param sql   sql内容
     * @param index 起始位置
     * @return 标签，非$$引用返回null
     */
    private String readDollarTag(CharSequence sql, int index) {
        int next = -1;
        for (int i = index + 1; i < sql.length(); i++) {
            if (sql.charAt(i) == '$') {
                next = i;
                break;
            }
        }
        if (next < 0) {
            return null;
        }
        String tag = sql.subSequence(index, next + 1).toString();
        if (tag.length() == 2) {
            return tag;
        }
        String name = tag.substring(1, tag.length() - 1);
        return name.matches("[A-Za-z_][A-Za-z0-9_]*") ? tag : null;
    }

    /**
     * 判断字符串指定位置是否以目标内容开头
     *
     * @param sql    sql内容
     * @param target 目标内容
     * @param index  起始位置
     * @return 结果
     */
    private boolean startsWith(CharSequence sql, String target, int index) {
        if (target == null || index + target.length() > sql.length()) {
            return false;
        }
        for (int i = 0; i < target.length(); i++) {
            if (sql.charAt(index + i) != target.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void doBatchInsert(List<String> list, boolean parallel) throws Exception {
        try {
            int result = this.dbClient.insertBatch(this.dbName, list, parallel);
            this.processedIncr(result);
        } catch (Exception ex) {
            this.processedDecr(list.size());
            throw ex;
        }
    }

    @Override
    public boolean enableParallel() {
        return false;
    }

    /**
     * 扫描状态
     */
    private enum State {
        NORMAL,
        SINGLE_QUOTE,
        DOUBLE_QUOTE,
        DOLLAR_QUOTE,
        LINE_COMMENT,
        BLOCK_COMMENT
    }
}
