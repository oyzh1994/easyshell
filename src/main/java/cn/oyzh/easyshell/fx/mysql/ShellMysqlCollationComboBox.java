package cn.oyzh.easyshell.fx.mysql;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * MySQL排序规则下拉框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlCollationComboBox extends FXComboBox<String> {

    /**
     * 根据字符集初始化排序规则列表
     *
     * @param charset 字符集
     * @param client  MySQL客户端
     */
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
        } else {
            super.clearSelection();
        }
    }
}
