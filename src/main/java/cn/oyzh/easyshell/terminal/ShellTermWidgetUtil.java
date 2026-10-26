package cn.oyzh.easyshell.terminal;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.tty.TtyTermWidget;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

/**
 * 终端组件工具类。
 *
 * @author oyzh
 * @since 2026-10-26
 */
public final class ShellTermWidgetUtil {

    private ShellTermWidgetUtil() {
    }

    /**
     * 在父容器中原位替换终端组件，避免重建整个标签页。
     *
     * @param current     当前终端组件
     * @param replacement 新终端组件
     */
    public static void replace(TtyTermWidget current, TtyTermWidget replacement) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("terminal widget replacement must run on the JavaFX thread");
        }
        if (current == null || replacement == null || current == replacement) {
            throw new IllegalArgumentException("terminal widget is invalid");
        }
        Parent parent = current.getParent();
        if (!(parent instanceof Pane pane)) {
            throw new IllegalStateException("terminal widget is not attached to a pane");
        }
        int index = pane.getChildren().indexOf(current);
        if (index < 0) {
            throw new IllegalStateException("terminal widget is not attached to its parent");
        }
        replacement.setId(current.getId());
        replacement.setFlexWidth(current.getFlexWidth());
        replacement.setFlexHeight(current.getFlexHeight());
        replacement.setManaged(current.isManaged());
        replacement.setVisible(current.isVisible());
        pane.getChildren().set(index, replacement);
        pane.applyCss();
        pane.layout();
        replacement.requestFocus();
    }

    /**
     * 在 JavaFX 线程关闭已被替换的终端组件。
     *
     * @param widget 终端组件
     */
    public static void close(TtyTermWidget widget) {
        if (widget == null) {
            return;
        }
        FXUtil.runWait(() -> {
            try {
                widget.close();
            } catch (Throwable ex) {
                JulLog.warn("close terminal widget failed", ex);
            }
        });
    }
}
