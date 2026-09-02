package cn.oyzh.easyshell.dameng.query;

import cn.oyzh.easyshell.dameng.query.ShellDamengQueryToken;
import cn.oyzh.easyshell.dameng.query.ShellDamengQueryTokenAnalyzer;
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
public class ShellDamengQueryPromptPopup extends DBQueryPromptPopup<ShellDamengQueryPromptItem, ShellDamengQueryToken> {

    @Override
    protected DBQueryPromptListView<ShellDamengQueryPromptItem> initListView() {
        return new ShellDamengQueryPromptListView();
    }

    @Override
    protected DBQueryTokenAnalyzer<ShellDamengQueryPromptItem, ShellDamengQueryToken> tokenAnalyzer() {
        return ShellDamengQueryTokenAnalyzer.INSTANCE;
    }

    @Override
    public void autoComplete(DBQueryEditor editor, ShellDamengQueryPromptItem item) {
        if (this.token != null) {
            try {
                super.replaceText(editor, item.wrapContent());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
