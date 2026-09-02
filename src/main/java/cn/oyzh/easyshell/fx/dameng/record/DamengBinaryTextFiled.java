package cn.oyzh.easyshell.fx.dameng.record;//package cn.oyzh.easyshell.fx.dameng.record;
//
//import cn.oyzh.common.util.NumberUtil;
//import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
//
///**
// * @author oyzh
// * @since 2024/7/10
// */
//@Deprecated
//public class DamengBinaryTextFiled extends ChooseFileTextField {
//
//    private String columnType;
//
//    public DamengBinaryTextFiled(String columnType) {
//        this.columnType = columnType;
//    }
//
//    @Override
//    public void formatValue() {
//        this.setText(format(this.columnType, super.getValue()));
//    }
//
//    public static String format(String columnType, Object o) {
//        if (o instanceof byte[] bytes) {
//            return "(" + columnType + ")" + " " + NumberUtil.formatSize(bytes.length);
//        }
//        return "(" + columnType + ")";
//    }
//}
