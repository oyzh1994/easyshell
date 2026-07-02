package cn.oyzh.easyshell.mongo.user;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;

import java.util.List;

/**
 *
 * @author oyzh
 * @since 2026-07-02
 */
public class MongoUser implements ObjectCopier<MongoUser>, ObjectComparator<MongoUser> {

    private String db;

    private String user;

    private String password;

    private List<MongoUserRole> roles;

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<MongoUserRole> getRoles() {
        return roles;
    }

    public void setRoles(List<MongoUserRole> roles) {
        this.roles = roles;
    }

    @Override
    public void copy(MongoUser t1) {
        if (t1 != null) {
            this.setRoles(t1.getRoles());
            this.setPassword(t1.getPassword());
        }
    }

    @Override
    public boolean compare(MongoUser t1) {
        if (t1 == null) {
            return false;
        }
        if (t1 == this) {
            return true;
        }
        if (!StringUtil.equals(this.getDb(), t1.getDb())) {
            return false;
        }
        return StringUtil.equals(this.getUser(), t1.getUser());
    }
}
