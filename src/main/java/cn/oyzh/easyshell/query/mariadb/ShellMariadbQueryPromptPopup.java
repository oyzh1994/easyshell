package cn.oyzh.easyshell.query.mariadb;

import cn.oyzh.fx.db.query.DBQueryEditor;
import cn.oyzh.fx.db.query.DBQueryPromptPopup;
import cn.oyzh.fx.db.query.DBQueryTokenAnalyzer;
import cn.oyzh.fx.db.query.ui.DBQueryPromptListView;

/**
 * 查询提示框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbQueryPromptPopup extends DBQueryPromptPopup<ShellMariadbQueryPromptItem, ShellMariadbQueryToken> {

    @Override
    protected DBQueryPromptListView<ShellMariadbQueryPromptItem> initListView() {
        return new ShellMariadbQueryPromptListView();
    }

    @Override
    protected DBQueryTokenAnalyzer<ShellMariadbQueryPromptItem, ShellMariadbQueryToken> tokenAnalyzer() {
        return ShellMariadbQueryTokenAnalyzer.INSTANCE;
    }

    @Override
    public void autoComplete(DBQueryEditor editor, ShellMariadbQueryPromptItem item) {
        if (this.token != null) {
            try {
                super.replaceText(editor, item.wrapContent());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
