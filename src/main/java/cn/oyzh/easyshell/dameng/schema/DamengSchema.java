package cn.oyzh.easyshell.dameng.schema;


import cn.oyzh.fx.db.DBSchema;

/**
 * @author oyzh
 * @since 2024/1/30
 */
public class DamengSchema implements DBSchema {

    /**
     * 模式名称
     */
    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
