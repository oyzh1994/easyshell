package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.query.DBQueryTokenAnalyzer;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * SQLite 查询提示分析器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryTokenAnalyzer extends DBQueryTokenAnalyzer<ShellSqliteQueryPromptItem, ShellSqliteQueryToken> {

    public static final ShellSqliteQueryTokenAnalyzer INSTANCE = new ShellSqliteQueryTokenAnalyzer();

    @Override
    public ShellSqliteQueryToken currentToken(String content, int currentIndex) {
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
            ShellSqliteQueryToken token = new ShellSqliteQueryToken();
            content = content.substring(0, currentIndex);
            int tokenIndex = 0;
            Character tokenType = null;
            char[] chars = ArrayUtil.reverse(content.toCharArray());
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (c == '\n' || c == ' ' || c == '"' || c == '`' || c == '['
                        || c == ']' || c == '.' || c == ',') {
                    tokenType = c;
                    tokenIndex = chars.length - i - 1;
                    break;
                }
            }
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

    @Override
    public List<ShellSqliteQueryPromptItem> initPrompts(ShellSqliteQueryToken token, float minCorr) {
        if (token == null || token.isEmpty()) {
            return Collections.emptyList();
        }
        String text = token.getContent().toUpperCase();
        List<ShellSqliteQueryPromptItem> items = new CopyOnWriteArrayList<>();
        List<Runnable> tasks = new ArrayList<>();
        if (token.isPossibilityKeyword()) {
            tasks.add(() -> this.addItems(items, text, ShellSqliteQueryUtil.getKeywords(),
                    (byte) 4, null, minCorr));
        }
        if (token.isPossibilityDatabase()) {
            tasks.add(() -> this.addItems(items, text, ShellSqliteQueryUtil.getDatabases(),
                    (byte) 1, null, minCorr));
        }
        if (token.isPossibilityTable()) {
            tasks.add(() -> this.addItems(items, text, ShellSqliteQueryUtil.getTables(),
                    (byte) 2, "main", minCorr));
        }
        if (token.isPossibilityView()) {
            tasks.add(() -> this.addItems(items, text, ShellSqliteQueryUtil.getViews(),
                    (byte) 5, "main", minCorr));
        }
        if (token.isPossibilityColumn()) {
            tasks.add(() -> this.addColumnItems(items, text, minCorr));
        }
        ThreadUtil.submit(tasks);
        List<ShellSqliteQueryPromptItem> itemList = items.parallelStream()
                .sorted(Comparator.comparingDouble(ShellSqliteQueryPromptItem::getCorrelation))
                .collect(Collectors.toList());
        return itemList.reversed();
    }

    private void addItems(List<ShellSqliteQueryPromptItem> items, String input, Set<String> values,
                          byte type, String ext, float minCorr) {
        values.parallelStream().forEach(value -> {
            double correlation = TextUtil.clacCorr(value, input);
            if (correlation > minCorr) {
                ShellSqliteQueryPromptItem item = new ShellSqliteQueryPromptItem();
                item.setType(type);
                item.setContent(value);
                item.setExtContent(ext);
                item.setCorrelation(correlation);
                items.add(item);
            }
        });
    }

    private void addColumnItems(List<ShellSqliteQueryPromptItem> items, String input, float minCorr) {
        for (SQLiteColumn column : ShellSqliteQueryUtil.getColumnItems()) {
            double correlation = TextUtil.clacCorr(column.getName(), input);
            if (correlation > minCorr) {
                ShellSqliteQueryPromptItem item = new ShellSqliteQueryPromptItem();
                item.setType((byte) 3);
                item.setContent(column.getName());
                item.setExtContent(DBUtil.wrap(column.getDbName(), column.getTableName(), DBDialect.SQLITE));
                item.setCorrelation(correlation);
                items.add(item);
            }
        }
    }
}
