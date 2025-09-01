package cn.oyzh.easyshell.fx.redis;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * redis数据库选择框
 *
 * @author oyzh
 * @since 2023/07/07
 */
public class RedisDatabaseComboBox extends FXComboBox<String> {

    public int getDbCount() {
        return dbCount;
    }

    private int dbCount;

    public void setDbCount(int dbCount) {
        this.dbCount = dbCount;
        for (int i = 0; i < dbCount; i++) {
            this.addDB(i);
        }
    }

    public void addDB(int dbIndex) {
        this.addItem("db" + dbIndex);
    }

    public int getDB() {
        String val = this.getValue();
        if (val == null || !val.contains("db")) {
            return -1;
        }
        val = val.replace("db", "");
        if (val.isEmpty()) {
            return -1;
        }
        return Integer.parseInt(val);
    }
}
