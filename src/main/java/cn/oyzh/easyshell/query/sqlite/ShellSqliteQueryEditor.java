package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.query.DBQueryEditor;
import cn.oyzh.fx.db.sql.DBSqlParser;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SQLite SQL 查询编辑器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryEditor extends DBQueryEditor {

    /**
     * 数据库类型
     */
    private DBDialect dialect;

    private ShellSqliteQueryPromptPopup promptPopup;

    private Runnable runCallback;

    /**
     * 获取数据库类型
     *
     * @return 数据库类型
     */
    public DBDialect getDialect() {
        return this.dialect;
    }

    /**
     * 设置数据库类型
     *
     * @param dialect 数据库类型
     */
    public void setDialect(DBDialect dialect) {
        this.dialect = dialect;
    }

    @Override
    protected ShellSqliteQueryPromptPopup promptPopup() {
        if (this.promptPopup == null) {
            this.promptPopup = new ShellSqliteQueryPromptPopup();
        }
        return this.promptPopup;
    }

    @Override
    public void initNode() {
        super.initNode();
        this.promptPopup().setOnItemSelected(item -> this.promptPopup().autoComplete(this, item));
        this.setFormatType(EditorFormatType.SQL);
    }

    @Override
    protected void doComment() {
        try {
            IndexRange range = this.getSelectionRange();
            if (range == null) {
                return;
            }
            int start = range.getStart();
            int end = range.getEnd();
            String text = this.getText();
            if (StringUtil.isEmpty(text)) {
                this.setText("-- ");
                this.selectRange(3, 3);
                return;
            }
            Map<Integer, String> lines = super.getSelectionLines();
            AtomicBoolean uncomment = new AtomicBoolean(true);
            for (String value : lines.values()) {
                if (!value.stripLeading().startsWith("-- ")) {
                    uncomment.set(false);
                    break;
                }
            }
            int delta = uncomment.get() ? -3 : 3;
            AtomicLong textLength = new AtomicLong();
            StringBuilder content = new StringBuilder();
            AtomicBoolean changed = new AtomicBoolean();
            text.lines().forEach(line -> {
                long lineStart = textLength.get();
                long lineEnd = lineStart + line.length() + 1;
                if (NumberUtil.checkBound(lineStart, lineEnd, start, end)) {
                    if (uncomment.get()) {
                        line = line.stripLeading().substring(3);
                    } else {
                        line = "-- " + line;
                    }
                    changed.set(true);
                }
                content.append(line).append('\n');
                textLength.addAndGet(line.length() + 1L);
            });
            if (changed.get()) {
                this.setText(content.substring(0, content.length() - 1));
                this.selectRange(Math.max(0, start + delta), Math.max(0, end + delta * lines.size()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 美化SQL。
     */
    public void pretty() {
        this.setText(DBSqlParser.prettySql(this.getText(), this.dialect));
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        if (this.isSelectedText()) {
            FXMenuItem menuItem = MenuItemHelper.runSelected(this::run);
            menuItems.add(menuItem);
        }
        if (!menuItems.isEmpty()) {
            menuItems.add(MenuItemHelper.separator());
        }
        menuItems.addAll(super.getMenuItems());
        return menuItems;
    }

    public void setRunCallback(Runnable runCallback) {
        this.runCallback = runCallback;
    }

    protected void run() {
        if (this.runCallback != null) {
            this.runCallback.run();
        }
    }

    @Override
    public Font getEditorFont() {
        ShellSetting setting = ShellSettingStore.SETTING;
        return FontManager.toFont(setting.editorFontConfig());
    }
}
