package cn.oyzh.easyshell.fx.record;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.ShellRecordHeaderUtil;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.menu.ContextMenuAdapter;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.fx.plus.menu.MenuItemManager;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * 记录表格列。
 * <p>
 * 统一各协议记录表格（表、视图、查询结果）的列头外观：
 * 第一行为字段名称，第二行为字段类型，第三行为字段注释。
 * 类型或注释为空时不占行（主键用 PK 标记），表头高度随实际行数增减。
 * 右键菜单可切换「显示字段类型」「显示注释」，开关状态保存在应用设置中。
 *
 * @author oyzh
 * @since 2026-11-27
 */
public abstract class ShellRecordColumn<T> extends FXTableColumn<T, Object> implements MenuItemAdapter, ContextMenuAdapter, ShellRecordHeader {

    /**
     * 表头最小高度：只显示字段名时保证文字不被裁切
     */
    private static final double MIN_HEADER_HEIGHT = 22;

    /**
     * 列头内容上下的留白（单边）
     */
    private static final double HEADER_PADDING = 2;

    /**
     * 字段名称
     */
    private final String columnName;

    /**
     * 字段类型
     */
    private final String columnType;

    /**
     * 字段注释
     */
    private final String columnComment;

    /**
     * 是否主键
     */
    private final boolean primaryKey;

    /**
     * 构造记录表格列
     *
     * @param name       字段名称
     * @param type       字段类型
     * @param comment    字段注释
     * @param primaryKey 是否主键
     */
    protected ShellRecordColumn(String name, String type, String comment, boolean primaryKey) {
        this.columnName = name;
        this.columnType = type;
        this.columnComment = comment;
        this.primaryKey = primaryKey;
        this.setReorderable(true);
        this.refreshHeader();
        this.text(name);
        // 列加入表格（或换表）后同步一次表头高度
        this.tableViewProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.syncHeaderHeight();
            }
        });
        // 注册列头，切换显示开关时可统一刷新
        ShellRecordHeaderUtil.register(this);
    }

    /**
     * 刷新列头
     */
    @Override
    public void refreshHeader() {
        Node graphic = this.buildHeader();
        this.setGraphic(graphic);
        // 列头首次进入场景（例如标签页被激活）时再同步一次高度，
        // 否则未激活标签页里的表格测不到内容高度，会保留切换前的高度
        graphic.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.syncHeaderHeight();
            }
        });
        this.syncHeaderHeight();
    }

    /**
     * 同步表头高度。
     * <p>
     * 列头行数会随「显示字段类型/显示注释」变化，表头高度需要跟着增减，
     * 这里按列头内容的首选高度重新计算：
     * 内容变少时（甚至只剩字段名）也能立即收缩，不会停留在原来的高度。
     */
    private void syncHeaderHeight() {
        // 等列头挂到场景并完成 CSS 后再测量，否则字号（0.9em）尚未生效
        FXUtil.runPulse(this::applyHeaderHeight);
        FXUtil.runLater(this::applyHeaderHeight, 200);
    }

    /**
     * 应用表头高度。
     * <p>
     * 表头高度是整个表格共享的，所以取所有列头内容中的最大高度：
     * 同一张表里有的字段有注释、有的没有，不能按单个列的高度去设置。
     */
    private void applyHeaderHeight() {
        if (!(this.getTableView() instanceof FXTableView<?> tableView)) {
            return;
        }
        double height = 0;
        for (TableColumn<?, ?> column : tableView.getColumns()) {
            if (column instanceof ShellRecordColumn<?> recordColumn) {
                height = Math.max(height, recordColumn.contentHeight());
            }
        }
        // 列头还没完成布局，测不到内容高度，等下一轮再设置，避免误压小
        if (height <= 0) {
            return;
        }
        // 留出少量上下呼吸空间，文字不至于贴边；行数少时也要能压到很矮
        height = Math.max(height + HEADER_PADDING * 2, MIN_HEADER_HEIGHT);
        if (Math.abs(tableView.getHeaderHeight() - height) > 0.5) {
            tableView.setHeaderHeight(height);
        }
    }

    /**
     * 列头内容的实际高度
     *
     * @return 高度
     */
    private double contentHeight() {
        if (!(this.getGraphic() instanceof FXVBox box) || box.getScene() == null) {
            return 0;
        }
        box.applyCss();
        double width = box.getWidth() > 0 ? box.getWidth() : -1;
        return box.prefHeight(width);
    }

    /**
     * 构建列头内容
     *
     * @return 列头内容
     */
    private FXVBox buildHeader() {
        FXVBox box = new FXVBox();

        // 字段名称
        FXLabel nameLabel = new FXLabel(this.columnName);
        nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        nameLabel.setFont(FontUtil.newFontByWeight(nameLabel.getFont(), FontWeight.BOLD));
        box.addChild(nameLabel);

        // 字段类型
        if (ShellRecordHeaderUtil.showType()) {
            String text = ShellRecordHeaderUtil.typeText(this.columnType, null, false);
            // 类型未知时不显示占位行
            if (StringUtil.isNotBlank(text)) {
                FXLabel typeLabel = new FXLabel("# " + text);
                typeLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
                typeLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 0.9em;");
                box.addChild(typeLabel);
            }
        }

        // 字段注释（没有注释时不占行）
        if (ShellRecordHeaderUtil.showComment()) {
            String text = ShellRecordHeaderUtil.commentText(this.columnComment, this.primaryKey);
            if (StringUtil.isNotBlank(text)) {
                FXLabel commentLabel = new FXLabel(text);
                commentLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
                commentLabel.setStyle("-fx-text-fill: -color-fg-subtle; -fx-font-size: 0.9em;");
                box.addChild(commentLabel);
            }
        }

        // 右键菜单
        box.setOnContextMenuRequested(e -> this.showContextMenu(this.getMenuItems(), e.getScreenX(), e.getScreenY()));
        return box;
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        FXMenuItem columnInfo = MenuItemHelper.columnInfo(this::showColumnInfo);
        FXMenuItem copyName = MenuItemHelper.copyColumnName(this::copyColumnName);
        CheckMenuItem showType = MenuItemManager.getCheckMenuItem(ShellI18nHelper.recordShowColumnType(),
                () -> {
                    ShellRecordHeaderUtil.toggleShowType();
                    this.refreshTableHeaders();
                }, ShellRecordHeaderUtil.showType());
        CheckMenuItem showComment = MenuItemManager.getCheckMenuItem(ShellI18nHelper.recordShowColumnComment(),
                () -> {
                    ShellRecordHeaderUtil.toggleShowComment();
                    this.refreshTableHeaders();
                }, ShellRecordHeaderUtil.showComment());
        return List.of(columnInfo, copyName, MenuItemHelper.separator(), showType, showComment);
    }

    /**
     * 刷新当前表格的所有列头
     */
    protected void refreshTableHeaders() {
        if (this.getTableView() == null) {
            return;
        }
        for (TableColumn<?, ?> column : this.getTableView().getColumns()) {
            if (column instanceof ShellRecordHeader header) {
                header.refreshHeader();
            }
        }
    }

    /**
     * 显示字段信息
     */
    protected abstract void showColumnInfo();

    /**
     * 复制字段名称
     */
    protected void copyColumnName() {
        ClipboardUtil.copy(this.columnName);
    }

    /**
     * 获取字段名称
     *
     * @return 字段名称
     */
    public String getName() {
        return this.columnName;
    }

    /**
     * 获取字段类型
     *
     * @return 字段类型
     */
    public String getType() {
        return this.columnType;
    }

    /**
     * 获取字段注释
     *
     * @return 字段注释
     */
    public String getComment() {
        return this.columnComment;
    }

    /**
     * 是否主键
     *
     * @return 结果
     */
    public boolean isPrimaryKey() {
        return this.primaryKey;
    }

    /**
     * 获取当前字体
     *
     * @return 当前字体
     */
    public Font getFont() {
        return FontManager.currentFont();
    }

    @Override
    public void initNode() {
        super.showGraphicOnlyLater();
        super.initNode();
    }

    /**
     * 格式化字段类型（含长度）
     *
     * @param type        字段类型
     * @param size        字段长度
     * @param supportSize 是否支持长度
     * @return 类型文本
     */
    protected static String typeText(String type, Integer size, boolean supportSize) {
        return ShellRecordHeaderUtil.typeText(type, size, supportSize);
    }

    /**
     * 格式化字段注释（空值占位）
     *
     * @param comment    字段注释
     * @param primaryKey 是否主键
     * @return 注释文本
     */
    protected static String commentText(String comment, boolean primaryKey) {
        return ShellRecordHeaderUtil.commentText(comment, primaryKey);
    }

    /**
     * 是否显示字段类型
     *
     * @return 结果
     */
    protected static boolean showType() {
        return ShellRecordHeaderUtil.showType();
    }

    /**
     * 是否显示字段注释
     *
     * @return 结果
     */
    protected static boolean showComment() {
        return ShellRecordHeaderUtil.showComment();
    }
}
