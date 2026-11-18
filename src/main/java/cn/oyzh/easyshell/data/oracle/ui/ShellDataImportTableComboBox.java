//package cn.oyzh.easyshell.data.oracle.ui;
//
//import cn.oyzh.easyshell.data.oracle.dto.ShellOracleDataImportFile;
//import cn.oyzh.fx.plus.controls.combo.FXComboBox;
//import cn.oyzh.fx.plus.converter.SimpleStringConverter;
//
///**
// * @author oyzh
// * @since 2024/8/27
// */
//public class ShellDataImportTableComboBox extends FXComboBox<ShellOracleDataImportFile> {
//
//    {
//        this.setConverter(new SimpleStringConverter<>() {
//            @Override
//            public String toString(ShellOracleDataImportFile object) {
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
