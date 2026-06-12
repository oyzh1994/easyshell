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
public class MysqlQueryPromptPopup extends ShellQueryPromptPopup<MysqlQueryPromptItem, MysqlQueryToken> {

    @Override
    protected ShellQueryPromptListView<MysqlQueryPromptItem> initListView() {
        return new MysqlQueryPromptListView();
    }

    @Override
    protected ShellQueryTokenAnalyzer<MysqlQueryPromptItem, MysqlQueryToken> tokenAnalyzer() {
        return MysqlQueryTokenAnalyzer.INSTANCE;
    }

    @Override
    public void autoComplete(ShellQueryEditor editor, MysqlQueryPromptItem item) {
        if (this.token != null) {
            try {
                super.replaceText(editor, item.wrapContent());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
