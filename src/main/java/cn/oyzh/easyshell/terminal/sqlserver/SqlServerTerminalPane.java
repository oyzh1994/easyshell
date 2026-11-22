package cn.oyzh.easyshell.terminal.sqlserver;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.easyshell.query.sqlserver.SqlServerExecuteResult;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.query.sqlserver.ShellSqlServerQueryUtil;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.terminal.ShellTerminalHistoryHandler;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.db.query.DBQueryResults;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SqlServer终端文本域
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class SqlServerTerminalPane extends TerminalPane {

    @Override
    public Font getEditorFont() {
        ShellSetting setting = ShellSettingStore.SETTING;
        return FontManager.toFont(setting.terminalFontConfig());
    }

    /**
     * SqlServer客户端
     */
    private ShellSqlServerClient client;

    /**
     * 获取SqlServer客户端
     *
     * @return SqlServer客户端
     */
    public ShellSqlServerClient getClient() {
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
    public static final String TERMINAL_NAME = "sqlserver";

    static {
        TerminalManager.setLoadHandler(TERMINAL_NAME, SqlServerTerminalManager::registerHandlers);
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
    public void init(ShellSqlServerClient client, String schema) {
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
        this.flushPrompt();
        this.appendByPrompt("");
        this.enableInput();
        this.flushAndMoveCaretEnd();
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
            DBQueryResults<SqlServerExecuteResult> results = this.client.executeSql(this.schema, input);
            if (!results.isSuccess()) {
                terminalResult.setException(new RuntimeException(results.getErrMsg()));
            } else if (results.isEmpty()) {
                terminalResult.setResult("OK");
            } else {
                StringBuilder sb = new StringBuilder();
                for (SqlServerExecuteResult result : results.getResults()) {
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
                return this.evalMetaQuery("SELECT s.name AS [Schema], t.name AS [Name],"
                        + " USER_NAME(t.principal_id) AS [Owner]"
                        + " FROM sys.tables t JOIN sys.schemas s ON s.schema_id = t.schema_id"
                        + " WHERE s.name = " + this.sqlLiteral(this.schema)
                        + " AND t.name LIKE " + this.likeLiteral(arg)
                        + " ORDER BY s.name, t.name");
            case "\\d":
                if (arg == null) {
                    return this.evalMetaQuery("SELECT s.name AS [Schema], o.name AS [Name],"
                            + " CASE o.type WHEN 'U' THEN 'table' WHEN 'V' THEN 'view'"
                            + " WHEN 'P' THEN 'procedure' WHEN 'FN' THEN 'function'"
                            + " WHEN 'IF' THEN 'inline function' WHEN 'TF' THEN 'table function'"
                            + " WHEN 'SO' THEN 'sequence' ELSE o.type_desc END AS [Type]"
                            + " FROM sys.objects o JOIN sys.schemas s ON s.schema_id = o.schema_id"
                            + " WHERE s.name = " + this.sqlLiteral(this.schema)
                            + " AND o.type IN ('U','V','P','FN','IF','TF','SO')"
                            + " ORDER BY s.name, o.name");
                }
                return this.evalDescribe(arg);
            case "\\df":
                return this.evalMetaQuery("SELECT s.name AS [Schema], o.name AS [Name],"
                        + " STUFF((SELECT ', ' + TYPE_NAME(p.user_type_id)"
                        + " + CASE WHEN p.name IS NULL THEN '' ELSE ' ' + p.name END"
                        + " FROM sys.parameters p WHERE p.object_id = o.object_id AND p.parameter_id > 0"
                        + " ORDER BY p.parameter_id FOR XML PATH(''), TYPE).value('.', 'nvarchar(max)'), 1, 2, '') AS [Arguments],"
                        + " TYPE_NAME((SELECT TOP 1 p.user_type_id FROM sys.parameters p"
                        + " WHERE p.object_id = o.object_id AND p.parameter_id = 0)) AS [Result]"
                        + " FROM sys.objects o JOIN sys.schemas s ON s.schema_id = o.schema_id"
                        + " WHERE s.name = " + this.sqlLiteral(this.schema)
                        + " AND o.type IN ('FN','IF','TF','FS','FT','AF')"
                        + (arg == null ? "" : " AND o.name LIKE " + this.likeLiteral(arg))
                        + " ORDER BY s.name, o.name");
            case "\\dv":
                return this.evalMetaQuery("SELECT s.name AS [Schema], v.name AS [Name],"
                        + " USER_NAME(v.principal_id) AS [Owner]"
                        + " FROM sys.views v JOIN sys.schemas s ON s.schema_id = v.schema_id"
                        + " WHERE s.name = " + this.sqlLiteral(this.schema)
                        + (arg == null ? "" : " AND v.name LIKE " + this.likeLiteral(arg))
                        + " ORDER BY s.name, v.name");
            case "\\ds":
                return this.evalMetaQuery("SELECT s.name AS [Schema], q.name AS [Name],"
                        + " TYPE_NAME(q.user_type_id) AS [Type]"
                        + " FROM sys.sequences q JOIN sys.schemas s ON s.schema_id = q.schema_id"
                        + " WHERE s.name = " + this.sqlLiteral(this.schema)
                        + (arg == null ? "" : " AND q.name LIKE " + this.likeLiteral(arg))
                        + " ORDER BY s.name, q.name");
            case "\\dp":
                return this.evalMetaQuery("SELECT s.name AS [Schema], o.name AS [Name],"
                        + " USER_NAME(o.principal_id) AS [Owner],"
                        + " HAS_PERMS_BY_NAME(QUOTENAME(s.name) + '.' + QUOTENAME(o.name),"
                        + " 'OBJECT', 'SELECT') AS [Can Select]"
                        + " FROM sys.objects o JOIN sys.schemas s ON s.schema_id = o.schema_id"
                        + " WHERE s.name = " + this.sqlLiteral(this.schema)
                        + " AND o.type IN ('U','V','P','FN','IF','TF','SO')"
                        + (arg == null ? "" : " AND o.name LIKE " + this.likeLiteral(arg))
                        + " ORDER BY s.name, o.name");
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
        TerminalCommandHandler handler = TerminalManager.findHandler(SqlServerTerminalPane.TERMINAL_NAME, line);
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
            DBQueryResults<SqlServerExecuteResult> results = this.client.executeSql(this.schema, sql);
            if (!results.isSuccess()) {
                result.setException(new RuntimeException(results.getErrMsg()));
                return result;
            }
            StringBuilder builder = new StringBuilder();
            for (SqlServerExecuteResult executeResult : results.getResults()) {
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
        String sql = "SELECT c.name AS [Column],"
                + " TYPE_NAME(c.user_type_id) + CASE"
                + " WHEN c.max_length = -1 THEN '(MAX)'"
                + " WHEN TYPE_NAME(c.user_type_id) IN ('nvarchar','nchar')"
                + " THEN '(' + CAST(c.max_length / 2 AS varchar(10)) + ')'"
                + " WHEN TYPE_NAME(c.user_type_id) IN ('varchar','char','varbinary','binary')"
                + " THEN '(' + CAST(c.max_length AS varchar(10)) + ')'"
                + " WHEN TYPE_NAME(c.user_type_id) IN ('decimal','numeric')"
                + " THEN '(' + CAST(c.precision AS varchar(10)) + ',' + CAST(c.scale AS varchar(10)) + ')'"
                + " ELSE '' END AS [Type],"
                + " CASE WHEN c.is_nullable = 1 THEN 'nullable' ELSE 'not null' END AS [Nullable],"
                + " dc.definition AS [Default], c.is_identity AS [Identity],"
                + " c.is_computed AS [Computed]"
                + " FROM sys.columns c"
                + " JOIN sys.objects o ON o.object_id = c.object_id AND o.type IN ('U','V')"
                + " JOIN sys.schemas s ON s.schema_id = o.schema_id"
                + " LEFT JOIN sys.default_constraints dc"
                + " ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id"
                + " WHERE o.name = " + this.sqlLiteral(table)
                + (schema == null
                ? " AND s.name = " + this.sqlLiteral(this.schema)
                : " AND s.name = " + this.sqlLiteral(schema))
                + " ORDER BY c.column_id";
        return this.evalMetaQuery(sql);
    }

    /**
     * psql元命令帮助
     *
     * @return 帮助内容
     */
    private String metaHelp() {
        String nl = this.lineEndingText();
        return "SqlServer terminal commands:" + nl
                + "  \\l              list databases" + nl
                + "  \\c [schema]     change current schema" + nl
                + "  \\dn             list schemas" + nl
                + "  \\dt [pattern]   list tables" + nl
                + "  \\d [table]      describe table columns" + nl
                + "  \\df [pattern]   list functions" + nl
                + "  \\dv [pattern]   list views" + nl
                + "  \\ds [pattern]   list sequences" + nl
                + "  \\dp [pattern]   list object SELECT permission" + nl
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
    private String formatResultSet(SqlServerExecuteResult result) {
        // 展开显示
        if (this.expanded) {
            return this.formatExpandedResultSet(result);
        }
        StringBuilder sb = new StringBuilder();
        SqlServerColumns columns = result.getColumns();
        List<SqlServerRecord> records = result.getRecords();
        if (columns == null || records == null) {
            return "";
        }

        int colCount = columns.size();
        int[] colWidths = new int[colCount];
        for (int i = 0; i < colCount; i++) {
            colWidths[i] = Math.max(colWidths[i], columns.get(i).getName().length());
        }
        for (SqlServerRecord record : records) {
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
        for (SqlServerRecord record : records) {
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
    private String formatExpandedResultSet(SqlServerExecuteResult result) {
        SqlServerColumns columns = result.getColumns();
        List<SqlServerRecord> records = result.getRecords();
        if (columns == null || records == null) {
            return "";
        }
        int nameWidth = 0;
        for (int i = 0; i < columns.size(); i++) {
            nameWidth = Math.max(nameWidth, columns.get(i).getName().length());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < records.size(); i++) {
            SqlServerRecord record = records.get(i);
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
        TerminalCommandHandler<?, ?> handler = TerminalManager.findHandler(SqlServerTerminalPane.TERMINAL_NAME, input);
        if (handler == null) {
            handler = new SqlServerTerminalCommandHandler<>() {

                @Override
                public String commandName() {
                    return "";
                }

                @Override
                public TerminalExecuteResult execute(TerminalCommand command, SqlServerTerminalPane terminal) {
                    String input = command.getContent();
                    return terminal.eval(input);
                }
            };
        }
        return handler;
    }

    @Override
    public void initNode() {
        this.keyHandler(SqlServerTerminalKeyHandler.INSTANCE);
        this.helpHandler(SqlServerTerminalHelpHandler.INSTANCE);
        this.mouseHandler(SqlServerTerminalMouseHandler.INSTANCE);
        this.historyHandler(ShellTerminalHistoryHandler.INSTANCE);
        this.completeHandler(SqlServerTerminalCompleteHandler.INSTANCE);
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
            prompts.addAll(ShellSqlServerQueryUtil.getDbKeywords());
            this.setPrompts(prompts);
        }
        return super.getPrompts();
    }

}
