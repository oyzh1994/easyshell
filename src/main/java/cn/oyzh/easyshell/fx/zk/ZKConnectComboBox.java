package cn.oyzh.easyshell.fx.zk;

import cn.oyzh.easyshell.domain.zk.ZKConnect;
import cn.oyzh.easyshell.store.zk.ZKConnectStore;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;

import java.util.List;

/**
 * 连接选择框
 *
 * @author oyzh
 * @since 2023/04/08
 */
public class ZKConnectComboBox extends FXComboBox<ZKConnect> {

    {
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(ZKConnect o) {
                if (o == null) {
                    return "";
                }
                return o.getName();
            }
        });
        List<ZKConnect> connects = ZKConnectStore.INSTANCE.load();
        this.setItem(connects);
    }
}
