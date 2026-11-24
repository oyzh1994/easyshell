package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.fx.db.query.DBSqlQueryPromptCategory;
import cn.oyzh.fx.db.query.DBSqlQueryTokenAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SQLite 查询提示分析器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryTokenAnalyzer extends DBSqlQueryTokenAnalyzer<ShellSqliteQueryPromptItem, ShellSqliteQueryToken> {

    public static final ShellSqliteQueryTokenAnalyzer INSTANCE = new ShellSqliteQueryTokenAnalyzer();

    @Override
    protected ShellSqliteQueryToken createToken() {
        return new ShellSqliteQueryToken();
    }

    @Override
    protected ShellSqliteQueryPromptItem createPromptItem() {
        return new ShellSqliteQueryPromptItem();
    }

    @Override
    public List<ShellSqliteQueryPromptItem> initPrompts(ShellSqliteQueryToken token, float minCorr) {
        if (token == null) {
            return Collections.emptyList();
        }
        String text = token.getContent() == null ? "" : token.getContent();
        List<ShellSqliteQueryPromptItem> items = new CopyOnWriteArrayList<>();
        List<Runnable> tasks = new ArrayList<>();
        if (token.isPossibilityKeyword()) {
            tasks.add(() -> ShellSqliteQueryUtil.getKeywords().forEach(keyword ->
                    this.addPrompt(items, token, text, minCorr, TYPE_KEYWORD, keyword,
                            null, null, DBSqlQueryPromptCategory.KEYWORD)));
        }
        if (token.isPossibilityDatabase()) {
            tasks.add(() -> ShellSqliteQueryUtil.getDatabases().forEach(database ->
                    this.addPrompt(items, token, text, minCorr, TYPE_DATABASE, database,
                            null, null, DBSqlQueryPromptCategory.DATABASE)));
        }
        if (token.isPossibilityTable()) {
            tasks.add(() -> ShellSqliteQueryUtil.getTables().forEach(table ->
                    this.addPrompt(items, token, text, minCorr, TYPE_TABLE, table,
                            "main", "main", DBSqlQueryPromptCategory.TABLE)));
        }
        if (token.isPossibilityView()) {
            tasks.add(() -> ShellSqliteQueryUtil.getViews().forEach(view ->
                    this.addPrompt(items, token, text, minCorr, TYPE_VIEW, view,
                            "main", "main", DBSqlQueryPromptCategory.VIEW)));
        }
        ThreadUtil.submit(tasks);
        return this.sortPrompts(items);
    }
}
