package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.EditorFold;
import cn.oyzh.fx.editor.incubator.EditorHeading;
import cn.oyzh.fx.editor.incubator.EditorSkin;
import cn.oyzh.fx.editor.incubator.EditorSyntaxAsyncStrategy;
import cn.oyzh.fx.editor.incubator.EditorSyntaxDecorator;
import cn.oyzh.fx.editor.incubator.EditorSyntaxStrategy;
import cn.oyzh.fx.editor.incubator.control.LLMEditor;
import cn.oyzh.fx.plus.menu.MenuItemManager;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import com.sun.jfx.incubator.scene.control.richtext.VFlow;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 只读消息视图，基于 {@link LLMEditor}，支持选择、复制、语法高亮与表情符号。
 * <p>
 * 消息列表使用虚拟滚动，消息高度由其内容决定，因此这里需要自行计算首选高度：
 * 宽度未知时用纯文本测量估算；实际布局后再从内部 {@link VFlow} 读取精确高度并修正。
 * 由于消息会流式追加，内容更新会按界面脉冲合并，语法着色在后台执行，避免阻塞界面。
 *
 * @author oyzh
 * @since 2026-11-06
 */
public final class LLMMessageView extends LLMEditor {

    private static final Color THINKING_LIGHT_COLOR = Color.web("#6E7781");

    private static final Color THINKING_DARK_COLOR = Color.web("#8B949E");

    /**
     * 默认高度
     */
    private static final double DEFAULT_HEIGHT = 20;

    /**
     * 文本测量器
     */
    private final Text measurer = new Text();

    /**
     * 测量宽度，由单元格绑定为控件最大宽度
     */
    private final DoubleProperty measurementWidth = new SimpleDoubleProperty(-1);

    /**
     * 已测量的宽度
     */
    private double measuredWidth = -1;

    /**
     * 已测量的高度
     */
    private double measuredHeight = DEFAULT_HEIGHT;

    /**
     * 已测量的首选宽度
     */
    private double measuredPrefWidth = -1;

    /**
     * 待更新文本
     */
    private String pendingText;

    /**
     * 待更新格式
     */
    private EditorFormatType pendingFormat;

    /**
     * 是否折叠已结束的思考链
     */
    private boolean pendingThinkingCollapsed;

    /**
     * 内容更新是否已调度
     */
    private boolean updateScheduled;

    /**
     * 高度修正是否已调度
     */
    private boolean refineScheduled;

    /**
     * 折叠状态变化处理器
     */
    private Consumer<List<EditorFold>> foldChangeHandler;

    /**
     * 外部提供的折叠状态
     */
    private Map<String, Boolean> foldStates = Map.of();

    public LLMMessageView() {
        this.getStyleClass().add("llm-message-text");
        this.setEditable(false);
        this.setWrapText(true);
        this.hideLineNum();
        this.setHighlightCurrentParagraph(false);
        this.setContentPadding(Insets.EMPTY);
        this.setBorder(Border.EMPTY);
        this.setMinWidth(0);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setFocusTraversable(true);
        this.setUseContentHeight(true);
        this.setFormatType(EditorFormatType.LLM_CHAT);
        // 消息内容会频繁流式更新，着色统一放到后台执行
        if (this.getSyntaxDecorator() instanceof EditorSyntaxDecorator decorator) {
            decorator.setSyntaxStrategy(EditorSyntaxStrategy.ASYNC);
            decorator.setSyntaxAsyncStrategy(EditorSyntaxAsyncStrategy.ALWAYS);
        }
        this.measurementWidth.addListener((observable, oldValue, newValue) -> this.invalidateSize());
        this.fontProperty().addListener((observable, oldValue, newValue) -> this.invalidateSize());
        this.addEventFilter(ScrollEvent.SCROLL, this::forwardScroll);
    }

    /**
     * 设置消息内容
     *
     * @param text       消息内容
     * @param formatType 内容格式
     */
    public void setMessage(String text, EditorFormatType formatType) {
        this.setMessage(text, formatType, false);
    }

    /**
     * 设置消息内容。
     *
     * @param text              消息内容
     * @param formatType        内容格式
     * @param thinkingCollapsed 是否折叠已结束的思考链
     */
    public void setMessage(String text, EditorFormatType formatType, boolean thinkingCollapsed) {
        this.pendingText = text == null ? "" : text;
        this.pendingFormat = formatType == null ? EditorFormatType.RAW : formatType;
        this.pendingThinkingCollapsed = thinkingCollapsed;
        if (this.updateScheduled) {
            return;
        }
        this.updateScheduled = true;
        FXUtil.runLater(() -> {
            this.updateScheduled = false;
            this.applyMessage(true);
        });
    }

    /**
     * 立即设置消息内容，用于单元格首次显示，保证测量时内容已就绪
     *
     * @param text       消息内容
     * @param formatType 内容格式
     */
    public void setMessageNow(String text, EditorFormatType formatType) {
        this.setMessageNow(text, formatType, false);
    }

    /**
     * 立即设置消息内容，用于单元格首次显示。
     *
     * @param text              消息内容
     * @param formatType        内容格式
     * @param thinkingCollapsed 是否折叠已结束的思考链
     */
    public void setMessageNow(String text, EditorFormatType formatType,
                              boolean thinkingCollapsed) {
        this.pendingText = text == null ? "" : text;
        this.pendingFormat = formatType == null ? EditorFormatType.RAW : formatType;
        this.pendingThinkingCollapsed = thinkingCollapsed;
        this.updateScheduled = false;
        this.applyMessage(false);
    }

    /**
     * 应用待更新消息
     */
    private void applyMessage(boolean notifyFoldChanges) {
        String text = this.pendingText == null ? "" : this.pendingText;
        EditorFormatType formatType = this.pendingFormat == null ? EditorFormatType.RAW : this.pendingFormat;
        if (StringUtil.equals(text, this.getText())) {
            if (this.getFormatType() != formatType) {
                this.setFormatType(formatType);
            }
        } else {
            this.showData(text, formatType);
        }
        this.applyThinkingFolds(text, this.pendingThinkingCollapsed, notifyFoldChanges);
        this.invalidateSize();
    }

    /**
     * 设置折叠状态变化处理器。
     *
     * @param handler 处理器
     */
    public void setFoldChangeHandler(Consumer<List<EditorFold>> handler) {
        this.foldChangeHandler = handler;
    }

    /**
     * 设置外部折叠状态。
     *
     * @param states 折叠状态
     */
    public void setFoldStates(Map<String, Boolean> states) {
        this.foldStates = states == null ? Map.of() : Map.copyOf(states);
    }

    /**
     * 根据思考链标题计算可折叠段落。
     *
     * @param text      消息内容
     * @param collapsed 是否折叠
     */
    private void applyThinkingFolds(String text, boolean collapsed, boolean notifyFoldChanges) {
        // 流式思考期间保持普通富文本渲染，避免每个 token 都扫描全文。
        // 思考结束后才建立折叠区块，并在这里按标识保留用户的展开选择。
        if (!collapsed || text == null || text.isBlank()) {
            this.updateBlocks(List.of(), List.of(), notifyFoldChanges);
            return;
        }
        String thinkingHeader = "**" + ShellI18nHelper.thinkingLabel() + "**";
        String resultHeader = "**" + I18nResourceBundle.i18nString(
                "shell.llm.agent.trace.result") + "**";
        String errorHeader = "**" + I18nResourceBundle.i18nString(
                "shell.llm.agent.trace.error") + "**";
        String answerHeader = "**" + ShellI18nHelper.answerLabel() + "**";
        String[] lines = text.split("\\R", -1);
        List<EditorHeading> headings = new ArrayList<>();
        List<EditorFold> folds = new ArrayList<>();
        int foldIndex = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].strip();
            if (line.startsWith("### ") && line.length() > 4) {
                headings.add(new EditorHeading(i, line.substring(4).strip(),
                        "editor-block-heading-major"));
            } else if (line.length() > 4 && line.startsWith("**") && line.endsWith("**")) {
                headings.add(new EditorHeading(i,
                        line.substring(2, line.length() - 2).strip(),
                        "editor-block-heading-section"));
            }
        }
        for (int start = 0; start < lines.length; start++) {
            if (!thinkingHeader.equals(lines[start].strip())) {
                continue;
            }
            int end = lines.length - 1;
            for (int next = start + 1; next < lines.length; next++) {
                String line = lines[next].strip();
                if (resultHeader.equals(line) || errorHeader.equals(line)
                        || answerHeader.equals(line) || "---".equals(line)
                        || line.startsWith("### ")) {
                    end = next - 1;
                    break;
                }
            }
            String id = "thinking-collapsed-" + foldIndex++;
            boolean foldCollapsed = this.foldStates.getOrDefault(id, collapsed);
            folds.add(new EditorFold(id, start, Math.max(start, end),
                    ShellI18nHelper.thinkingLabel(), foldCollapsed, thinkingColor()));
            start = end;
        }
        this.updateBlocks(headings, folds, notifyFoldChanges);
    }

    /**
     * 获取思考链正文颜色。
     *
     * @return 思考链正文颜色
     */
    private static Color thinkingColor() {
        return ThemeManager.isDarkMode() ? THINKING_DARK_COLOR : THINKING_LIGHT_COLOR;
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        super.changeTheme(style);
        if (this.pendingText != null) {
            this.applyThinkingFolds(this.pendingText,
                    this.pendingThinkingCollapsed, false);
        }
    }

    /**
     * 更新标题与折叠配置，并在可见范围变化时通知父列表重新测量单元格。
     *
     * @param headings 段落标题
     * @param folds 折叠配置
     */
    private void updateBlocks(List<EditorHeading> headings,
                              List<EditorFold> folds,
                              boolean notifyFoldChanges) {
        List<EditorFold> previous = this.getFolds();
        this.setBlocks(headings, folds);
        if (notifyFoldChanges && !previous.equals(this.getFolds())
                && this.foldChangeHandler != null) {
            this.foldChangeHandler.accept(this.getFolds());
        }
    }

    /**
     * 重置测量结果
     */
    private void invalidateSize() {
        this.measuredWidth = -1;
        this.measuredPrefWidth = -1;
        this.requestLayout();
    }

    @Override
    public void toggleFold(String id) {
        super.toggleFold(id);
        this.invalidateSize();
        if (this.foldChangeHandler != null) {
            this.foldChangeHandler.accept(this.getFolds());
        }
    }

    /**
     * 获取测量宽度属性
     *
     * @return 测量宽度属性
     */
    public DoubleProperty measurementWidthProperty() {
        return this.measurementWidth;
    }

    @Override
    protected double computePrefWidth(double height) {
        if (this.measuredPrefWidth >= 0) {
            return this.measuredPrefWidth;
        }
        Text measurer = this.prepareMeasurer();
        double max = 0;
        int count = this.getParagraphCount();
        for (int i = 0; i < count; i++) {
            if (this.isHiddenFoldBody(i)) {
                continue;
            }
            String line = this.getModel().getPlainText(i);
            if (line.isEmpty()) {
                continue;
            }
            measurer.setWrappingWidth(0);
            measurer.setText(line);
            max = Math.max(max, measurer.getLayoutBounds().getWidth());
        }
        if (max <= 0) {
            this.measuredPrefWidth = 0;
            return 0;
        }
        this.measuredPrefWidth = Math.ceil(max) + this.snapSizeX(2);
        return this.measuredPrefWidth;
    }

    @Override
    protected double computePrefHeight(double width) {
        // ListCell 测量时传入的宽度为 -1，这里使用绑定到最大宽度的测量宽度
        if (width <= 0) {
            width = this.measurementWidth.get();
        }
        if (width <= 0) {
            return this.measuredHeight;
        }
        if (Math.abs(width - this.measuredWidth) > 0.5) {
            this.measuredWidth = width;
            this.measuredHeight = this.estimateHeight(width);
        }
        return this.measuredHeight;
    }

    /**
     * 估算内容高度
     *
     * @param width 宽度
     * @return 高度
     */
    private double estimateHeight(double width) {
        Text measurer = this.prepareMeasurer();
        Insets padding = this.getContentPadding();
        double contentWidth = Math.max(1, width - padding.getLeft() - padding.getRight());
        double total = 0;
        int count = this.getParagraphCount();
        for (int i = 0; i < count; i++) {
            if (this.isHiddenFoldBody(i)) {
                continue;
            }
            String line = this.getModel().getPlainText(i);
            measurer.setText(line.isEmpty() ? " " : line);
            measurer.setWrappingWidth(contentWidth);
            total += this.snapSizeY(measurer.getLayoutBounds().getHeight());
        }
        return total + this.snapSizeY(2);
    }

    /**
     * 判断段落是否属于已折叠正文。
     *
     * @param paragraphIndex 段落索引
     * @return 是否隐藏
     */
    private boolean isHiddenFoldBody(int paragraphIndex) {
        return this.getFolds().stream().anyMatch(fold -> fold.collapsed()
                && paragraphIndex > fold.startParagraph()
                && paragraphIndex <= fold.endParagraph());
    }

    /**
     * 准备文本测量器
     *
     * @return 测量器
     */
    private Text prepareMeasurer() {
        Font font = this.getEditorFont() == null ? this.getFont() : this.getEditorFont();
        this.measurer.setFont(font);
        this.measurer.setBoundsType(TextBoundsType.LOGICAL);
        return this.measurer;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (this.refineScheduled) {
            return;
        }
        this.refineScheduled = true;
        Platform.runLater(() -> {
            this.refineScheduled = false;
            this.refineHeight();
        });
    }

    /**
     * 使用实际渲染高度修正首选高度，保证长文本、粗体换行等情况下不截断
     */
    private void refineHeight() {
        if (!(this.getSkin() instanceof EditorSkin skin)) {
            return;
        }
        VFlow flow = skin.getVFlow();
        if (flow == null) {
            return;
        }
        double width = this.getWidth();
        double actual = flow.prefHeight(-1);
        // VFlow 尚未完成布局时会返回约 10 的最小高度，此时忽略，避免把消息高度锁死成一行
        if (actual <= 10.5) {
            return;
        }
        if (actual > 0 && width > 0 && Math.abs(width - this.measuredWidth) < 0.5
                && Math.abs(actual - this.measuredHeight) > 0.5) {
            this.measuredHeight = actual;
            this.requestLayout();
        }
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        // 右键位置存在超链接时，增加打开超链接菜单项
        this.addHyperlinkMenuItems(items);
        items.add(MenuItemManager.getMenuItem(I18nHelper.copy(), this::copyMessage));
        items.add(MenuItemManager.getMenuItem(I18nHelper.selectAll(), this::selectAll));
        return items;
    }

    /**
     * 复制选中的内容，未选中时复制全部内容
     */
    private void copyMessage() {
        String text = this.getSelectedText();
        if (StringUtil.isBlank(text)) {
            text = this.getText();
        }
        if (StringUtil.isNotBlank(text)) {
            ClipboardUtil.setString(text);
        }
    }

    /**
     * 滚动事件转发给父节点，避免消息内部滚动
     *
     * @param event 滚动事件
     */
    private void forwardScroll(ScrollEvent event) {
        Parent parent = this.getParent();
        if (parent != null) {
            parent.fireEvent(event.copyFor(parent, parent));
            event.consume();
        }
    }
}
