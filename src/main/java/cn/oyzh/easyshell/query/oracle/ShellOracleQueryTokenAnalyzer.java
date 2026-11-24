package cn.oyzh.easyshell.query.oracle;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.fx.db.query.DBSqlQueryPromptCategory;
import cn.oyzh.fx.db.query.DBSqlQueryTokenAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Oracle查询token解析器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleQueryTokenAnalyzer extends DBSqlQueryTokenAnalyzer<ShellOracleQueryPromptItem, ShellOracleQueryToken> {

    public static final ShellOracleQueryTokenAnalyzer INSTANCE = new ShellOracleQueryTokenAnalyzer();

    @Override
    protected ShellOracleQueryToken createToken() {
        return new ShellOracleQueryToken();
    }

    @Override
    protected ShellOracleQueryPromptItem createPromptItem() {
        return new ShellOracleQueryPromptItem();
    }

    @Override
    public List<ShellOracleQueryPromptItem> initPrompts(ShellOracleQueryToken token, float minCorr) {
        if (token == null) {
            return Collections.emptyList();
        }
        String text = token.getContent() == null ? "" : token.getContent();
        List<ShellOracleQueryPromptItem> items = new CopyOnWriteArrayList<>();
        List<Runnable> tasks = new ArrayList<>();
        if (token.isPossibilityKeyword()) {
            tasks.add(() -> ShellOracleQueryUtil.getKeywords().forEach(keyword ->
                    this.addPrompt(items, token, text, minCorr, TYPE_KEYWORD, keyword,
                            null, null, DBSqlQueryPromptCategory.KEYWORD)));
        }
        if (token.isPossibilityDatabase()) {
            tasks.add(() -> ShellOracleQueryUtil.getSchemas().forEach(schema ->
                    this.addPrompt(items, token, text, minCorr, TYPE_DATABASE, schema.getName(),
                            null, null, DBSqlQueryPromptCategory.DATABASE)));
        }
        if (token.isPossibilityTable()) {
            tasks.add(() -> ShellOracleQueryUtil.getTables().forEach(table ->
                    this.addPrompt(items, token, text, minCorr, TYPE_TABLE, table.getName(),
                            table.getSchema(), table.getSchema(), DBSqlQueryPromptCategory.TABLE)));
        }
        if (token.isPossibilityView()) {
            tasks.add(() -> ShellOracleQueryUtil.getViews().forEach(view ->
                    this.addPrompt(items, token, text, minCorr, TYPE_VIEW, view.getName(),
                            view.getSchema(), view.getSchema(), DBSqlQueryPromptCategory.VIEW)));
        }
        if (token.isPossibilityFunction()) {
            tasks.add(() -> ShellOracleQueryUtil.getFunctions().forEach(function ->
                    this.addPrompt(items, token, text, minCorr, TYPE_FUNCTION, function.getName(),
                            function.getSchema(), function.getSchema(), DBSqlQueryPromptCategory.FUNCTION)));
        }
        if (token.isPossibilityProcedure()) {
            tasks.add(() -> ShellOracleQueryUtil.getProcedures().forEach(procedure ->
                    this.addPrompt(items, token, text, minCorr, TYPE_PROCEDURE, procedure.getName(),
                            procedure.getSchema(), procedure.getSchema(), DBSqlQueryPromptCategory.PROCEDURE)));
        }
        if (token.isPossibilitySequence()) {
            tasks.add(() -> ShellOracleQueryUtil.getSequences().forEach(sequence ->
                    this.addPrompt(items, token, text, minCorr, TYPE_SEQUENCE, sequence.getName(),
                            sequence.getSchema(), sequence.getSchema(), DBSqlQueryPromptCategory.SEQUENCE)));
        }
        ThreadUtil.submit(tasks);
        return this.sortPrompts(items);
    }
}
