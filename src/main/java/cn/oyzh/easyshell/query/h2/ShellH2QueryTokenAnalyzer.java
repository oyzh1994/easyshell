package cn.oyzh.easyshell.query.h2;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.fx.db.query.DBSqlQueryPromptCategory;
import cn.oyzh.fx.db.query.DBSqlQueryTokenAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * H2 查询提示分析器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2QueryTokenAnalyzer extends DBSqlQueryTokenAnalyzer<ShellH2QueryPromptItem, ShellH2QueryToken> {

    public static final ShellH2QueryTokenAnalyzer INSTANCE = new ShellH2QueryTokenAnalyzer();

    @Override
    protected ShellH2QueryToken createToken() {
        return new ShellH2QueryToken();
    }

    @Override
    protected ShellH2QueryPromptItem createPromptItem() {
        return new ShellH2QueryPromptItem();
    }

    @Override
    public List<ShellH2QueryPromptItem> initPrompts(ShellH2QueryToken token, float minCorr) {
        if (token == null) {
            return Collections.emptyList();
        }
        String text = token.getContent() == null ? "" : token.getContent();
        List<ShellH2QueryPromptItem> items = new CopyOnWriteArrayList<>();
        List<Runnable> tasks = new ArrayList<>();
        if (token.isPossibilityKeyword()) {
            tasks.add(() -> ShellH2QueryUtil.getKeywords().forEach(keyword ->
                    this.addPrompt(items, token, text, minCorr, TYPE_KEYWORD, keyword,
                            null, null, DBSqlQueryPromptCategory.KEYWORD)));
        }
        if (token.isPossibilityDatabase()) {
            tasks.add(() -> ShellH2QueryUtil.getDatabases().forEach(database ->
                    this.addPrompt(items, token, text, minCorr, TYPE_DATABASE, database,
                            null, null, DBSqlQueryPromptCategory.DATABASE)));
        }
        if (token.isPossibilityTable()) {
            tasks.add(() -> ShellH2QueryUtil.getTables().forEach(table ->
                    this.addPrompt(items, token, text, minCorr, TYPE_TABLE, table,
                            "main", "main", DBSqlQueryPromptCategory.TABLE)));
        }
        if (token.isPossibilityView()) {
            tasks.add(() -> ShellH2QueryUtil.getViews().forEach(view ->
                    this.addPrompt(items, token, text, minCorr, TYPE_VIEW, view,
                            "main", "main", DBSqlQueryPromptCategory.VIEW)));
        }
        ThreadUtil.submit(tasks);
        return this.sortPrompts(items);
    }
}
