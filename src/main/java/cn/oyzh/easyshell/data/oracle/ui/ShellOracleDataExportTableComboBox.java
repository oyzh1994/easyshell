//package cn.oyzh.easyshell.data.oracle.ui;
//
//import cn.oyzh.easyshell.data.oracle.dto.ShellOracleDataExportTable;
//import cn.oyzh.fx.plus.controls.combo.FXComboBox;
//import cn.oyzh.fx.plus.converter.SimpleStringConverter;
//
///**
// * @author oyzh
// * @since 2024/8/27
// */
//public class ShellOracleDataExportTableComboBox extends FXComboBox<ShellOracleDataExportTable> {
//
//    @Override
//   public void initNode(){
//        this.setConverter(new SimpleStringConverter<>() {
//            @Override
//            public String toString(ShellOracleDataExportTable object) {
//                if (object != null) {
//                    return object.getName();
//                }
//                return null;
//            }
//        });
//        super.initNode();
//    }
//}
