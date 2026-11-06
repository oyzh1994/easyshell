//package cn.oyzh.easyshell.data.postgresql.ui;
//
//import cn.oyzh.easyshell.data.postgresql.dto.ShellPostgresqlDataExportTable;
//import cn.oyzh.fx.plus.controls.combo.FXComboBox;
//import cn.oyzh.fx.plus.converter.SimpleStringConverter;
//
///**
// * @author oyzh
// * @since 2024/8/27
// */
//public class ShellPostgresqlDataExportTableComboBox extends FXComboBox<ShellPostgresqlDataExportTable> {
//
//    @Override
//   public void initNode(){
//        this.setConverter(new SimpleStringConverter<>() {
//            @Override
//            public String toString(ShellPostgresqlDataExportTable object) {
//                if (object != null) {
//                    return object.getName();
//                }
//                return null;
//            }
//        });
//        super.initNode();
//    }
//}
