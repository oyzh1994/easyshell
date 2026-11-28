package cn.oyzh.easyshell.h2.index;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.schema.H2SchemaManager;
import cn.oyzh.easyshell.h2.schema.H2SchemaObject;
import cn.oyzh.fx.db.DBIndex;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.regex.Pattern;

/**
 * H2 索引。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2Index extends DBObject implements DBIndex, ObjectCopier<H2Index> {

    private static final Pattern UNIQUE_INDEX = Pattern.compile(
            "(?is)^\\s*CREATE\\s+UNIQUE\\s+(?:(?:HASH|SPATIAL)\\s+)?INDEX\\b");

    private String name;

    private String originalName;

    private String definition;

    private String originalDefinition;

    private String tableName;

    private String createDefinition;

    public H2Index() {
    }

    public H2Index(String name) {
        this.setName(name);
    }

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

    @Override
    public boolean isUnique() {
        return UNIQUE_INDEX.matcher(
                StringUtil.blankToDefault(this.definition, "")).find();
    }

    @Override
    public void setUnique(boolean unique) {
        String sql = StringUtil.blankToDefault(this.definition, "").trim();
        if (sql.isEmpty()) {
            return;
        }
        if (unique) {
            if (!this.isUnique()) {
                int index = sql.toUpperCase().indexOf("INDEX");
                if (index >= 0) {
                    sql = sql.substring(0, index) + "UNIQUE " + sql.substring(index);
                }
            }
        } else {
            sql = sql.replaceFirst("(?is)^(\\s*CREATE\\s+)UNIQUE\\s+(INDEX\\b)", "$1$2");
        }
        this.setDefinition(sql);
    }

    public boolean isChangedDefinition() {
        return !StringUtil.equals(this.originalDefinition, this.definition)
                || !StringUtil.equals(this.originalName(), this.name);
    }

    public boolean isInvalid() {
        return StringUtil.isBlank(this.name) || StringUtil.isBlank(this.definition);
    }

    public String normalizedDefinition(String previousTable, String targetTable) {
        String sql = H2SchemaManager.replaceCreateObjectName(this.definition.trim(), this.name);
        String oldTable = StringUtil.blankToDefault(this.tableName, previousTable);
        if (StringUtil.isNotBlank(oldTable) && StringUtil.isNotBlank(targetTable)) {
            sql = H2SchemaManager.replaceTableReference(sql, oldTable, targetTable);
        }
        return sql;
    }

    @Override
    public void copy(H2Index index) {
        if (index != null) {
            this.setName(index.getName());
            this.setOriginalName(index.originalName());
            this.setDefinition(index.getDefinition());
            this.setOriginalDefinition(index.getOriginalDefinition());
            this.setTableName(index.getTableName());
            this.setCreateDefinition(index.getCreateDefinition());
        }
    }

    public static H2Index of(H2SchemaObject object) {
        H2Index index = new H2Index();
        index.setName(object.getName());
        index.setOriginalName(object.getName());
        index.setDefinition(object.getSql());
        index.setOriginalDefinition(object.getSql());
        index.setTableName(object.getTableName());
        return index;
    }

    public static H2Index newIndex(String tableName, String columnName) {
        H2Index index = new H2Index();
        index.setName(DBUtil.genIndexName());
        index.setDefinition("CREATE INDEX " + H2GeneratorUtil.quote(index.getName())
                + " ON " + H2GeneratorUtil.quote(tableName) + " ("
                + (StringUtil.isBlank(columnName) ? "" : H2GeneratorUtil.quote(columnName)) + ");");
        index.setTableName(tableName);
        index.setCreated(true);
        return index;
    }
}
