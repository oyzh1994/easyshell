// package cn.oyzh.easyshell.fx.mysql;
//
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.store.ShellConnectStore;
// import cn.oyzh.fx.plus.controls.combo.FXComboBox;
// import cn.oyzh.fx.plus.converter.SimpleStringConverter;
//
// /**
//  * db连接库选择框
//  *
//  * @author oyzh
//  * @since 2024/09/05
//  */
// public class DBInfoComboBox extends FXComboBox<ShellConnect> {
//
//     {
//         this.setConverter(new SimpleStringConverter<>() {
//             @Override
//             public String toString(ShellConnect object) {
//                 if (object == null) {
//                     return null;
//                 }
//                 return object.getName();
//             }
//         });
//         this.setItem(ShellConnectStore.INSTANCE.load());
//     }
// }
