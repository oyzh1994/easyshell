package cn.oyzh.easyshell.fx;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * ssh终端类型选择框
 *
 * @author oyzh
 * @since 23/03/09
 */
public class SSHTerminalTypeComboBox extends FXComboBox<String> {

    {
        if (OSUtil.isLinux()) {
            this.setItem("/bin/bash");
        } else if (OSUtil.isWindows()) {
            this.setItem(List.of("powershell.exe", "cmd.exe"));
        } else if (OSUtil.isMacOS()) {
            this.setItem(List.of("/bin/zsh", "/bin/bash"));
        }
    }
}
