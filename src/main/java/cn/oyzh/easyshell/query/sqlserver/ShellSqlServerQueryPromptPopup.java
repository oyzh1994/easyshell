package cn.oyzh.easyshell.query.sqlserver;

import cn.oyzh.fx.db.query.DBQueryEditor;
import cn.oyzh.fx.db.query.DBQueryPromptPopup;
import cn.oyzh.fx.db.query.DBQueryTokenAnalyzer;
import cn.oyzh.fx.db.query.ui.DBQueryPromptListView;

/**
 * 查询提示框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerQueryPromptPopup extends DBQueryPromptPopup<ShellSqlServerQueryPromptItem, ShellSqlServerQueryToken> {

    @Override
    protected DBQueryPromptListView<ShellSqlServerQueryPromptItem> initListView() {
        return new ShellSqlServerQueryPromptListView();
    }

    @Override
    protected DBQueryTokenAnalyzer<ShellSqlServerQueryPromptItem, ShellSqlServerQueryToken> tokenAnalyzer() {
        return ShellSqlServerQueryTokenAnalyzer.INSTANCE;
    }

    @Override
    public void autoComplete(DBQueryEditor editor, ShellSqlServerQueryPromptItem item) {
        if (this.token != null) {
            try {
                super.replaceText(editor, item.wrapContent());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
