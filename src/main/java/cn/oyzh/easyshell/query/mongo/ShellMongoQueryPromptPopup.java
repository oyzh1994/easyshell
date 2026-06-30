package cn.oyzh.easyshell.query.mongo;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.query.ShellQueryEditor;
import cn.oyzh.easyshell.query.ShellQueryPromptListView;
import cn.oyzh.easyshell.query.ShellQueryPromptPopup;
import cn.oyzh.easyshell.query.ShellQueryTokenAnalyzer;
import cn.oyzh.easyshell.query.ShellQueryUtil;
import cn.oyzh.easyshell.query.mysql.ShellMysqlQueryPromptItem;
import cn.oyzh.easyshell.query.mysql.ShellMysqlQueryPromptListView;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.thread.RenderService;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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
