package cn.oyzh.easyshell.fx.sync;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2025-07-03
 */
public class ShellSyncTypeCombobox extends FXComboBox<String> {

    {
        this.addItem("Gitee");
        this.addItem("Github");
        this.selectFirst();
    }

    public boolean isGitee() {
        return this.getSelectedIndex() == 0;
    }

    public boolean isGithub() {
        return this.getSelectedIndex() == 1;
    }

}
