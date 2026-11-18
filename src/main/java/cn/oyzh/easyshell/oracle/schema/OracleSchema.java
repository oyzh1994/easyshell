package cn.oyzh.easyshell.oracle.schema;


import cn.oyzh.fx.db.DBSchema;

/**
 * Oracle数据库模式
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleSchema implements DBSchema {

    /**
     * 模式名称
     */
    private String name;

    /** 新建用户密码或修改后的用户密码 */
    private String password;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
