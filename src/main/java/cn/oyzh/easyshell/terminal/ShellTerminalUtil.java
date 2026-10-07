package cn.oyzh.easyshell.terminal;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.fx.tty.TtyHyperlinkFilter;
import cn.oyzh.fx.tty.TtyTermWidget;
import com.jediterm.terminal.CursorShape;

/**
 * shell终端工具
 *
 * @author oyzh
 * @since 2026-07-06
 */
public class ShellTerminalUtil {

    //    /**
    //     * 获取退格码
    //     *
    //     * @param backspaceType 退格类型
    //     * @return 退格码
    //     */
    //    public static Object getBackspaceCode(Integer backspaceType) {
    //        if (backspaceType == null || backspaceType == 1) {
    //            return new byte[]{0x08};
    //        }
    //        if (backspaceType == 0) {
    //            return new byte[]{0x7F};
    //        }
    //        if (backspaceType == 2) {
    //            return "ESC[3~";
    //        }
    //        return null;
    //    }
    //
    //    /**
    //     * 从fx颜色生成
    //     *
    //     * @param color1 颜色
    //     * @return 结果
    //     */
    //    public static Color fromFXColor(javafx.scene.paint.Color color1) {
    //        int red = (int) (color1.getRed() * 255);
    //        int green = (int) (color1.getGreen() * 255);
    //        int blue = (int) (color1.getBlue() * 255);
    //        int opacity = (int) (color1.getOpacity() * 255);
    //        return new Color(red, green, blue, opacity);
    //    }

    /**
     * 应用设置
     *
     * @param widget  tty组件
     * @param setting 设置
     */
    public static void applySetting(TtyTermWidget widget, ShellSetting setting) {
        if (setting.isTermParseHyperlink()) {
            widget.addHyperlinkFilter(new TtyHyperlinkFilter());
        }
        // 初始化光标
        if (setting.getTermCursorBlinks() > 0) {
            switch (setting.getTermCursorStyle()) {
                case 1:
                    widget.getTerminalPanel().setCursorShape(CursorShape.BLINK_UNDERLINE);
                    break;
                case 2:
                    widget.getTerminalPanel().setCursorShape(CursorShape.BLINK_VERTICAL_BAR);
                    break;
                default:
                    widget.getTerminalPanel().setCursorShape(CursorShape.BLINK_BLOCK);
                    break;
            }
        } else {
            switch (setting.getTermCursorStyle()) {
                case 1:
                    widget.getTerminalPanel().setCursorShape(CursorShape.STEADY_UNDERLINE);
                    break;
                case 2:
                    widget.getTerminalPanel().setCursorShape(CursorShape.STEADY_VERTICAL_BAR);
                    break;
                default:
                    widget.getTerminalPanel().setCursorShape(CursorShape.STEADY_BLOCK);
                    break;
            }
        }
    }
}
