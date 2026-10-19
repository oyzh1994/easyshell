package cn.oyzh.easyshell.fx.tunneling;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * 隧道类型下拉框
 *
 * @author oyzh
 * @since 2025-04-16
 */
public class ShellTunnelingTypeComboBox extends FXComboBox<String> {

    /**
     * 是否为本地转发
     *
     * @return 结果
     */
    public boolean isLocalAuth() {
        return this.getSelectedIndex() == 0;
    }

    /**
     * 是否为远程转发
     *
     * @return 结果
     */
    public boolean isRemoteAuth() {
        return this.getSelectedIndex() == 1;
    }

    /**
     * 是否为动态转发
     *
     * @return 结果
     */
    public boolean isDynamicAuth() {
        return this.getSelectedIndex() == 2;
    }

    /**
     * 获取隧道类型
     *
     * @return 隧道类型
     */
    public String getTunnelingType() {
        if (this.isLocalAuth()) {
            return "local";
        }
        if (this.isRemoteAuth()) {
            return "remote";
        }
        return "dynamic";
    }

    /**
     * 设置隧道类型
     *
     * @param type 隧道类型
     */
    public void setType(String type) {
        if ("local".equals(type)) {
            this.selectFirst();
        } else if ("remote".equals(type)) {
            this.select(1);
        } else if ("dynamic".equals(type)) {
            this.selectLast();
        }
    }

    @Override
    public void initNode() {
        this.addItem(I18nHelper.local());
        this.addItem(I18nHelper.remote());
        this.addItem(I18nHelper.dynamic());
    }
}
