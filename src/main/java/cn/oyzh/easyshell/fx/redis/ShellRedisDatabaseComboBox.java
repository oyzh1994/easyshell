package cn.oyzh.easyshell.fx.redis;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * redis数据库选择框
 *
 * @author oyzh
 * @since 2023/07/07
 */
public class ShellRedisDatabaseComboBox extends FXComboBox<String> {

    /**
     * 获取数据库数量
     *
     * @return 数据库数量
     */
    public Integer getDbCount() {
        return dbCount;
    }

    /**
     * 数据库数量
     */
    private Integer dbCount;

    /**
     * 设置数据库数量
     *
     * @param dbCount 数据库数量
     */
    public void setDbCount(Integer dbCount) {
        this.dbCount = dbCount;
        if (dbCount == null) {
            this.addItem(I18nHelper.allDatabase());
        } else {
            for (int i = 0; i < dbCount; i++) {
                this.addDB(i);
            }
        }
    }

    /**
     * 新增数据库项
     *
     * @param dbIndex 数据库索引
     */
    public void addDB(int dbIndex) {
        this.addItem("db" + dbIndex);
    }

    /**
     * 获取当前数据库索引
     *
     * @return 当前数据库索引
     */
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
