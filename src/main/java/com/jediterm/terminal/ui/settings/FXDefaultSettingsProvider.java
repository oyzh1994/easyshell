package com.jediterm.terminal.ui.settings;

import cn.oyzh.fx.plus.font.FontUtil;
import javafx.scene.text.Font;

public class FXDefaultSettingsProvider extends DefaultSettingsProvider {

    @Override
    public java.awt.Font getTerminalFont() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Font getFXTerminalFont() {
//        String fontName;
//        if (OSUtil.isWindows()) {
//            fontName = "Consolas";
//        } else if (OSUtil.isMacOS()) {
//            fontName = "Menlo";
//        } else {
//            fontName = "Monospaced";
//        }
//        return Font.font(fontName, getTerminalFontSize());
//        return Font.font("Monospaced", getTerminalFontSize());
        return FontUtil.newFontBySize("Monospaced", this.getTerminalFontSize());
    }
}
