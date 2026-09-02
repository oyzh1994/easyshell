package cn.oyzh.easyshell.mysql.query;

import cn.oyzh.fx.db.query.DBQueryEditor;
import cn.oyzh.fx.db.query.DBQueryPromptPopup;
import cn.oyzh.fx.db.query.DBQueryTokenAnalyzer;
import cn.oyzh.fx.db.query.ui.DBQueryPromptListView;

/**
 * 查询提示框
 *
 * @author oyzh
 * @since 2024/02/21
 */
public class ShellMysqlQueryPromptPopup extends DBQueryPromptPopup<ShellMysqlQueryPromptItem, ShellMysqlQueryToken> {

    @Override
    protected DBQueryPromptListView<ShellMysqlQueryPromptItem> initListView() {
        return new ShellMysqlQueryPromptListView();
    }

    @Override
    protected DBQueryTokenAnalyzer<ShellMysqlQueryPromptItem, ShellMysqlQueryToken> tokenAnalyzer() {
        return ShellMysqlQueryTokenAnalyzer.INSTANCE;
    }

    @Override
    public void autoComplete(DBQueryEditor editor, ShellMysqlQueryPromptItem item) {
        if (this.token != null) {
            try {
                super.replaceText(editor, item.wrapContent());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
