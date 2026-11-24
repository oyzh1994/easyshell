package cn.oyzh.easyshell.query.sqlserver;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.fx.db.query.DBSqlQueryPromptCategory;
import cn.oyzh.fx.db.query.DBSqlQueryTokenAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SqlServer查询token解析器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerQueryTokenAnalyzer extends DBSqlQueryTokenAnalyzer<ShellSqlServerQueryPromptItem, ShellSqlServerQueryToken> {

    public static final ShellSqlServerQueryTokenAnalyzer INSTANCE = new ShellSqlServerQueryTokenAnalyzer();

    @Override
    protected ShellSqlServerQueryToken createToken() {
        return new ShellSqlServerQueryToken();
    }

    @Override
    protected ShellSqlServerQueryPromptItem createPromptItem() {
        return new ShellSqlServerQueryPromptItem();
    }

    @Override
    public List<ShellSqlServerQueryPromptItem> initPrompts(ShellSqlServerQueryToken token, float minCorr) {
        if (token == null) {
            return Collections.emptyList();
        }
        String text = token.getContent() == null ? "" : token.getContent();
        List<ShellSqlServerQueryPromptItem> items = new CopyOnWriteArrayList<>();
        List<Runnable> tasks = new ArrayList<>();
        if (token.isPossibilityKeyword()) {
            tasks.add(() -> ShellSqlServerQueryUtil.getKeywords().forEach(keyword ->
                    this.addPrompt(items, token, text, minCorr, TYPE_KEYWORD, keyword,
                            null, null, DBSqlQueryPromptCategory.KEYWORD)));
        }
        if (token.isPossibilityDatabase()) {
            tasks.add(() -> ShellSqlServerQueryUtil.getSchemas().forEach(schema ->
                    this.addPrompt(items, token, text, minCorr, TYPE_DATABASE, schema.getName(),
                            null, null, DBSqlQueryPromptCategory.DATABASE)));
        }
        if (token.isPossibilityTable()) {
            tasks.add(() -> ShellSqlServerQueryUtil.getTables().forEach(table ->
                    this.addPrompt(items, token, text, minCorr, TYPE_TABLE, table.getName(),
                            table.getSchema(), table.getSchema(), DBSqlQueryPromptCategory.TABLE)));
        }
        if (token.isPossibilityView()) {
            tasks.add(() -> ShellSqlServerQueryUtil.getViews().forEach(view ->
                    this.addPrompt(items, token, text, minCorr, TYPE_VIEW, view.getName(),
                            view.getSchema(), view.getSchema(), DBSqlQueryPromptCategory.VIEW)));
        }
        if (token.isPossibilityFunction()) {
            tasks.add(() -> ShellSqlServerQueryUtil.getFunctions().forEach(function ->
                    this.addPrompt(items, token, text, minCorr, TYPE_FUNCTION, function.getName(),
                            function.getSchema(), function.getSchema(), DBSqlQueryPromptCategory.FUNCTION)));
        }
        if (token.isPossibilityProcedure()) {
            tasks.add(() -> ShellSqlServerQueryUtil.getProcedures().forEach(procedure ->
                    this.addPrompt(items, token, text, minCorr, TYPE_PROCEDURE, procedure.getName(),
                            procedure.getSchema(), procedure.getSchema(), DBSqlQueryPromptCategory.PROCEDURE)));
        }
        if (token.isPossibilitySequence()) {
            tasks.add(() -> ShellSqlServerQueryUtil.getSequences().forEach(sequence ->
                    this.addPrompt(items, token, text, minCorr, TYPE_SEQUENCE, sequence.getName(),
                            sequence.getSchema(), sequence.getSchema(), DBSqlQueryPromptCategory.SEQUENCE)));
        }
        ThreadUtil.submit(tasks);
        return this.sortPrompts(items);
    }
}
