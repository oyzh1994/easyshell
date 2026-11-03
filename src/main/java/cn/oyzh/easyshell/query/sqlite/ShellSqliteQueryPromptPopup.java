package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.fx.db.query.DBQueryEditor;
import cn.oyzh.fx.db.query.DBQueryPromptPopup;
import cn.oyzh.fx.db.query.DBQueryTokenAnalyzer;
import cn.oyzh.fx.db.query.ui.DBQueryPromptListView;

/**
 * SQLite 查询提示弹窗。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryPromptPopup extends DBQueryPromptPopup<ShellSqliteQueryPromptItem, ShellSqliteQueryToken> {

    @Override
    protected DBQueryPromptListView<ShellSqliteQueryPromptItem> initListView() {
        return new ShellSqliteQueryPromptListView();
    }

    @Override
    protected DBQueryTokenAnalyzer<ShellSqliteQueryPromptItem, ShellSqliteQueryToken> tokenAnalyzer() {
        return ShellSqliteQueryTokenAnalyzer.INSTANCE;
    }

    @Override
    public void autoComplete(DBQueryEditor editor, ShellSqliteQueryPromptItem item) {
        if (this.token != null) {
            try {
                super.replaceText(editor, item.wrapContent());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

