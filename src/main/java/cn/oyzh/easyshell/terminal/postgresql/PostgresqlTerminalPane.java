package cn.oyzh.easyshell.terminal.postgresql;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.query.postgresql.PostgresqlExecuteResult;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.query.postgresql.ShellPostgresqlQueryUtil;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.terminal.ShellTerminalHistoryHandler;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.terminal.TerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.fx.terminal.util.TerminalManager;
import cn.oyzh.fx.terminal.util.TerminalUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.text.Font;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PostgreSQL终端文本域
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class PostgresqlTerminalPane extends TerminalPane {

    @Override
    public Font getEditorFont() {
        ShellSetting setting = ShellSettingStore.SETTING;
        return FontManager.toFont(setting.terminalFontConfig());
    }

    /**
     * PostgreSQL客户端
     */
    private ShellPostgresqlClient client;

    /**
     * 获取PostgreSQL客户端
     *
     * @return PostgreSQL客户端
     */
    public ShellPostgresqlClient getClient() {
        return client;
    }

    /**
     * 获取关联的连接信息
     *
     * @return 连接信息，客户端为空时返回 null
     */
    public ShellConnect shellConnect() {
        return this.client == null ? null : this.client.getShellConnect();
    }

    // /**
    //  * 客户端连接状态监听器
    //  */
    // private ChangeListener<DBConnState> stateChangeListener;

    @Override
    public void flushPrompt() {
        String str = this.schema;
        str += "@" + this.shellConnect().getName();
        if (this.isConnecting()) {
            str += ShellI18nHelper.terminalConnectingPrompt();
        } else if (this.isConnected()) {
            str += ShellI18nHelper.terminalConnectedPrompt();
        } else {
            str += "> ";
        }
        this.prompt(str);
    }

    /**
     * 终端名称
     */
    public static final String TERMINAL_NAME = "postgresql";

    static {
        TerminalManager.setLoadHandler(TERMINAL_NAME, PostgresqlTerminalManager::registerHandlers);
    }

    @Override
    public String terminalName() {
        return TERMINAL_NAME;
    }

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String getSchema() {
        return schema;
    }

    /**
     * 设置模式名称并刷新提示符
     *
     * @param schema 模式名称
     */
    public void setSchema(String schema) {
        this.schema = schema;
        this.flushPrompt();
    }

    /**
     * 初始化
     *
     * @param client 客户端
     * @param schema 模式名称
     */
    public void init(ShellPostgresqlClient client, String schema) {
        this.client = client;
        this.setSchema(schema);
        FXUtil.runPulse(() -> {
            this.disableInput();
            this.outputLine(ShellI18nHelper.welcome());
            this.outputLine("Powered By oyzh(2026-2026).");
            this.flushPrompt();
            if (this.isTemporary()) {
                this.initByTemporary();
            } else {
                this.initByPermanent();
            }
        });
    }


    /**
     * 是否临时连接
     *
     * @return 结果
     */
    public boolean isTemporary() {
        return this.client == null || this.client.iid() == null;
    }

    @Override
    public void outputPrompt() {
        if (!this.isConnecting()) {
            super.outputPrompt();
        }
    }

    /**
     * 是否已连接
     *
     * @return 结果
     */
    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    /**
     * 是否连接中
     *
     * @return 结果
     */
    public boolean isConnecting() {
        return this.client != null && this.client.isConnecting();
    }

    /**
     * 是否已关闭
     *
     * @return 结果
     */
    public boolean isClosed() {
        return this.client != null && this.client.isClosed();
    }

    /**
     * 临时连接处理
     */
    private void initByTemporary() {
        this.outputLine("Please enter connection info or SQL.");
        this.appendByPrompt("");
        this.enableInput();
        this.flushAndMoveCaretEnd();
    }

    /**
     * 常驻连接处理
     */
    private void initByPermanent() {
        this.applySearchPath();
        this.flushPrompt();
        this.appendByPrompt("");
        this.enableInput();
        this.flushAndMoveCaretEnd();
    }

    /**
     * 应用当前模式的 search_path，确保终端中未限定模式的 SQL 使用提示符中的模式
     */
    private void applySearchPath() {
        if (this.schema == null || this.schema.isBlank() || this.client == null || !this.client.isConnected()) {
            return;
        }
        Statement statement = null;
        try {
            Connection connection = this.client.getConnManager().connection();
            statement = connection.createStatement();
            statement.execute("SET search_path TO " + DBUtil.wrap(this.schema, DBDialect.POSTGRESQL));
        } catch (Exception ex) {
            JulLog.warn("set terminal search_path error, schema: {}", this.schema, ex);
        } finally {
            IOUtil.close(statement);
        }
    }

    // /**
    //  * 刷新光标并移动到尾部
    //  */
    // private void flushAndMoveCaretEnd() {
    //     ExecutorUtil.start(() -> {
    //         this.flushCaret();
    //         this.moveCaretEnd();
    //     }, 50);
    // }

    // /**
    //  * 初始化连接状态监听器
    //  */
    // private void initStatListener() {
    //     if (this.stateChangeListener == null) {
    //         this.stateChangeListener = (observableValue, state, t1) -> {
    //             this.flushPrompt();
    //             String host = this.dbConnect != null ? this.dbConnect.getHost() : "";
    //             if (t1 == DBConnState.CONNECTED) {
    //                 this.outputLine(host + " " + I18nHelper.connectSuccess() + ".");
    //                 this.outputPrompt();
    //                 this.flushCaret();
    //                 super.enableInput();
    //             } else if (t1 == DBConnState.CLOSED) {
    //                 this.outputLine(host + " " + I18nHelper.connectionClosed() + ".");
    //                 this.enableInput();
    //             } else if (t1 == DBConnState.CONNECTING) {
    //                 this.outputLine(host + " " + I18nHelper.connectIng() + "...", false);
    //             } else if (t1 == DBConnState.FAILED) {
    //                 this.outputLine(host + " " + I18nHelper.connectFail() + ".");
    //                 this.enableInput();
    //             }
    //             JulLog.info("connState={}", t1);
    //         };
    //         this.getClient().addStateListener(this.stateChangeListener);
    //     }
    // }

    @Override
    public void enableInput() {
        if (this.isConnecting()) {
            return;
        }
        if (this.isConnected() || (!this.isConnected() && this.isTemporary())) {
            super.enableInput();
        }
    }

    @Override
    public void fontSizeIncr() {
        super.fontSizeIncr();
        this.saveFontSize();
    }

    @Override
    public void fontSizeDecr() {
        super.fontSizeDecr();
        this.saveFontSize();
    }

    /**
     * 保存字体大小
     */
    private void saveFontSize() {
        ShellSetting setting = ShellSettingStore.SETTING;
        setting.setTerminalFontSize((byte) this.getFontSize());
        ShellSettingStore.INSTANCE.replace(setting);
    }

    // @Override
    // public void destroy() {
    //     if (this.client != null && this.stateChangeListener != null) {
    //         this.client.stateProperty().removeListener(this.stateChangeListener);
    //     }
    //     this.stateChangeListener = null;
    //     super.destroy();
    // }

    /**
     * 是否展开显示（psql的\x命令）
     */
    private boolean expanded;

    /**
     * 是否输出耗时（psql的\timing命令）
     */
    private boolean timing = true;

    /**
     * 执行输入的终端命令
     *
     * @param input 输入内容
     * @return 执行结果
     */
    public TerminalExecuteResult eval(String input) {
        // psql风格元命令
        if (input != null && input.stripLeading().startsWith("\\")) {
            return this.evalMetaCommand(input.trim());
        }
        TerminalExecuteResult terminalResult = new TerminalExecuteResult();
        try {
            if (this.schema == null) {
                terminalResult.setResult("No schema selected. Use '\\c <schema>' or 'use <schema>' to select one.");
                return terminalResult;
            }
            DBQueryResults<PostgresqlExecuteResult> results = this.client.executeSql(this.schema, input);
            if (!results.isSuccess()) {
                terminalResult.setException(new RuntimeException(results.getErrMsg()));
            } else if (results.isEmpty()) {
                terminalResult.setResult("OK");
            } else {
                StringBuilder sb = new StringBuilder();
                for (PostgresqlExecuteResult result : results.getResults()) {
                    if (result.isSuccess()) {
                        if (result.getUpdateCount() > 0) {
                            sb.append("Query OK, ").append(result.getUpdateCount()).append(" rows affected");
                            long ms = result.getUsedMs();
                            if (this.timing && ms > 0) {
                                sb.append(" (").append(ms).append(" ms)");
                            }
                            sb.append(this.lineEndingText());
                        } else if (result.getRecords() != null && !result.getRecords().isEmpty()) {
                            sb.append(this.formatResultSet(result));
                        } else {
                            sb.append("OK").append(this.lineEndingText());
                        }
                    } else {
                        sb.append("ERROR: ").append(result.getMsg()).append(this.lineEndingText());
                    }
                }
                terminalResult.setResult(sb.toString().trim());
            }
        } catch (Exception ex) {
            terminalResult.setException(ex);
        }
        return terminalResult;
    }

    /**
     * 构造消息结果
     *
     * @param message 消息内容
     * @return 执行结果
     */
    private TerminalExecuteResult message(String message) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        result.setResult(message);
        return result;
    }

    /**
     * 执行psql风格元命令
     *
     * @param input 输入内容
     * @return 执行结果
     */
    private TerminalExecuteResult evalMetaCommand(String input) {
        // 去掉结尾分号
        if (input.endsWith(";")) {
            input = input.substring(0, input.length() - 1).trim();
        }
        String[] args = TerminalUtil.split(input);
        String command = args[0];
        // 命令参数，保留空格
        String arg = args.length > 1 ? input.substring(command.length()).trim() : null;
        switch (command) {
            case "\\l":
            case "\\list":
                return this.execCommand("show databases;");
            case "\\dn":
                return this.execCommand("show dbs;");
            case "\\dt":
                if (arg == null) {
                    return this.execCommand("show tables;");
                }
                return this.evalMetaQuery("SELECT schemaname AS \"Schema\", tablename AS \"Name\", tableowner AS \"Owner\""
                        + " FROM pg_tables WHERE schemaname = ANY (current_schemas(false))"
                        + " AND tablename LIKE " + this.likeLiteral(arg)
                        + " ORDER BY schemaname, tablename");
            case "\\d":
                if (arg == null) {
                    return this.evalMetaQuery("SELECT n.nspname AS \"Schema\", c.relname AS \"Name\","
                            + " CASE c.relkind WHEN 'r' THEN 'table' WHEN 'v' THEN 'view' WHEN 'm' THEN 'materialized view'"
                            + " WHEN 'S' THEN 'sequence' WHEN 'f' THEN 'foreign table' ELSE c.relkind::text END AS \"Type\""
                            + " FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace"
                            + " WHERE n.nspname = ANY (current_schemas(false))"
                            + " AND c.relkind IN ('r', 'p', 'v', 'm', 'S', 'f') ORDER BY n.nspname, c.relname");
                }
                return this.evalDescribe(arg);
            case "\\df":
                return this.evalMetaQuery("SELECT n.nspname AS \"Schema\", p.proname AS \"Name\","
                        + " pg_get_function_arguments(p.oid) AS \"Arguments\","
                        + " pg_get_function_result(p.oid) AS \"Result\""
                        + " FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace"
                        + " WHERE n.nspname = ANY (current_schemas(false))"
                        + (arg == null ? "" : " AND p.proname LIKE " + this.likeLiteral(arg))
                        + " ORDER BY n.nspname, p.proname");
            case "\\dv":
                return this.evalMetaQuery("SELECT schemaname AS \"Schema\", viewname AS \"Name\", viewowner AS \"Owner\""
                        + " FROM pg_views WHERE schemaname = ANY (current_schemas(false))"
                        + (arg == null ? "" : " AND viewname LIKE " + this.likeLiteral(arg))
                        + " ORDER BY schemaname, viewname");
            case "\\ds":
                return this.evalMetaQuery("SELECT schemaname AS \"Schema\", sequencename AS \"Name\", data_type AS \"Type\""
                        + " FROM pg_sequences WHERE schemaname = ANY (current_schemas(false))"
                        + (arg == null ? "" : " AND sequencename LIKE " + this.likeLiteral(arg))
                        + " ORDER BY schemaname, sequencename");
            case "\\dp":
                return this.evalMetaQuery("SELECT n.nspname AS \"Schema\", c.relname AS \"Name\","
                        + " pg_get_userbyid(c.relowner) AS \"Owner\","
                        + " array_to_string(c.relacl, E'\\n') AS \"Access privileges\""
                        + " FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace"
                        + " WHERE n.nspname = ANY (current_schemas(false))"
                        + " AND c.relkind IN ('r', 'p', 'v', 'm', 'S', 'f')"
                        + (arg == null ? "" : " AND c.relname LIKE " + this.likeLiteral(arg))
                        + " ORDER BY n.nspname, c.relname");
            case "\\c":
            case "\\connect":
                if (arg == null) {
                    return this.message("You are connected to schema \"" + this.schema + "\".");
                }
                return this.execCommand("use " + this.unquote(arg) + ";");
            case "\\x":
                this.expanded = !this.expanded;
                return this.message("Expanded display is " + (this.expanded ? "on." : "off."));
            case "\\timing":
                this.timing = !this.timing;
                return this.message("Timing is " + (this.timing ? "on." : "off."));
            case "\\?":
            case "\\h":
                return this.message(this.metaHelp());
            default:
                return this.message("Invalid command " + command + ". Try \\? for help.");
        }
    }

    /**
     * 执行已注册的终端命令
     *
     * @param line 命令行
     * @return 执行结果
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private TerminalExecuteResult execCommand(String line) {
        TerminalCommandHandler handler = TerminalManager.findHandler(PostgresqlTerminalPane.TERMINAL_NAME, line);
        if (handler == null) {
            return this.message("Command not found: " + line);
        }
        try {
            TerminalCommand command = handler.parseCommand(line);
            return handler.execute(command, this);
        } catch (Exception ex) {
            return TerminalExecuteResult.fail(ex);
        }
    }

    /**
     * 执行元命令查询
     *
     * @param sql sql语句
     * @return 执行结果
     */
    private TerminalExecuteResult evalMetaQuery(String sql) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            DBQueryResults<PostgresqlExecuteResult> results = this.client.executeSql(this.schema, sql);
            if (!results.isSuccess()) {
                result.setException(new RuntimeException(results.getErrMsg()));
                return result;
            }
            StringBuilder builder = new StringBuilder();
            for (PostgresqlExecuteResult executeResult : results.getResults()) {
                if (executeResult.isSuccess()) {
                    if (executeResult.getRecords() != null && !executeResult.getRecords().isEmpty()) {
                        if (!builder.isEmpty()) {
                            builder.append(this.lineEndingText());
                        }
                        builder.append(this.formatResultSet(executeResult));
                    }
                } else {
                    builder.append("ERROR: ").append(executeResult.getMsg()).append(this.lineEndingText());
                }
            }
            result.setResult(builder.toString().trim());
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }

    /**
     * 描述表字段，等价于psql的\d <table>
     *
     * @param arg 表名称，支持schema.table形式
     * @return 执行结果
     */
    private TerminalExecuteResult evalDescribe(String arg) {
        String schema = null;
        String table = arg;
        int index = arg.indexOf('.');
        if (index > 0) {
            schema = this.unquote(arg.substring(0, index));
            table = arg.substring(index + 1);
        }
        table = this.unquote(table);
        String sql = "SELECT a.attname AS \"Column\","
                + " pg_catalog.format_type(a.atttypid, a.atttypmod) AS \"Type\","
                + " CASE WHEN a.attnotnull THEN 'not null' ELSE '' END AS \"Nullable\","
                + " pg_get_expr(d.adbin, d.adrelid) AS \"Default\""
                + " FROM pg_attribute a"
                + " JOIN pg_class c ON c.oid = a.attrelid"
                + " JOIN pg_namespace n ON n.oid = c.relnamespace"
                + " LEFT JOIN pg_attrdef d ON d.adrelid = a.attrelid AND d.adnum = a.attnum"
                + " WHERE a.attnum > 0 AND NOT a.attisdropped"
                + " AND c.relname = " + this.sqlLiteral(table)
                + (schema == null ? " AND n.nspname = ANY (current_schemas(false))" : " AND n.nspname = " + this.sqlLiteral(schema))
                + " ORDER BY a.attnum";
        return this.evalMetaQuery(sql);
    }

    /**
     * psql元命令帮助
     *
     * @return 帮助内容
     */
    private String metaHelp() {
        String nl = this.lineEndingText();
        return "PostgreSQL terminal commands:" + nl
                + "  \\l              list databases" + nl
                + "  \\c [schema]     change current schema (search_path)" + nl
                + "  \\dn             list schemas" + nl
                + "  \\dt [pattern]   list tables" + nl
                + "  \\d [table]      describe table columns" + nl
                + "  \\df [pattern]   list functions" + nl
                + "  \\dv [pattern]   list views" + nl
                + "  \\ds [pattern]   list sequences" + nl
                + "  \\dp [pattern]   list privileges" + nl
                + "  \\x              toggle expanded output" + nl
                + "  \\timing         toggle timing" + nl
                + "  \\?              show this help" + nl
                + "  help            show command help" + nl
                + "  clear [-his]    clear terminal" + nl
                + "Patterns support * and ? wildcards; SQL statements end with ';'.";
    }

    /**
     * 去掉标识符两端的双引号
     *
     * @param value 值
     * @return 结果
     */
    private String unquote(String value) {
        if (value != null && value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * 转换为sql字符串字面量
     *
     * @param value 值
     * @return 结果
     */
    private String sqlLiteral(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    /**
     * 转换为sql模糊匹配字面量
     *
     * @param value 值
     * @return 结果
     */
    private String likeLiteral(String value) {
        value = this.unquote(value).replace('*', '%').replace('?', '_');
        return this.sqlLiteral(value);
    }

    /**
     * 格式化结果集
     *
     * @param result 结果集
     * @return 结果
     */
    private String formatResultSet(PostgresqlExecuteResult result) {
        // 展开显示
        if (this.expanded) {
            return this.formatExpandedResultSet(result);
        }
        StringBuilder sb = new StringBuilder();
        PostgresqlColumns columns = result.getColumns();
        List<PostgresqlRecord> records = result.getRecords();
        if (columns == null || records == null) {
            return "";
        }

        int colCount = columns.size();
        int[] colWidths = new int[colCount];
        for (int i = 0; i < colCount; i++) {
            colWidths[i] = Math.max(colWidths[i], columns.get(i).getName().length());
        }
        for (PostgresqlRecord record : records) {
            for (int i = 0; i < colCount; i++) {
                Object val = record.getValue(columns.get(i).getName());
                String str = val == null ? "NULL" : val.toString();
                colWidths[i] = Math.max(colWidths[i], str.length());
            }
        }

        // header
        for (int i = 0; i < colCount; i++) {
            sb.append(String.format("%-" + (colWidths[i] + 2) + "s", columns.get(i).getName()));
        }
        sb.append(this.lineEndingText());

        // separator
        for (int i = 0; i < colCount; i++) {
            sb.append("-".repeat(colWidths[i]));
            sb.append("  ");
        }
        sb.append(this.lineEndingText());

        // data rows
        for (PostgresqlRecord record : records) {
            for (int i = 0; i < colCount; i++) {
                Object val = record.getValue(columns.get(i).getName());
                String str = val == null ? "NULL" : val.toString();
                sb.append(String.format("%-" + (colWidths[i] + 2) + "s", str));
            }
            sb.append(this.lineEndingText());
        }

        sb.append("#").append(records.size()).append(" row(s) in set");

        long ms = result.getUsedMs();
        if (this.timing && ms > 0) {
            sb.append(" (").append(ms).append(" ms)");
        }
        return sb.toString();
    }

    /**
     * 格式化结果集，展开显示（psql的\x模式）
     *
     * @param result 结果集
     * @return 结果
     */
    private String formatExpandedResultSet(PostgresqlExecuteResult result) {
        PostgresqlColumns columns = result.getColumns();
        List<PostgresqlRecord> records = result.getRecords();
        if (columns == null || records == null) {
            return "";
        }
        int nameWidth = 0;
        for (int i = 0; i < columns.size(); i++) {
            nameWidth = Math.max(nameWidth, columns.get(i).getName().length());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < records.size(); i++) {
            PostgresqlRecord record = records.get(i);
            sb.append("-[ RECORD ").append(i + 1).append(" ]").append("-".repeat(20));
            sb.append(this.lineEndingText());
            for (int j = 0; j < columns.size(); j++) {
                String name = columns.get(j).getName();
                Object val = record.getValue(name);
                String str = val == null ? "NULL" : val.toString();
                sb.append(String.format("%-" + nameWidth + "s | %s", name, str));
                sb.append(this.lineEndingText());
            }
        }
        sb.append("(").append(records.size()).append(" row(s))");

        long ms = result.getUsedMs();
        if (this.timing && ms > 0) {
            sb.append(" (").append(ms).append(" ms)");
        }
        return sb.toString();
    }

    @Override
    protected TerminalCommandHandler findHandler(String input) {
        TerminalCommandHandler<?, ?> handler = TerminalManager.findHandler(PostgresqlTerminalPane.TERMINAL_NAME, input);
        if (handler == null) {
            handler = new PostgresqlTerminalCommandHandler<>() {

                @Override
                public String commandName() {
                    return "";
                }

                @Override
                public TerminalExecuteResult execute(TerminalCommand command, PostgresqlTerminalPane terminal) {
                    String input = command.getContent();
                    return terminal.eval(input);
                }
            };
        }
        return handler;
    }

    @Override
    public void initNode() {
        this.keyHandler(PostgresqlTerminalKeyHandler.INSTANCE);
        this.helpHandler(PostgresqlTerminalHelpHandler.INSTANCE);
        this.mouseHandler(PostgresqlTerminalMouseHandler.INSTANCE);
        this.historyHandler(ShellTerminalHistoryHandler.INSTANCE);
        this.completeHandler(PostgresqlTerminalCompleteHandler.INSTANCE);
        super.initNode();
    }

    /**
     * 是否已初始化提示词
     */
    private boolean initPrompts;

    @Override
    public Set<String> getPrompts() {
        if (!this.initPrompts) {
            this.initPrompts = true;
            Set<String> prompts = super.getPrompts();
            if (prompts == null) {
                prompts = new HashSet<>();
            } else {
                prompts = new HashSet<>(prompts);
            }
            // 设置内容提示符
            prompts.addAll(ShellPostgresqlQueryUtil.getDbKeywords());
            this.setPrompts(prompts);
        }
        return super.getPrompts();
    }

}
