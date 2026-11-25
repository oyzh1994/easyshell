package cn.oyzh.easyshell.sqlserver.type;

import cn.oyzh.fx.db.DBTypeDefinition;

/**
 * SqlServer用户自定义类型定义
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class SqlServerTypeDefinition implements DBTypeDefinition {

    public static final String OBJECT_TYPE_ALIAS = "ALIAS";

    public static final String OBJECT_TYPE_TABLE = "TABLE";

    /**
     * 类型名称
     */
    private String name;

    /**
     * 对象类型，ALIAS/TABLE
     */
    private String objectType;

    /**
     * 创建语句
     */
    private String createDefinition;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getObjectType() {
        return objectType;
    }

    @Override
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    @Override
    public String getCreateDefinition() {
        return createDefinition;
    }

    @Override
    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
    }
}
