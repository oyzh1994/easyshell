package cn.oyzh.easyssh.fx;

import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.store.SSHConnectStore;
import cn.oyzh.fx.plus.SimpleStringConverter;
import cn.oyzh.fx.plus.controls.combo.FlexComboBox;

/**
 * ssh连接选择框
 *
 * @author oyzh
 * @since 2023/07/20
 */
public class SSHConnectComboBox extends FlexComboBox<SSHConnect> {

    {
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(SSHConnect o) {
                if (o == null) {
                    return "";
                }
                return o.getName();
            }
        });
        this.getItems().setAll(SSHConnectStore.INSTANCE.load());
    }
}
