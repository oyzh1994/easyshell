package cn.oyzh.easyshell.fx.rdp;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025-04-18
 */
public class ShellRdpMethodComboBox extends FXComboBox<String> {

    public int getMethod() {
        if (this.getSelectedIndex() == 0) {
            return 0;
        }
        if (this.getSelectedIndex() == 1) {
            return 1;
        }
        return 0;
    }

    public void selectMethod(int method) {
        this.select(method);
    }

    @Override
    public void initNode() {
        this.addItem(I18nHelper.builtIn());
        if (OSUtil.isWindows()) {
            this.addItem("mstsc.exe");
        } else if (OSUtil.isMacOS()) {
            this.addItem("Windows.app");
        }
        super.initNode();
    }

}
