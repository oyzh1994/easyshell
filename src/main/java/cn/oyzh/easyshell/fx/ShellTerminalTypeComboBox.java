package cn.oyzh.easyshell.fx;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * shell终端类型选择框
 *
 * @author oyzh
 * @since 23/03/09
 */
public class ShellTerminalTypeComboBox extends FXComboBox<String> {

    {
        if (OSUtil.isWindows()) {
            this.setItem(List.of("cmd.exe", "powershell.exe"));
        } else if (OSUtil.isLinux()) {
            String result = RuntimeUtil.execForStr("cat /etc/shells");
            if (StringUtil.isNotBlank(result)) {
                result.lines().forEach(l -> {
                    if (l.startsWith("/")) {
                        this.addItem(l);
                    }
                });
            } else {
                this.setItem(List.of("/bin/bash"));
            }
//            this.setItem(List.of("/bin/bash", "/bin/zsh"));
        } else if (OSUtil.isMacOS()) {
            String result = RuntimeUtil.execForStr("cat /etc/shells");
            if (StringUtil.isNotBlank(result)) {
                result.lines().forEach(l -> {
                    if (l.startsWith("/")) {
                        this.addItem(l);
                    }
                });
            } else {
                this.setItem(List.of("/bin/bash"));
            }
//            this.setItem(List.of("/bin/zsh", "/bin/bash"));
        }
    }
}
