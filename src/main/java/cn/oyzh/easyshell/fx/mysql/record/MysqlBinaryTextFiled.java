package cn.oyzh.easyshell.fx.mysql.record;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;

/**
 * @author oyzh
 * @since 2024/7/10
 */
public class MysqlBinaryTextFiled extends ChooseFileTextField {

    private String columnType;

    public MysqlBinaryTextFiled(String columnType) {
        this.columnType = columnType;
    }

    @Override
    public void setValue(Object val) {
        super.setValue(format(this.columnType, val));
    }

    public static String format(String columnType, Object o) {
        if (o instanceof byte[] bytes) {
            return "(" + columnType + ")" + " " + NumberUtil.formatSize(bytes.length);
        }
        return "(" + columnType + ")";
    }
}
