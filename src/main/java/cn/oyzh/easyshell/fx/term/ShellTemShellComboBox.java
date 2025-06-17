package cn.oyzh.easyshell.fx.term;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * shell类型选择框
 *
 * @author oyzh
 * @since 25/04/01
 */
public class ShellTemShellComboBox extends FXComboBox<String> {

    {
        if (OSUtil.isWindows()) {
            this.setItem(List.of("cmd.exe", "powershell.exe", "git-bash", "git-sh"));
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
        } else if (OSUtil.isMacOS()) {
            String result = RuntimeUtil.execForStr("cat /etc/shells");
            if (StringUtil.isNotBlank(result)) {
                result.lines().forEach(l -> {
                    if (l.startsWith("/")) {
                        this.addItem(l);
                    }
                });
            } else {
                this.setItem(List.of("/bin/bash", "/bin/zsh"));
            }
        }
    }

    @Override
    public void select(String obj) {
        if (obj == null || obj.isEmpty()) {
            this.selectFirst();
        } else {
            super.select(obj);
        }
    }
}
