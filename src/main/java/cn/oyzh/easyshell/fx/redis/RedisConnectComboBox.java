// package cn.oyzh.easyshell.fx.redis;
//
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.store.redis.RedisConnectStore;
// import cn.oyzh.fx.plus.controls.combo.FXComboBox;
// import cn.oyzh.fx.plus.converter.SimpleStringConverter;
//
// /**
//  * redis连接选择框
//  *
//  * @author oyzh
//  * @since 2023/07/20
//  */
// public class RedisConnectComboBox extends FXComboBox<ShellConnect> {
//
//     {
//         this.setConverter(new SimpleStringConverter<>() {
//             @Override
//             public String toString(ShellConnect o) {
//                 if (o == null) {
//                     return "";
//                 }
//                 return o.getName();
//             }
//         });
//         this.getItems().setAll(RedisConnectStore.INSTANCE.load());
//     }
// }
