package cn.oyzh.easyssh.fx;

import cn.oyzh.easyfx.SimpleStringConverter;
import cn.oyzh.easyfx.controls.FlexComboBox;
import cn.oyzh.easyssh.domain.SSHInfo;
import cn.oyzh.easyssh.store.SSHInfoStore;

/**
 * ssh连接选择框
 *
 * @author oyzh
 * @since 2023/07/20
 */
public class SSHConnectComboBox extends FlexComboBox<SSHInfo> {

    {
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(SSHInfo o) {
                if (o == null) {
                    return "";
                }
                return o.getName();
            }
        });
        this.getItems().setAll(SSHInfoStore.INSTANCE.load());
    }
}
