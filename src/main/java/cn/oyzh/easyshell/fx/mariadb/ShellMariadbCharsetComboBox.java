package cn.oyzh.easyshell.fx.mariadb;

import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * MariaDB字符集下拉框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbCharsetComboBox extends FXComboBox<String> {

    /**
     * 初始化字符集列表
     *
     * @param client MariaDB客户端
     */
    public void init(ShellMariadbClient client) {
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
