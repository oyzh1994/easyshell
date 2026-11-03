package cn.oyzh.easyshell.sqlite.function;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;

/**
 * SQLite 函数定义。
 *
 * <p>SQLite 不支持服务端持久化存储函数。该模型同时表达内置/扩展函数元数据，
 * 以及 EasyShell 提供的生成列和表函数视图设计模板。</p>
 *
 * @since 2026-11-02
 */
public class SQLiteFunction {

    public static final String KIND_BUILTIN = "BUILTIN";

    public static final String KIND_GENERATED_COLUMN = "GENERATED_COLUMN";

    public static final String KIND_TABLE_VIEW = "TABLE_VIEW";

    public static final String KIND_SQL_EXPRESSION = "SQL_EXPRESSION";

    private String dbName = "main";

    private String name;

    private String kind = KIND_BUILTIN;

    private int argumentCount = -1;

    private boolean deterministic;

    private boolean directOnly;

    private boolean finalFunction;

    private String returnType;

    private String targetTable;

    private String expression;

    private String definition;

    private String generatedStorage = "VIRTUAL";

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return this.kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getArgumentCount() {
        return this.argumentCount;
    }

    public void setArgumentCount(int argumentCount) {
        this.argumentCount = argumentCount;
    }

    public boolean isDeterministic() {
        return this.deterministic;
    }

    public void setDeterministic(boolean deterministic) {
        this.deterministic = deterministic;
    }

    public boolean isDirectOnly() {
        return this.directOnly;
    }

    public void setDirectOnly(boolean directOnly) {
        this.directOnly = directOnly;
    }

    public boolean isFinalFunction() {
        return this.finalFunction;
    }

    public void setFinalFunction(boolean finalFunction) {
        this.finalFunction = finalFunction;
    }

    public String getReturnType() {
        return this.returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getTargetTable() {
        return this.targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getGeneratedStorage() {
        return this.generatedStorage;
    }

    public void setGeneratedStorage(String generatedStorage) {
        this.generatedStorage = generatedStorage;
    }

    public String getDefinition() {
        return this.definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    /**
     * 转换为生成列定义。
     *
     * @return SQLite字段
     */
    public SQLiteColumn toColumn() {
        SQLiteColumn column = new SQLiteColumn();
        column.setName(this.name);
        column.setType(this.returnType);
        column.setGeneratedExpression(this.expression);
        column.setGeneratedStorage(this.generatedStorage);
        return column;
    }
}
