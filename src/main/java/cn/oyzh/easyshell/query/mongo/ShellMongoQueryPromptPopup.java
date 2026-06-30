package cn.oyzh.easyshell.query.mongo;

import cn.oyzh.easyshell.query.ShellQueryPromptListView;
import cn.oyzh.easyshell.query.ShellQueryPromptPopup;
import cn.oyzh.easyshell.query.ShellQueryTokenAnalyzer;

/**
 * 查询提示框
 *
 * @author oyzh
 * @since 2024/02/21
 */
public class ShellMongoQueryPromptPopup extends ShellQueryPromptPopup<ShellMongoQueryPromptItem, ShellMongoQueryToken> {

    @Override
    protected ShellQueryPromptListView<ShellMongoQueryPromptItem> initListView() {
        return new ShellMongoQueryPromptListView();
    }

    @Override
    protected ShellQueryTokenAnalyzer<ShellMongoQueryPromptItem, ShellMongoQueryToken> tokenAnalyzer() {
        return ShellMongoQueryTokenAnalyzer.INSTANCE;
    }
}
