package cn.oyzh.easyshell.query.h2;

import cn.oyzh.fx.db.query.DBQueryEditor;
import cn.oyzh.fx.db.query.DBQueryPromptPopup;
import cn.oyzh.fx.db.query.DBQueryTokenAnalyzer;
import cn.oyzh.fx.db.query.ui.DBQueryPromptListView;

/**
 * H2 查询提示弹窗。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2QueryPromptPopup extends DBQueryPromptPopup<ShellH2QueryPromptItem, ShellH2QueryToken> {

    @Override
    protected DBQueryPromptListView<ShellH2QueryPromptItem> initListView() {
        return new ShellH2QueryPromptListView();
    }

    @Override
    protected DBQueryTokenAnalyzer<ShellH2QueryPromptItem, ShellH2QueryToken> tokenAnalyzer() {
        return ShellH2QueryTokenAnalyzer.INSTANCE;
    }

    @Override
    public void autoComplete(DBQueryEditor editor, ShellH2QueryPromptItem item) {
        if (this.token != null) {
            try {
                super.replaceText(editor, item.wrapContent());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
