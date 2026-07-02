package cn.oyzh.easyshell.mongo.user;

/**
 *
 * @author oyzh
 * @since 2026-07-02
 */
public class MongoUserRole {

    private String db;

    private String role;

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
