package cn.oyzh.easyshell.query.sqlserver;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.query.DBQueryEditor;
import cn.oyzh.fx.db.query.DBQueryPromptPopup;
import cn.oyzh.fx.db.query.DBQueryTokenAnalyzer;
import cn.oyzh.fx.db.query.ui.DBQueryPromptListView;
import cn.oyzh.fx.db.util.DBUtil;

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
    protected boolean tokenAvailable() {
        return super.tokenAvailable()
                || (this.token != null && this.token.isOpenBracketContext());
    }

    @Override
    public void autoComplete(DBQueryEditor editor, ShellSqlServerQueryPromptItem item) {
        if (this.token != null) {
            try {
                String content;
                if (this.token.isOpenBracketContext()) {
                    String text = editor.getText();
                    int endIndex = this.token.getEndIndex();
                    boolean hasClosingBracket = endIndex < text.length() && text.charAt(endIndex) == ']';
                    content = hasClosingBracket
                            ? item.getContent()
                            : DBUtil.wrap(item.getContent(), DBDialect.SQLSERVER);
                } else {
                    content = item.wrapContent();
                }
                super.replaceText(editor, content);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
