package cn.oyzh.easyshell.query.postgresql;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.fx.db.query.DBSqlQueryPromptCategory;
import cn.oyzh.fx.db.query.DBSqlQueryTokenAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * PostgreSQL查询token解析器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlQueryTokenAnalyzer extends DBSqlQueryTokenAnalyzer<ShellPostgresqlQueryPromptItem, ShellPostgresqlQueryToken> {

    public static final ShellPostgresqlQueryTokenAnalyzer INSTANCE = new ShellPostgresqlQueryTokenAnalyzer();

    @Override
    protected ShellPostgresqlQueryToken createToken() {
        return new ShellPostgresqlQueryToken();
    }

    @Override
    protected ShellPostgresqlQueryPromptItem createPromptItem() {
        return new ShellPostgresqlQueryPromptItem();
    }

    @Override
    public List<ShellPostgresqlQueryPromptItem> initPrompts(ShellPostgresqlQueryToken token, float minCorr) {
        if (token == null) {
            return Collections.emptyList();
        }
        String text = token.getContent() == null ? "" : token.getContent();
        List<ShellPostgresqlQueryPromptItem> items = new CopyOnWriteArrayList<>();
        List<Runnable> tasks = new ArrayList<>();
        if (token.isPossibilityKeyword()) {
            tasks.add(() -> ShellPostgresqlQueryUtil.getKeywords().forEach(keyword ->
                    this.addPrompt(items, token, text, minCorr, TYPE_KEYWORD, keyword,
                            null, null, DBSqlQueryPromptCategory.KEYWORD)));
        }
        if (token.isPossibilityDatabase()) {
            tasks.add(() -> ShellPostgresqlQueryUtil.getSchemas().forEach(schema ->
                    this.addPrompt(items, token, text, minCorr, TYPE_DATABASE, schema.getName(),
                            null, null, DBSqlQueryPromptCategory.DATABASE)));
        }
        if (token.isPossibilityTable()) {
            tasks.add(() -> ShellPostgresqlQueryUtil.getTables().forEach(table ->
                    this.addPrompt(items, token, text, minCorr, TYPE_TABLE, table.getName(),
                            table.getSchema(), table.getSchema(), DBSqlQueryPromptCategory.TABLE)));
        }
        if (token.isPossibilityView()) {
            tasks.add(() -> ShellPostgresqlQueryUtil.getViews().forEach(view ->
                    this.addPrompt(items, token, text, minCorr, TYPE_VIEW, view.getName(),
                            view.getSchema(), view.getSchema(), DBSqlQueryPromptCategory.VIEW)));
        }
        if (token.isPossibilityFunction()) {
            tasks.add(() -> ShellPostgresqlQueryUtil.getFunctions().forEach(function ->
                    this.addPrompt(items, token, text, minCorr, TYPE_FUNCTION, function.getName(),
                            function.getSchema(), function.getSchema(), DBSqlQueryPromptCategory.FUNCTION)));
        }
        if (token.isPossibilityProcedure()) {
            tasks.add(() -> ShellPostgresqlQueryUtil.getProcedures().forEach(procedure ->
                    this.addPrompt(items, token, text, minCorr, TYPE_PROCEDURE, procedure.getName(),
                            procedure.getSchema(), procedure.getSchema(), DBSqlQueryPromptCategory.PROCEDURE)));
        }
        if (token.isPossibilitySequence()) {
            tasks.add(() -> ShellPostgresqlQueryUtil.getSequences().forEach(sequence ->
                    this.addPrompt(items, token, text, minCorr, TYPE_SEQUENCE, sequence.getName(),
                            sequence.getSchema(), sequence.getSchema(), DBSqlQueryPromptCategory.SEQUENCE)));
        }
        ThreadUtil.submit(tasks);
        return this.sortPrompts(items);
    }
}
