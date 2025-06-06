//package cn.oyzh.easyshell.fx.connect;
//
//import cn.oyzh.easyshell.domain.ShellConnect;
//import cn.oyzh.easyshell.store.ShellConnectStore;
//import cn.oyzh.fx.plus.controls.combo.FXComboBox;
//import cn.oyzh.fx.plus.converter.SimpleStringConverter;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * ssh连接下拉选择框
// *
// * @author oyzh
// * @since 25/04/24
// */
//public class ShellSSHConnectComboBox extends FXComboBox<ShellConnect> {
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
//        connects = connects.stream().filter(ShellConnect::isSSHType).collect(Collectors.toList());
//        this.setItem(connects);
//    }
//}
