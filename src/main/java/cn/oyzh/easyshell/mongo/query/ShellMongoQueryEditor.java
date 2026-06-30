package cn.oyzh.easyshell.mongo.query;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.query.ShellQueryEditor;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * db查询文本域
 *
 * @author oyzh
 * @since 2024/02/18
 */
public class ShellMongoQueryEditor extends ShellQueryEditor {

    /**
     * 提示词组件
     */
    private ShellMongoQueryPromptPopup promptPopup;

    @Override
    protected ShellMongoQueryPromptPopup promptPopup() {
        if (this.promptPopup == null) {
            this.promptPopup = new ShellMongoQueryPromptPopup();
        }
        return this.promptPopup;
    }

    @Override
    public void initNode() {
        this.setFormatType(EditorFormatType.SQL);
        this.promptPopup().setOnItemSelected(item -> this.promptPopup().autoComplete(this, item));
        super.initNode();
    }

    @Override
    protected void doComment() {
        try {
            // 选区
            IndexRange range = this.getSelectionRange();
            if (range == null) {
                return;
            }
            // 选区范围
            int end = range.getEnd();
            int start = range.getStart();
            // 内容
            String text = this.getText();
            // 变更标志位
            AtomicBoolean changed = new AtomicBoolean(false);
            // 修正的选区开始、结束
            int fixedStart, fixedEnd;
            // 新内容
            StringBuilder textNew = new StringBuilder();
            // 无内容
            if (StringUtil.isEmpty(text)) {
                textNew.append("// ").append(System.lineSeparator());
                changed.set(true);
                fixedStart = fixedEnd = -3;
            } else {
                // 获取选区行
                Map<Integer, String> lines = super.getSelectionLines();
                // 反注释标志位
                AtomicBoolean undoComment = new AtomicBoolean(true);
                for (String value : lines.values()) {
                    // 如果有内容不是以注释开头，则执行反注释
                    if (!value.stripLeading().startsWith("-- ")) {
                        undoComment.set(false);
                        break;
                    }
                }
                fixedStart = undoComment.get() ? -3 : 3;
                fixedEnd = lines.size() * fixedStart;
                // 文字长度
                AtomicLong textLen = new AtomicLong();
                // 遍历行
                text.lines().forEach(str -> {
                    int len = str.length();
                    // 行开始
                    long lineStart = textLen.get();
                    // 行结束
                    long lineEnd = textLen.get() + len + 1;
                    // 判断边距
                    if (NumberUtil.checkBound(lineStart, lineEnd, start, end)) {
                        if (undoComment.get()) {
                            str = str.stripLeading().substring(3);
                        } else {
                            str = "// " + str;
                        }
                        changed.set(true);
                    }
                    // 追加内容
                    textNew.append(str).append("\n");
                    // 追加长度
                    textLen.addAndGet(len + 1);
                });
            }
            // 判断是否变更
            if (changed.get()) {
                // 设置内容
                this.setText(textNew.substring(0, textNew.length() - 1));
                // 修正选区
                this.selectRange(start + fixedStart, end + fixedEnd);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    /**
     * 运行回调
     */
    private Runnable runCallback;

    public void setRunCallback(Runnable runCallback) {
        this.runCallback = runCallback;
    }

    /**
     * 运行
     */
    protected void run() {
        if (this.runCallback != null) {
            this.runCallback.run();
        }
    }
}
