package cn.oyzh.easyshell.query.mysql;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.DBDialect;
import cn.oyzh.easyshell.mysql.sql.DBSqlParser;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
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
public class MysqlQueryEditor extends Editor {

    /**
     * 提示词组件
     */
    private final MysqlQueryPromptPopup promptPopup = new MysqlQueryPromptPopup();

    /**
     * 方言
     */
    private DBDialect dialect;

    {
        // this.showLineNum();
        this.setOnMouseReleased(e -> this.promptPopup.hide());
        // this.addTextChangeListener((observable, oldValue, newValue) -> this.initTextStyle());
        this.promptPopup.setOnItemSelected(item -> this.promptPopup.autoComplete(this, item));
        this.focusedProperty().addListener((observable, oldValue, newValue) -> this.promptPopup.hide());
        this.setOnKeyReleased(event -> {
            if (KeyboardUtil.isCtrlSlash(event)) {
                this.doComment();
                event.consume();
            } else {
                this.promptPopup.prompt(this, event);
            }
        });
    }

    /**
     * 执行注释
     */
    private void doComment() {
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
                textNew.append("-- ").append(System.lineSeparator());
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
                            str = "-- " + str;
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

    /**
     * 美化sql
     */
    public void pretty() throws Exception {
        String sql = this.getText();
        String prettySql = DBSqlParser.prettySql(sql, this.dialect);
        this.setText(prettySql);
        // this.initTextStyle();
    }

    // /**
    //  * sql关键字正则模式
    //  */
    // private static Pattern Sql_Symbol_Pattern;
    //
    // private static Pattern sqlSymbolPattern() {
    //     if (Sql_Symbol_Pattern == null) {
    //         StringBuilder keywords = new StringBuilder();
    //         for (String keyword : MysqlQueryUtil.getKeywords()) {
    //             keywords.append("|").append(keyword);
    //         }
    //         String regex = "(?i)\\b(" + keywords.substring(1) + ")\\b";
    //         Sql_Symbol_Pattern = Pattern.compile(regex);
    //     }
    //     return Sql_Symbol_Pattern;
    // }
    //
    // /**
    //  * sql注释正则模式
    //  */
    // private static Pattern Sql_Comment_Pattern;
    //
    // private static Pattern sqlCommentPattern() {
    //     if (Sql_Comment_Pattern == null) {
    //         String regex = "#(?:[^\r\n]*|$)|-- (?:[^\r\n]*|$)|/\\*[\\s\\S]*?\\*/";
    //         Sql_Comment_Pattern = Pattern.compile(regex);
    //     }
    //     return Sql_Comment_Pattern;
    // }
    //
    // /**
    //  * 样式标志位
    //  */
    // private final AtomicInteger styleFlag = new AtomicInteger();

    // @Override
    // public synchronized void initTextStyle() {
    //     // 生成标志位
    //     int styleFlagVal = this.styleFlag.incrementAndGet();
    //     Runnable task = () -> {
    //         try {
    //             if (this.styleFlag.get() == styleFlagVal) {
    //                 this.clearTextStyle();
    //                 String text = this.getText();
    //                 if (!text.isEmpty()) {
    //                     List<RichTextStyle> styles = new ArrayList<>();
    //                     Matcher matcher1 = sqlSymbolPattern().matcher(text);
    //                     Matcher matcher2 = sqlCommentPattern().matcher(text);
    //                     while (matcher1.find()) {
    //                         styles.add(new RichTextStyle(matcher1.start(), matcher1.end(), "-fx-fill: #4169E1;"));
    //                     }
    //                     while (matcher2.find()) {
    //                         styles.add(new RichTextStyle(matcher2.start(), matcher2.end(), "-fx-fill: #999999;"));
    //                     }
    //                     this.setStyles(styles);
    //                 }
    //             }
    //         } catch (Exception ex) {
    //             ex.printStackTrace();
    //         }
    //     };
    //     TaskManager.startDelay("query:initTextStyle:" + this.hashCode(), task::run, 150);
    // }


    // @Override
    // public Set<String> getPrompts() {
    //     return new HashSet<>(MysqlQueryUtil.getKeywords());
    // }

    public DBDialect getDialect() {
        return dialect;
    }

    public void setDialect(DBDialect dialect) {
        this.dialect = dialect;
    }

    @Override
    public void initNode() {
        super.initNode();
        super.setFormatType(EditorFormatType.SQL);
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
        if (runCallback != null) {
            runCallback.run();
        }
    }
}
