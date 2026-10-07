package cn.oyzh.easyshell.fx.sync;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * shell 同步类型选择框
 *
 * @author oyzh
 * @since 2025-07-03
 */
public class ShellSyncTypeCombobox extends FXComboBox<String> {

    {
        this.addItem("Gitee");
        this.addItem("Github");
        this.selectFirst();
    }

    /**
     * 是否为 Gitee
     *
     * @return 是否为 Gitee
     */
    public boolean isGitee() {
        return this.getSelectedIndex() == 0;
    }

    /**
     * 是否为 Github
     *
     * @return 是否为 Github
     */
    public boolean isGithub() {
        return this.getSelectedIndex() == 1;
    }

}
