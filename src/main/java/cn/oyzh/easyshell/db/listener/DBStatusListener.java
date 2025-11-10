package cn.oyzh.easyshell.db.listener;

import javafx.beans.value.ChangeListener;

import java.util.UUID;

/**
 * @author oyzh
 * @since 2024/7/23
 */
public abstract class DBStatusListener implements ChangeListener<Object> {

    private final String key;

    public DBStatusListener() {
        this.key = UUID.randomUUID().toString();
        DBStatusListenerManager.addListener(this);
    }

    public DBStatusListener(String key) {
        this.key = key;
        DBStatusListenerManager.addListener(this);
    }

    public DBStatusListener(String dbName, String tableName) {
        this(dbName + ":" + ":" + tableName);
    }

    public DBStatusListener(String dbName, String schema, String tableName) {
        this(dbName + ":" + schema + ":" + tableName);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        DBStatusListenerManager.removeListener(this);
    }

    public String getKey() {
        return key;
    }
}
