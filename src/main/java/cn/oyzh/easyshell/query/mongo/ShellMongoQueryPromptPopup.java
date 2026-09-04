package cn.oyzh.easyshell.query.mongo;

import cn.oyzh.fx.db.query.DBQueryPromptPopup;
import cn.oyzh.fx.db.query.DBQueryTokenAnalyzer;
import cn.oyzh.fx.db.query.ui.DBQueryPromptListView;

/**
 * 查询提示框
 *
 * @author oyzh
 * @since 2024/02/21
 */
public class ShellMongoQueryPromptPopup extends DBQueryPromptPopup<ShellMongoQueryPromptItem, ShellMongoQueryToken> {

    @Override
    protected DBQueryPromptListView<ShellMongoQueryPromptItem> initListView() {
        return new ShellMongoQueryPromptListView();
    }

    @Override
    protected DBQueryTokenAnalyzer<ShellMongoQueryPromptItem, ShellMongoQueryToken> tokenAnalyzer() {
        return ShellMongoQueryTokenAnalyzer.INSTANCE;
    }

    @Override
    protected boolean tokenAvailable() {
        return this.token != null && (this.token.isPossibilityKeyword() || this.token.isPossibilityFunction() || this.token.isNotEmpty());
    }
}
