//package cn.oyzh.easyshell.data.mysql.ui;
//
//import cn.oyzh.easyshell.data.mysql.dto.ShellMysqlDataExportTable;
//import cn.oyzh.fx.plus.controls.combo.FXComboBox;
//import cn.oyzh.fx.plus.converter.SimpleStringConverter;
//
///**
// * @author oyzh
// * @since 2024/8/27
// */
//public class ShellMysqlDataExportTableComboBox extends FXComboBox<ShellMysqlDataExportTable> {
//
//    @Override
//    public void initNode() {
//        this.setConverter(new SimpleStringConverter<>() {
//            @Override
//            public String toString(ShellMysqlDataExportTable object) {
//                if (object != null) {
//                    return object.getName();
//                }
//                return null;
//            }
//        });
//        super.initNode();
//    }
//}
