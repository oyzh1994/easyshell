package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.db.query.DBQueryTokenAnalyzer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * SQLite 查询提示分析器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryTokenAnalyzer extends DBQueryTokenAnalyzer<ShellSqliteQueryPromptItem, ShellSqliteQueryToken> {

    public static final ShellSqliteQueryTokenAnalyzer INSTANCE = new ShellSqliteQueryTokenAnalyzer();

    @Override
    public ShellSqliteQueryToken currentToken(String input, int currentIndex) {
        ShellSqliteQueryToken token = new ShellSqliteQueryToken();
        if (input == null || currentIndex < 0) {
            return token;
        }
        int start = Math.min(currentIndex, input.length());
        while (start > 0 && (Character.isLetterOrDigit(input.charAt(start - 1))
                || input.charAt(start - 1) == '_')) {
            start--;
        }
        int end = Math.min(currentIndex, input.length());
        while (end < input.length() && (Character.isLetterOrDigit(input.charAt(end))
                || input.charAt(end) == '_')) {
            end++;
        }
        token.setStartIndex(start);
        token.setEndIndex(end);
        token.setContent(input.substring(start, end));
        token.setToken(end < input.length() ? input.charAt(end) : '\0');
        return token;
    }

    @Override
    public List<ShellSqliteQueryPromptItem> initPrompts(ShellSqliteQueryToken token, float minCorr) {
        List<ShellSqliteQueryPromptItem> items = new ArrayList<>();
        if (token == null || token.isEmpty()) {
            return items;
        }
        addItems(items, token.getContent(), ShellSqliteQueryUtil.getKeywords(), (byte) 4, null, minCorr);
        addItems(items, token.getContent(), ShellSqliteQueryUtil.getTables(), (byte) 2, "table", minCorr);
        addItems(items, token.getContent(), ShellSqliteQueryUtil.getViews(), (byte) 5, "view", minCorr);
        addItems(items, token.getContent(), ShellSqliteQueryUtil.getColumns(), (byte) 3, "column", minCorr);
        return items.stream()
                .sorted(Comparator.comparingDouble(ShellSqliteQueryPromptItem::getCorrelation).reversed())
                .toList();
    }

    private void addItems(List<ShellSqliteQueryPromptItem> items, String input, Set<String> values,
                          byte type, String ext, float minCorr) {
        for (String value : values) {
            double correlation = TextUtil.clacCorr(value, input);
            if (correlation >= minCorr) {
                ShellSqliteQueryPromptItem item = new ShellSqliteQueryPromptItem();
                item.setType(type);
                item.setContent(value);
                item.setExtContent(ext);
                item.setCorrelation(correlation);
                items.add(item);
            }
        }
    }
}
