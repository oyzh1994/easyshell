package cn.oyzh.easyshell.fx.term;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * shell类型选择框
 *
 * @author oyzh
 * @since 25/04/01
 */
public class ShellTermCursorComboBox extends FXComboBox<Integer> {

    {
        this.addItem(I18nHelper.close());
        this.addItem(I18nHelper.verySlow() + "(2000ms)");
        this.addItem(I18nHelper.slow() + "(1000ms)");
        this.addItem(I18nHelper.normal() + "(500ms)");
        this.addItem(I18nHelper.fast() + "(250ms)");
        this.addItem(I18nHelper.veryFast() + "(125ms)");
    }

    public int getCursorBlinks() {
        if (this.getSelectedIndex() == 0) {
            return -1;
        }
        if (this.getSelectedIndex() == 1) {
            return 2000;
        }
        if (this.getSelectedIndex() == 2) {
            return 1000;
        }
        if (this.getSelectedIndex() == 3) {
            return 500;
        }
        if (this.getSelectedIndex() == 4) {
            return 250;
        }
        if (this.getSelectedIndex() == 5) {
            return 125;
        }
        return 500;
    }

    public void selectCursorBlinks(int cursorBlinks) {
        if (cursorBlinks <= 0) {
            this.select(0);
        } else if (cursorBlinks >= 2000) {
            this.select(1);
        } else if (cursorBlinks >= 1000) {
            this.select(2);
        } else if (cursorBlinks >= 500) {
            this.select(3);
        } else if (cursorBlinks >= 250) {
            this.select(4);
        } else if (cursorBlinks >= 125) {
            this.select(5);
        } else {
            this.select(3);
        }
    }
}
