//package cn.oyzh.easyshell.data.postgresql.ui;
//
//import cn.oyzh.easyshell.data.postgresql.dto.ShellPostgresqlDataImportFile;
//import cn.oyzh.fx.plus.controls.combo.FXComboBox;
//import cn.oyzh.fx.plus.converter.SimpleStringConverter;
//
///**
// * @author oyzh
// * @since 2024/8/27
// */
//public class ShellDataImportTableComboBox extends FXComboBox<ShellPostgresqlDataImportFile> {
//
//    {
//        this.setConverter(new SimpleStringConverter<>() {
//            @Override
//            public String toString(ShellPostgresqlDataImportFile object) {
//                if (object != null) {
//                    return object.getTableName();
//                }
//                return null;
//            }
//        });
//    }
//
//    public String getSelectedTableName() {
//        return this.getSelectedItem().getTableName();
//    }
//}
