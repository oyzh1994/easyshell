//package cn.oyzh.easyshell.data.sqlserver.ui;
//
//import cn.oyzh.easyshell.data.sqlserver.dto.ShellSqlServerDataExportTable;
//import cn.oyzh.fx.plus.controls.combo.FXComboBox;
//import cn.oyzh.fx.plus.converter.SimpleStringConverter;
//
///**
// * @author oyzh
// * @since 2024/8/27
// */
//public class ShellSqlServerDataExportTableComboBox extends FXComboBox<ShellSqlServerDataExportTable> {
//
//    @Override
//   public void initNode(){
//        this.setConverter(new SimpleStringConverter<>() {
//            @Override
//            public String toString(ShellSqlServerDataExportTable object) {
//                if (object != null) {
//                    return object.getName();
//                }
//                return null;
//            }
//        });
//        super.initNode();
//    }
//}
