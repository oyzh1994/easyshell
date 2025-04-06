package cn.oyzh.easyshell.fx.term;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * shell刷新率选择框
 *
 * @author oyzh
 * @since 25/04/01
 */
public class ShellTermFpsComboBox extends FXComboBox<String> {

    {
        this.addItem(I18nHelper.auto());
        this.addItem("120");
        this.addItem("90");
        this.addItem("75");
        this.addItem("60");
        this.addItem("30");
        this.addItem("24");
    }

    public int getFps() {
        if (this.getSelectedIndex() == 0) {
            return -1;
        }
        String fps = this.getSelectedItem();
        if(StringUtil.isBlank(fps)) {
            return -1;
        }
        return Integer.parseInt(fps);
    }

    public void selectFps(int cursorBlinks) {
        if (cursorBlinks <= 0) {
            this.select(0);
        } else if (cursorBlinks >= 120) {
            this.select(1);
        } else if (cursorBlinks >= 90) {
            this.select(2);
        } else if (cursorBlinks >= 75) {
            this.select(3);
        } else if (cursorBlinks >= 60) {
            this.select(4);
        } else if (cursorBlinks >= 30) {
            this.select(5);
        } else if (cursorBlinks >= 24) {
            this.select(6);
        } else {
            this.select(0);
        }
    }
}
