package cn.oyzh.easyshell.fx.file;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件连接下拉选择框
 *
 * @author oyzh
 * @since 25/04/25
 */
public class ShellFileConnectComboBox extends FXComboBox<ShellConnect> {

    {
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(ShellConnect o) {
                if (o == null) {
                    return "";
                }
                return o.getName() + "(" + o.getType().toUpperCase() + ")";
            }
        });
        List<ShellConnect> connects = ShellConnectStore.INSTANCE.load();
        connects = connects.stream()
                .filter(s -> s.isSSHType() || s.isSFTPType() || s.isFTPType())
                .collect(Collectors.toList());
        this.setItem(connects);
    }
}
