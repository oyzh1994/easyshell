package cn.oyzh.easyshell.query.dameng;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.fx.db.query.DBSqlQueryPromptCategory;
import cn.oyzh.fx.db.query.DBSqlQueryTokenAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 达梦查询token解析器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengQueryTokenAnalyzer extends DBSqlQueryTokenAnalyzer<ShellDamengQueryPromptItem, ShellDamengQueryToken> {

    public static final ShellDamengQueryTokenAnalyzer INSTANCE = new ShellDamengQueryTokenAnalyzer();

    @Override
    protected ShellDamengQueryToken createToken() {
        return new ShellDamengQueryToken();
    }

    @Override
    protected ShellDamengQueryPromptItem createPromptItem() {
        return new ShellDamengQueryPromptItem();
    }

    @Override
    public List<ShellDamengQueryPromptItem> initPrompts(ShellDamengQueryToken token, float minCorr) {
        if (token == null) {
            return Collections.emptyList();
        }
        String text = token.getContent() == null ? "" : token.getContent();
        List<ShellDamengQueryPromptItem> items = new CopyOnWriteArrayList<>();
        List<Runnable> tasks = new ArrayList<>();
        if (token.isPossibilityKeyword()) {
            tasks.add(() -> ShellDamengQueryUtil.getKeywords().forEach(keyword ->
                    this.addPrompt(items, token, text, minCorr, TYPE_KEYWORD, keyword,
                            null, null, DBSqlQueryPromptCategory.KEYWORD)));
        }
        if (token.isPossibilityDatabase()) {
            tasks.add(() -> ShellDamengQueryUtil.getSchemas().forEach(schema ->
                    this.addPrompt(items, token, text, minCorr, TYPE_DATABASE, schema.getName(),
                            null, null, DBSqlQueryPromptCategory.DATABASE)));
        }
        if (token.isPossibilityTable()) {
            tasks.add(() -> ShellDamengQueryUtil.getTables().forEach(table ->
                    this.addPrompt(items, token, text, minCorr, TYPE_TABLE, table.getName(),
                            table.getSchema(), table.getSchema(), DBSqlQueryPromptCategory.TABLE)));
        }
        if (token.isPossibilityView()) {
            tasks.add(() -> ShellDamengQueryUtil.getViews().forEach(view ->
                    this.addPrompt(items, token, text, minCorr, TYPE_VIEW, view.getName(),
                            view.getSchema(), view.getSchema(), DBSqlQueryPromptCategory.VIEW)));
        }
        if (token.isPossibilityFunction()) {
            tasks.add(() -> ShellDamengQueryUtil.getFunctions().forEach(function ->
                    this.addPrompt(items, token, text, minCorr, TYPE_FUNCTION, function.getName(),
                            function.getSchema(), function.getSchema(), DBSqlQueryPromptCategory.FUNCTION)));
        }
        if (token.isPossibilityProcedure()) {
            tasks.add(() -> ShellDamengQueryUtil.getProcedures().forEach(procedure ->
                    this.addPrompt(items, token, text, minCorr, TYPE_PROCEDURE, procedure.getName(),
                            procedure.getSchema(), procedure.getSchema(), DBSqlQueryPromptCategory.PROCEDURE)));
        }
        if (token.isPossibilitySequence()) {
            tasks.add(() -> ShellDamengQueryUtil.getSequences().forEach(sequence ->
                    this.addPrompt(items, token, text, minCorr, TYPE_SEQUENCE, sequence.getName(),
                            sequence.getSchema(), sequence.getSchema(), DBSqlQueryPromptCategory.SEQUENCE)));
        }
        ThreadUtil.submit(tasks);
        return this.sortPrompts(items);
    }
}
