package cn.oyzh.easyshell.fx.vnc;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import com.glavsoft.viewer.settings.LocalMouseCursorShape;

/**
 * @author oyzh
 * @since 2026-08-29
 */
public class ShellVNCCursorComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        for (LocalMouseCursorShape value : LocalMouseCursorShape.values()) {
            this.addItem(value.getCursorName());
        }
    }
}
