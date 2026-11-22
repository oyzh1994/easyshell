//package cn.oyzh.easyshell.data.sqlserver.ui;
//
//import cn.oyzh.easyshell.data.sqlserver.dto.ShellSqlServerDataImportFile;
//import cn.oyzh.fx.plus.controls.combo.FXComboBox;
//import cn.oyzh.fx.plus.converter.SimpleStringConverter;
//
///**
// * @author oyzh
// * @since 2024/8/27
// */
//public class ShellDataImportTableComboBox extends FXComboBox<ShellSqlServerDataImportFile> {
//
//    {
//        this.setConverter(new SimpleStringConverter<>() {
//            @Override
//            public String toString(ShellSqlServerDataImportFile object) {
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
