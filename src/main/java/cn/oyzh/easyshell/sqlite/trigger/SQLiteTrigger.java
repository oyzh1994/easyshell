package cn.oyzh.easyshell.sqlite.trigger;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaManager;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaObject;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBTrigger;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQLite 触发器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteTrigger extends DBObject implements DBTrigger, ObjectCopier<SQLiteTrigger> {

    private static final Pattern POLICY = Pattern.compile(
            "(?is)^\\s*CREATE\\s+(?:TEMP(?:ORARY)?\\s+)?TRIGGER\\s+.*?\\b(BEFORE|AFTER|INSTEAD\\s+OF)\\s+"
                    + "(INSERT|UPDATE|DELETE)\\b");

    private String name;

    private String originalName;

    private String policy;

    private String definition;

    private String originalDefinition;

    private String tableName;

    private String createDefinition;

    public SQLiteTrigger() {
    }

    public SQLiteTrigger(String name) {
        this.setName(name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public String originalName() {
        return this.originalName == null
                ? (String) super.getOriginalData("name") : this.originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getPolicy() {
        return this.policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
        super.putOriginalData("policy", policy);
    }

    public void setPolicy(String timing, String manipulation) {
        this.setPolicy(timing.toUpperCase() + " " + manipulation.toUpperCase());
    }

    public String getDefinition() {
        return this.definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
        super.putOriginalData("definition", definition);
    }

    public String getOriginalDefinition() {
        return this.originalDefinition;
    }

    public void setOriginalDefinition(String originalDefinition) {
        this.originalDefinition = originalDefinition;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        super.putOriginalData("tableName", tableName);
    }

    public String getCreateDefinition() {
        return this.createDefinition;
    }

    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
    }

    public boolean isChangedDefinition() {
        return !StringUtil.equals(this.originalDefinition, this.definition)
                || !StringUtil.equals(this.originalName(), this.name);
    }

    @Override
    public boolean isInvalid() {
        return StringUtil.isBlank(this.name) || StringUtil.isBlank(this.definition);
    }

    public String normalizedDefinition(String previousTable, String targetTable) {
        String sql = SQLiteSchemaManager.replaceCreateObjectName(this.definition.trim(), this.name);
        String oldTable = StringUtil.blankToDefault(this.tableName, previousTable);
        if (StringUtil.isNotBlank(oldTable) && StringUtil.isNotBlank(targetTable)) {
            sql = SQLiteSchemaManager.replaceTableReference(sql, oldTable, targetTable);
        }
        return sql;
    }

    @Override
    public void copy(SQLiteTrigger trigger) {
        if (trigger != null) {
            this.setName(trigger.getName());
            this.setOriginalName(trigger.originalName());
            this.setPolicy(trigger.getPolicy());
            this.setDefinition(trigger.getDefinition());
            this.setOriginalDefinition(trigger.getOriginalDefinition());
            this.setTableName(trigger.getTableName());
            this.setCreateDefinition(trigger.getCreateDefinition());
        }
    }

    public static SQLiteTrigger of(SQLiteSchemaObject object) {
        SQLiteTrigger trigger = new SQLiteTrigger();
        trigger.setName(object.getName());
        trigger.setOriginalName(object.getName());
        trigger.setDefinition(object.getSql());
        trigger.setOriginalDefinition(object.getSql());
        trigger.setTableName(object.getTableName());
        trigger.setCreateDefinition(object.getSql());
        Matcher matcher = POLICY.matcher(StringUtil.blankToDefault(object.getSql(), ""));
        if (matcher.find()) {
            trigger.setPolicy(matcher.group(1).toUpperCase(Locale.ROOT).replaceAll("\\s+", " "),
                    matcher.group(2).toUpperCase(Locale.ROOT));
        }
        return trigger;
    }

    public static SQLiteTrigger newTrigger(String tableName) {
        SQLiteTrigger trigger = new SQLiteTrigger();
        trigger.setName(DBUtil.genTriggerName());
        trigger.setDefinition("CREATE TRIGGER " + SQLiteGeneratorUtil.quote(trigger.getName())
                + " AFTER INSERT ON " + SQLiteGeneratorUtil.quote(tableName)
                + " BEGIN\n    SELECT 1;\nEND;");
        trigger.setTableName(tableName);
        trigger.setCreated(true);
        return trigger;
    }
}
