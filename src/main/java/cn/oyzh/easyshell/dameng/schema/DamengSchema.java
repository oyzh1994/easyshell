package cn.oyzh.easyshell.dameng.schema;


import cn.oyzh.fx.db.DBSchema;

/**
 * 达梦数据库模式
 *
 * @author oyzh
 * @since 2026-09-02
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
