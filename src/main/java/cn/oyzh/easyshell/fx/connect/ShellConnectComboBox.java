//package cn.oyzh.easyshell.fx.connect;
//
//import cn.oyzh.easyshell.domain.ShellConnect;
//import cn.oyzh.easyshell.store.ShellConnectStore;
//import cn.oyzh.fx.plus.controls.combo.FXComboBox;
//import cn.oyzh.fx.plus.converter.SimpleStringConverter;
//
//import java.util.List;
//
///**
// * 连接下拉框
// *
// * @author oyzh
// * @since 25/03/21
// */
//public class ShellConnectComboBox extends FXComboBox<ShellConnect> {
//
//    {
//        this.setConverter(new SimpleStringConverter<>() {
//            @Override
//            public String toString(ShellConnect o) {
//                if (o == null) {
//                    return "";
//                }
//                return o.getName();
//            }
//        });
//        List<ShellConnect> connects = ShellConnectStore.INSTANCE.load();
//        this.setItem(connects);
//    }
//}
