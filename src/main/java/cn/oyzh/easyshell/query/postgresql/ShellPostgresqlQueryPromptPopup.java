package cn.oyzh.easyshell.query.postgresql;

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
public class ShellPostgresqlQueryPromptPopup extends DBQueryPromptPopup<ShellPostgresqlQueryPromptItem, ShellPostgresqlQueryToken> {

    @Override
    protected DBQueryPromptListView<ShellPostgresqlQueryPromptItem> initListView() {
        return new ShellPostgresqlQueryPromptListView();
    }

    @Override
    protected DBQueryTokenAnalyzer<ShellPostgresqlQueryPromptItem, ShellPostgresqlQueryToken> tokenAnalyzer() {
        return ShellPostgresqlQueryTokenAnalyzer.INSTANCE;
    }

    @Override
    public void autoComplete(DBQueryEditor editor, ShellPostgresqlQueryPromptItem item) {
        if (this.token != null) {
            try {
                super.replaceText(editor, item.wrapContent());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
