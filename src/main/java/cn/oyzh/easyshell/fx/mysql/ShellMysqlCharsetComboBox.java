package cn.oyzh.easyshell.fx.mysql;

import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * MySQL字符集下拉框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlCharsetComboBox extends FXComboBox<String> {

    /**
     * 初始化字符集列表
     *
     * @param client MySQL客户端
     */
    public void init(ShellMysqlClient client) {
        this.clearItems();
        // 空数据
        this.addItem("");
        // 正常数据
        for (String charset : client.charsets()) {
            this.addItem(charset.toUpperCase());
        }
    }

    @Override
    public void select(String obj) {
        if (obj != null) {
            super.select(obj.toUpperCase());
        } else {
            super.clearSelection();
        }
    }
}
