package cn.oyzh.easyshell.fx.dameng.table;//package cn.oyzh.easyshell.fx.dameng.table;
//
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easyshell.dameng.column.DamengColumn;
//import cn.oyzh.fx.plus.controls.combo.FXComboBox;
//import cn.oyzh.fx.plus.converter.SimpleStringConverter;
//
//import java.util.List;
//
///**
// * db字段类型选择框
// *
// * @author oyzh
// * @since 2024/01/16
// */
//public class DamengColumnComboBox extends FXComboBox<DamengColumn> {
//
//    {
//        this.setConverter(new SimpleStringConverter<>() {
//            @Override
//            public String toString(DamengColumn o) {
//                if (o == null) {
//                    return "";
//                }
//                return o.getName();
//            }
//        });
//    }
//
//    public DamengColumnComboBox() {
//
//    }
//
//    public DamengColumnComboBox(List<DamengColumn> columns) {
//        this.addItems(columns);
//    }
//
//    public void select(String colName) {
//        for (DamengColumn object : this.getItems()) {
//            if (StringUtil.equalsIgnoreCase(colName, object.getName())) {
//                this.select(object);
//                break;
//            }
//        }
//    }
//
//    public String getColumnName() {
//        return this.getSelectedItem().getName();
//    }
//}
