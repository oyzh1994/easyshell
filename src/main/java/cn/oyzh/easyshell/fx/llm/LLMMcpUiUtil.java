package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.fx.plus.controls.label.FXLabel;
import javafx.scene.control.Tooltip;

/**
 * MCP 页面 UI 工具。
 *
 * @author oyzh
 * @since 2026-11-20
 */
public final class LLMMcpUiUtil {

    private LLMMcpUiUtil() {
    }

    public static void showStatus(FXLabel label, String text, String tooltipText) {
        if (label == null) {
            return;
        }
        label.setText(text);
        label.setTooltip(tooltipText == null || tooltipText.isBlank()
                ? null : new Tooltip(tooltipText));
        label.setManaged(true);
        label.setVisible(true);
    }

    public static void clearStatus(FXLabel label) {
        if (label == null) {
            return;
        }
        label.setText("");
        label.setTooltip(null);
        label.setManaged(false);
        label.setVisible(false);
    }
}
