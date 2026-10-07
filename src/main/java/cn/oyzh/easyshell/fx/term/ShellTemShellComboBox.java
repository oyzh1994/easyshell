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
 * @since 2025-03-09
 */
public class ShellTemShellComboBox extends FXComboBox<String> {

    @Override
    public void select(String obj) {
        if (obj == null || obj.isEmpty()) {
            this.selectFirst();
        } else {
            super.select(obj);
        }
    }

    @Override
    public void initNode() {
        if (OSUtil.isWindows()) {
            this.setItem(List.of("cmd.exe", "powershell.exe", "git-bash", "git-sh", "msys2-bash", "cygwin-bash"));
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
        super.initNode();
    }
}
