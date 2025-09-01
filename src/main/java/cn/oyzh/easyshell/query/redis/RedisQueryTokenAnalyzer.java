package cn.oyzh.easyshell.query.redis;

import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.StringUtil;

/**
 * @author oyzh
 * @since 2025/01/21
 */
public class RedisQueryTokenAnalyzer {

    public static final RedisQueryTokenAnalyzer INSTANCE = new RedisQueryTokenAnalyzer();

    public RedisQueryToken currentToken(String input, int currentIndex) {
        try {
            if (StringUtil.isEmpty(input)) {
                return null;
            }
            if (currentIndex <= 0) {
                return null;
            }
            if (currentIndex > input.length()) {
                return null;
            }
            RedisQueryToken token = new RedisQueryToken();
            // 截取字符串
            String content = input.substring(0, currentIndex);
            // 当前位置
            int tokenIndex = 0;
            Character tokenType = null;
            if (content.contains(" ") || content.contains("-")) {
                char[] chars = ArrayUtil.reverse(content.toCharArray());
                for (int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    // 遇到换行符则停止
                    if (c == '\n') {
                        return null;
                    }
                    // 寻找操作符1
                    if (c == ' ') {
                        tokenType = c;
                        tokenIndex = chars.length - i;
                        break;
                    }
                }
            }
            String tokenContent = content.substring(tokenIndex);
            token.setInput(input);
            token.setToken(tokenType);
            token.setEndIndex(currentIndex);
            token.setStartIndex(tokenIndex);
            token.setContent(tokenContent.trim());
            return token;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
