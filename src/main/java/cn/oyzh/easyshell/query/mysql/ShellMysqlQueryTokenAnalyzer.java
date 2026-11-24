package cn.oyzh.easyshell.query.mysql;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.fx.db.query.DBSqlQueryPromptCategory;
import cn.oyzh.fx.db.query.DBSqlQueryTokenAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * mysql查询token解析器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlQueryTokenAnalyzer extends DBSqlQueryTokenAnalyzer<ShellMysqlQueryPromptItem, ShellMysqlQueryToken> {

    public static final ShellMysqlQueryTokenAnalyzer INSTANCE = new ShellMysqlQueryTokenAnalyzer();

    @Override
    protected ShellMysqlQueryToken createToken() {
        return new ShellMysqlQueryToken();
    }

    @Override
    protected ShellMysqlQueryPromptItem createPromptItem() {
        return new ShellMysqlQueryPromptItem();
    }

    @Override
    public List<ShellMysqlQueryPromptItem> initPrompts(ShellMysqlQueryToken token, float minCorr) {
        if (token == null) {
            return Collections.emptyList();
        }
        String text = token.getContent() == null ? "" : token.getContent();
        List<ShellMysqlQueryPromptItem> items = new CopyOnWriteArrayList<>();
        List<Runnable> tasks = new ArrayList<>();
        if (token.isPossibilityKeyword()) {
            tasks.add(() -> ShellMysqlQueryUtil.getKeywords().forEach(keyword ->
                    this.addPrompt(items, token, text, minCorr, TYPE_KEYWORD, keyword,
                            null, null, DBSqlQueryPromptCategory.KEYWORD)));
        }
        if (token.isPossibilityDatabase()) {
            tasks.add(() -> ShellMysqlQueryUtil.getDatabases().forEach(database ->
                    this.addPrompt(items, token, text, minCorr, TYPE_DATABASE, database.getName(),
                            null, null, DBSqlQueryPromptCategory.DATABASE)));
        }
        if (token.isPossibilityTable()) {
            tasks.add(() -> ShellMysqlQueryUtil.getTables().forEach(table ->
                    this.addPrompt(items, token, text, minCorr, TYPE_TABLE, table.getName(),
                            table.getDbName(), table.getDbName(), DBSqlQueryPromptCategory.TABLE)));
        }
        if (token.isPossibilityView()) {
            tasks.add(() -> ShellMysqlQueryUtil.getViews().forEach(view ->
                    this.addPrompt(items, token, text, minCorr, TYPE_VIEW, view.getName(),
                            view.getDbName(), view.getDbName(), DBSqlQueryPromptCategory.VIEW)));
        }
        if (token.isPossibilityFunction()) {
            tasks.add(() -> ShellMysqlQueryUtil.getFunctions().forEach(function ->
                    this.addPrompt(items, token, text, minCorr, TYPE_FUNCTION, function.getName(),
                            function.getDbName(), function.getDbName(), DBSqlQueryPromptCategory.FUNCTION)));
        }
        if (token.isPossibilityProcedure()) {
            tasks.add(() -> ShellMysqlQueryUtil.getProcedures().forEach(procedure ->
                    this.addPrompt(items, token, text, minCorr, TYPE_PROCEDURE, procedure.getName(),
                            procedure.getDbName(), procedure.getDbName(), DBSqlQueryPromptCategory.PROCEDURE)));
        }
        if (token.isPossibilityEvent()) {
            tasks.add(() -> ShellMysqlQueryUtil.getEvents().forEach(event ->
                    this.addPrompt(items, token, text, minCorr, TYPE_EVENT, event.getName(),
                            event.getDbName(), event.getDbName(), DBSqlQueryPromptCategory.EVENT)));
        }
        ThreadUtil.submit(tasks);
        return this.sortPrompts(items);
    }
}
