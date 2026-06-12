package cn.oyzh.easyshell.terminal;

import com.jediterm.core.Color;

/**
 *
 * @author oyzh
 * @since 2025-10-16
 */
public class ShelTerminalUtil {

    /**
     * 获取退格码
     *
     * @param backspaceType 退格类型
     * @return 退格码
     */
    public static Object getBackspaceCode(Integer backspaceType) {
        if (backspaceType == null || backspaceType == 1) {
            return new byte[]{0x08};
        }
        if (backspaceType == 0) {
            return new byte[]{0x7F};
        }
        if (backspaceType == 2) {
            return "ESC[3~";
        }
        return null;
    }

    /**
     * 从fx颜色生成
     *
     * @param color1 颜色
     * @return 结果
     */
    public static Color fromFXColor(javafx.scene.paint.Color color1) {
        int red = (int) (color1.getRed() * 255);
        int green = (int) (color1.getGreen() * 255);
        int blue = (int) (color1.getBlue() * 255);
        int opacity = (int) (color1.getOpacity() * 255);
        return new Color(red, green, blue, opacity);
    }
}
