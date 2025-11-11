// package cn.oyzh.easyshell.fx.mysql.data;
//
// import cn.oyzh.fx.plus.controls.combo.FXComboBox;
// import cn.oyzh.fx.plus.converter.SimpleStringConverter;
//
// /**
//  * @author oyzh
//  * @since 2024/8/27
//  */
// public class DataImportTableComboBox extends FXComboBox<ShellMysqlDataImportFile> {
//
//     {
//         this.setConverter(new SimpleStringConverter<>() {
//             @Override
//             public String toString(ShellMysqlDataImportFile object) {
//                 if (object != null) {
//                     return object.getTableName();
//                 }
//                 return null;
//             }
//         });
//     }
//
//     public String getSelectedTableName() {
//         return this.getSelectedItem().getTableName();
//     }
// }
