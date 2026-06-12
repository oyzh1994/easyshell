package cn.oyzh.easyshell.query.mysql;

import cn.oyzh.easyshell.query.ShellQueryEditor;
import cn.oyzh.easyshell.query.ShellQueryPromptListView;
import cn.oyzh.easyshell.query.ShellQueryPromptPopup;
import cn.oyzh.easyshell.query.ShellQueryTokenAnalyzer;

/**
 * 查询提示框
 *
 * @author oyzh
 * @since 2024/02/21
 */
public class ShellMysqlQueryPromptPopup extends ShellQueryPromptPopup<ShellMysqlQueryPromptItem, ShellMysqlQueryToken> {

    @Override
    protected ShellQueryPromptListView<ShellMysqlQueryPromptItem> initListView() {
        return new ShellMysqlQueryPromptListView();
    }

    @Override
    protected ShellQueryTokenAnalyzer<ShellMysqlQueryPromptItem, ShellMysqlQueryToken> tokenAnalyzer() {
        return ShellMysqlQueryTokenAnalyzer.INSTANCE;
    }

    @Override
    public void autoComplete(ShellQueryEditor editor, ShellMysqlQueryPromptItem item) {
        if (this.token != null) {
            try {
                super.replaceText(editor, item.wrapContent());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
