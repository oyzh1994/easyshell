package cn.oyzh.easyshell.fx.rdp;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;

/**
 * RDP颜色深度选择框
 *
 * @author oyzh
 * @since 2026-09-20
 */
public class ShellRdpColorComboBox extends FXComboBox<String> {

    /**
     * 获取颜色深度
     *
     * @return 颜色深度
     */
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

    /**
     * 选择颜色深度
     *
     * @param color 颜色深度
     */
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
        this.addItem(ShellI18nHelper.colorDepth(8));
        this.addItem(ShellI18nHelper.colorDepth(15));
        this.addItem(ShellI18nHelper.colorDepth(16));
        this.addItem(ShellI18nHelper.colorDepth(24));
        this.addItem(ShellI18nHelper.colorDepth(32));
        super.initNode();
    }

}
