package cn.oyzh.easyshell.fx.rdp;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025-04-18
 */
public class ShellRdpColorComboBox extends FXComboBox<String> {

    public int getColor() {
        if (this.getSelectedIndex() == 0) {
            return 8;
        }
        if (this.getSelectedIndex() == 1) {
            return 15;
        }
        if (this.getSelectedIndex() == 2) {
            return 16;
        }
        if (this.getSelectedIndex() == 3) {
            return 24;
        }
        if (this.getSelectedIndex() == 4) {
            return 32;
        }
        return 0;
    }

    public void selectColor(int color) {
        if (color == 8) {
            this.select(0);
        } else if (color == 15) {
            this.select(1);
        } else if (color == 16) {
            this.select(2);
        } else if (color == 24) {
            this.select(3);
        } else if (color == 32) {
            this.select(4);
        }
    }

    @Override
    public void initNode() {
        this.addItem("8" + I18nHelper.bit());
        this.addItem("15" + I18nHelper.bit());
        this.addItem("16" + I18nHelper.bit());
        this.addItem("24" + I18nHelper.bit());
        this.addItem("32" + I18nHelper.bit());
        super.initNode();
    }

}
