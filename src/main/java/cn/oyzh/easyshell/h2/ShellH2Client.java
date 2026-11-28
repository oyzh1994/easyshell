package cn.oyzh.easyshell.h2;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.h2.check.H2Check;
import cn.oyzh.easyshell.h2.check.H2Checks;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2ColumnDefinitionParser;
import cn.oyzh.easyshell.h2.column.H2SelectColumnParam;
import cn.oyzh.easyshell.h2.database.H2Database;
import cn.oyzh.easyshell.h2.foreignKey.H2ForeignKey;
import cn.oyzh.easyshell.h2.foreignKey.H2ForeignKeys;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.generator.function.H2FunctionSqlGenerator;
import cn.oyzh.easyshell.h2.generator.sequence.H2SequenceSqlGenerator;
import cn.oyzh.easyshell.h2.generator.table.H2TableAlertSqlGenerator;
import cn.oyzh.easyshell.h2.generator.table.H2TableCreateSqlGenerator;
import cn.oyzh.easyshell.h2.generator.view.H2ViewAlertSqlGenerator;
import cn.oyzh.easyshell.h2.generator.view.H2ViewCreateSqlGenerator;
import cn.oyzh.easyshell.h2.index.H2Index;
import cn.oyzh.easyshell.db.index.ShellIndexHelper;
import cn.oyzh.easyshell.db.index.ShellSelectIndexParam;
import cn.oyzh.easyshell.db.trigger.ShellSelectTriggerParam;
import cn.oyzh.easyshell.db.trigger.ShellTriggerHelper;
import cn.oyzh.easyshell.h2.index.H2Indexes;
import cn.oyzh.easyshell.h2.record.H2DeleteRecordParam;
import cn.oyzh.easyshell.h2.record.H2InsertRecordParam;
import cn.oyzh.easyshell.h2.record.H2Record;
import cn.oyzh.easyshell.h2.record.H2RecordFilter;
import cn.oyzh.easyshell.h2.record.H2SelectRecordParam;
import cn.oyzh.easyshell.h2.record.H2UpdateRecordParam;
import cn.oyzh.easyshell.h2.routine.H2Routine;
import cn.oyzh.easyshell.h2.schema.H2Schema;
import cn.oyzh.easyshell.h2.schema.H2SchemaManager;
import cn.oyzh.easyshell.h2.schema.H2SchemaObject;
import cn.oyzh.easyshell.h2.sequence.H2Sequence;
import cn.oyzh.easyshell.h2.table.H2AlertTableParam;
import cn.oyzh.easyshell.h2.table.H2CreateTableParam;
import cn.oyzh.easyshell.h2.table.H2SelectTableParam;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.easyshell.h2.trigger.H2SelectTriggerParam;
import cn.oyzh.easyshell.h2.trigger.H2Trigger;
import cn.oyzh.easyshell.h2.trigger.H2Triggers;
import cn.oyzh.easyshell.h2.view.H2AlertViewParam;
import cn.oyzh.easyshell.h2.view.H2CreateViewParam;
import cn.oyzh.easyshell.h2.view.H2SelectViewParam;
import cn.oyzh.easyshell.h2.view.H2View;
import cn.oyzh.easyshell.h2.view.H2ViewUpdateMapping;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.query.h2.H2ExecuteResult;
import cn.oyzh.easyshell.util.h2.ShellH2ColumnUtil;
import cn.oyzh.easyshell.util.h2.ShellH2DataUtil;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * H2 数据库客户端。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellH2Client implements ShellBaseClient, DBClient {

    private final ShellConnect shellConnect;

    private ShellH2ConnManager connManager;

    private Map<String, Object> properties;

    private final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

    private final ChangeListener<ShellConnState> stateListener =
            (observable, oldValue, newValue) -> ShellBaseClient.super.onStateChanged(newValue);

    private boolean stateListenerRegistered;

    public ShellH2Client(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.registerStateListener();
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    @Override
    public ShellH2ConnManager getConnManager() {
        if (this.connManager == null) {
            this.connManager = new ShellH2ConnManager();
            this.connManager.setConfig(ShellH2Helper.connConfig(this.shellConnect));
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

    /**
     * 获取 H2 连接配置。
     *
     * @return 连接配置
     */
    public ShellH2ConnConfig h2Config() {
        return this.getConnManager().h2Config();
    }

    /**
     * 获取当前默认数据库名称。H2 为单库：文件模式为文件名（不含 .mv.db 后缀），
     * 内存模式为库名，远程模式为 database 字段。
     *
     * @return 默认数据库名称
     */
    public String defaultDatabaseName() {
        ShellH2ConnConfig config = this.h2Config();
        if (config.isTcpMode()) {
            return StringUtil.blankToDefault(config.getDatabase(), "PUBLIC");
        }
        if (config.isMemMode()) {
            return StringUtil.blankToDefault(config.getHost(), "mem");
        }
        Path file = config.resolveFilePath();
        return file.getFileName().toString();
    }

    /**
     * 获取当前管理的 schema 名称（默认 PUBLIC）。
     *
     * @return schema 名称
     */
    public String schema() {
        return StringUtil.blankToDefault(this.h2Config().getSchema(), "PUBLIC")
                .toUpperCase(Locale.ROOT);
    }

    /**
     * 将树节点中的模式参数规范化为 H2 schema 名称。
     * 兼容旧调用中传入的 main/数据库文件名。
     *
     * @param dbName 模式或旧数据库标识
     * @return schema 名称
     */
    public String schema(String dbName) {
        String value = StringUtil.blankToDefault(dbName, "").trim();
        if (value.isEmpty() || "main".equalsIgnoreCase(value)
                || value.equalsIgnoreCase(this.defaultDatabaseName())) {
            return this.schema();
        }
        return value;
    }

    /**
     * 查询用户模式。
     *
     * @return 模式列表
     */
    public List<H2Schema> selectSchemas() {
        String sql = """
                SELECT SCHEMA_NAME, SCHEMA_OWNER
                FROM INFORMATION_SCHEMA.SCHEMATA
                ORDER BY SCHEMA_NAME
                """;
        List<H2Schema> schemas = new ArrayList<>();
        try {
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    H2Schema schema = new H2Schema();
                    schema.setName(resultSet.getString("SCHEMA_NAME"));
                    schema.setOwner(resultSet.getString("SCHEMA_OWNER"));
                    schemas.add(schema);
                }
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
        return schemas;
    }

    /**
     * 查询指定模式。
     *
     * @param name 模式名称
     * @return 模式；不存在时返回 null
     */
    public H2Schema findSchema(String name) {
        if (StringUtil.isBlank(name)) {
            return null;
        }
        return this.selectSchemas().stream()
                .filter(schema -> StringUtil.equalsIgnoreCase(schema.getName(), name))
                .findFirst()
                .orElse(null);
    }

    /**
     * 创建模式。
     *
     * @param name 模式名称
     * @return 新模式
     */
    public H2Schema createSchema(String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("H2 schema name is empty");
        }
        this.executeSqlSimple("CREATE SCHEMA " + H2GeneratorUtil.quote(name.trim()));
        return this.findSchema(name.trim());
    }

    /**
     * 重命名模式。
     *
     * @param originalName 原名称
     * @param targetName   新名称
     */
    public void renameSchema(String originalName, String targetName) {
        if (StringUtil.isBlank(originalName) || StringUtil.isBlank(targetName)) {
            throw new IllegalArgumentException("H2 schema name is empty");
        }
        if (StringUtil.equalsIgnoreCase(originalName, targetName)) {
            return;
        }
        this.executeSqlSimple("ALTER SCHEMA " + H2GeneratorUtil.quote(originalName)
                + " RENAME TO " + H2GeneratorUtil.quote(targetName.trim()));
    }

    /**
     * 删除模式及其对象。
     *
     * @param name 模式名称
     */
    public void dropSchema(String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("H2 schema name is empty");
        }
        this.executeSqlSimple("DROP SCHEMA " + H2GeneratorUtil.quote(name) + " CASCADE");
    }

    /**
     * 查询指定模式中的函数。
     *
     * @param dbName 模式名称
     * @return 函数列表
     */
    public List<H2Routine> selectFunctions(String dbName) {
        return this.selectRoutines(dbName, "FUNCTION");
    }

    /**
     * 查询指定模式中的过程。
     *
     * @param dbName 模式名称
     * @return 过程列表
     */
    public List<H2Routine> selectProcedures(String dbName) {
        return this.selectRoutines(dbName, "PROCEDURE");
    }

    private List<H2Routine> selectRoutines(String dbName, String routineType) {
        String sql = """
                SELECT
                    ROUTINE_SCHEMA, ROUTINE_NAME, ROUTINE_TYPE, DATA_TYPE,
                    ROUTINE_BODY, ROUTINE_DEFINITION, EXTERNAL_NAME,
                    IS_DETERMINISTIC
                FROM INFORMATION_SCHEMA.ROUTINES
                WHERE ROUTINE_SCHEMA = ? AND ROUTINE_TYPE = ?
                ORDER BY ROUTINE_NAME
                """;
        List<H2Routine> routines = new ArrayList<>();
        try {
            String schema = this.schema(dbName);
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, schema);
                statement.setString(2, routineType);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        H2Routine routine = new H2Routine();
                        routine.setSchema(resultSet.getString("ROUTINE_SCHEMA"));
                        routine.setName(resultSet.getString("ROUTINE_NAME"));
                        routine.setType(resultSet.getString("ROUTINE_TYPE"));
                        routine.setDataType(resultSet.getString("DATA_TYPE"));
                        routine.setBody(resultSet.getString("ROUTINE_BODY"));
                        routine.setDefinition(resultSet.getString("ROUTINE_DEFINITION"));
                        routine.setExternalName(resultSet.getString("EXTERNAL_NAME"));
                        routine.setDeterministic(resultSet.getBoolean("IS_DETERMINISTIC"));
                        routines.add(routine);
                    }
                }
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
        return routines;
    }

    /**
     * 查询指定模式中的函数。
     *
     * @param dbName     模式名称
     * @param functionName 函数名称
     * @return 函数
     */
    public H2Routine selectFunction(String dbName, String functionName) {
        return this.selectFunctions(dbName).stream()
                .filter(function -> StringUtil.equalsIgnoreCase(
                        function.getName(), functionName))
                .findFirst()
                .orElse(null);
    }

    public H2Routine selectProcedure(String dbName, String procedureName) {
        return this.selectProcedures(dbName).stream()
                .filter(procedure -> StringUtil.equalsIgnoreCase(
                        procedure.getName(), procedureName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 查询函数或过程。
     *
     * @param dbName 模式
     * @param name   名称
     * @return 例程
     */
    public H2Routine selectRoutine(String dbName, String name) {
        H2Routine routine = this.selectFunction(dbName, name);
        return routine != null ? routine : this.selectProcedure(dbName, name);
    }

    /**
     * 新增函数。
     *
     * @param function 函数
     * @return 已创建的函数
     */
    public H2Routine createFunction(H2Routine function) {
        this.executeSqlSimple(H2FunctionSqlGenerator.create(function));
        return this.selectRoutine(function.getSchema(), function.getName());
    }

    /**
     * 修改函数。H2 不支持 ALTER ALIAS，采用 DROP + CREATE。
     *
     * @param originalName 原函数名
     * @param function     新函数
     * @return 修改后的函数
     */
    public H2Routine alterFunction(String originalName, H2Routine function) {
        this.dropFunction(function.getSchema(), originalName);
        return this.createFunction(function);
    }

    public H2Routine createRoutine(H2Routine routine) {
        return this.createFunction(routine);
    }

    public H2Routine alterRoutine(String originalName, H2Routine routine) {
        return this.alterFunction(originalName, routine);
    }

    /**
     * 删除函数。
     *
     * @param schema       模式
     * @param functionName 函数名称
     */
    public void dropFunction(String schema, String functionName) {
        this.executeSqlSimple(H2FunctionSqlGenerator.drop(schema, functionName));
    }

    /**
     * 重命名函数。
     *
     * @param schema       模式
     * @param originalName 原函数名
     * @param targetName   新函数名
     * @return 新函数
     */
    public H2Routine renameFunction(String schema, String originalName, String targetName) {
        H2Routine source = this.selectRoutine(schema, originalName);
        if (source == null) {
            throw new IllegalArgumentException("H2 function does not exist: " + originalName);
        }
        if (this.selectRoutine(schema, targetName) != null) {
            throw new IllegalArgumentException("H2 function already exists: " + targetName);
        }
        source.setName(targetName);
        this.dropFunction(schema, originalName);
        return this.createFunction(source);
    }

    /**
     * 克隆函数。
     *
     * @param schema       模式
     * @param sourceName   原函数名
     * @param targetName   新函数名
     * @return 新函数
     */
    public H2Routine cloneFunction(String schema, String sourceName, String targetName) {
        H2Routine source = this.selectRoutine(schema, sourceName);
        if (source == null) {
            throw new IllegalArgumentException("H2 function does not exist: " + sourceName);
        }
        if (this.selectRoutine(schema, targetName) != null) {
            throw new IllegalArgumentException("H2 function already exists: " + targetName);
        }
        source.setName(targetName);
        return this.createFunction(source);
    }

    /**
     * 查询指定模式中的序列。
     *
     * @param dbName 模式名称
     * @return 序列列表
     */
    public List<H2Sequence> selectSequences(String dbName) {
        String sql = """
                SELECT
                    SEQUENCE_SCHEMA, SEQUENCE_NAME, BASE_VALUE, START_VALUE,
                    MINIMUM_VALUE, MAXIMUM_VALUE, INCREMENT, CACHE, CYCLE_OPTION
                FROM INFORMATION_SCHEMA.SEQUENCES
                WHERE SEQUENCE_SCHEMA = ?
                ORDER BY SEQUENCE_NAME
                """;
        List<H2Sequence> sequences = new ArrayList<>();
        try {
            String schema = this.schema(dbName);
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, schema);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        H2Sequence sequence = new H2Sequence();
                        sequence.setSchema(resultSet.getString("SEQUENCE_SCHEMA"));
                        sequence.setName(resultSet.getString("SEQUENCE_NAME"));
                        sequence.setCurrentValue(resultSet.getString("BASE_VALUE"));
                        sequence.setStartValue(resultSet.getString("START_VALUE"));
                        sequence.setMinimumValue(resultSet.getString("MINIMUM_VALUE"));
                        sequence.setMaximumValue(resultSet.getString("MAXIMUM_VALUE"));
                        sequence.setIncrement(resultSet.getString("INCREMENT"));
                        sequence.setCacheSize(resultSet.getString("CACHE"));
                        sequence.setCycle(resultSet.getBoolean("CYCLE_OPTION"));
                        sequences.add(sequence);
                    }
                }
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
        return sequences;
    }

    public H2Sequence selectSequence(String dbName, String sequenceName) {
        return this.selectSequences(dbName).stream()
                .filter(sequence -> StringUtil.equalsIgnoreCase(
                        sequence.getName(), sequenceName))
                .findFirst()
                .orElse(null);
    }

    public H2Sequence createSequence(H2Sequence sequence) {
        this.executeSqlSimple(H2SequenceSqlGenerator.create(sequence));
        return this.selectSequence(sequence.getSchema(), sequence.getName());
    }

    public H2Sequence alterSequence(String originalName, H2Sequence sequence) {
        H2Sequence original = this.selectSequence(sequence.getSchema(), originalName);
        for (String sql : H2SequenceSqlGenerator.alter(originalName, original, sequence)) {
            this.executeSqlSimple(sql);
        }
        return this.selectSequence(sequence.getSchema(), sequence.getName());
    }

    public void dropSequence(String schema, String sequenceName) {
        this.executeSqlSimple(H2SequenceSqlGenerator.drop(schema, sequenceName));
    }

    public H2Sequence renameSequence(String schema, String originalName, String targetName) {
        H2Sequence source = this.selectSequence(schema, originalName);
        if (source == null) {
            throw new IllegalArgumentException("H2 sequence does not exist: " + originalName);
        }
        if (this.selectSequence(schema, targetName) != null) {
            throw new IllegalArgumentException("H2 sequence already exists: " + targetName);
        }
        source.setName(targetName);
        source.setStartValue(StringUtil.blankToDefault(
                source.getCurrentValue(), source.getStartValue()));
        source.setCurrentValue(source.getStartValue());
        H2Sequence renamed = this.createSequence(source);
        this.dropSequence(schema, originalName);
        return renamed;
    }

    public H2Sequence cloneSequence(String schema, String sourceName, String targetName) {
        H2Sequence source = this.selectSequence(schema, sourceName);
        if (source == null) {
            throw new IllegalArgumentException("H2 sequence does not exist: " + sourceName);
        }
        if (this.selectSequence(schema, targetName) != null) {
            throw new IllegalArgumentException("H2 sequence already exists: " + targetName);
        }
        source.setName(targetName);
        source.setStartValue(StringUtil.blankToDefault(
                source.getCurrentValue(), source.getStartValue()));
        source.setCurrentValue(source.getStartValue());
        return this.createSequence(source);
    }

    /**
     * 查询视图 SELECT 定义（来自 INFORMATION_SCHEMA.VIEWS.VIEW_DEFINITION）。
     *
     * @param viewName 视图名称
     * @return 定义内容，不存在时返回 null
     */
    public String viewDefinition(String viewName) {
        return this.viewDefinition(this.schema(), viewName);
    }

    /**
     * 查询指定模式中的视图 SELECT 定义。
     *
     * @param dbName   模式名称
     * @param viewName 视图名称
     * @return 定义内容，不存在时返回 null
     */
    public String viewDefinition(String dbName, String viewName) {
        String schema = this.schema(dbName);
        viewName = this.canonicalObjectName(schema, viewName);
        String sql = """
                SELECT VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS
                WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?
                """;
        try {
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, schema);
                statement.setString(2, viewName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }
            }
        } catch (Exception ignore) {
            // 元数据不可用时返回 null
        }
        return null;
    }

    /**
     * 解析对象的规范名称。H2 未加引号的标识符统一按大写存储，
     * 用户输入小写名称时元数据查询与引号化 SQL 需使用实际存储名。
     *
     * @param objectName 用户输入的对象名
     * @return 数据库中实际存储的名称；不存在时原样返回
     */
    public String canonicalObjectName(String objectName) {
        return this.canonicalObjectName(this.schema(), objectName);
    }

    /**
     * 在指定模式中解析对象规范名称。
     *
     * @param dbName     模式名称
     * @param objectName 用户输入的对象名
     * @return 数据库中实际存储的名称
     */
    public String canonicalObjectName(String dbName, String objectName) {
        if (StringUtil.isBlank(objectName)) {
            return objectName;
        }
        String schema = this.schema(dbName);
        String sql = """
                SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = ? AND UPPER(TABLE_NAME) = ?
                """;
        try {
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, schema);
                statement.setString(2, objectName.trim().toUpperCase(Locale.ROOT));
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }
            }
        } catch (Exception ignore) {
            // 解析失败时按原名称处理
        }
        return objectName;
    }

    @Override
    public synchronized void start(int timeout) throws Throwable {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        ShellH2ColumnUtil.init();
        this.registerStateListener();
        this.state.set(ShellConnState.CONNECTING);
        try {
            this.getConnManager().setConnectTimeout(Math.max(1, timeout / 1000));
            Connection connection = this.getConnManager().connection();
            if (connection == null || !connection.isValid(Math.max(1, timeout / 1000))) {
                throw new ShellException("H2 connection is invalid");
            }
            this.state.set(ShellConnState.CONNECTED);
            ShellClientChecker.push(this);
        } catch (Throwable ex) {
            this.state.set(ShellConnState.FAILED);
            IOUtil.close(this.connManager);
            this.connManager = null;
            JulLog.warn("H2 client start error", ex);
            throw new ShellException(ex);
        }
    }

    @Override
    public synchronized void close() {
        try {
            IOUtil.close(this.connManager);
        } catch (Exception ex) {
            JulLog.warn("H2 client close error", ex);
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
            Connection connection = this.connManager.connection();
            return connection != null && !connection.isClosed() && connection.isValid(1);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public DBDialect dialect() {
        return DBDialect.H2;
    }

    @Override
    public int tableSize(String dbName) {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'";
        return this.queryInt(sql, this.schema(dbName));
    }

    @Override
    public int viewSize(String dbName) {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'VIEW'";
        return this.queryInt(sql, this.schema(dbName));
    }

    @Override
    public int procedureSize(String dbName) {
        String sql = """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.ROUTINES
                WHERE ROUTINE_SCHEMA = ? AND ROUTINE_TYPE = 'PROCEDURE'
                """;
        return this.queryInt(sql, this.schema(dbName));
    }

    @Override
    public int functionSize(String dbName) {
        String sql = """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.ROUTINES
                WHERE ROUTINE_SCHEMA = ? AND ROUTINE_TYPE = 'FUNCTION'
                """;
        return this.queryInt(sql, this.schema(dbName));
    }

    @Override
    public String selectVersion() {
        return this.queryString("SELECT H2VERSION()");
    }

    @Override
    public String selectProduct() {
        return "H2 " + this.selectVersion();
    }

    /**
     * 获取H2数据库与连接信息。
     *
     * @return 数据库信息
     */
    public H2Database database() {
        return this.database(this.defaultDatabaseName());
    }

    /**
     * 查询数据库列表。H2 单库固定返回当前默认库。
     *
     * @return 数据库列表
     */
    public List<H2Database> databases() {
        return List.of(this.database(this.defaultDatabaseName()));
    }

    /**
     * 查询数据库名称列表。H2 单库固定返回当前默认库。
     *
     * @return 数据库名称列表
     */
    public List<String> databaseNames() {
        return List.of(this.defaultDatabaseName());
    }

    /**
     * 查询数据库。
     *
     * @param dbName 数据库名称
     * @return 数据库
     */
    public H2Database database(String dbName) {
        try {
            ShellH2ConnConfig config = this.h2Config();
            H2Database database = new H2Database();
            database.setName(StringUtil.blankToDefault(dbName, this.defaultDatabaseName()));
            database.setVersion(this.selectVersion());
            database.setProduct(this.selectProduct());
            database.setUser(StringUtil.blankToDefault(config.getUser(), "SA"));
            database.setSchema(this.schema());
            database.setReadOnly(this.isReadonly() || config.isReadOnly());
            if (config.isFileMode()) {
                Path actual = config.actualFilePath();
                database.setFilePath(actual.toString());
                database.setFileSize(Files.exists(actual) ? Files.size(actual) : 0L);
            } else if (config.isMemMode()) {
                database.setFilePath("mem:" + config.getHost());
            } else {
                String host = config.getHost() + (config.getPort() != null ? ":" + config.getPort() : "");
                database.setFilePath((config.isUseSSL() ? "ssl://" : "tcp://") + host + "/" + config.getDatabase());
            }
            try {
                database.setIsolationLevel(this.queryString(
                        "SELECT ISOLATION_LEVEL FROM INFORMATION_SCHEMA.SESSIONS WHERE SESSION_ID = SESSION_ID()"));
            } catch (Exception ignored) {
                database.setIsolationLevel("READ COMMITTED");
            }
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
    public List<H2Table> selectTables() {
        return this.selectTablesSimple(this.defaultDatabaseName());
    }

    /**
     * 查询表列表(完整信息)
     *
     * @param dbName 库名称
     * @return 表列表
     */
    public List<H2Table> selectTables(String dbName) {
        H2SelectTableParam param = new H2SelectTableParam();
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
    public List<H2Table> selectTablesSimple(String dbName) {
        H2SelectTableParam param = new H2SelectTableParam();
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
    public List<H2Table> selectTables(H2SelectTableParam param) {
        String schema = this.schema(param.getDbName());
        String sql = """
                SELECT
                    TABLE_NAME, REMARKS
                FROM
                    INFORMATION_SCHEMA.TABLES
                WHERE
                    TABLE_SCHEMA = ?
                AND
                    TABLE_TYPE = 'BASE TABLE'
                ORDER BY
                    TABLE_NAME
                """;
        List<H2Table> tables = new ArrayList<>();
        try {
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, schema);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String tableName = resultSet.getString("TABLE_NAME");
                        if (StringUtil.isNotBlank(param.getTableName())
                                && !StringUtil.equalsIgnoreCase(tableName, param.getTableName())) {
                            continue;
                        }
                        H2Table table = new H2Table();
                        table.setDbName(schema);
                        table.setName(tableName);
                        table.setComment(resultSet.getString("REMARKS"));
                        // H2 2.x INFORMATION_SCHEMA.TABLES 无 SQL 定义列，DDL 由生成器按字段构建
                        if (param.isFull()) {
                            table.setColumns(this.selectColumns(
                                    new H2SelectColumnParam(schema, tableName)));
                        }
                        tables.add(table);
                    }
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
    public List<H2View> selectViews() {
        return this.selectViewsSimple(this.defaultDatabaseName());
    }

    /**
     * 查询视图列表(完整信息)
     *
     * @param dbName 库名称
     * @return 视图列表
     */
    public List<H2View> selectViews(String dbName) {
        H2SelectViewParam param = new H2SelectViewParam();
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
    public List<H2View> selectViewsSimple(String dbName) {
        H2SelectViewParam param = new H2SelectViewParam();
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
    public List<H2View> selectViews(H2SelectViewParam param) {
        String schema = this.schema(param.getDbName());
        String sql = """
                SELECT
                    TABLE_NAME, REMARKS
                FROM
                    INFORMATION_SCHEMA.TABLES
                WHERE
                    TABLE_SCHEMA = ?
                AND
                    TABLE_TYPE = 'VIEW'
                ORDER BY
                    TABLE_NAME
                """;
        List<H2View> views = new ArrayList<>();
        try {
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, schema);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String viewName = resultSet.getString("TABLE_NAME");
                        if (StringUtil.isNotBlank(param.getViewName())
                                && !StringUtil.equalsIgnoreCase(viewName, param.getViewName())) {
                            continue;
                        }
                        H2View view = new H2View();
                        view.setDbName(schema);
                        view.setName(viewName);
                        view.setComment(resultSet.getString("REMARKS"));
                        // 视图 SQL 定义由设计器编辑保存，完整模式加载定义
                        if (param.isFull()) {
                            view.setDefinition(this.viewDefinition(schema, viewName));
                            this.loadViewColumns(view, schema);
                            this.applyViewUpdateMapping(view, this.resolveViewUpdateMapping(
                                    connection, schema, viewName, view.getColumns()));
                        }
                        views.add(view);
                    }
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
    public H2Table selectTable(String tableName) {
        H2SelectTableParam param = new H2SelectTableParam(tableName);
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
    public H2Table selectTable(String dbName, String tableName) {
        H2SelectTableParam param = new H2SelectTableParam(tableName);
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
    public H2Table selectTableSimple(String dbName, String tableName) {
        H2SelectTableParam param = new H2SelectTableParam(tableName);
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
    public H2Table selectTable(H2SelectTableParam param) {
        String dbName = StringUtil.blankToDefault(param.getDbName(), this.defaultDatabaseName());
        H2SelectTableParam selectParam = new H2SelectTableParam();
        selectParam.setDbName(dbName);
        selectParam.setTableName(param.getTableName());
        selectParam.setFull(param.isFull());
        List<H2Table> tables = this.selectTables(selectParam);
        H2Table table = tables.stream()
                .filter(item -> StringUtil.equalsIgnoreCase(item.getName(), param.getTableName()))
                .findFirst()
                .orElse(null);
        if (table != null && param.isFull() && table.getColumns() != null) {
            String schema = this.schema(dbName);
            String definition = this.scriptTableDefinition(schema, table.getName());
            if (StringUtil.isNotBlank(definition)) {
                H2ColumnDefinitionParser.enrich(table.getColumns(), definition);
            }
        }
        return table;
    }

    /**
     * 查询指定视图。
     *
     * @param viewName 视图名称
     * @return 视图定义
     */
    public H2View selectView(String viewName) {
        H2SelectViewParam param = new H2SelectViewParam(viewName);
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
    public H2View selectView(String dbName, String viewName) {
        H2SelectViewParam param = new H2SelectViewParam(viewName);
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
    public H2View selectViewSimple(String dbName, String viewName) {
        H2SelectViewParam param = new H2SelectViewParam(viewName);
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
    public H2View selectView(H2SelectViewParam param) {
        String dbName = StringUtil.blankToDefault(param.getDbName(), this.defaultDatabaseName());
        H2SelectViewParam selectParam = new H2SelectViewParam();
        selectParam.setDbName(dbName);
        selectParam.setViewName(param.getViewName());
        selectParam.setFull(param.isFull());
        List<H2View> views = this.selectViews(selectParam);
        return views.stream()
                .filter(item -> StringUtil.equalsIgnoreCase(item.getName(), param.getViewName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 解析简单视图到基表的更新映射。
     *
     * @param dbName    模式名称
     * @param viewName  视图名称
     * @param viewColumns 视图字段
     * @return 更新映射，复杂视图返回 null
     */
    public H2ViewUpdateMapping viewUpdateMapping(String dbName, String viewName,
                                                 List<H2Column> viewColumns) {
        try {
            String schema = this.schema(dbName);
            Connection connection = this.getConnManager().connection();
            return this.resolveViewUpdateMapping(connection, schema, viewName, viewColumns);
        } catch (Exception ex) {
            JulLog.debug("Resolve h2 view update mapping failed: {}", ex.getMessage());
            return null;
        }
    }

    private void applyViewUpdateMapping(H2View view, H2ViewUpdateMapping mapping) {
        view.setUpdateMapping(mapping);
        if (mapping != null && view.getColumns() != null) {
            for (H2Column column : view.getColumns()) {
                if (mapping.isVirtualColumn(column.getName())) {
                    column.setGeneratedExpression(
                            mapping.virtualExpression(column.getName()));
                }
            }
        }
    }

    /**
     * 加载视图字段。视图依赖的表不存在时仍保留视图定义，供设计器修正 SQL。
     *
     * @param view   视图
     * @param dbName 库名称
     */
    private void loadViewColumns(H2View view, String dbName) {
        try {
            view.setColumns(this.selectColumns(
                    new H2SelectColumnParam(dbName, view.getName())));
        } catch (Exception ex) {
            JulLog.warn("Load h2 columns for view [{}] failed: {}", view.getName(), ex.getMessage());
        }
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
     * 查询 INFORMATION_SCHEMA 通用对象。
     * 支持类型：table、view、index、trigger。
     *
     * @param types 对象类型
     * @return 对象列表
     */
    public List<H2SchemaObject> selectSchemaObjects(String... types) {
        return this.selectSchemaObjectsIn(this.schema(), types);
    }

    /**
     * 查询指定模式中的通用对象。
     *
     * @param dbName 模式名称
     * @param types  对象类型
     * @return 对象列表
     */
    public List<H2SchemaObject> selectSchemaObjectsIn(String dbName, String... types) {
        List<H2SchemaObject> objects = new ArrayList<>();
        if (types == null || types.length == 0) {
            return objects;
        }
        String schema = this.schema(dbName);
        try {
            Connection connection = this.getConnManager().connection();
            List<String> typeList = List.of(types);
            if (typeList.contains("table") || typeList.contains("view")) {
                this.loadTableLikeObjects(connection, schema, typeList, objects);
            }
            if (typeList.contains("index")) {
                this.loadIndexObjects(connection, schema, objects);
            }
            if (typeList.contains("trigger")) {
                this.loadTriggerObjects(connection, schema, objects);
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
        return objects;
    }

    /**
     * 加载表与视图对象。
     *
     * @param connection 连接
     * @param typeList   类型列表
     * @param objects    结果集合
     */
    private void loadTableLikeObjects(Connection connection, String schema,
                                      List<String> typeList, List<H2SchemaObject> objects) {
        List<String> tableTypes = new ArrayList<>();
        if (typeList.contains("table")) {
            tableTypes.add("BASE TABLE");
        }
        if (typeList.contains("view")) {
            tableTypes.add("VIEW");
        }
        String placeholders = String.join(",", Collections.nCopies(tableTypes.size(), "?"));
        String sql = "SELECT TABLE_NAME, TABLE_TYPE, REMARKS FROM INFORMATION_SCHEMA.TABLES"
                + " WHERE TABLE_SCHEMA = ? AND TABLE_TYPE IN (" + placeholders + ") ORDER BY TABLE_NAME";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            for (int index = 0; index < tableTypes.size(); index++) {
                statement.setString(index + 2, tableTypes.get(index));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    H2SchemaObject object = new H2SchemaObject();
                    String type = "VIEW".equalsIgnoreCase(resultSet.getString("TABLE_TYPE"))
                            ? "view" : "table";
                    object.setName(resultSet.getString("TABLE_NAME"));
                    object.setType(type);
                    object.setTableName(resultSet.getString("TABLE_NAME"));
                    object.setRemark(resultSet.getString("REMARKS"));
                    objects.add(object);
                }
            }
        } catch (Exception ex) {
            JulLog.warn("Load h2 table objects failed: {}", ex.getMessage());
        }
    }

    /**
     * 加载索引对象（排除主键等自动生成的索引）。
     *
     * @param connection 连接
     * @param objects    结果集合
     */
    private void loadIndexObjects(Connection connection, String schema, List<H2SchemaObject> objects) {
        // INFORMATION_SCHEMA.INDEXES 无定义列，定义由 DatabaseMetaData 按需构建；
        // IS_GENERATED=TRUE 标记主键等自动生成的索引，直接在查询中排除
        String sql = "SELECT INDEX_NAME, TABLE_NAME, INDEX_TYPE_NAME FROM INFORMATION_SCHEMA.INDEXES"
                + " WHERE INDEX_SCHEMA = ? AND IS_GENERATED = FALSE ORDER BY INDEX_NAME";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String indexName = resultSet.getString("INDEX_NAME");
                    String tableName = resultSet.getString("TABLE_NAME");
                    String indexType = resultSet.getString("INDEX_TYPE_NAME");
                    if (this.isAutoIndex(indexName)) {
                        continue;
                    }
                    H2SchemaObject object = new H2SchemaObject();
                    object.setName(indexName);
                    object.setType("index");
                    object.setTableName(tableName);
                    object.setSql(this.buildIndexDefinition(
                            schema, indexName, tableName, indexType));
                    objects.add(object);
                }
            }
        } catch (Exception ex) {
            JulLog.warn("Load h2 index objects failed: {}", ex.getMessage());
        }
    }

    /**
     * 是否主键等自动生成的索引。
     *
     * <p>H2 主键索引名为 PRIMARY_KEY_x（x 为全局递增序号，与表名无关），
     * 作为 IS_GENERATED 过滤的兜底判断。</p>
     *
     * @param indexName 索引名称
     * @return 是否自动索引
     */
    private boolean isAutoIndex(String indexName) {
        if (indexName == null) {
            return false;
        }
        String prefix = "PRIMARY_KEY_";
        if (!indexName.startsWith(prefix) || indexName.length() <= prefix.length()) {
            return false;
        }
        for (int index = prefix.length(); index < indexName.length(); index++) {
            if (!Character.isDigit(indexName.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 加载触发器对象。
     *
     * @param connection 连接
     * @param objects    结果集合
     */
    private void loadTriggerObjects(Connection connection, String schema, List<H2SchemaObject> objects) {
        Map<String, String> scriptedDefinitions = this.scriptTriggerDefinitions(connection);
        String sql = """
                SELECT
                    TRIGGER_NAME, EVENT_OBJECT_TABLE, ACTION_TIMING, EVENT_MANIPULATION, JAVA_CLASS
                FROM
                    INFORMATION_SCHEMA.TRIGGERS
                WHERE
                    TRIGGER_SCHEMA = ?
                ORDER BY
                    TRIGGER_NAME
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String triggerName = resultSet.getString("TRIGGER_NAME");
                    String tableName = resultSet.getString("EVENT_OBJECT_TABLE");
                    String timing = resultSet.getString("ACTION_TIMING");
                    String event = resultSet.getString("EVENT_MANIPULATION");
                    String javaClass = resultSet.getString("JAVA_CLASS");
                    H2SchemaObject object = new H2SchemaObject();
                    object.setName(triggerName);
                    object.setType("trigger");
                    object.setTableName(tableName);
                    String definition = scriptedDefinitions.get(triggerName);
                    if (StringUtil.isBlank(definition)) {
                        definition = this.buildTriggerDefinition(
                                triggerName, tableName, timing, event, javaClass);
                    }
                    object.setSql(definition);
                    objects.add(object);
                }
            }
        } catch (Exception ex) {
            JulLog.warn("Load h2 trigger objects failed: {}", ex.getMessage());
        }
    }

    /**
     * 从 H2 SCRIPT 输出读取触发器完整定义，保留 AS 源码。
     *
     * @param connection 连接
     * @return 触发器名称与 DDL
     */
    private Map<String, String> scriptTriggerDefinitions(Connection connection) {
        Map<String, String> result = new HashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SCRIPT NODATA NOPASSWORDS NOSETTINGS")) {
            StringBuilder script = new StringBuilder();
            while (resultSet.next()) {
                script.append(resultSet.getString(1)).append('\n');
            }
            Pattern namePattern = Pattern.compile(
                    "(?is)^\\s*CREATE\\s+(?:FORCE\\s+)?TRIGGER\\s+"
                            + "(?:IF\\s+NOT\\s+EXISTS\\s+)?"
                            + "(?:(?:\"[^\"]+\"|`[^`]+`|\\[[^]]+]|[^.\\s]+)\\s*\\.\\s*)?"
                            + "(\"[^\"]+\"|`[^`]+`|\\[[^]]+]|[^.\\s(]+)");
            for (String ddl : DBSqlParser.parseSql(script.toString(), DBDialect.H2)) {
                if (H2GeneratorUtil.keywordIndex(ddl, "TRIGGER", 0) < 0) {
                    continue;
                }
                Matcher matcher = namePattern.matcher(ddl);
                if (matcher.find()) {
                    result.put(H2GeneratorUtil.unquote(matcher.group(1)), ddl);
                }
            }
        } catch (Exception ex) {
            JulLog.debug("Load h2 trigger script failed: {}", ex.getMessage());
        }
        return result;
    }

    /**
     * 根据元数据重建索引定义。
     *
     * @param indexName 索引名称
     * @param tableName 表名称
     * @return 索引定义，不可用时返回null
     */
    private String buildIndexDefinition(String schema, String indexName,
                                        String tableName, String indexType) {
        try {
            Connection connection = this.getConnManager().connection();
            List<String> columns = new ArrayList<>();
            boolean unique = false;
            try (ResultSet resultSet = connection.getMetaData()
                    .getIndexInfo(null, schema, tableName, false, false)) {
                while (resultSet.next()) {
                    String name = resultSet.getString("INDEX_NAME");
                    if (!StringUtil.equalsIgnoreCase(name, indexName)) {
                        continue;
                    }
                    unique = !resultSet.getBoolean("NON_UNIQUE");
                    String column = resultSet.getString("COLUMN_NAME");
                    if (StringUtil.isNotBlank(column) && !columns.contains(column)) {
                        columns.add(column);
                    }
                }
            }
            if (columns.isEmpty()) {
                return null;
            }
            String type = StringUtil.blankToDefault(indexType, "").toUpperCase(Locale.ROOT);
            String typePrefix = type.contains("HASH") ? "HASH "
                    : type.contains("SPATIAL") ? "SPATIAL " : "";
            return "CREATE " + (unique && !typePrefix.equals("SPATIAL ")
                    ? "UNIQUE " : "") + typePrefix + "INDEX "
                    + H2GeneratorUtil.qualified(schema, indexName)
                    + " ON " + H2GeneratorUtil.qualified(schema, tableName) + " ("
                    + columns.stream().map(H2GeneratorUtil::quote).collect(Collectors.joining(", "))
                    + ");";
        } catch (Exception ex) {
            JulLog.debug("Build h2 index definition failed: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * 根据元数据重建触发器定义。
     *
     * @param triggerName 触发器名称
     * @param tableName   所属表
     * @param timing      BEFORE/AFTER
     * @param event       INSERT/UPDATE/DELETE
     * @param javaClass   Java类名
     * @return 触发器定义
     */
    private String buildTriggerDefinition(String triggerName, String tableName,
                                          String timing, String event, String javaClass) {
        if (StringUtil.isBlank(triggerName)) {
            return null;
        }
        StringBuilder builder = new StringBuilder("CREATE TRIGGER ")
                .append(H2GeneratorUtil.quote(triggerName));
        if (StringUtil.isNotBlank(timing)) {
            builder.append(' ').append(timing);
        }
        if (StringUtil.isNotBlank(event)) {
            builder.append(' ').append(event);
        }
        if (StringUtil.isNotBlank(tableName)) {
            builder.append(" ON ").append(H2GeneratorUtil.quote(tableName));
        }
        if (StringUtil.isNotBlank(javaClass)) {
            builder.append(" CALL \"").append(javaClass).append("\"");
        }
        return builder.append(';').toString();
    }

    /**
     * 查询索引列表
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 索引列表
     */
    public List<H2Index> selectIndexes(String dbName, String tableName) {
        return this.selectSchemaObjectsIn(dbName, "index").stream()
                .filter(item -> StringUtil.equalsIgnoreCase(item.getTableName(), tableName))
                .map(H2Index::of)
                .sorted(Comparator.comparing(H2Index::getName,
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
    public List<H2Index> indexes(String dbName, String tableName) {
        return this.selectIndexes(dbName, tableName);
    }

    /**
     * 查询索引列表（数据库级别，包含所属表名称）
     *
     * @param param 查询参数，包含数据库名称与索引名称
     * @return 索引列表
     */
    public List<H2Index> selectSchemaIndexes(ShellSelectIndexParam param) {
        String indexName = param.getIndexName();
        return this.selectSchemaObjectsIn(param.getScope(), "index").stream()
                .filter(item -> StringUtil.isBlank(indexName)
                        || StringUtil.equalsIgnoreCase(item.getName(), indexName))
                .map(H2Index::of)
                .sorted(Comparator.comparing(H2Index::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * 查询索引定义
     *
     * @param dbName    数据库名称
     * @param indexName 索引名称
     * @return 索引定义
     */
    public String selectIndexDefinition(String dbName, String indexName) {
        return ShellIndexHelper.stripTerminator(this.showCreate(dbName, indexName));
    }

    /**
     * 判断索引是否存在
     *
     * @param dbName    数据库名称
     * @param indexName 索引名称
     * @return 是否存在
     */
    public boolean existIndex(String dbName, String indexName) {
        return this.selectSchemaObjectsIn(dbName, "index").stream()
                .anyMatch(item -> StringUtil.equalsIgnoreCase(item.getName(), indexName));
    }

    /**
     * 删除索引
     *
     * @param dbName    数据库名称
     * @param indexName 索引名称
     * @param tableName 表名称
     */
    public void dropIndex(String dbName, String indexName, String tableName) {
        String sql = "DROP INDEX " + H2GeneratorUtil.qualified(this.schema(dbName), indexName);
        this.executeIndexSql(dbName, List.of(sql));
    }

    /**
     * 查询触发器列表（模式/数据库级别，包含所属表名称）
     *
     * @param param 查询参数
     * @return 触发器列表
     */
    public List<H2Trigger> selectSchemaTriggers(ShellSelectTriggerParam param) {
        H2SelectTriggerParam selectParam = new H2SelectTriggerParam();
        selectParam.setDbName(param.getScope());
        selectParam.setTriggerName(param.getTriggerName());
        // 树节点仅需要名称与所属表，不查询完整定义
        DBObjects<H2Trigger> triggers = this.selectTriggers(selectParam);
        String triggerName = param.getTriggerName();
        if (StringUtil.isNotBlank(triggerName)) {
            return triggers.stream()
                    .filter(item -> StringUtil.equalsIgnoreCase(item.getName(), triggerName))
                    .toList();
        }
        return new ArrayList<>(triggers);
    }

    /**
     * 查询触发器定义
     *
     * @param scope       模式/数据库名称
     * @param triggerName 触发器名称
     * @return 触发器定义
     */
    public String selectTriggerDefinition(String scope, String triggerName) {
        return ShellTriggerHelper.stripTerminator(this.showCreateTrigger(scope, triggerName));
    }

    /**
     * 判断触发器是否存在
     *
     * @param scope       模式/数据库名称
     * @param triggerName 触发器名称
     * @return 是否存在
     */
    public boolean existTrigger(String scope, String triggerName) {
        return !this.selectSchemaTriggers(new ShellSelectTriggerParam(scope, triggerName)).isEmpty();
    }

    /**
     * 删除触发器
     *
     * @param scope       模式/数据库名称
     * @param triggerName 触发器名称
     * @param tableName   所属表名称
     */
    public void dropTrigger(String scope, String triggerName, String tableName) {
        String sql = "DROP TRIGGER IF EXISTS "
                + H2GeneratorUtil.qualified(this.schema(scope), triggerName);
        this.executeTriggerSql(scope, List.of(sql));
    }

    /**
     * 执行触发器相关SQL
     *
     * @param scope   模式/数据库名称
     * @param sqlList SQL列表
     */
    public void executeTriggerSql(String scope, List<String> sqlList) {
        // 复用与索引一致的通用DDL执行逻辑
        this.executeIndexSql(scope, sqlList);
    }

    /**
     * 执行索引相关SQL
     *
     * @param dbName  数据库名称
     * @param sqlList SQL列表
     */
    public void executeIndexSql(String dbName, List<String> sqlList) {
        if (sqlList == null || sqlList.isEmpty()) {
            return;
        }
        this.executeSqlSimple(String.join(";\n", sqlList));
    }

    /**
     * 查询索引集合
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 索引集合
     */
    public H2Indexes selectIndexList(String dbName, String tableName) {
        return new H2Indexes(this.selectIndexes(dbName, tableName));
    }

    /**
     * 查询触发器列表
     *
     * @param dbName 数据库名称
     * @return 触发器列表
     */
    public DBObjects<H2Trigger> selectTriggers(String dbName) {
        H2SelectTriggerParam param = new H2SelectTriggerParam();
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
    public DBObjects<H2Trigger> selectTriggers(String dbName, String tableName) {
        H2SelectTriggerParam param = new H2SelectTriggerParam();
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
    public DBObjects<H2Trigger> selectTriggers(H2SelectTriggerParam param) {
        DBObjects<H2Trigger> triggers = new DBObjects<>();
        for (H2SchemaObject object : this.selectSchemaObjectsIn(param.getDbName(), "trigger")) {
            if (StringUtil.isNotBlank(param.getTableName())
                    && !StringUtil.equalsIgnoreCase(object.getTableName(), param.getTableName())) {
                continue;
            }
            if (StringUtil.isNotBlank(param.getTriggerName())
                    && !StringUtil.equalsIgnoreCase(object.getName(), param.getTriggerName())) {
                continue;
            }
            H2Trigger trigger = H2Trigger.of(object);
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
    public H2Triggers selectTriggerList(String dbName, String tableName) {
        return new H2Triggers(this.selectTriggers(dbName, tableName));
    }

    /**
     * 查询检查约束列表
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 检查约束列表
     */
    public List<H2Check> selectChecks(String dbName, String tableName) {
        String schema = this.schema(dbName);
        tableName = this.canonicalObjectName(schema, tableName);
        List<H2Check> checks = new ArrayList<>();
        String sql = """
                SELECT
                    TC.CONSTRAINT_NAME, CC.CHECK_CLAUSE
                FROM
                    INFORMATION_SCHEMA.TABLE_CONSTRAINTS TC
                JOIN
                    INFORMATION_SCHEMA.CHECK_CONSTRAINTS CC
                    ON TC.CONSTRAINT_NAME = CC.CONSTRAINT_NAME
                WHERE
                    TC.TABLE_SCHEMA = ?
                AND
                    TC.TABLE_NAME = ?
                AND
                    TC.CONSTRAINT_TYPE = 'CHECK'
                ORDER BY
                    TC.CONSTRAINT_NAME
                """;
        try {
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, schema);
                statement.setString(2, tableName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        H2Check check = new H2Check(resultSet.getString("CONSTRAINT_NAME"));
                        check.setClause(resultSet.getString("CHECK_CLAUSE"));
                        check.setDbName(dbName);
                        check.setTableName(tableName);
                        checks.add(check);
                    }
                }
            }
        } catch (Exception ex) {
            // 元数据不可用时保守返回空列表
            JulLog.debug("h2 check constraints is unavailable", ex);
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
    public H2Checks selectCheckList(String dbName, String tableName) {
        return new H2Checks(this.selectChecks(dbName, tableName));
    }

    /**
     * 查询外键列表
     *
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @return 外键列表
     */
    public List<H2ForeignKey> selectForeignKeys(String dbName, String tableName) {
        String schema = this.schema(dbName);
        tableName = this.canonicalObjectName(schema, tableName);
        List<H2ForeignKey> foreignKeys = H2ColumnDefinitionParser.foreignKeys(
                this.showCreate(schema, tableName));
        for (H2ForeignKey foreignKey : foreignKeys) {
            foreignKey.setDbName(schema);
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
    public H2ForeignKeys selectForeignKeyList(String dbName, String tableName) {
        return new H2ForeignKeys(this.selectForeignKeys(dbName, tableName));
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
    public List<Map<String, Object>> selectRecords(String objectName, List<H2Column> columns,
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
    public List<Map<String, Object>> selectRecords(String objectName, List<H2Column> columns,
                                                   long offset, int limit, String filter) {
        return this.selectRecords(this.schema(), objectName, columns, offset, limit, filter);
    }

    /**
     * 分页查询指定模式中的记录。
     *
     * @param dbName     模式名称
     * @param objectName 表或视图名称
     * @param columns    字段列表
     * @param offset     起始位置
     * @param limit      数量限制
     * @return 记录列表
     */
    public List<Map<String, Object>> selectRecords(String dbName, String objectName,
                                                   List<H2Column> columns, long offset, int limit) {
        return this.selectRecords(dbName, objectName, columns, offset, limit, null);
    }

    /**
     * 分页查询指定模式中的记录。
     *
     * @param dbName     模式名称
     * @param objectName 表或视图名称
     * @param columns    字段列表
     * @param offset     起始位置
     * @param limit      数量限制
     * @param filter     过滤条件
     * @return 记录列表
     */
    public List<Map<String, Object>> selectRecords(String dbName, String objectName,
                                                   List<H2Column> columns, long offset, int limit,
                                                   String filter) {
        if (columns == null || columns.isEmpty()) {
            return List.of();
        }
        String schema = this.schema(dbName);
        objectName = this.canonicalObjectName(schema, objectName);
        String columnSql = columns.stream()
                .map(column -> ShellH2Helper.quote(column.getName()))
                .collect(java.util.stream.Collectors.joining(", "));
        String sql = "SELECT " + columnSql + " FROM " + H2GeneratorUtil.qualified(schema, objectName)
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
                            H2Column column = columns.get(index);
                            Object value = resultSet.getObject(index + 1);
                            record.put(column.getName(), ShellH2DataUtil.valueStandardization(column, value));
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
    public List<H2Record> selectRecords(H2SelectRecordParam param) {
        String schema = this.schema(param.getDbName());
        param.setTableName(this.canonicalObjectName(schema, param.getTableName()));
        List<H2Column> columns = param.getColumns();
        if (columns == null || columns.isEmpty()) {
            columns = this.selectColumns(schema, param.getTableName());
            param.setColumns(columns);
        }
        StringBuilder builder = new StringBuilder("SELECT * FROM ")
                .append(H2GeneratorUtil.qualified(schema, param.getTableName()));
        try {
            String filter = DBUtil.buildCondition(param.getFilters());
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
                    List<H2Record> records = new ArrayList<>();
                    while (resultSet.next()) {
                        H2Record record = new H2Record(columns, param.isReadonly());
                        for (H2Column column : columns) {
                            Object value = resultSet.getObject(column.getName());
                            record.putValue(column, ShellH2DataUtil.valueStandardization(column, value));
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
    public List<H2Record> viewRecords(String dbName, String viewName, Long start, Long limit, List<H2RecordFilter> filters) {
        H2SelectRecordParam param = new H2SelectRecordParam();
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
    public long selectRecordCount(H2SelectRecordParam param) {
        String schema = this.schema(param.getDbName());
        StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM ")
                .append(H2GeneratorUtil.qualified(schema,
                        this.canonicalObjectName(schema, param.getTableName())));
        try {
            String filter = DBUtil.buildCondition(param.getFilters());
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
    public H2Record selectRecord(H2SelectRecordParam param) {
        String schema = this.schema(param.getDbName());
        List<H2Column> columns = param.getColumns();
        if (columns == null || columns.isEmpty()) {
            columns = this.selectColumns(schema, param.getTableName());
            param.setColumns(columns);
        }
        StringBuilder builder = new StringBuilder("SELECT * FROM ")
                .append(H2GeneratorUtil.qualified(schema, param.getTableName()))
                .append(" WHERE ");
        try {
            Object value;
            if (param.getPrimaryKey() != null && StringUtil.isNotBlank(param.getPrimaryKey().getColumnName())) {
                value = param.getPrimaryKey().data();
                builder.append(H2GeneratorUtil.quote(param.getPrimaryKey().getColumnName()));
                builder.append(value == null ? " IS NULL" : " = ?");
            } else {
                String filter = DBUtil.buildCondition(param.getFilters());
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
                    H2Record record = new H2Record(columns, param.isReadonly());
                    for (H2Column column : columns) {
                        Object data = resultSet.getObject(column.getName());
                        record.putValue(column, ShellH2DataUtil.valueStandardization(column, data));
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
    public int insertRecord(H2InsertRecordParam param) {
        if (param == null || param.getRecord() == null || param.getRecord().isEmpty()) {
            return 0;
        }
        String schema = this.schema(param.getDbName());
        param.setTableName(this.canonicalObjectName(schema, param.getTableName()));
        String table = H2GeneratorUtil.qualified(schema, param.getTableName());
        StringBuilder builder = new StringBuilder("INSERT INTO ")
                .append(table)
                .append(" (");
        try {
            List<DBColumnValue> values = this.insertRecordValues(param.getRecord());
            if (values.isEmpty()) {
                builder = new StringBuilder("INSERT INTO ")
                        .append(table)
                        .append(" DEFAULT VALUES");
            } else {
                builder.append(values.stream().map(item -> H2GeneratorUtil.quote(item.column()))
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
                return this.executeWriteTransaction(connection, () -> {
                    int count = statement.executeUpdate();
                    if (param.getPrimaryKey() != null && param.getPrimaryKey().shouldReturnData()) {
                        param.getPrimaryKey().setReturnData(this.getGeneratedKeys(statement));
                    }
                    return count;
                });
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
    public int updateRecord(H2UpdateRecordParam param) {
        if (param == null || param.getUpdateRecord() == null || param.getUpdateRecord().isEmpty()) {
            return 0;
        }
        String schema = this.schema(param.getDbName());
        param.setTableName(this.canonicalObjectName(schema, param.getTableName()));
        List<DBColumnValue> values = this.recordValues(param.getUpdateRecord());
        if (values.isEmpty()) {
            return 0;
        }
        StringBuilder builder = new StringBuilder("UPDATE ")
                .append(H2GeneratorUtil.qualified(schema, param.getTableName())).append(" SET ");
        builder.append(values.stream().map(item -> H2GeneratorUtil.quote(item.column()) + " = ?")
                .collect(java.util.stream.Collectors.joining(", ")));
        List<DBColumnValue> conditions = new ArrayList<>();
        if (param.getPrimaryKey() != null && StringUtil.isNotBlank(param.getPrimaryKey().getColumnName())) {
            conditions.add(new DBColumnValue(param.getPrimaryKey().getColumnName(),
                    param.getPrimaryKey().originalData()));
        } else if (param.getRecord() != null) {
            conditions.addAll(this.recordValues(param.getRecord()));
        }
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("H2 update condition is empty");
        }
        builder.append(" WHERE ");
        builder.append(conditions.stream().map(item -> H2GeneratorUtil.quote(item.column())
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
                return this.executeWriteTransaction(connection, statement::executeUpdate);
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
    public int deleteRecord(H2DeleteRecordParam param) {
        if (param == null) {
            return 0;
        }
        String schema = this.schema(param.getDbName());
        param.setTableName(this.canonicalObjectName(schema, param.getTableName()));
        List<DBColumnValue> conditions = new ArrayList<>();
        if (param.getPrimaryKey() != null && StringUtil.isNotBlank(param.getPrimaryKey().getColumnName())) {
            conditions.add(new DBColumnValue(param.getPrimaryKey().getColumnName(),
                    param.getPrimaryKey().originalData()));
        } else if (param.getRecord() != null) {
            conditions.addAll(this.recordValues(param.getRecord()));
        }
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("H2 delete condition is empty");
        }
        StringBuilder builder = new StringBuilder("DELETE FROM ")
                .append(H2GeneratorUtil.qualified(schema, param.getTableName())).append(" WHERE ");
        builder.append(conditions.stream().map(item -> H2GeneratorUtil.quote(item.column())
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
                return this.executeWriteTransaction(connection, statement::executeUpdate);
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    private List<DBColumnValue> recordValues(DBRecordData recordData) {
        List<DBColumnValue> values = new ArrayList<>();
        for (Map.Entry<cn.oyzh.fx.db.DBColumn, Object> entry : recordData.entrySet()) {
            values.add(new DBColumnValue(entry.getKey().getName(),
                    ShellH2DataUtil.jdbcValue(entry.getKey(), entry.getValue())));
        }
        return values;
    }

    private List<DBColumnValue> insertRecordValues(DBRecordData recordData) {
        List<DBColumnValue> values = new ArrayList<>();
        for (Map.Entry<cn.oyzh.fx.db.DBColumn, Object> entry : recordData.entrySet()) {
            if (entry.getValue() == null || entry.getKey().isGenerated()) {
                continue;
            }
            values.add(new DBColumnValue(entry.getKey().getName(),
                    ShellH2DataUtil.jdbcValue(entry.getKey(), entry.getValue())));
        }
        return values;
    }

    private record DBColumnValue(String column, Object value) {
    }

    /**
     * 在事务中执行记录写入。触发器内部不允许提交，外层提交必须等触发器返回后执行。
     *
     * @param connection 连接
     * @param operation  写入操作
     * @return 受影响行数
     * @throws SQLException SQL异常
     */
    private int executeWriteTransaction(Connection connection, SqlWriteOperation operation)
            throws Exception {
        boolean autoCommit = connection.getAutoCommit();
        if (autoCommit) {
            connection.setAutoCommit(false);
        }
        try {
            int result = operation.execute();
            if (autoCommit) {
                connection.commit();
            }
            return result;
        } catch (Exception ex) {
            if (autoCommit) {
                connection.rollback();
            }
            throw ex;
        } finally {
            if (autoCommit) {
                connection.setAutoCommit(true);
            }
        }
    }

    @FunctionalInterface
    private interface SqlWriteOperation {
        int execute() throws Exception;
    }

    /**
     * 执行单条或脚本 SQL，失败时抛出异常。
     *
     * @param sql SQL
     */
    public void executeSqlSimple(String sql) {
        DBQueryResults<H2ExecuteResult> results = this.executeSql(sql);
        if (!results.isSuccess()) {
            throw new ShellException(results.getErrMsg());
        }
    }

    /**
     * 在指定模式中执行单条或脚本 SQL，失败时抛出异常。
     *
     * @param dbName 模式名称
     * @param sql    SQL
     */
    public void executeSqlSimple(String dbName, String sql) {
        DBQueryResults<H2ExecuteResult> results = this.executeSql(dbName, sql);
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
    public H2ExecuteResult executeSingleSql(String sql) {
        DBQueryResults<H2ExecuteResult> results = this.executeSql(sql);
        return this.firstExecuteResult(results);
    }

    /**
     * 在指定模式中执行单条SQL
     *
     * @param dbName 模式名称
     * @param sql    SQL
     * @return 执行结果
     */
    public H2ExecuteResult executeSingleSql(String dbName, String sql) {
        DBQueryResults<H2ExecuteResult> results = this.executeSql(dbName, sql);
        return this.firstExecuteResult(results);
    }

    private H2ExecuteResult firstExecuteResult(DBQueryResults<H2ExecuteResult> results) {
        if (!results.isSuccess()) {
            throw new ShellException(results.getErrMsg());
        }
        if (results.getResults() == null || results.getResults().isEmpty()) {
            return null;
        }
        for (H2ExecuteResult result : results.getResults()) {
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
    public List<H2Column> selectColumns(String objectName) {
        return this.selectColumns(this.schema(), objectName);
    }

    /**
     * 查询指定模式中对象的字段。
     *
     * @param dbName     模式名称
     * @param objectName 表或视图名称
     * @return 字段列表
     */
    public List<H2Column> selectColumns(String dbName, String objectName) {
        String schema = this.schema(dbName);
        objectName = this.canonicalObjectName(schema, objectName);
        String sql = """
                SELECT
                    COLUMN_NAME, ORDINAL_POSITION, COLUMN_DEFAULT, IS_NULLABLE, DATA_TYPE,
                    CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE,
                    IS_IDENTITY, IS_GENERATED, GENERATION_EXPRESSION, REMARKS
                FROM
                    INFORMATION_SCHEMA.COLUMNS
                WHERE
                    TABLE_SCHEMA = ?
                AND
                    TABLE_NAME = ?
                ORDER BY
                    ORDINAL_POSITION
                """;
        List<H2Column> columns = new ArrayList<>();
        try {
            Connection connection = this.getConnManager().connection();
            List<String> primaryKeys = this.selectPrimaryKeys(connection, schema, objectName);
            if (primaryKeys.isEmpty() && this.isView(connection, schema, objectName)) {
                primaryKeys = this.inferViewPrimaryKeys(connection, schema, objectName);
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, schema);
                statement.setString(2, objectName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        H2Column column = new H2Column();
                        column.setName(resultSet.getString("COLUMN_NAME"));
                        column.setTableName(objectName);
                        column.setType(resultSet.getString("DATA_TYPE"));
                        Integer size = intIfInRange(resultSet.getObject("CHARACTER_MAXIMUM_LENGTH"));
                        if (size == null) {
                            size = intIfInRange(resultSet.getObject("NUMERIC_PRECISION"));
                        }
                        column.setSize(size);
                        column.setDigits(intIfInRange(resultSet.getObject("NUMERIC_SCALE")));
                        column.setPosition(resultSet.getInt("ORDINAL_POSITION"));
                        column.setNotNull("NO".equalsIgnoreCase(resultSet.getString("IS_NULLABLE")));
                        column.setDefaultValue(resultSet.getString("COLUMN_DEFAULT"));
                        column.setPrimaryKey(primaryKeys.stream()
                                .anyMatch(item -> StringUtil.equalsIgnoreCase(item, column.getName())));
                        column.setIdentity("YES".equalsIgnoreCase(resultSet.getString("IS_IDENTITY")));
                        boolean generated = "ALWAYS".equalsIgnoreCase(resultSet.getString("IS_GENERATED"));
                        column.setGenerated(generated);
                        if (generated) {
                            column.setGeneratedExpression(resultSet.getString("GENERATION_EXPRESSION"));
                        }
                        column.setComment(resultSet.getString("REMARKS"));
                        columns.add(column);
                    }
                }
            }
            return columns;
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    private static Integer intIfInRange(Object value) {
        if (!(value instanceof Number number)) {
            return null;
        }
        long result = number.longValue();
        return result < Integer.MIN_VALUE || result > Integer.MAX_VALUE
                ? null : (int) result;
    }

    /**
     * 查询表的主键字段名称。
     *
     * @param connection 连接
     * @param tableName  表名称
     * @return 主键字段列表
     */
    private List<String> selectPrimaryKeys(Connection connection, String schema, String tableName) {
        tableName = this.canonicalObjectName(schema, tableName);
        String sql = """
                SELECT
                    KCU.COLUMN_NAME
                FROM
                    INFORMATION_SCHEMA.KEY_COLUMN_USAGE KCU
                WHERE
                    KCU.TABLE_SCHEMA = ?
                AND
                    KCU.TABLE_NAME = ?
                AND
                    KCU.CONSTRAINT_NAME IN (
                        SELECT CONSTRAINT_NAME
                        FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                        WHERE TABLE_SCHEMA = ?
                        AND TABLE_NAME = ?
                        AND CONSTRAINT_TYPE = 'PRIMARY KEY'
                    )
                ORDER BY
                    KCU.ORDINAL_POSITION
                """;
        List<String> primaryKeys = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, tableName);
            statement.setString(3, schema);
            statement.setString(4, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    primaryKeys.add(resultSet.getString("COLUMN_NAME"));
                }
            }
        } catch (Exception ex) {
            // 主键元数据不可用时保守返回空列表
            JulLog.debug("h2 primary keys is unavailable", ex);
        }
        return primaryKeys;
    }

    /**
     * 判断对象是否为视图。
     */
    private boolean isView(Connection connection, String schema, String objectName) {
        String sql = """
                SELECT TABLE_TYPE FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            statement.setString(2, objectName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        && "VIEW".equalsIgnoreCase(resultSet.getString(1));
            }
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * H2 视图没有主键约束。对简单单表投影视图，按源表主键和 SELECT 别名推断主键列。
     *
     * <p>JOIN、聚合等复杂视图不做推断，避免把非唯一结果误标为主键。
     * 单表纯投影视图后续会依据该信息映射并写回基表。</p>
     */
    private List<String> inferViewPrimaryKeys(Connection connection, String schema, String viewName) {
        String definition = this.viewDefinition(schema, viewName);
        if (StringUtil.isBlank(definition)
                || Pattern.compile("(?i)\\b(JOIN|UNION|INTERSECT|EXCEPT|GROUP\\s+BY|HAVING|DISTINCT)\\b")
                .matcher(definition).find()) {
            return List.of();
        }
        int selectIndex = H2GeneratorUtil.keywordIndex(definition, "SELECT", 0);
        int fromIndex = H2GeneratorUtil.keywordIndex(definition, "FROM", selectIndex + 6);
        if (selectIndex < 0 || fromIndex < 0) {
            return List.of();
        }
        String selectList = definition.substring(selectIndex + 6, fromIndex);
        String fromClause = definition.substring(fromIndex + 4).trim();
        String tableClause = fromClause;
        for (String keyword : List.of("WHERE", "ORDER BY", "LIMIT", "OFFSET", "FETCH")) {
            int index = H2GeneratorUtil.keywordIndex(tableClause, keyword, 0);
            if (index >= 0) {
                tableClause = tableClause.substring(0, index).trim();
            }
        }
        if (tableClause.contains(",")) {
            return List.of();
        }
        String tableName = viewSourceTable(tableClause);
        if (StringUtil.isBlank(tableName)) {
            return List.of();
        }
        List<String> sourcePrimaryKeys = this.selectPrimaryKeys(connection, schema, tableName);
        if (sourcePrimaryKeys.isEmpty()) {
            return List.of();
        }
        List<String> viewPrimaryKeys = new ArrayList<>();
        for (String expression : splitTopLevel(selectList, ',')) {
            String sourceExpression = expression.trim();
            String outputName = null;
            Matcher aliasMatcher = Pattern.compile(
                    "(?is)^(.*?)\\s+AS\\s+(\"(?:[^\"]|\"\")*\"|[^\\s]+)\\s*$")
                    .matcher(sourceExpression);
            if (aliasMatcher.matches()) {
                sourceExpression = aliasMatcher.group(1).trim();
                outputName = lastIdentifier(aliasMatcher.group(2));
            }
            String sourceColumn = simpleColumnName(sourceExpression);
            if (sourceColumn == null || sourcePrimaryKeys.stream()
                    .noneMatch(item -> StringUtil.equalsIgnoreCase(item, sourceColumn))) {
                continue;
            }
            if (outputName == null) {
                outputName = sourceColumn;
            }
            if (StringUtil.isNotBlank(outputName) && !viewPrimaryKeys.contains(outputName)) {
                viewPrimaryKeys.add(outputName);
            }
        }
        return viewPrimaryKeys;
    }

    /**
     * 解析单表、纯字段投影视图到基表的字段映射。
     * 只有完整映射且包含基表主键的视图才允许写回。
     */
    private H2ViewUpdateMapping resolveViewUpdateMapping(Connection connection, String schema,
                                                         String viewName, List<H2Column> viewColumns) {
        if (viewColumns == null || viewColumns.isEmpty()) {
            return null;
        }
        String definition = this.viewDefinition(schema, viewName);
        if (StringUtil.isBlank(definition)
                || Pattern.compile("(?i)\\b(JOIN|UNION|INTERSECT|EXCEPT|GROUP\\s+BY|HAVING|DISTINCT)\\b")
                .matcher(definition).find()) {
            return null;
        }
        int selectIndex = H2GeneratorUtil.keywordIndex(definition, "SELECT", 0);
        int fromIndex = H2GeneratorUtil.keywordIndex(definition, "FROM", selectIndex + 6);
        if (selectIndex < 0 || fromIndex < 0) {
            return null;
        }
        String selectList = definition.substring(selectIndex + 6, fromIndex).trim();
        String fromClause = definition.substring(fromIndex + 4).trim();
        String tableClause = fromClause;
        for (String keyword : List.of("WHERE", "ORDER BY", "LIMIT", "OFFSET", "FETCH")) {
            int index = H2GeneratorUtil.keywordIndex(tableClause, keyword, 0);
            if (index >= 0) {
                tableClause = tableClause.substring(0, index).trim();
            }
        }
        if (tableClause.contains(",")) {
            return null;
        }
        String sourceTableName = viewSourceTable(tableClause);
        if (StringUtil.isBlank(sourceTableName)) {
            return null;
        }
        H2Table sourceTable = this.selectTableSimple(schema, sourceTableName);
        if (sourceTable == null) {
            return null;
        }
        List<H2Column> sourceColumns = this.selectColumns(schema, sourceTable.getName());
        List<String> sourcePrimaryKeys = this.selectPrimaryKeys(
                connection, schema, sourceTable.getName());
        // 当前记录编辑器按单主键定位，复合主键视图保持只读以避免误更新多行。
        if (sourcePrimaryKeys.size() != 1) {
            return null;
        }
        Map<String, String> mappings = new LinkedHashMap<>();
        Map<String, String> virtualColumns = new LinkedHashMap<>();
        List<String> expressions = splitTopLevel(selectList, ',');
        if (expressions.size() != viewColumns.size()) {
            return null;
        }
        for (int expressionIndex = 0; expressionIndex < expressions.size(); expressionIndex++) {
            String expression = expressions.get(expressionIndex);
            String sourceExpression = expression.trim();
            String outputName = null;
            Matcher aliasMatcher = Pattern.compile(
                    "(?is)^(.*?)\\s+AS\\s+(\"(?:[^\"]|\"\")*\"|[^\\s]+)\\s*$")
                    .matcher(sourceExpression);
            if (aliasMatcher.matches()) {
                sourceExpression = aliasMatcher.group(1).trim();
                outputName = lastIdentifier(aliasMatcher.group(2));
            }
            String sourceColumnName = simpleColumnName(sourceExpression);
            H2Column sourceColumn = findColumn(sourceColumns, sourceColumnName);
            if (sourceColumn != null) {
                String viewColumnName = StringUtil.blankToDefault(outputName, sourceColumn.getName());
                H2Column viewColumn = findColumn(viewColumns, viewColumnName);
                if (viewColumn == null || containsKeyIgnoreCase(mappings, viewColumn.getName())
                        || containsKeyIgnoreCase(virtualColumns, viewColumn.getName())) {
                    return null;
                }
                if (sourceColumn.isGenerated()) {
                    // 基表生成列虽然以简单字段形式投影，仍然不可写。
                    virtualColumns.put(viewColumn.getName(),
                            sourceColumn.getGeneratedExpression());
                } else if (containsValueIgnoreCase(mappings, sourceColumn.getName())) {
                    return null;
                } else {
                    mappings.put(viewColumn.getName(), sourceColumn.getName());
                }
            } else {
                // 表达式列保留展示，但不参与写回。
                String viewColumnName = StringUtil.blankToDefault(
                        outputName, viewColumns.get(expressionIndex).getName());
                H2Column viewColumn = findColumn(viewColumns, viewColumnName);
                if (viewColumn == null || containsKeyIgnoreCase(mappings, viewColumn.getName())
                        || containsKeyIgnoreCase(virtualColumns, viewColumn.getName())) {
                    return null;
                }
                virtualColumns.put(viewColumn.getName(), sourceExpression);
            }
        }
        if (mappings.size() + virtualColumns.size() != viewColumns.size()) {
            return null;
        }
        boolean mappedPrimaryKey = sourcePrimaryKeys.stream().allMatch(primaryKey ->
                containsValueIgnoreCase(mappings, primaryKey));
        return mappedPrimaryKey
                ? new H2ViewUpdateMapping(sourceTable.getName(), mappings, virtualColumns)
                : null;
    }

    private static boolean containsKeyIgnoreCase(Map<String, String> values, String key) {
        return values.keySet().stream()
                .anyMatch(item -> StringUtil.equalsIgnoreCase(item, key));
    }

    private static boolean containsValueIgnoreCase(Map<String, String> values, String value) {
        return values.values().stream()
                .anyMatch(item -> StringUtil.equalsIgnoreCase(item, value));
    }

    private static H2Column findColumn(List<H2Column> columns, String name) {
        if (columns == null || StringUtil.isBlank(name)) {
            return null;
        }
        return columns.stream()
                .filter(column -> StringUtil.equalsIgnoreCase(column.getName(), name))
                .findFirst()
                .orElse(null);
    }

    private static String viewSourceTable(String fromClause) {
        Matcher matcher = Pattern.compile(
                "^(?:\"(?:[^\"]|\"\")*\"|`[^`]*`|\\[[^\\]]*\\]|[^\\s.]+)"
                        + "(?:\\s*\\.\\s*(?:\"(?:[^\"]|\"\")*\"|`[^`]*`|\\[[^\\]]*\\]|[^\\s.]+))*",
                Pattern.CASE_INSENSITIVE).matcher(fromClause);
        return matcher.find() ? lastIdentifier(matcher.group()) : null;
    }

    private static String simpleColumnName(String expression) {
        Matcher matcher = Pattern.compile(
                "^(?:\"(?:[^\"]|\"\")*\"|`[^`]*`|\\[[^\\]]*\\]|[A-Za-z0-9_$]+)"
                        + "(?:\\s*\\.\\s*(?:\"(?:[^\"]|\"\")*\"|`[^`]*`|\\[[^\\]]*\\]|[A-Za-z0-9_$]+))*$")
                .matcher(expression.trim());
        return matcher.matches() ? lastIdentifier(matcher.group()) : null;
    }

    private static String lastIdentifier(String value) {
        int start = 0;
        char quote = 0;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (quote == 0) {
                if (current == '"' || current == '`' || current == '[') {
                    quote = current == '[' ? ']' : current;
                } else if (current == '.') {
                    start = index + 1;
                }
            } else if (current == quote) {
                if (current != ']' && index + 1 < value.length()
                        && value.charAt(index + 1) == quote) {
                    index++;
                } else {
                    quote = 0;
                }
            }
        }
        return H2GeneratorUtil.unquote(value.substring(start).trim());
    }

    private static List<String> splitTopLevel(String value, char separator) {
        List<String> result = new ArrayList<>();
        int start = 0;
        int depth = 0;
        char quote = 0;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (quote != 0) {
                if (current == quote) {
                    if (current != ']' && index + 1 < value.length()
                            && value.charAt(index + 1) == quote) {
                        index++;
                    } else {
                        quote = 0;
                    }
                }
                continue;
            }
            if (current == '\'' || current == '"' || current == '`') {
                quote = current;
            } else if (current == '[') {
                quote = ']';
            } else if (current == '(') {
                depth++;
            } else if (current == ')') {
                depth--;
            } else if (current == separator && depth == 0) {
                result.add(value.substring(start, index).trim());
                start = index + 1;
            }
        }
        result.add(value.substring(start).trim());
        return result;
    }

    /**
     * 查询字段
     *
     * @param param 参数
     * @return 字段列表
     */
    public List<H2Column> selectColumns(H2SelectColumnParam param) {
        String schema = this.schema(param.getDbName());
        String tableName = param.getTableName();
        List<H2Column> columns = this.selectColumns(schema, tableName);
        for (H2Column column : columns) {
            column.setDbName(schema);
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
    public List<H2Column> viewColumns(String dbName, String viewName) {
        H2SelectColumnParam param = new H2SelectColumnParam(dbName, viewName);
        return this.selectColumns(param);
    }

    /**
     * 查询对象定义。H2 2.x INFORMATION_SCHEMA 不提供对象 SQL 定义，
     * 表/视图按字段元数据重建 DDL，索引/触发器按元数据重建，均不可用时返回null。
     *
     * @param objectName 对象名称
     * @return SQL定义
     */
    public String showCreate(String objectName) {
        return this.showCreate(this.schema(), objectName);
    }

    /**
     * 查询指定模式中的对象定义。
     *
     * @param dbName     模式名称
     * @param objectName 对象名称
     * @return SQL 定义
     */
    public String showCreate(String dbName, String objectName) {
        String schema = this.schema(dbName);
        objectName = this.canonicalObjectName(schema, objectName);
        H2Table table = this.selectTableSimple(schema, objectName);
        if (table != null) {
            table.setColumns(this.selectColumns(schema, objectName));
            String scriptDefinition = this.scriptTableDefinition(schema, table.getName());
            if (StringUtil.isNotBlank(scriptDefinition)) {
                return scriptDefinition;
            }
            return H2TableCreateSqlGenerator.createTable(table.getName(), table.getColumns(), null);
        }
        H2View view = this.selectViewSimple(schema, objectName);
        if (view != null) {
            String definition = this.viewDefinition(schema, view.getName());
            return StringUtil.isBlank(definition)
                    ? null : H2ViewCreateSqlGenerator.createView(view.getName(), definition);
        }
        for (H2SchemaObject object : this.selectSchemaObjectsIn(schema, "index")) {
            if (StringUtil.equalsIgnoreCase(object.getName(), objectName)) {
                return object.getSql();
            }
        }
        for (H2SchemaObject object : this.selectSchemaObjectsIn(schema, "trigger")) {
            if (StringUtil.equalsIgnoreCase(object.getName(), objectName)) {
                return object.getSql();
            }
        }
        return null;
    }

    /**
     * 使用 H2 SCRIPT 读取表、字段和约束的真实定义。
     *
     * @param tableName 表名称
     * @return 合并后的 CREATE TABLE 定义，不可用时返回 null
     */
    private String scriptTableDefinition(String schema, String tableName) {
        String sql = "SCRIPT NODATA NOPASSWORDS NOSETTINGS TABLE "
                + H2GeneratorUtil.qualified(schema, tableName);
        String createTable = null;
        List<String> constraints = new ArrayList<>();
        try {
            Connection connection = this.getConnManager().connection();
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    String value = resultSet.getString(1);
                    if (StringUtil.isBlank(value)) {
                        continue;
                    }
                    String trimmed = value.strip();
                    String upper = trimmed.toUpperCase(Locale.ROOT);
                    if (createTable == null && upper.startsWith("CREATE ")
                            && upper.contains(" TABLE ")) {
                        createTable = stripStatementTerminator(trimmed);
                    } else if (upper.startsWith("ALTER TABLE ")
                            && upper.contains(" ADD CONSTRAINT ")) {
                        int index = upper.indexOf(" ADD ");
                        constraints.add(stripStatementTerminator(stripConstraintSchema(
                                trimmed.substring(index + " ADD ".length()))));
                    }
                }
            }
        } catch (Exception ex) {
            JulLog.debug("Load H2 table script failed: {}", ex.getMessage());
            return null;
        }
        if (createTable == null) {
            return null;
        }
        if (constraints.isEmpty()) {
            return createTable;
        }
        int bodyEnd = createTable.lastIndexOf(')');
        if (bodyEnd < 0) {
            return createTable;
        }
        String indentation = "\n    ";
        return createTable.substring(0, bodyEnd).stripTrailing()
                + ","
                + indentation
                + String.join("," + indentation, constraints)
                + createTable.substring(bodyEnd);
    }

    private static String stripStatementTerminator(String sql) {
        String value = sql.strip();
        while (value.endsWith(";")) {
            value = value.substring(0, value.length() - 1).stripTrailing();
        }
        return value;
    }

    /**
     * SCRIPT 生成的约束名可能带 schema 前缀，CREATE TABLE 内部约束名不能带前缀。
     *
     * @param constraint CONSTRAINT 定义
     * @return 去除约束名 schema 前缀后的定义
     */
    private static String stripConstraintSchema(String constraint) {
        String prefix = "CONSTRAINT ";
        if (!constraint.regionMatches(true, 0, prefix, 0, prefix.length())) {
            return constraint;
        }
        int start = prefix.length();
        int end = start;
        char quote = 0;
        while (end < constraint.length()) {
            char value = constraint.charAt(end);
            if (quote == 0) {
                if (Character.isWhitespace(value)) {
                    break;
                }
                if (value == '"' || value == '`' || value == '[') {
                    quote = value == '[' ? ']' : value;
                }
            } else if (value == quote) {
                if (value != ']' && end + 1 < constraint.length()
                        && constraint.charAt(end + 1) == quote) {
                    end++;
                } else {
                    quote = 0;
                }
            }
            end++;
        }
        String qualifiedName = constraint.substring(start, end);
        String name = lastQualifiedIdentifier(qualifiedName);
        return prefix + name + constraint.substring(end);
    }

    private static String lastQualifiedIdentifier(String value) {
        List<String> identifiers = new ArrayList<>();
        int start = 0;
        char quote = 0;
        for (int index = 0; index <= value.length(); index++) {
            char current = index < value.length() ? value.charAt(index) : '.';
            if (quote == 0) {
                if (current == '"' || current == '`' || current == '[') {
                    quote = current == '[' ? ']' : current;
                } else if (current == '.') {
                    identifiers.add(value.substring(start, index).trim());
                    start = index + 1;
                }
            } else if (current == quote) {
                if (current != ']' && index + 1 < value.length()
                        && value.charAt(index + 1) == quote) {
                    index++;
                } else {
                    quote = 0;
                }
            }
        }
        return identifiers.isEmpty()
                ? value : identifiers.getLast();
    }

    /**
     * 查询记录数量。
     *
     * @param objectName 对象名称
     * @return 数量
     */
    public long selectRowCount(String objectName) {
        return this.queryLong("SELECT COUNT(*) FROM " + ShellH2Helper.quote(this.canonicalObjectName(objectName)));
    }

    /**
     * 查询记录数量。
     *
     * @param objectName 对象名称
     * @param filter     过滤条件
     * @return 数量
     */
    public long selectRowCount(String objectName, String filter) {
        return this.queryLong("SELECT COUNT(*) FROM " + ShellH2Helper.quote(this.canonicalObjectName(objectName))
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
        H2Table table = this.selectTable(dbName, tableName);
        if (table == null) {
            return null;
        }
        table.setColumns(this.selectColumns(tableName));
        return H2TableCreateSqlGenerator.createTable(table.getName(), table.getColumns(), null);
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
        return this.selectColumns(dbName, tableName).stream()
                .filter(H2Column::isPrimaryKey)
                .map(H2Column::getName)
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
        return "UTF-8";
    }

    /**
     * 创建表
     *
     * @param param 参数
     */
    public void createTable(H2CreateTableParam param) {
        List<String> sqlList = H2TableCreateSqlGenerator.generateSql(param);
        for (String sql : sqlList) {
            this.executeSqlSimple(sql);
        }
        H2Table table = param.getTable();
        for (String commentSql : H2TableCreateSqlGenerator.commentSql(
                table.getDbName(), table.getName(), table.getComment(), table.getColumns())) {
            this.executeSqlSimple(commentSql);
        }
    }

    /**
     * 修改表。H2 支持标准 ALTER TABLE，使用 ALTER 序列更新。
     *
     * @param param 参数
     */
    public void alertTable(H2AlertTableParam param) {
        String sql = H2TableAlertSqlGenerator.generateSqlSingle(param);
        if (StringUtil.isBlank(sql)) {
            return;
        }
        this.executeSqlSimple(sql);
    }

    /**
     * 创建视图
     *
     * @param param 参数
     */
    public void createView(H2CreateViewParam param) {
        this.executeSqlSimple(H2ViewCreateSqlGenerator.generateSqlSingle(param));
    }

    /**
     * 修改视图
     *
     * @param param 参数
     */
    public void alertView(H2AlertViewParam param) {
        this.executeSqlSimple(H2ViewAlertSqlGenerator.generateSqlSingle(param));
    }

    /**
     * 重命名表。H2 的 ALTER TABLE RENAME TO 会自动维护索引等依赖。
     *
     * @param oldName 原表名
     * @param newName 新表名
     */
    public void renameTable(String oldName, String newName) {
        this.renameTable(this.schema(), oldName, newName);
    }

    /**
     * 重命名表。
     *
     * @param dbName  库名称
     * @param oldName 原表名
     * @param newName 新表名
     */
    public void renameTable(String dbName, String oldName, String newName) {
        String schema = this.schema(dbName);
        oldName = this.requireExistingObject(schema, oldName, "table");
        this.requireAvailableName(schema, newName, oldName);
        this.executeSqlSimple("ALTER TABLE " + H2GeneratorUtil.qualified(schema, oldName)
                + " RENAME TO " + H2GeneratorUtil.quote(newName));
    }

    /**
     * 清空表数据。
     *
     * @param tableName 表名
     */
    public void clearTable(String tableName) {
        this.clearTable(this.schema(), tableName);
    }

    /**
     * 清空表数据。
     *
     * @param dbName    库名称
     * @param tableName 表名
     */
    public void clearTable(String dbName, String tableName) {
        String schema = this.schema(dbName);
        tableName = this.requireExistingObject(schema, tableName, "table");
        this.executeSqlSimple("DELETE FROM " + H2GeneratorUtil.qualified(schema, tableName));
    }

    /**
     * 清空表数据。
     *
     * @param tableName 表名
     */
    public void truncateTable(String tableName) {
        this.truncateTable(this.schema(), tableName);
    }

    /**
     * 清空表数据。
     *
     * @param dbName    库名称
     * @param tableName 表名
     */
    public void truncateTable(String dbName, String tableName) {
        String schema = this.schema(dbName);
        tableName = this.requireExistingObject(schema, tableName, "table");
        this.executeSqlSimple("TRUNCATE TABLE " + H2GeneratorUtil.qualified(schema, tableName));
    }

    /**
     * 删除表。
     *
     * @param tableName 表名
     */
    public void dropTable(String tableName) {
        this.dropTable(this.schema(), tableName);
    }

    /**
     * 删除表。
     *
     * @param dbName    库名称
     * @param tableName 表名
     */
    public void dropTable(String dbName, String tableName) {
        String schema = this.schema(dbName);
        tableName = this.requireExistingObject(schema, tableName, "table");
        this.executeSqlSimple("DROP TABLE " + H2GeneratorUtil.qualified(schema, tableName));
    }

    /**
     * 克隆表。
     *
     * @param sourceName     源表名
     * @param targetName     目标表名
     * @param includeRecords 是否复制数据
     */
    public void cloneTable(String sourceName, String targetName, boolean includeRecords) {
        sourceName = this.requireExistingObject(this.schema(), sourceName, "table");
        new H2SchemaManager(this).cloneTable(sourceName, targetName, includeRecords);
    }

    /**
     * 重命名视图。
     *
     * @param oldName 原视图名
     * @param newName 新视图名
     */
    public void renameView(String oldName, String newName) {
        this.renameView(this.schema(), oldName, newName);
    }

    /**
     * 重命名视图。
     *
     * @param dbName  库名称
     * @param oldName 原视图名
     * @param newName 新视图名
     */
    public void renameView(String dbName, String oldName, String newName) {
        String schema = this.schema(dbName);
        oldName = this.requireExistingObject(schema, oldName, "view");
        this.executeSqlSimple("ALTER TABLE " + H2GeneratorUtil.qualified(schema, oldName)
                + " RENAME TO " + H2GeneratorUtil.quote(newName));
    }

    /**
     * 删除视图。
     *
     * @param viewName 视图名
     */
    public void dropView(String viewName) {
        String schema = this.schema();
        viewName = this.requireExistingObject(schema, viewName, "view");
        this.executeSqlSimple("DROP VIEW " + H2GeneratorUtil.qualified(schema, viewName));
    }

    /**
     * 删除视图。
     *
     * @param dbName 库名称
     * @param view   视图
     */
    public void dropView(String dbName, H2View view) {
        String schema = this.schema(dbName);
        String viewName = this.requireExistingObject(schema, view.getName(), "view");
        this.executeSqlSimple("DROP VIEW " + H2GeneratorUtil.qualified(schema, viewName));
    }

    /**
     * 克隆视图。
     *
     * @param sourceName 源视图名
     * @param targetName 目标视图名
     */
    public void cloneView(String sourceName, String targetName) {
        sourceName = this.requireExistingObject(this.schema(), sourceName, "view");
        new H2SchemaManager(this).cloneView(sourceName, targetName);
    }

    private String requireExistingObject(String schema, String name, String type) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("H2 " + type + " name is empty");
        }
        boolean exists = this.selectSchemaObjectsIn(schema, type).stream()
                .anyMatch(item -> StringUtil.equalsIgnoreCase(item.getName(), name));
        if (!exists) {
            throw new IllegalArgumentException("H2 " + type + " does not exist: " + name);
        }
        return this.canonicalObjectName(schema, name);
    }

    private void requireAvailableName(String schema, String name, String currentName) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("H2 object name is empty");
        }
        if (StringUtil.equalsIgnoreCase(name, currentName)) {
            return;
        }
        boolean exists = this.selectSchemaObjectsIn(schema, "table", "view").stream()
                .anyMatch(item -> StringUtil.equalsIgnoreCase(item.getName(), name));
        if (exists) {
            throw new IllegalArgumentException("H2 object already exists: " + name);
        }
    }

    public DBQueryResults<H2ExecuteResult> executeSql(String sql) {
        return this.executeSql(this.schema(), sql);
    }

    /**
     * 在指定模式中执行 SQL。
     *
     * @param dbName 模式名称
     * @param sql    SQL
     * @return 执行结果
     */
    public DBQueryResults<H2ExecuteResult> executeSql(String dbName, String sql) {
        DBQueryResults<H2ExecuteResult> results = new DBQueryResults<>();
        Connection connection = null;
        Statement statement = null;
        String oldSchema = null;
        boolean oldAutoCommit = true;
        boolean transactional = true;
        boolean failed = false;
        String failedSql = null;
        try {
            this.printSql(sql);
            List<String> sqlList = DBSqlParser.parseSql(sql, this.dialect());
            connection = this.getConnManager().connection();
            oldSchema = connection.getSchema();
            String schema = this.schema(dbName);
            if (!StringUtil.equalsIgnoreCase(oldSchema, schema)) {
                connection.setSchema(schema);
            }
            oldAutoCommit = connection.getAutoCommit();
            transactional = oldAutoCommit
                    && sqlList.stream().noneMatch(ShellH2Helper::isTransactionControl);
            if (transactional) {
                connection.setAutoCommit(false);
            }
            statement = connection.createStatement();
            for (String execSql : sqlList) {
                failedSql = execSql;
                H2ExecuteResult result = new H2ExecuteResult();
                result.setContent(execSql);
                long startTime = System.nanoTime();
                try {
                    String transactionCommand = ShellH2Helper.transactionCommand(execSql);
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
                                for (H2Column column : result.getColumns()) {
                                    if (StringUtil.isBlank(column.getDbName())) {
                                        column.setDbName(schema);
                                    }
                                }
                                this.enrichExecuteResult(result, execSql, schema);
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
            this.restoreSchema(connection, oldSchema);
        }
        return results;
    }

    /**
     * 补充查询结果的可编辑信息
     *
     * @param result 查询结果
     * @param sql    SQL
     */
    private void enrichExecuteResult(H2ExecuteResult result, String sql, String schema) {
        result.setFullColumn(new DBBaseSqlParser(sql, this.dialect()).isFullColumn(sql));
        String tableName = result.tableName();
        if (StringUtil.isBlank(tableName)) {
            return;
        }
        try {
            // 只有真实表才允许按主键编辑，视图字段不参与可编辑判定。
            if (this.selectTableSimple(schema, tableName) == null) {
                return;
            }
            List<H2Column> tableColumns = this.selectColumns(
                    new H2SelectColumnParam(schema, tableName));
            result.enrichColumns(tableColumns);
        } catch (Exception ex) {
            JulLog.warn("Failed to enrich H2 query result columns: {}", ex.getMessage());
        }
    }

    /**
     * 执行 H2 查询计划分析。
     *
     * @param sql SQL
     * @return 分析结果
     */
    public DBQueryResults<H2ExecuteResult> explainSql(String sql) {
        return this.explainSql(this.schema(), sql);
    }

    /**
     * 在指定模式中执行 H2 查询计划分析。
     *
     * @param dbName 模式名称
     * @param sql    SQL
     * @return 分析结果
     */
    public DBQueryResults<H2ExecuteResult> explainSql(String dbName, String sql) {
        DBQueryResults<H2ExecuteResult> results = new DBQueryResults<>();
        List<String> sqlList = DBSqlParser.parseSql(sql, this.dialect());
        for (String statement : sqlList) {
            if (StringUtil.isBlank(statement)) {
                continue;
            }
            String explain = statement.trim().toUpperCase(Locale.ROOT).startsWith("EXPLAIN")
                    ? statement : "EXPLAIN PLAN FOR " + statement;
            DBQueryResults<H2ExecuteResult> one = this.executeSql(dbName, explain);
            if (one.getResults() != null) {
                for (H2ExecuteResult result : one.getResults()) {
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
                    && sqlList.stream().noneMatch(ShellH2Helper::isTransactionControl);
            if (transactional) {
                connection.setAutoCommit(false);
            }
            for (String execSql : sqlList) {
                Statement statement = connection.createStatement();
                String transactionCommand = ShellH2Helper.transactionCommand(execSql);
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
        String oldSchema = null;
        boolean oldAutoCommit = true;
        int result = 0;
        try {
            connection = this.getConnManager().connection();
            oldSchema = connection.getSchema();
            String schema = this.schema(dbName);
            if (!StringUtil.equalsIgnoreCase(oldSchema, schema)) {
                connection.setSchema(schema);
            }
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
            this.restoreSchema(connection, oldSchema);
        }
    }

    /**
     * 恢复连接当前模式。
     *
     * @param connection 连接
     * @param schema     模式
     */
    private void restoreSchema(Connection connection, String schema) {
        if (connection == null || StringUtil.isBlank(schema)) {
            return;
        }
        try {
            if (!StringUtil.equalsIgnoreCase(connection.getSchema(), schema)) {
                connection.setSchema(schema);
            }
        } catch (SQLException ex) {
            JulLog.warn("Restore h2 schema failed: {}", ex.getMessage());
        }
    }

    /**
     * 批量执行SQL。
     *
     * @param dbName   数据库名称
     * @param sqlList  SQL列表
     * @param parallel 是否并行，当前实现固定串行执行
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

    private int queryInt(String sql, Object... params) {
        try {
            Connection connection = this.getConnManager().connection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int index = 0; index < params.length; index++) {
                    statement.setObject(index + 1, params[index]);
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt(1) : 0;
                }
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
            JulLog.warn("Restore h2 auto commit failed", ex);
        }
    }

    private void registerStateListener() {
        if (!this.stateListenerRegistered) {
            this.addStateListener(this.stateListener);
            this.stateListenerRegistered = true;
        }
    }

}
