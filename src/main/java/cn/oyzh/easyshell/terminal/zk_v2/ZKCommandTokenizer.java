package cn.oyzh.easyshell.terminal.zk_v2;

import java.util.ArrayList;
import java.util.List;

/**
 * ZooKeeper 终端命令分词器。
 */
public final class ZKCommandTokenizer {

    private ZKCommandTokenizer() {
    }

    public static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        if (input == null || input.isBlank()) {
            return tokens;
        }

        StringBuilder token = new StringBuilder();
        boolean quoted = false;
        char quote = 0;
        boolean escaped = false;
        boolean tokenStarted = false;

        for (int index = 0; index < input.length(); index++) {
            char current = input.charAt(index);
            if (escaped) {
                token.append(current);
                escaped = false;
                tokenStarted = true;
                continue;
            }
            if (current == '\\' && quote != '\'') {
                escaped = true;
                tokenStarted = true;
                continue;
            }
            if (quoted) {
                if (current == quote) {
                    quoted = false;
                } else {
                    token.append(current);
                }
                continue;
            }
            if (current == '\'' || current == '\"') {
                quoted = true;
                quote = current;
                tokenStarted = true;
                continue;
            }
            if (Character.isWhitespace(current)) {
                if (tokenStarted) {
                    tokens.add(token.toString());
                    token.setLength(0);
                    tokenStarted = false;
                }
                continue;
            }
            token.append(current);
            tokenStarted = true;
        }

        if (escaped) {
            token.append('\\');
        }
        if (quoted) {
            throw new IllegalArgumentException("Unterminated quoted argument");
        }
        if (tokenStarted) {
            tokens.add(token.toString());
        }
        return tokens;
    }
}
