package cn.oyzh.easyshell.query.mysql;


import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.StringUtil;

/**
 * db查询文本域
 *
 * @author oyzh
 * @since 2024/02/18
 */
public class MysqlQueryTokenAnalyzer {

    public static final MysqlQueryTokenAnalyzer INSTANCE = new MysqlQueryTokenAnalyzer();

    public MysqlQueryToken currentToken(String content, int currentIndex) {
        try {
            if (StringUtil.isEmpty(content)) {
                return null;
            }
            if (currentIndex <= 0) {
                return null;
            }
            if (currentIndex > content.length()) {
                return null;
            }
            MysqlQueryToken token = new MysqlQueryToken();
            // 截取字符串
            content = content.substring(0, currentIndex);
            // 当前位置
            int tokenIndex = 0;
            // token类型
            Character tokenType = null;
            char[] chars = ArrayUtil.reverse(content.toCharArray());
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                // 寻找操作符
                if (c == '\n' || c == ' ' || c == '`' || c == '.') {
                    tokenType = c;
                    tokenIndex = chars.length - i - 1;
                    break;
                }
            }
            // 特殊类型，默认为关键字
            if (tokenType == null) {
                tokenType = '\0';
            }
            String tokenContent;
            if (tokenType != '\0') {
                tokenContent = content.substring(tokenIndex + 1);
            } else {
                tokenContent = content.substring(tokenIndex);
            }
            token.setToken(tokenType);
            if (tokenType != '\0') {
                token.setStartIndex(tokenIndex + 1);
                token.setEndIndex(currentIndex);
            } else {
                token.setStartIndex(tokenIndex);
                token.setEndIndex(currentIndex);
            }
            token.setContent(tokenContent.trim());
            return token;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
