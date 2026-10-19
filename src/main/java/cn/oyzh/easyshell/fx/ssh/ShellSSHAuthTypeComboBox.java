package cn.oyzh.easyshell.fx.ssh;

import cn.oyzh.fx.gui.combobox.SSHAuthTypeCombobox;
import cn.oyzh.i18n.I18nHelper;

/**
 * SSH认证类型下拉框
 *
 * @author oyzh
 * @since 2025-04-03
 */
public class ShellSSHAuthTypeComboBox extends SSHAuthTypeCombobox {

    {
        this.addItem(I18nHelper.key1Manager());
    }

    /**
     * 是否为管理员认证
     *
     * @return 是否为管理员认证
     */
    public boolean isManagerAuth() {
        return this.getSelectedIndex() == 3;
    }

    @Override
    public String getAuthType() {
        if (this.isManagerAuth()) {
            return "manager";
        }
        return super.getAuthType();
    }
}
