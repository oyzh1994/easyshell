package cn.oyzh.easyshell.fx.mysql;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2024/01/26
 */
public class DBCollationComboBox extends FXComboBox<String> {

    public void init(String charset, ShellMysqlClient client) {
        if (charset == null) {
            return;
        }
        String aCharset = this.getProp("charset");
        if (!StringUtil.equalsIgnoreCase(charset, aCharset)) {
            this.setProp("charset", charset);
            this.clearItems();
            for (String collation : client.collation(charset)) {
                this.addItem(collation.toUpperCase());
            }
        }
    }

    @Override
    public void select(String obj) {
        if (obj != null) {
            super.select(obj.toUpperCase());
        }
    }
}
