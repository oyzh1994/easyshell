package cn.oyzh.easyshell.terminal;

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
}
