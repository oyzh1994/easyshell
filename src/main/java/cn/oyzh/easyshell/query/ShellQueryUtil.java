package cn.oyzh.easyshell.query;

import cn.oyzh.common.util.StringUtil;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询工具类
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellQueryUtil {

    /**
     * 计算相近度
     *
     * @param str  内容
     * @param text 文本
     * @return 结果
     */
    public static double clacCorr(String str, String text) {
        double corr = 0.0;
        if (StringUtil.containsIgnoreCase(str, text) || StringUtil.containsIgnoreCase(text, str)) {
            corr = StringUtil.similarity(str.toUpperCase(), text.toUpperCase());
            if (StringUtil.startWithIgnoreCase(str, text)) {
                corr += 0.35;
            } else if (StringUtil.containsIgnoreCase(str, text)) {
                corr += 0.25;
            }
            if (StringUtil.endWithIgnoreCase(str, text)) {
                corr += 0.15;
            }
        }
        return corr;
    }

    /**
     * 提示字符
     */
    public final static List<KeyCode> PROMPT_CODES = new ArrayList<>();

    /**
     * 更新字符
     */
    public final static List<KeyCode> UPDATE_CODES = new ArrayList<>();

    static {
        // 特殊字符
        PROMPT_CODES.add(KeyCode.MINUS);
        PROMPT_CODES.add(KeyCode.UNDERSCORE);
        // 字母
        PROMPT_CODES.add(KeyCode.A);
        PROMPT_CODES.add(KeyCode.B);
        PROMPT_CODES.add(KeyCode.C);
        PROMPT_CODES.add(KeyCode.D);
        PROMPT_CODES.add(KeyCode.E);
        PROMPT_CODES.add(KeyCode.F);
        PROMPT_CODES.add(KeyCode.G);
        PROMPT_CODES.add(KeyCode.H);
        PROMPT_CODES.add(KeyCode.I);
        PROMPT_CODES.add(KeyCode.J);
        PROMPT_CODES.add(KeyCode.K);
        PROMPT_CODES.add(KeyCode.L);
        PROMPT_CODES.add(KeyCode.M);
        PROMPT_CODES.add(KeyCode.N);
        PROMPT_CODES.add(KeyCode.O);
        PROMPT_CODES.add(KeyCode.P);
        PROMPT_CODES.add(KeyCode.Q);
        PROMPT_CODES.add(KeyCode.R);
        PROMPT_CODES.add(KeyCode.S);
        PROMPT_CODES.add(KeyCode.T);
        PROMPT_CODES.add(KeyCode.U);
        PROMPT_CODES.add(KeyCode.V);
        PROMPT_CODES.add(KeyCode.W);
        PROMPT_CODES.add(KeyCode.X);
        PROMPT_CODES.add(KeyCode.Y);
        PROMPT_CODES.add(KeyCode.Z);
        // 小键盘数字
        PROMPT_CODES.add(KeyCode.NUMPAD0);
        PROMPT_CODES.add(KeyCode.NUMPAD1);
        PROMPT_CODES.add(KeyCode.NUMPAD2);
        PROMPT_CODES.add(KeyCode.NUMPAD3);
        PROMPT_CODES.add(KeyCode.NUMPAD4);
        PROMPT_CODES.add(KeyCode.NUMPAD5);
        PROMPT_CODES.add(KeyCode.NUMPAD6);
        PROMPT_CODES.add(KeyCode.NUMPAD7);
        PROMPT_CODES.add(KeyCode.NUMPAD8);
        PROMPT_CODES.add(KeyCode.NUMPAD9);
        // 软盘数字
        PROMPT_CODES.add(KeyCode.SOFTKEY_0);
        PROMPT_CODES.add(KeyCode.SOFTKEY_1);
        PROMPT_CODES.add(KeyCode.SOFTKEY_2);
        PROMPT_CODES.add(KeyCode.SOFTKEY_3);
        PROMPT_CODES.add(KeyCode.SOFTKEY_4);
        PROMPT_CODES.add(KeyCode.SOFTKEY_5);
        PROMPT_CODES.add(KeyCode.SOFTKEY_6);
        PROMPT_CODES.add(KeyCode.SOFTKEY_7);
        PROMPT_CODES.add(KeyCode.SOFTKEY_8);
        PROMPT_CODES.add(KeyCode.SOFTKEY_9);
        // 数字
        PROMPT_CODES.add(KeyCode.DIGIT0);
        PROMPT_CODES.add(KeyCode.DIGIT1);
        PROMPT_CODES.add(KeyCode.DIGIT2);
        PROMPT_CODES.add(KeyCode.DIGIT3);
        PROMPT_CODES.add(KeyCode.DIGIT4);
        PROMPT_CODES.add(KeyCode.DIGIT5);
        PROMPT_CODES.add(KeyCode.DIGIT6);
        PROMPT_CODES.add(KeyCode.DIGIT7);
        PROMPT_CODES.add(KeyCode.DIGIT8);
        PROMPT_CODES.add(KeyCode.DIGIT9);

        // 更新字符
        UPDATE_CODES.add(KeyCode.BACK_SPACE);
        UPDATE_CODES.add(KeyCode.DELETE);
        UPDATE_CODES.add(KeyCode.SPACE);
    }

}
