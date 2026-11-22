package cn.oyzh.easyshell.sqlserver.schema;


import cn.oyzh.fx.db.DBSchema;

import java.util.Locale;
import java.util.Set;

/**
 * SqlServer数据库模式
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerSchema implements DBSchema {

    /**
     * SQL Server内置模式
     */
    private static final Set<String> SYSTEM_SCHEMAS = Set.of(
            "dbo",
            "guest",
            "information_schema",
            "sys",
            "db_owner",
            "db_accessadmin",
            "db_backupoperator",
            "db_datareader",
            "db_datawriter",
            "db_ddladmin",
            "db_denydatareader",
            "db_denydatawriter",
            "db_securityadmin"
    );

    /**
     * 模式名称
     */
    private String name;

    /**
     * 模式id
     */
    private Integer schemaId;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Integer getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(Integer schemaId) {
        this.schemaId = schemaId;
    }

    /**
     * 是否为SQL Server内置模式
     *
     * @return 内置模式返回true
     */
    public boolean isSystemSchema() {
        return this.name != null
                && SYSTEM_SCHEMAS.contains(this.name.toLowerCase(Locale.ROOT));
    }
}
