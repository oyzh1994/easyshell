package cn.oyzh.easyshell.fx.proxy;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * 代理认证类型选择框
 *
 * @author oyzh
 * @since 2025-04-14
 */
public class ShellProxyAuthTypeComboBox extends FXComboBox<String> {

    /**
     * 是否为密码认证
     *
     * @return 是否为密码认证
     */
    public boolean isPasswordAuth() {
        return this.getSelectedIndex() == 1;
    }

    /**
     * 获取认证类型
     *
     * @return 认证类型
     */
    public String getAuthType() {
        if (this.getSelectedIndex() == 0) {
            return "none";
        }
        return "password";
    }

    @Override
    public void initNode() {
        this.addItem(I18nHelper.none());
        this.addItem(I18nHelper.password());
        super.initNode();
    }
}
