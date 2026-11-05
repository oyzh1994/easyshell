package cn.oyzh.easyshell.sqlite;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.easyshell.sqlite.check.SQLiteCheck;
import cn.oyzh.easyshell.sqlite.check.SQLiteChecks;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumnDefinitionParser;
import cn.oyzh.easyshell.sqlite.column.SQLiteSelectColumnParam;
import cn.oyzh.easyshell.sqlite.condition.SQLiteConditionUtil;
import cn.oyzh.easyshell.sqlite.database.SQLiteDatabase;
import cn.oyzh.easyshell.sqlite.foreignKey.SQLiteForeignKey;
import cn.oyzh.easyshell.sqlite.foreignKey.SQLiteForeignKeys;
import cn.oyzh.easyshell.sqlite.function.SQLiteAlertFunctionParam;
import cn.oyzh.easyshell.sqlite.function.SQLiteCreateFunctionParam;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.function.SQLiteSelectFunctionParam;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.generator.function.SQLiteFunctionAlertSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.function.SQLiteFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.table.SQLiteTableAlertSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.table.SQLiteTableCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.view.SQLiteViewAlertSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.view.SQLiteViewCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.index.SQLiteIndex;
import cn.oyzh.easyshell.sqlite.index.SQLiteIndexes;
import cn.oyzh.easyshell.sqlite.record.SQLiteDeleteRecordParam;
import cn.oyzh.easyshell.sqlite.record.SQLiteInsertRecordParam;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordFilter;
import cn.oyzh.easyshell.sqlite.record.SQLiteSelectRecordParam;
import cn.oyzh.easyshell.sqlite.record.SQLiteUpdateRecordParam;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaManager;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaObject;
import cn.oyzh.easyshell.sqlite.table.SQLiteAlertTableParam;
import cn.oyzh.easyshell.sqlite.table.SQLiteCreateTableParam;
import cn.oyzh.easyshell.sqlite.table.SQLiteSelectTableParam;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.trigger.SQLiteSelectTriggerParam;
import cn.oyzh.easyshell.sqlite.trigger.SQLiteTrigger;
import cn.oyzh.easyshell.sqlite.trigger.SQLiteTriggers;
import cn.oyzh.easyshell.sqlite.view.SQLiteAlertViewParam;
import cn.oyzh.easyshell.sqlite.view.SQLiteCreateViewParam;
import cn.oyzh.easyshell.sqlite.view.SQLiteSelectViewParam;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.fx.db.DBClient;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBFeature;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBRawQueryResults;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.db.sql.DBBaseSqlParser;
import cn.oyzh.fx.db.sql.DBSqlParser;
import cn.oyzh.fx.db.util.DBUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SQLite 数据库客户端。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteClient implements ShellBaseClient, DBClient {

    private final ShellConnect shellConnect;

    private ShellSqliteConnManager connManager;

    private Map<String, Object> properties;

    private final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

    private final ChangeListener<ShellConnState> stateListener =
            (observable, oldValue, newValue) -> ShellBaseClient.super.onStateChanged(newValue);

    private boolean stateListenerRegistered;

    public ShellSqliteClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.registerStateListener();
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    @Override
    public ShellSqliteConnManager getConnManager() {
        if (this.connManager == null) {
            this.connManager = new ShellSqliteConnManager();
            this.connManager.setConfig(ShellSqliteHelper.connConfig(this.shellConnect));
        }
        return this.connManager;
    }

    @Override
    public Map<String, Object> getProperties() {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        return this.properties;
    }

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    @Override
    public boolean isReadonly() {
        return this.shellConnect.isReadonly();
    }

    @Override
    public synchronized void start(int timeout) throws Throwable {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        this.registerStateListener();
        this.state.set(ShellConnState.CONNECTING);
        try {
            this.getConnManager().setConnectTimeout(timeout);
            Connection connection = this.getConnManager().connection();
            if (connection == null || !connection.isValid(Math.max(1, timeout / 1000))) {
                throw new ShellException("SQLite connection is invalid");
            }
            this.state.set(ShellConnState.CONNECTED);
            ShellClientChecker.push(this);
        } catch (Throwable ex) {
            this.state.set(ShellConnState.FAILED);
            IOUtil.close(this.connManager);
            this.connManager = null;
            JulLog.warn("Sqlite client start error", ex);
            throw new ShellException(ex);
        }
    }

    @Override
    public synchronized void close() {
        try {
            IOUtil.close(this.connManager);
        } catch (Exception ex) {
            JulLog.warn("Sqlite client close error", ex);
        } finally {
            this.connManager = null;
            this.state.set(ShellConnState.CLOSED);
            ShellClientChecker.remove(this);
            this.removeStateListener(this.stateListener);
            this.stateListenerRegistered = false;
        }
    }

    @Override
    public boolean isConnected() {
        try {
            if (this.connManager == null) {
                return false;
            }
            Connection connection = this.connManager.getServerConnection();
            return connection != null && !connection.isClosed() && connection.isValid(1);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public DBDialect dialect() {
        return DBDialect.SQLITE;
    }

    @Override
    public int tableSize(String dbName) {
        String sql = "SELECT COUNT(*) FROM " + this.sqliteMaster(dbName)
                + " WHERE type = 'table' AND name NOT LIKE 'sqlite_%'";
        return this.queryInt(sql);
    }

    @Override
    public int viewSize(String dbName) {
        String sql = "SELECT COUNT(*) FROM " + this.sqliteMaster(dbName)
                + " WHERE type = 'view' AND name NOT LIKE 'sqlite_%'";
        return this.queryInt(sql);
    }

    @Override
    public int procedureSize(String dbName) {
        return 0;
    }

    @Override
    public int functionSize(String dbName) {
        return this.selectFunctions(dbName).size();
    }

    @Override
    public String selectVersion() {
        return this.queryString("SELECT sqlite_version()");
    }

    @Override
    public String selectProduct() {
        return "SQLite " + this.selectVersion();
    }

    /**
     * 获取SQLite文件与连接信息。
     *
     * @return 数据库信息
     */
    public SQLiteDatabase database() {
        return this.database("main");
    }

    /**
     * 查询数据库列表。SQLite 单文件数据库固定返回 main。
     *
     * @return 数据库列表
     */
    public List<SQLiteDatabase> databases() {
        return List.of(this.database("main"));
    }

    /**
     * 查询数据库名称列表。SQLite 单文件数据库固定返回 main。
     *
     * @return 数据库名称列表
     */
    public List<String> databaseNames() {
        return List.of("main");
    }

    /**
     * 查询数据库。
     *
     * @param dbName 数据库名称
     * @return 数据库
     */
    public SQLiteDatabase database(String dbName) {
        try {
            SQLiteDatabase database = new SQLiteDatabase();
            database.setName(StringUtil.blankToDefault(dbName, "main"));
            database.setVersion(this.selectVersion());
            Path file = this.getConnManager().fileConfig().resolveFilePath();
            database.setFilePath(file.toString());
            database.setFileSize(Files.exists(file) ? Files.size(file) : 0L);
            database.setPageSize(this.queryInt("PRAGMA page_size"));
            database.setPageCount(this.queryLong("PRAGMA page_count"));
            database.setEncoding(this.queryString("PRAGMA encoding"));
            database.setJournalMode(this.queryString("PRAGMA journal_mode"));
            database.setForeignKeys(this.queryInt("PRAGMA foreign_keys") == 1);
            database.setSynchronous(this.queryString("PRAGMA synchronous"));
            database.setReadOnly(this.isReadonly());
            return database;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询表。
     *
     * @return 表列表
     */
    public List<SQLiteTable> selectTables() {
        return this.selectTablesSimple("main");
    }

    /**
     * 查询表列表(完整信息)
     *
     * @param dbName 库名称
     * @return 表列表
     */
    public List<SQLiteTable> selectTables(String dbName) {
        SQLiteSelectTableParam param = new SQLiteSelectTableParam();
        param.setFull(true);
        param.setDbName(dbName);
        return this.selectTables(param);
    }

    /**
     * 查询表列表(简要信息)
     *
     * @param dbName 库名称
     * @return 表列表
     */
    public List<SQLiteTable> selectTablesSimple(String dbName) {
        SQLiteSelectTableParam param = new SQLiteSelectTableParam();
        param.setFull(false);
        param.setDbName(dbName);
        return this.selectTables(param);
    }

    /**
     * 查询表列表
     *
     * @param param 参数
     * @return 表列表
     */
    public List<SQLiteTable> selectTables(SQLiteSelectTableParam param) {
        String dbName = StringUtil.blankToDefault(param.getDbName(), "main");
        String sql = """
                SELECT
                    name, type, sql
                FROM
                    %s
                WHERE
                    type = 'table'
                AND
                    name NOT LIKE 'sqlite_%%'
                ORDER BY
                    name
                """.formatted(this.sqliteMaster(dbName));
        List<SQLiteTable> tables = new ArrayList<>();
        try {
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SQLiteTable table = new SQLiteTable();
                    table.setDbName(dbName);
                    table.setName(resultSet.getString("name"));
                    table.setType(resultSet.getString("type"));
                    String definition = resultSet.getString("sql");
                    table.setDefinition(definition);
                    String options = definition == null ? "" : definition;
                    int bodyEnd = options.lastIndexOf(')');
                    if (bodyEnd >= 0) {
                        options = options.substring(bodyEnd + 1);
                    }
                    String upperOptions = options.toUpperCase(Locale.ROOT);
                    table.setWithoutRowid(upperOptions.contains("WITHOUT ROWID"));
                    table.setStrict(java.util.regex.Pattern.compile("\\bSTRICT\\b")
                            .matcher(upperOptions).find());
                    if (param.isFull()) {
                        table.setColumns(this.selectColumns(
                                new SQLiteSelectColumnParam(dbName, table.getName())));
                        table.setConstraints(SQLiteColumnDefinitionParser.tableConstraints(table.getDefinition()));
                    }
                    tables.add(table);
                }
            }
            return tables;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询视图。
     *
     * @return 视图列表
     */
    public List<SQLiteView> selectViews() {
        return this.selectViewsSimple("main");
    }

    /**
     * 查询视图列表(完整信息)
     *
     * @param dbName 库名称
     * @return 视图列表
     */
    public List<SQLiteView> selectViews(String dbName) {
        SQLiteSelectViewParam param = new SQLiteSelectViewParam();
        param.setFull(true);
        param.setDbName(dbName);
        return this.selectViews(param);
    }

    /**
     * 查询视图列表(简要信息)
     *
     * @param dbName 库名称
     * @return 视图列表
     */
    public List<SQLiteView> selectViewsSimple(String dbName) {
        SQLiteSelectViewParam param = new SQLiteSelectViewParam();
        param.setFull(false);
        param.setDbName(dbName);
        return this.selectViews(param);
    }

    /**
     * 查询视图列表
     *
     * @param param 参数
     * @return 视图列表
     */
    public List<SQLiteView> selectViews(SQLiteSelectViewParam param) {
        String dbName = StringUtil.blankToDefault(param.getDbName(), "main");
        String sql = """
                SELECT
                    name, sql
                FROM
                    %s
                WHERE
                    type = 'view'
                AND
                    name NOT LIKE 'sqlite_%%'
                ORDER BY
                    name
                """.formatted(this.sqliteMaster(dbName));
        List<SQLiteView> views = new ArrayList<>();
        try {
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SQLiteView view = new SQLiteView();
                    view.setDbName(dbName);
                    view.setName(resultSet.getString("name"));
                    view.setDefinition(SQLiteViewCreateSqlGenerator.viewDefinition(resultSet.getString("sql")));
                    if (param.isFull()) {
                        this.loadViewColumns(view, dbName);
                    }
                    views.add(view);
                }
            }
            return views;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询指定表。
     *
     * @param tableName 表名称
     * @return 表定义
     */
    public SQLiteTable selectTable(String tableName) {
        SQLiteSelectTableParam param = new SQLiteSelectTableParam(tableName);
        param.setFull(true);
        return this.selectTable(param);
    }

    /**
     * 查询指定表(完整信息)
     *
     * @param dbName    库名称
     * @param tableName 表名称
     * @return 表定义
     */
    public SQLiteTable selectTable(String dbName, String tableName) {
        SQLiteSelectTableParam param = new SQLiteSelectTableParam(tableName);
        param.setFull(true);
        param.setDbName(dbName);
        return this.selectTable(param);
    }

    /**
     * 查询指定表(简要信息)
     *
     * @param dbName    库名称
     * @param tableName 表名称
     * @return 表定义
     */
    public SQLiteTable selectTableSimple(String dbName, String tableName) {
        SQLiteSelectTableParam param = new SQLiteSelectTableParam(tableName);
        param.setFull(false);
        param.setDbName(dbName);
        return this.selectTable(param);
    }

    /**
     * 查询指定表
     *
     * @param param 参数
     * @return 表定义
     */
    public SQLiteTable selectTable(SQLiteSelectTableParam param) {
        String dbName = StringUtil.blankToDefault(param.getDbName(), "main");
        String sql = """
                SELECT
                    name, type, sql
                FROM
                    %s
                WHERE
                    type = 'table'
                AND
                    name = ? COLLATE NOCASE
                AND
                    name NOT LIKE 'sqlite_%%'
                """.formatted(this.sqliteMaster(dbName));
        SQLiteTable table = null;
        try {
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, param.getTableName());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        table = new SQLiteTable();
                        table.setDbName(dbName);
                        table.setName(resultSet.getString("name"));
                        table.setType(resultSet.getString("type"));
                        String definition = resultSet.getString("sql");
                        table.setDefinition(definition);
                        String options = definition == null ? "" : definition;
                        int bodyEnd = options.lastIndexOf(')');
                        if (bodyEnd >= 0) {
                            options = options.substring(bodyEnd + 1);
                        }
                        String upperOptions = options.toUpperCase(Locale.ROOT);
                        table.setWithoutRowid(upperOptions.contains("WITHOUT ROWID"));
                        table.setStrict(java.util.regex.Pattern.compile("\\bSTRICT\\b")
                                .matcher(upperOptions).find());
                    }
                }
            }
            if (table != null && param.isFull()) {
                SQLiteSelectColumnParam columnParam = new SQLiteSelectColumnParam(dbName, table.getName());
                table.setColumns(this.selectColumns(columnParam));
                table.setConstraints(SQLiteColumnDefinitionParser.tableConstraints(table.getDefinition()));
            }
            return table;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询指定视图。
     *
     * @param viewName 视图名称
     * @return 视图定义
     */
    public SQLiteView selectView(String viewName) {
        SQLiteSelectViewParam param = new SQLiteSelectViewParam(viewName);
        param.setFull(true);
        return this.selectView(param);
    }

    /**
     * 查询指定视图(完整信息)
     *
     * @param dbName   库名称
     * @param viewName 视图名称
     * @return 视图定义
     */
    public SQLiteView selectView(String dbName, String viewName) {
        SQLiteSelectViewParam param = new SQLiteSelectViewParam(viewName);
        param.setFull(true);
        param.setDbName(dbName);
        return this.selectView(param);
    }

    /**
     * 查询指定视图(简要信息)
     *
     * @param dbName   库名称
     * @param viewName 视图名称
     * @return 视图定义
     */
    public SQLiteView selectViewSimple(String dbName, String viewName) {
        SQLiteSelectViewParam param = new SQLiteSelectViewParam(viewName);
        param.setFull(false);
        param.setDbName(dbName);
        return this.selectView(param);
    }

    /**
     * 查询指定视图
     *
     * @param param 参数
     * @return 视图定义
     */
    public SQLiteView selectView(SQLiteSelectViewParam param) {
        String dbName = StringUtil.blankToDefault(param.getDbName(), "main");
        String sql = """
                SELECT
                    name, sql
                FROM
                    %s
                WHERE
                    type = 'view'
                AND
                    name = ? COLLATE NOCASE
                AND
                    name NOT LIKE 'sqlite_%%'
                """.formatted(this.sqliteMaster(dbName));
        SQLiteView view = null;
        try {
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, param.getViewName());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        view = new SQLiteView();
                        view.setDbName(dbName);
                        view.setName(resultSet.getString("name"));
                        view.setDefinition(SQLiteViewCreateSqlGenerator.viewDefinition(resultSet.getString("sql")));
                    }
                }
            }
            if (view != null && param.isFull()) {
                this.loadViewColumns(view, dbName);
            }
            return view;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 加载视图字段。视图依赖的表不存在时仍保留视图定义，供设计器修正 SQL。
     *
     * @param view   视图
     * @param dbName 库名称
     */
    private void loadViewColumns(SQLiteView view, String dbName) {
        try {
            view.setColumns(this.selectColumns(
                    new SQLiteSelectColumnParam(dbName, view.getName())));
        } catch (Exception ex) {
            JulLog.warn("Load sqlite columns for view [{}] failed: {}", view.getName(), ex.getMessage());
        }
    }

    /**
     * 查询 SQLite 内置及扩展函数。
     *
     * @return 函数列表
     */
    public List<SQLiteFunction> selectFunctions() {
        return this.selectFunctions("main");
    }

    /**
     * 查询函数列表(完整信息)
     *
     * @param dbName 库名称
     * @return 函数列表
     */
    public List<SQLiteFunction> selectFunctions(String dbName) {
        SQLiteSelectFunctionParam param = new SQLiteSelectFunctionParam();
        param.setFull(true);
        param.setDbName(dbName);
        return this.selectFunctions(param);
    }

    /**
     * 查询函数列表(简要信息)
     *
     * @param dbName 库名称
     * @return 函数列表
     */
    public List<SQLiteFunction> selectFunctionsSimple(String dbName) {
        SQLiteSelectFunctionParam param = new SQLiteSelectFunctionParam();
        param.setFull(false);
        param.setDbName(dbName);
        return this.selectFunctions(param);
    }

    /**
     * 查询函数列表
     *
     * @param param 参数
     * @return 函数列表
     */
    public List<SQLiteFunction> selectFunctions(SQLiteSelectFunctionParam param) {
        List<SQLiteFunction> functions = new ArrayList<>();
        try {
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement("PRAGMA function_list");
                 ResultSet resultSet = statement.executeQuery()) {
                ResultSetMetaData metadata = resultSet.getMetaData();
                int nameIndex = findColumn(metadata, "name");
                int argumentIndex = findColumn(metadata, "narg");
                int flagsIndex = findColumn(metadata, "flags");
                int deterministicIndex = findColumn(metadata, "deterministic");
                int directOnlyIndex = findColumn(metadata, "direct_only");
                int finalIndex = findColumn(metadata, "final");
                while (resultSet.next()) {
                    SQLiteFunction function = new SQLiteFunction();
                    function.setDbName(param.getDbName());
                    function.setKind(SQLiteFunction.KIND_BUILTIN);
                    function.setName(resultSet.getString(nameIndex));
                    function.setArgumentCount(argumentIndex > 0 ? resultSet.getInt(argumentIndex) : -1);
                    int flags = flagsIndex > 0 ? resultSet.getInt(flagsIndex) : 0;
                    function.setDeterministic(deterministicIndex > 0
                            ? resultSet.getBoolean(deterministicIndex)
                            : (flags & 0x00000800) != 0);
                    function.setDirectOnly(directOnlyIndex > 0
                            ? resultSet.getBoolean(directOnlyIndex)
                            : (flags & 0x00080000) != 0);
                    function.setFinalFunction(finalIndex > 0 && resultSet.getBoolean(finalIndex));
                    functions.add(function);
                }
            }
        } catch (Exception ex) {
            JulLog.debug("sqlite function_list is unavailable", ex);
        }
        return functions;
    }

    /**
     * 查询指定函数(完整信息)
     *
     * @param dbName       库名称
     * @param functionName 函数名称
     * @return 函数信息
     */
    public SQLiteFunction selectFunction(String dbName, String functionName) {
        SQLiteSelectFunctionParam param = new SQLiteSelectFunctionParam();
        param.setFull(true);
        param.setDbName(dbName);
        param.setFunctionName(functionName);
        return this.selectFunction(param);
    }

    /**
     * 查询指定函数(简要信息)
     *
     * @param dbName       库名称
     * @param functionName 函数名称
     * @return 函数信息
     */
    public SQLiteFunction selectFunctionSimple(String dbName, String functionName) {
        SQLiteSelectFunctionParam param = new SQLiteSelectFunctionParam();
        param.setFull(false);
        param.setDbName(dbName);
        param.setFunctionName(functionName);
        return this.selectFunction(param);
    }

    /**
     * 查询指定函数。
     *
     * @param functionName 函数名称
     * @return 函数信息
     */
    public SQLiteFunction selectFunction(String functionName) {
        return this.selectFunction("main", functionName);
    }

    /**
     * 查询指定函数
     *
     * @param param 参数
     * @return 函数信息
     */
    public SQLiteFunction selectFunction(SQLiteSelectFunctionParam param) {
        return this.selectFunctionsSimple(param.getDbName()).stream()
                .filter(item -> StringUtil.equalsIgnoreCase(item.getName(), param.getFunctionName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 查询当前连接可用的排序规则。
     *
     * @return 排序规则名称
     */
    public List<String> selectCollations() {
        List<String> collations = new ArrayList<>();
        for (String name : new String[]{"BINARY", "NOCASE", "RTRIM"}) {
            if (!collations.contains(name)) {
                collations.add(name);
            }
        }
        try {
            Connection connection = this.getConnManager().connection();
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("PRAGMA collation_list")) {
                ResultSetMetaData metadata = resultSet.getMetaData();
                int nameIndex = findColumn(metadata, "name");
                if (nameIndex < 0) {
                    return collations;
                }
                while (resultSet.next()) {
                    String name = resultSet.getString(nameIndex);
                    if (StringUtil.isNotBlank(name)
                            && collations.stream().noneMatch(item -> StringUtil.equalsIgnoreCase(item, name))) {
                        collations.add(name);
                    }
                }
            }
        } catch (Exception ex) {
            JulLog.debug("sqlite collation_list is unavailable", ex);
        }
        return collations;
    }

    private static int findColumn(ResultSetMetaData metadata, String label) throws SQLException {
        for (int index = 1; index <= metadata.getColumnCount(); index++) {
            if (label.equalsIgnoreCase(metadata.getColumnLabel(index))) {
                return index;
            }
        }
        return -1;
    }

    /**
     * 查询 sqlite_master 通用对象。
     *
     * @param types 对象类型
     * @return 对象列表
     */
    public List<SQLiteSchemaObject> selectSchemaObjects(String... types) {
        List<SQLiteSchemaObject> objects = new ArrayList<>();
        if (types == null || types.length == 0) {
            return objects;
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(types.length, "?"));
        String sql = "SELECT name, type, tbl_name, sql FROM sqlite_master WHERE type IN ("
                + placeholders + ") AND name NOT LIKE 'sqlite_%' ORDER BY type, name";
        try {
            Connection connection = this.getConnManager().connection();
            try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int index = 0; index < types.length; index++) {
                    statement.setString(index + 1, types[index]);
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        SQLiteSchemaObject object = new SQLiteSchemaObject();
                        object.setName(resultSet.getString("name"));
                        object.setType(resultSet.getString("type"));
                        object.setTableName(resultSet.getString("tbl_name"));
                        object.setSql(resultSet.getString("sql"));
                        objects.add(object);
                    }
                }
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
        return objects;
    }

    /**
     * 查询索引列表
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 索引列表
     */
    public List<SQLiteIndex> selectIndexes(String dbName, String tableName) {
        return this.selectSchemaObjects("index").stream()
                .filter(item -> StringUtil.equalsIgnoreCase(item.getTableName(), tableName))
                .map(SQLiteIndex::of)
                .sorted(Comparator.comparing(SQLiteIndex::getName,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * 查询索引列表
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 索引列表
     */
    public List<SQLiteIndex> indexes(String dbName, String tableName) {
        return this.selectIndexes(dbName, tableName);
    }

    /**
     * 查询索引集合
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 索引集合
     */
    public SQLiteIndexes selectIndexList(String dbName, String tableName) {
        return new SQLiteIndexes(this.selectIndexes(dbName, tableName));
    }

    /**
     * 查询触发器列表
     *
     * @param dbName 数据库名称
     * @return 触发器列表
     */
    public DBObjects<SQLiteTrigger> selectTriggers(String dbName) {
        SQLiteSelectTriggerParam param = new SQLiteSelectTriggerParam();
        param.setFull(true);
        param.setDbName(dbName);
        return this.selectTriggers(param);
    }

    /**
     * 查询触发器列表
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 触发器列表
     */
    public DBObjects<SQLiteTrigger> selectTriggers(String dbName, String tableName) {
        SQLiteSelectTriggerParam param = new SQLiteSelectTriggerParam();
        param.setFull(true);
        param.setDbName(dbName);
        param.setTableName(tableName);
        return this.selectTriggers(param);
    }

    /**
     * 查询触发器列表
     *
     * @param param 参数
     * @return 触发器列表
     */
    public DBObjects<SQLiteTrigger> selectTriggers(SQLiteSelectTriggerParam param) {
        DBObjects<SQLiteTrigger> triggers = new DBObjects<>();
        for (SQLiteSchemaObject object : this.selectSchemaObjects("trigger")) {
            if (StringUtil.isNotBlank(param.getTableName())
                    && !StringUtil.equalsIgnoreCase(object.getTableName(), param.getTableName())) {
                continue;
            }
            if (StringUtil.isNotBlank(param.getTriggerName())
                    && !StringUtil.equalsIgnoreCase(object.getName(), param.getTriggerName())) {
                continue;
            }
            SQLiteTrigger trigger = SQLiteTrigger.of(object);
            if (!param.isFull()) {
                trigger.setDefinition(null);
                trigger.setOriginalDefinition(null);
                trigger.setCreateDefinition(null);
            }
            triggers.add(trigger);
        }
        return triggers;
    }

    /**
     * 查询触发器集合
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 触发器集合
     */
    public SQLiteTriggers selectTriggerList(String dbName, String tableName) {
        return new SQLiteTriggers(this.selectTriggers(dbName, tableName));
    }

    /**
     * 查询检查约束列表
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 检查约束列表
     */
    public List<SQLiteCheck> selectChecks(String dbName, String tableName) {
        List<SQLiteCheck> checks = SQLiteColumnDefinitionParser.checks(this.showCreate(tableName));
        for (SQLiteCheck check : checks) {
            check.setDbName(dbName);
            check.setTableName(tableName);
        }
        return checks;
    }

    /**
     * 查询检查约束集合
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 检查约束集合
     */
    public SQLiteChecks selectCheckList(String dbName, String tableName) {
        return new SQLiteChecks(this.selectChecks(dbName, tableName));
    }

    /**
     * 查询外键列表
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 外键列表
     */
    public List<SQLiteForeignKey> selectForeignKeys(String dbName, String tableName) {
        List<SQLiteForeignKey> foreignKeys = SQLiteColumnDefinitionParser.foreignKeys(this.showCreate(tableName));
        for (SQLiteForeignKey foreignKey : foreignKeys) {
            foreignKey.setDbName(dbName);
            foreignKey.setTableName(tableName);
        }
        return foreignKeys;
    }

    /**
     * 查询外键集合
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 外键集合
     */
    public SQLiteForeignKeys selectForeignKeyList(String dbName, String tableName) {
        return new SQLiteForeignKeys(this.selectForeignKeys(dbName, tableName));
    }

    /**
     * 分页查询记录。
     *
     * @param objectName 表或视图名称
     * @param columns    字段列表
     * @param offset     起始位置
     * @param limit      数量限制
     * @return 记录列表
     */
    public List<Map<String, Object>> selectRecords(String objectName, List<SQLiteColumn> columns,
                                                   long offset, int limit) {
        return this.selectRecords(objectName, columns, offset, limit, null);
    }

    /**
     * 分页查询记录。
     *
     * @param objectName 表或视图名称
     * @param columns    字段列表
     * @param offset     起始位置
     * @param limit      数量限制
     * @param filter     过滤条件
     * @return 记录列表
     */
    public List<Map<String, Object>> selectRecords(String objectName, List<SQLiteColumn> columns,
                                                   long offset, int limit, String filter) {
        if (columns == null || columns.isEmpty()) {
            return List.of();
        }
        String columnSql = columns.stream()
                .map(column -> ShellSqliteHelper.quote(column.getName()))
                .collect(java.util.stream.Collectors.joining(", "));
        String sql = "SELECT " + columnSql + " FROM " + ShellSqliteHelper.quote(objectName)
                + whereSql(filter) + " LIMIT ? OFFSET ?";
        List<Map<String, Object>> records = new ArrayList<>();
        try {
            Connection connection = this.getConnManager().connection();
            try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, Math.max(0, limit));
                statement.setLong(2, Math.max(0L, offset));
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Map<String, Object> record = new HashMap<>();
                        for (int index = 0; index < columns.size(); index++) {
                            record.put(columns.get(index).getName(), resultSet.getObject(index + 1));
                        }
                        records.add(record);
                    }
                }
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
        return records;
    }

    /**
     * 查询记录列表
     *
     * @param param 参数
     * @return 记录列表
     */
    public List<SQLiteRecord> selectRecords(SQLiteSelectRecordParam param) {
        List<SQLiteColumn> columns = param.getColumns();
        if (columns == null || columns.isEmpty()) {
            columns = this.selectColumns(param.getTableName());
            param.setColumns(columns);
        }
        StringBuilder builder = new StringBuilder("SELECT * FROM ")
                .append(SQLiteGeneratorUtil.quote(param.getTableName()));
        try {
            String filter = SQLiteConditionUtil.buildCondition(param.getFilters());
            if (StringUtil.isNotBlank(filter)) {
                builder.append(" WHERE ").append(filter);
            }
            if (param.hasPageControl()) {
                builder.append(" LIMIT ? OFFSET ?");
            }
            String sql = builder.toString();
            this.printSql(sql);
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int index = 1;
                if (param.hasPageControl()) {
                    statement.setLong(index++, Math.max(0L, param.getLimit()));
                    statement.setLong(index, Math.max(0L, param.getStart()));
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<SQLiteRecord> records = new ArrayList<>();
                    while (resultSet.next()) {
                        SQLiteRecord record = new SQLiteRecord(columns, param.isReadonly());
                        for (SQLiteColumn column : columns) {
                            record.putValue(column, resultSet.getObject(column.getName()));
                        }
                        records.add(record);
                    }
                    return records;
                }
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询视图记录
     *
     * @param dbName   数据库名称
     * @param viewName 视图名称
     * @param start    起始位置
     * @param limit    限制行数
     * @param filters  过滤条件
     * @return 记录列表
     */
    public List<SQLiteRecord> viewRecords(String dbName, String viewName, Long start, Long limit, List<SQLiteRecordFilter> filters) {
        SQLiteSelectRecordParam param = new SQLiteSelectRecordParam();
        param.setDbName(dbName);
        param.setTableName(viewName);
        param.setStart(start);
        param.setLimit(limit);
        param.setFilters(filters);
        param.setColumns(this.viewColumns(dbName, viewName));
        param.setReadonly(true);
        return this.selectRecords(param);
    }

    /**
     * 查询记录数量
     *
     * @param param 参数
     * @return 记录数量
     */
    public long selectRecordCount(SQLiteSelectRecordParam param) {
        StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM ")
                .append(SQLiteGeneratorUtil.quote(param.getTableName()));
        try {
            String filter = SQLiteConditionUtil.buildCondition(param.getFilters());
            if (StringUtil.isNotBlank(filter)) {
                builder.append(" WHERE ").append(filter);
            }
            String sql = builder.toString();
            this.printSql(sql);
            Connection connection = this.getConnManager().connection();
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                return resultSet.next() ? resultSet.getLong(1) : 0L;
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询单条记录
     *
     * @param param 参数
     * @return 记录
     */
    public SQLiteRecord selectRecord(SQLiteSelectRecordParam param) {
        List<SQLiteColumn> columns = param.getColumns();
        if (columns == null || columns.isEmpty()) {
            columns = this.selectColumns(param.getTableName());
            param.setColumns(columns);
        }
        StringBuilder builder = new StringBuilder("SELECT * FROM ")
                .append(SQLiteGeneratorUtil.quote(param.getTableName()))
                .append(" WHERE ");
        try {
            Object value;
            if (param.getPrimaryKey() != null && StringUtil.isNotBlank(param.getPrimaryKey().getColumnName())) {
                value = param.getPrimaryKey().data();
                builder.append(SQLiteGeneratorUtil.quote(param.getPrimaryKey().getColumnName()));
                builder.append(value == null ? " IS NULL" : " = ?");
            } else {
                String filter = SQLiteConditionUtil.buildCondition(param.getFilters());
                if (StringUtil.isBlank(filter)) {
                    return null;
                }
                builder.append(filter);
                value = null;
            }
            builder.append(" LIMIT 1");
            String sql = builder.toString();
            this.printSql(sql);
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (value != null) {
                    statement.setObject(1, value);
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    SQLiteRecord record = new SQLiteRecord(columns, param.isReadonly());
                    for (SQLiteColumn column : columns) {
                        record.putValue(column, resultSet.getObject(column.getName()));
                    }
                    return record;
                }
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 插入记录
     *
     * @param param 参数
     * @return 受影响行数
     */
    public int insertRecord(SQLiteInsertRecordParam param) {
        if (param == null || param.getRecord() == null || param.getRecord().isEmpty()) {
            return 0;
        }
        StringBuilder builder = new StringBuilder("INSERT INTO ")
                .append(SQLiteGeneratorUtil.quote(param.getTableName()))
                .append(" (");
        try {
            List<DBColumnValue> values = this.recordValues(param.getRecord());
            if (values.isEmpty()) {
                builder = new StringBuilder("INSERT INTO ")
                        .append(SQLiteGeneratorUtil.quote(param.getTableName()))
                        .append(" DEFAULT VALUES");
            } else {
                builder.append(values.stream().map(item -> SQLiteGeneratorUtil.quote(item.column()))
                        .collect(Collectors.joining(", ")));
                builder.append(") VALUES (");
                builder.append(String.join(", ", Collections.nCopies(values.size(), "?")));
                builder.append(')');
            }
            String sql = builder.toString();
            this.printSql(sql);
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int index = 1;
                for (DBColumnValue value : values) {
                    statement.setObject(index++, value.value());
                }
                int count = statement.executeUpdate();
                if (param.getPrimaryKey() != null && param.getPrimaryKey().shouldReturnData()) {
                    param.getPrimaryKey().setReturnData(this.getGeneratedKeys(statement));
                }
                return count;
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 更新记录
     *
     * @param param 参数
     * @return 受影响行数
     */
    public int updateRecord(SQLiteUpdateRecordParam param) {
        if (param == null || param.getUpdateRecord() == null || param.getUpdateRecord().isEmpty()) {
            return 0;
        }
        List<DBColumnValue> values = this.recordValues(param.getUpdateRecord());
        if (values.isEmpty()) {
            return 0;
        }
        StringBuilder builder = new StringBuilder("UPDATE ")
                .append(SQLiteGeneratorUtil.quote(param.getTableName())).append(" SET ");
        builder.append(values.stream().map(item -> SQLiteGeneratorUtil.quote(item.column()) + " = ?")
                .collect(java.util.stream.Collectors.joining(", ")));
        List<DBColumnValue> conditions = new ArrayList<>();
        if (param.getPrimaryKey() != null && StringUtil.isNotBlank(param.getPrimaryKey().getColumnName())) {
            conditions.add(new DBColumnValue(param.getPrimaryKey().getColumnName(),
                    param.getPrimaryKey().originalData()));
        } else if (param.getRecord() != null) {
            conditions.addAll(this.recordValues(param.getRecord()));
        }
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("SQLite update condition is empty");
        }
        builder.append(" WHERE ");
        builder.append(conditions.stream().map(item -> SQLiteGeneratorUtil.quote(item.column())
                        + (item.value() == null ? " IS NULL" : " = ?"))
                .collect(Collectors.joining(" AND ")));
        String sql = builder.toString();
        this.printSql(sql);
        try {
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int index = 1;
                for (DBColumnValue value : values) {
                    statement.setObject(index++, value.value());
                }
                for (DBColumnValue condition : conditions) {
                    if (condition.value() != null) {
                        statement.setObject(index++, condition.value());
                    }
                }
                return statement.executeUpdate();
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 删除记录
     *
     * @param param 参数
     * @return 受影响行数
     */
    public int deleteRecord(SQLiteDeleteRecordParam param) {
        if (param == null) {
            return 0;
        }
        List<DBColumnValue> conditions = new ArrayList<>();
        if (param.getPrimaryKey() != null && StringUtil.isNotBlank(param.getPrimaryKey().getColumnName())) {
            conditions.add(new DBColumnValue(param.getPrimaryKey().getColumnName(),
                    param.getPrimaryKey().originalData()));
        } else if (param.getRecord() != null) {
            conditions.addAll(this.recordValues(param.getRecord()));
        }
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("SQLite delete condition is empty");
        }
        StringBuilder builder = new StringBuilder("DELETE FROM ")
                .append(SQLiteGeneratorUtil.quote(param.getTableName())).append(" WHERE ");
        builder.append(conditions.stream().map(item -> SQLiteGeneratorUtil.quote(item.column())
                        + (item.value() == null ? " IS NULL" : " = ?"))
                .collect(java.util.stream.Collectors.joining(" AND ")));
        String sql = builder.toString();
        this.printSql(sql);
        try {
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int index = 1;
                for (DBColumnValue condition : conditions) {
                    if (condition.value() != null) {
                        statement.setObject(index++, condition.value());
                    }
                }
                return statement.executeUpdate();
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    private List<DBColumnValue> recordValues(DBRecordData recordData) {
        List<DBColumnValue> values = new ArrayList<>();
        for (Map.Entry<cn.oyzh.fx.db.DBColumn, Object> entry : recordData.entrySet()) {
            values.add(new DBColumnValue(entry.getKey().getName(), entry.getValue()));
        }
        return values;
    }

    private record DBColumnValue(String column, Object value) {
    }

    /**
     * 执行单条或脚本 SQL，失败时抛出异常。
     *
     * @param sql SQL
     */
    public void executeSqlSimple(String sql) {
        DBQueryResults<SQLiteExecuteResult> results = this.executeSql(sql);
        if (!results.isSuccess()) {
            throw new ShellException(results.getErrMsg());
        }
    }

    /**
     * 执行单条SQL
     *
     * @param sql SQL
     * @return 执行结果
     */
    public SQLiteExecuteResult executeSingleSql(String sql) {
        DBQueryResults<SQLiteExecuteResult> results = this.executeSql(sql);
        if (!results.isSuccess()) {
            throw new ShellException(results.getErrMsg());
        }
        if (results.getResults() == null || results.getResults().isEmpty()) {
            return null;
        }
        for (SQLiteExecuteResult result : results.getResults()) {
            if (result.isSuccess()) {
                return result;
            }
        }
        return results.getResults().getFirst();
    }

    /**
     * 查询字段。
     *
     * @param objectName 表或视图名称
     * @return 字段列表
     */
    public List<SQLiteColumn> selectColumns(String objectName) {
        String sql = "PRAGMA table_xinfo(" + ShellSqliteHelper.quote(objectName) + ")";
        List<SQLiteColumn> columns = new ArrayList<>();
        try {
            Connection connection = this.getConnManager().connection();
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    SQLiteColumn column = new SQLiteColumn();
                    column.setName(resultSet.getString("name"));
                    column.setTableName(objectName);
                    //String type = resultSet.getString("type");
                    //column.setType(ShellSqliteColumnUtil.normalizeJdbcType(type));
                    column.setType(resultSet.getString("type"));
                    column.setSize(cn.oyzh.easyshell.util.sqlite.ShellSqliteColumnUtil.parseSize(column.getType()));
                    column.setNotNull(resultSet.getInt("notnull") == 1);
                    column.setDefaultValue(resultSet.getString("dflt_value"));
                    column.setPrimaryKey(resultSet.getInt("pk") > 0);
                    int hidden = resultSet.getInt("hidden");
                    column.setHidden(hidden > 0);
                    columns.add(column);
                }
            }
            SQLiteColumnDefinitionParser.enrich(columns, this.showCreate(objectName));
            return columns;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询字段
     *
     * @param param 参数
     * @return 字段列表
     */
    public List<SQLiteColumn> selectColumns(SQLiteSelectColumnParam param) {
        String tableName = param.getTableName();
        List<SQLiteColumn> columns = this.selectColumns(tableName);
        for (SQLiteColumn column : columns) {
            column.setDbName(param.getDbName());
            column.setTableName(tableName);
        }
        return columns;
    }

    /**
     * 查询视图字段
     *
     * @param dbName   库名称
     * @param viewName 视图名称
     * @return 字段列表
     */
    public List<SQLiteColumn> viewColumns(String dbName, String viewName) {
        SQLiteSelectColumnParam param = new SQLiteSelectColumnParam(dbName, viewName);
        return this.selectColumns(param);
    }

    /**
     * 查询对象定义。
     *
     * @param objectName 对象名称
     * @return SQL定义
     */
    public String showCreate(String objectName) {
        try {
            Connection connection = this.getConnManager().connection();
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "SELECT sql FROM sqlite_master WHERE name = ?")) {
                statement.setString(1, objectName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getString(1) : null;
                }
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 查询记录数量。
     *
     * @param objectName 对象名称
     * @return 数量
     */
    public long selectRowCount(String objectName) {
        return this.queryLong("SELECT COUNT(*) FROM " + ShellSqliteHelper.quote(objectName));
    }

    /**
     * 查询记录数量。
     *
     * @param objectName 对象名称
     * @param filter     过滤条件
     * @return 数量
     */
    public long selectRowCount(String objectName, String filter) {
        return this.queryLong("SELECT COUNT(*) FROM " + ShellSqliteHelper.quote(objectName)
                + whereSql(filter));
    }

    private static String whereSql(String filter) {
        return StringUtil.isBlank(filter) ? "" : " WHERE (" + filter.trim() + ")";
    }

    /**
     * 查询表的建表语句
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 建表语句
     */
    public String showCreateTable(String dbName, String tableName) {
        return this.showCreate(tableName);
    }

    /**
     * 查询视图的创建语句
     *
     * @param dbName   数据库名称
     * @param viewName 视图名称
     * @return 创建语句
     */
    public String showCreateView(String dbName, String viewName) {
        return this.showCreate(viewName);
    }

    /**
     * 查询函数的创建语句。SQLite 不支持服务端持久化函数，返回扩展函数的定义（如有）。
     *
     * @param dbName       数据库名称
     * @param functionName 函数名称
     * @return 创建语句
     */
    public String showCreateFunction(String dbName, String functionName) {
        SQLiteFunction function = this.selectFunction(dbName, functionName);
        return function == null ? null : function.getDefinition();
    }

    /**
     * 查询索引的创建语句
     *
     * @param dbName    数据库名称
     * @param indexName 索引名称
     * @return 创建语句
     */
    public String showCreateIndex(String dbName, String indexName) {
        return this.showCreate(indexName);
    }

    /**
     * 查询触发器的创建语句
     *
     * @param dbName      数据库名称
     * @param triggerName 触发器名称
     * @return 创建语句
     */
    public String showCreateTrigger(String dbName, String triggerName) {
        return this.showCreate(triggerName);
    }

    /**
     * 判断视图是否存在
     *
     * @param dbName   数据库名称
     * @param viewName 视图名称
     * @return 是否存在
     */
    public boolean existView(String dbName, String viewName) {
        return this.selectSchemaObjects("view").stream()
                .anyMatch(item -> StringUtil.equalsIgnoreCase(item.getName(), viewName));
    }

    /**
     * 查询主键字段名称
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 主键字段名称
     */
    public List<String> selectPrimaryKeys(String dbName, String tableName) {
        return this.selectColumns(tableName).stream()
                .filter(SQLiteColumn::isPrimaryKey)
                .map(SQLiteColumn::getName)
                .toList();
    }

    /**
     * 是否存在主键
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 是否存在
     */
    public boolean existPrimaryKey(String dbName, String tableName) {
        return !this.selectPrimaryKeys(dbName, tableName).isEmpty();
    }

    /**
     * 查询客户端字符集
     *
     * @return 客户端字符集
     */
    public String selectClientCharacter() {
        return this.queryString("PRAGMA encoding");
    }

    /**
     * 创建表
     *
     * @param param 参数
     */
    public void createTable(SQLiteCreateTableParam param) {
        List<String> sqlList = SQLiteTableCreateSqlGenerator.generateSql(param);
        for (String sql : sqlList) {
            this.executeSqlSimple(sql);
        }
    }

    /**
     * 修改表。SQLite 通过临时表重建实现，保留 SQLite 必要差异。
     *
     * @param param 参数
     */
    public void alertTable(SQLiteAlertTableParam param) {
        String sql = SQLiteTableAlertSqlGenerator.generateSqlSingle(param);
        if (StringUtil.isBlank(sql)) {
            return;
        }
        boolean foreignKeys = this.database().isForeignKeys();
        this.executeSqlSimple("PRAGMA foreign_keys=OFF");
        try {
            this.executeSqlSimple(sql);
        } finally {
            this.executeSqlSimple("PRAGMA foreign_keys=" + (foreignKeys ? "ON" : "OFF"));
        }
    }

    /**
     * 创建视图
     *
     * @param param 参数
     */
    public void createView(SQLiteCreateViewParam param) {
        this.executeSqlSimple(SQLiteViewCreateSqlGenerator.generateSqlSingle(param));
    }

    /**
     * 修改视图
     *
     * @param param 参数
     */
    public void alertView(SQLiteAlertViewParam param) {
        this.executeSqlSimple(SQLiteViewAlertSqlGenerator.generateSqlSingle(param));
    }

    /**
     * 创建函数。SQLite 无服务端持久化函数，支持生成列、表函数视图和 SQL 表达式模板。
     *
     * @param param 参数
     */
    public void createFunction(SQLiteCreateFunctionParam param) {
        SQLiteFunction.validate(param.getFunction());
        this.executeSqlSimple(SQLiteFunctionCreateSqlGenerator.generateSqlSingle(param));
    }

    /**
     * 修改函数。SQLite 无服务端持久化函数，按创建逻辑重建。
     *
     * @param param 参数
     */
    public void alertFunction(SQLiteAlertFunctionParam param) {
        SQLiteFunction.validate(param.getFunction());
        this.executeSqlSimple(SQLiteFunctionAlertSqlGenerator.generateSqlSingle(param));
    }

    /**
     * 重命名表。
     *
     * @param oldName 原表名
     * @param newName 新表名
     */
    public void renameTable(String oldName, String newName) {
        this.requireExistingObject(oldName, "table");
        this.requireAvailableName(newName, oldName);
        new SQLiteSchemaManager(this).renameTable(oldName, newName);
    }

    /**
     * 重命名表。
     *
     * @param dbName  库名称
     * @param oldName 原表名
     * @param newName 新表名
     */
    public void renameTable(String dbName, String oldName, String newName) {
        this.renameTable(oldName, newName);
    }

    /**
     * 清空表数据。
     *
     * @param tableName 表名
     */
    public void clearTable(String tableName) {
        this.requireExistingObject(tableName, "table");
        this.executeSqlSimple("DELETE FROM " + SQLiteGeneratorUtil.quote(tableName));
    }

    /**
     * 清空表数据。
     *
     * @param dbName    库名称
     * @param tableName 表名
     */
    public void clearTable(String dbName, String tableName) {
        this.clearTable(tableName);
    }

    /**
     * 清空表数据并重置 AUTOINCREMENT 序列。
     *
     * @param tableName 表名
     */
    public void truncateTable(String tableName) {
        this.requireExistingObject(tableName, "table");
        String script = "BEGIN;\nDELETE FROM " + SQLiteGeneratorUtil.quote(tableName) + ";\n";
        if (this.hasInternalTable("sqlite_sequence")) {
            script += "DELETE FROM sqlite_sequence WHERE name = "
                    + SQLiteGeneratorUtil.literal(tableName) + ";\n";
        }
        script += "COMMIT;";
        this.executeSqlSimple(script);
    }

    /**
     * 清空表数据并重置 AUTOINCREMENT 序列。
     *
     * @param dbName    库名称
     * @param tableName 表名
     */
    public void truncateTable(String dbName, String tableName) {
        this.truncateTable(tableName);
    }

    /**
     * 删除表。
     *
     * @param tableName 表名
     */
    public void dropTable(String tableName) {
        this.requireExistingObject(tableName, "table");
        this.executeSqlSimple("DROP TABLE " + SQLiteGeneratorUtil.quote(tableName));
    }

    /**
     * 删除表。
     *
     * @param dbName    库名称
     * @param tableName 表名
     */
    public void dropTable(String dbName, String tableName) {
        this.dropTable(tableName);
    }

    /**
     * 克隆表。
     *
     * @param sourceName     源表名
     * @param targetName     目标表名
     * @param includeRecords 是否复制数据
     */
    public void cloneTable(String sourceName, String targetName, boolean includeRecords) {
        new SQLiteSchemaManager(this).cloneTable(sourceName, targetName, includeRecords);
    }

    /**
     * 重命名视图。
     *
     * @param oldName 原视图名
     * @param newName 新视图名
     */
    public void renameView(String oldName, String newName) {
        new SQLiteSchemaManager(this).renameView(oldName, newName);
    }

    /**
     * 重命名视图。
     *
     * @param dbName  库名称
     * @param oldName 原视图名
     * @param newName 新视图名
     */
    public void renameView(String dbName, String oldName, String newName) {
        this.renameView(oldName, newName);
    }

    /**
     * 删除视图。
     *
     * @param viewName 视图名
     */
    public void dropView(String viewName) {
        this.requireExistingObject(viewName, "view");
        this.executeSqlSimple("DROP VIEW " + SQLiteGeneratorUtil.quote(viewName));
    }

    /**
     * 删除视图。
     *
     * @param dbName 库名称
     * @param view   视图
     */
    public void dropView(String dbName, SQLiteView view) {
        this.dropView(view.getName());
    }

    /**
     * 克隆视图。
     *
     * @param sourceName 源视图名
     * @param targetName 目标视图名
     */
    public void cloneView(String sourceName, String targetName) {
        new SQLiteSchemaManager(this).cloneView(sourceName, targetName);
    }

    private void requireExistingObject(String name, String type) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("SQLite " + type + " name is empty");
        }
        boolean exists = this.selectSchemaObjects(type).stream()
                .anyMatch(item -> StringUtil.equalsIgnoreCase(item.getName(), name));
        if (!exists) {
            throw new IllegalArgumentException("SQLite " + type + " does not exist: " + name);
        }
    }

    private void requireAvailableName(String name, String currentName) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("SQLite object name is empty");
        }
        if (StringUtil.equalsIgnoreCase(name, currentName)) {
            return;
        }
        boolean exists = this.selectSchemaObjects("table", "view").stream()
                .anyMatch(item -> StringUtil.equalsIgnoreCase(item.getName(), name));
        if (exists) {
            throw new IllegalArgumentException("SQLite object already exists: " + name);
        }
    }

    private boolean hasInternalTable(String tableName) {
        try {
            Connection connection = this.getConnManager().connection();
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?")) {
                statement.setString(1, tableName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    public DBQueryResults<SQLiteExecuteResult> executeSql(String sql) {
        DBQueryResults<SQLiteExecuteResult> results = new DBQueryResults<>();
        Connection connection = null;
        Statement statement = null;
        boolean oldAutoCommit = true;
        boolean transactional = true;
        boolean failed = false;
        String failedSql = null;
        try {
            this.printSql(sql);
            List<String> sqlList = DBSqlParser.parseSql(sql, this.dialect());
            connection = this.getConnManager().connection();
            oldAutoCommit = connection.getAutoCommit();
            transactional = oldAutoCommit
                    && sqlList.stream().noneMatch(ShellSqliteHelper::isTransactionControl)
                    && sqlList.stream().noneMatch(ShellSqliteHelper::isForeignKeysPragma);
            if (transactional) {
                connection.setAutoCommit(false);
            }
            statement = connection.createStatement();
            for (String execSql : sqlList) {
                failedSql = execSql;
                SQLiteExecuteResult result = new SQLiteExecuteResult();
                result.setContent(execSql);
                long startTime = System.nanoTime();
                try {
                    String transactionCommand = ShellSqliteHelper.transactionCommand(execSql);
                    if ("BEGIN".equals(transactionCommand)) {
                        connection.setAutoCommit(false);
                    } else if ("COMMIT".equals(transactionCommand) || "END".equals(transactionCommand)) {
                        connection.commit();
                        connection.setAutoCommit(true);
                    } else if ("ROLLBACK".equals(transactionCommand)) {
                        connection.rollback();
                        connection.setAutoCommit(true);
                    } else {
                        if ("SAVEPOINT".equals(transactionCommand) && connection.getAutoCommit()) {
                            connection.setAutoCommit(false);
                        }
                        boolean hasResult = statement.execute(execSql);
                        if (hasResult) {
                            try (ResultSet resultSet = statement.getResultSet()) {
                                result.parseResult(resultSet, connection, this.isReadonly());
                                this.enrichExecuteResult(result, execSql);
                            }
                        } else {
                            result.setUpdateCount(Math.max(0, statement.getUpdateCount()));
                        }
                    }
                    result.setSuccess(true);
                } finally {
                    result.setUsed(System.nanoTime() - startTime);
                }
                results.addResult(result);
                failedSql = null;
            }
            if (transactional) {
                connection.commit();
            }
        } catch (Exception ex) {
            failed = true;
            try {
                if (connection != null && !connection.getAutoCommit()) {
                    connection.rollback();
                }
            } catch (SQLException ignored) {
            }
            String message = ex.getMessage();
            if (failedSql != null) {
                message = message + " [SQL: " + failedSql + "]";
            }
            results.parseError(new RuntimeException(message, ex));
        } finally {
            IOUtil.close(statement);
            if (failed) {
                this.restoreAutoCommit(connection, true);
            } else if (transactional) {
                this.restoreAutoCommit(connection, oldAutoCommit);
            }
        }
        return results;
    }

    /**
     * 补充查询结果的可编辑信息
     *
     * @param result 查询结果
     * @param sql    SQL
     */
    private void enrichExecuteResult(SQLiteExecuteResult result, String sql) {
        result.setFullColumn(new DBBaseSqlParser(sql, this.dialect()).isFullColumn(sql));
        String tableName = result.tableName();
        if (StringUtil.isBlank(tableName)) {
            return;
        }
        try {
            // 只有真实表才允许按主键编辑，视图字段不参与可编辑判定。
            if (this.selectTableSimple(result.dbName(), tableName) == null) {
                return;
            }
            List<SQLiteColumn> tableColumns = this.selectColumns(
                    new SQLiteSelectColumnParam(result.dbName(), tableName));
            result.enrichColumns(tableColumns);
        } catch (Exception ex) {
            JulLog.warn("Failed to enrich SQLite query result columns: {}", ex.getMessage());
        }
    }

    /**
     * 执行 SQLite 查询计划分析。
     *
     * @param sql SQL
     * @return 分析结果
     */
    public DBQueryResults<SQLiteExecuteResult> explainSql(String sql) {
        DBQueryResults<SQLiteExecuteResult> results = new DBQueryResults<>();
        List<String> sqlList = DBSqlParser.parseSql(sql, this.dialect());
        for (String statement : sqlList) {
            if (StringUtil.isBlank(statement)) {
                continue;
            }
            String explain = statement.trim().toUpperCase(Locale.ROOT).startsWith("EXPLAIN QUERY PLAN")
                    ? statement : "EXPLAIN QUERY PLAN " + statement;
            DBQueryResults<SQLiteExecuteResult> one = this.executeSql(explain);
            if (one.getResults() != null) {
                for (SQLiteExecuteResult result : one.getResults()) {
                    result.setContent(statement);
                    results.addResult(result);
                }
            }
            if (!one.isSuccess()) {
                results.setErrMsg(one.getErrMsg());
                break;
            }
        }
        return results;
    }

    @Override
    public DBRawQueryResults executeSqlRaw(String name, String sql) throws Exception {
        return this.executeSqlRaw(sql);
    }

    @Override
    public DBRawQueryResults executeSqlRaw(String sql) throws Exception {
        DBRawQueryResults results = new DBRawQueryResults();
        Connection connection = null;
        boolean oldAutoCommit = true;
        boolean transactional = true;
        boolean failed = false;
        try {
            this.printSql(sql);
            List<String> sqlList = DBSqlParser.parseSql(sql, this.dialect());
            connection = this.getConnManager().connection();
            oldAutoCommit = connection.getAutoCommit();
            transactional = oldAutoCommit
                    && sqlList.stream().noneMatch(ShellSqliteHelper::isTransactionControl);
            if (transactional) {
                connection.setAutoCommit(false);
            }
            for (String execSql : sqlList) {
                Statement statement = connection.createStatement();
                String transactionCommand = ShellSqliteHelper.transactionCommand(execSql);
                if ("BEGIN".equals(transactionCommand)) {
                    connection.setAutoCommit(false);
                    statement.close();
                } else if ("COMMIT".equals(transactionCommand) || "END".equals(transactionCommand)) {
                    connection.commit();
                    connection.setAutoCommit(true);
                    statement.close();
                } else if ("ROLLBACK".equals(transactionCommand)) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    statement.close();
                } else {
                    if ("SAVEPOINT".equals(transactionCommand) && connection.getAutoCommit()) {
                        connection.setAutoCommit(false);
                    }
                    boolean hasResult = statement.execute(execSql);
                    results.add(statement, hasResult ? statement.getResultSet() : null);
                }
            }
            if (transactional) {
                connection.commit();
            }
            return results;
        } catch (Exception ex) {
            failed = true;
            results.close();
            if (connection != null && !connection.getAutoCommit()) {
                connection.rollback();
            }
            throw ex;
        } finally {
            if (failed) {
                this.restoreAutoCommit(connection, true);
            } else if (transactional) {
                this.restoreAutoCommit(connection, oldAutoCommit);
            }
        }
    }

    @Override
    public int insertBatch(String dbName, List<String> sqlList) {
        if (sqlList == null || sqlList.isEmpty()) {
            return 0;
        }
        Connection connection = null;
        Statement statement = null;
        boolean oldAutoCommit = true;
        int result = 0;
        try {
            connection = this.getConnManager().connection();
            oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            for (String sql : sqlList) {
                result += statement.executeUpdate(sql);
            }
            connection.commit();
            return result;
        } catch (Exception ex) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new ShellException(ex);
        } finally {
            IOUtil.close(statement);
            this.restoreAutoCommit(connection, oldAutoCommit);
        }
    }

    /**
     * 批量执行SQL。
     *
     * @param dbName   数据库名称
     * @param sqlList  SQL列表
     * @param parallel 是否并行，SQLite单文件写入固定串行
     * @return 更新数量
     */
    public int insertBatch(String dbName, List<String> sqlList, boolean parallel) {
        return this.insertBatch(dbName, sqlList);
    }

    @Override
    public boolean isSupportFeature(DBFeature feature) {
        return feature == DBFeature.CHECK;
    }

    @Override
    public Long getGeneratedKeys(Statement statement) throws Exception {
        try (ResultSet resultSet = statement.getGeneratedKeys()) {
            return resultSet.next() ? resultSet.getLong(1) : null;
        }
    }

    @Override
    public void printSql(String sql) {
        DBUtil.printSql(sql);
        String compressedSql = DBSqlParser.compressSql(sql, this.dialect());
        ShellEventUtil.printSql(compressedSql, this.shellConnect);
    }

    private String sqliteMaster(String dbName) {
        return ShellSqliteHelper.quote(StringUtil.blankToDefault(dbName, "main")) + ".sqlite_master";
    }

    private String queryString(String sql) {
        try {
            Connection connection = this.getConnManager().connection();
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                return resultSet.next() ? resultSet.getString(1) : null;
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    private int queryInt(String sql) {
        try {
            Connection connection = this.getConnManager().connection();
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    private long queryLong(String sql) {
        try {
            Connection connection = this.getConnManager().connection();
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                return resultSet.next() ? resultSet.getLong(1) : 0L;
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    private void restoreAutoCommit(Connection connection, boolean autoCommit) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException ex) {
            JulLog.warn("Restore sqlite auto commit failed", ex);
        }
    }

    private void registerStateListener() {
        if (!this.stateListenerRegistered) {
            this.addStateListener(this.stateListener);
            this.stateListenerRegistered = true;
        }
    }

}
