package cn.oyzh.easyshell.h2.schema;

import cn.oyzh.fx.db.DBSchema;

/**
 * H2 模式。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class H2Schema implements DBSchema {

    private String name;

    private String owner;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
