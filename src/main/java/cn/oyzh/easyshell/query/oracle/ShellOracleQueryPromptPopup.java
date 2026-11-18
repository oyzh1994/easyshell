package cn.oyzh.easyshell.query.oracle;

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
public class ShellOracleQueryPromptPopup extends DBQueryPromptPopup<ShellOracleQueryPromptItem, ShellOracleQueryToken> {

    @Override
    protected DBQueryPromptListView<ShellOracleQueryPromptItem> initListView() {
        return new ShellOracleQueryPromptListView();
    }

    @Override
    protected DBQueryTokenAnalyzer<ShellOracleQueryPromptItem, ShellOracleQueryToken> tokenAnalyzer() {
        return ShellOracleQueryTokenAnalyzer.INSTANCE;
    }

    @Override
    public void autoComplete(DBQueryEditor editor, ShellOracleQueryPromptItem item) {
        if (this.token != null) {
            try {
                super.replaceText(editor, item.wrapContent());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
