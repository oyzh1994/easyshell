package cn.oyzh.easyshell.query.mariadb;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.fx.db.query.DBSqlQueryPromptCategory;
import cn.oyzh.fx.db.query.DBSqlQueryTokenAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * MariaDB查询token解析器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbQueryTokenAnalyzer extends DBSqlQueryTokenAnalyzer<ShellMariadbQueryPromptItem, ShellMariadbQueryToken> {

    public static final ShellMariadbQueryTokenAnalyzer INSTANCE = new ShellMariadbQueryTokenAnalyzer();

    @Override
    protected ShellMariadbQueryToken createToken() {
        return new ShellMariadbQueryToken();
    }

    @Override
    protected ShellMariadbQueryPromptItem createPromptItem() {
        return new ShellMariadbQueryPromptItem();
    }

    @Override
    public List<ShellMariadbQueryPromptItem> initPrompts(ShellMariadbQueryToken token, float minCorr) {
        if (token == null) {
            return Collections.emptyList();
        }
        String text = token.getContent() == null ? "" : token.getContent();
        List<ShellMariadbQueryPromptItem> items = new CopyOnWriteArrayList<>();
        List<Runnable> tasks = new ArrayList<>();
        if (token.isPossibilityKeyword()) {
            tasks.add(() -> ShellMariadbQueryUtil.getKeywords().forEach(keyword ->
                    this.addPrompt(items, token, text, minCorr, TYPE_KEYWORD, keyword,
                            null, null, DBSqlQueryPromptCategory.KEYWORD)));
        }
        if (token.isPossibilityDatabase()) {
            tasks.add(() -> ShellMariadbQueryUtil.getDatabases().forEach(database ->
                    this.addPrompt(items, token, text, minCorr, TYPE_DATABASE, database.getName(),
                            null, null, DBSqlQueryPromptCategory.DATABASE)));
        }
        if (token.isPossibilityTable()) {
            tasks.add(() -> ShellMariadbQueryUtil.getTables().forEach(table ->
                    this.addPrompt(items, token, text, minCorr, TYPE_TABLE, table.getName(),
                            table.getDbName(), table.getDbName(), DBSqlQueryPromptCategory.TABLE)));
        }
        if (token.isPossibilityView()) {
            tasks.add(() -> ShellMariadbQueryUtil.getViews().forEach(view ->
                    this.addPrompt(items, token, text, minCorr, TYPE_VIEW, view.getName(),
                            view.getDbName(), view.getDbName(), DBSqlQueryPromptCategory.VIEW)));
        }
        if (token.isPossibilityFunction()) {
            tasks.add(() -> ShellMariadbQueryUtil.getFunctions().forEach(function ->
                    this.addPrompt(items, token, text, minCorr, TYPE_FUNCTION, function.getName(),
                            function.getDbName(), function.getDbName(), DBSqlQueryPromptCategory.FUNCTION)));
        }
        if (token.isPossibilityProcedure()) {
            tasks.add(() -> ShellMariadbQueryUtil.getProcedures().forEach(procedure ->
                    this.addPrompt(items, token, text, minCorr, TYPE_PROCEDURE, procedure.getName(),
                            procedure.getDbName(), procedure.getDbName(), DBSqlQueryPromptCategory.PROCEDURE)));
        }
        if (token.isPossibilityEvent()) {
            tasks.add(() -> ShellMariadbQueryUtil.getEvents().forEach(event ->
                    this.addPrompt(items, token, text, minCorr, TYPE_EVENT, event.getName(),
                            event.getDbName(), event.getDbName(), DBSqlQueryPromptCategory.EVENT)));
        }
        ThreadUtil.submit(tasks);
        return this.sortPrompts(items);
    }
}
