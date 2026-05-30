package cn.oyzh.easyshell.fx.mysql.record;

import atlantafx.base.controls.Popover;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.popups.mysql.ShellMysqlFieldInfoPopupController;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.table.FarkerResizeTableColumn;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.menu.ContextMenuAdapter;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.PopupWindow;

import java.util.List;


/**
 * @author oyzh
 * @since 2024/7/17
 */
public class ShellMysqlRecordColumn extends FarkerResizeTableColumn<MysqlRecord, Object> implements MenuItemAdapter, ContextMenuAdapter {

    private final MysqlColumn column;

    public ShellMysqlRecordColumn(MysqlColumn column) {
        this(column, true, 1);
    }

    public ShellMysqlRecordColumn(MysqlColumn column, boolean showComment, int mode) {
        this.column = column;
        this.setReorderable(true);
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
        FXVBox vBox = this.initContent(showComment);
        if (mode == 1) {
            this.setGraphic(vBox);
        } else {
            FXHBox hBox = super.initGraphic(vBox);
            this.setGraphic(hBox);
        }
        // 设置了文字，但是仅显示图标
        this.text(column.getName());
        this.showGraphicOnly();
    }

    /**
     * 初始化内容
     *
     * @param showComment 是否显示注释
     * @return 结果
     */
    private FXVBox initContent(boolean showComment) {
        FXVBox vBox = new FXVBox();

        // 字段名称
        FXLabel colName = new FXLabel(this.column.getName());
        colName.setTextOverrun(OverrunStyle.ELLIPSIS);
        colName.setFont(FontUtil.newFontByWeight(colName.getFont(), FontWeight.BOLD));
        vBox.addChild(colName);

        // 字段类型
        FXLabel colType;
        if (this.column.supportSize() && this.column.getSize() != null) {
            colType = new FXLabel(this.column.getType() + "(" + this.column.getSize() + ")");
        } else {
            colType = new FXLabel(this.column.getType());
        }
        colType.setTextFill(Color.GREEN);
        colType.setTextOverrun(OverrunStyle.ELLIPSIS);
        vBox.addChild(colType);

        // 字段注释
        if (showComment) {
            FXLabel colComment = new FXLabel(this.column.getComment());
            colComment.setTextFill(Color.GRAY);
            colComment.setTextOverrun(OverrunStyle.ELLIPSIS);
            vBox.addChild(colComment);
        }

        // 右键菜单
        vBox.setOnContextMenuRequested(e -> {
            this.showContextMenu(this.getMenuItems(), e.getScreenX() - 10, e.getScreenY() - 10);
        });

        // 实时更新行高
        vBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ShellMysqlRecordTableView tableView = (ShellMysqlRecordTableView) this.getTableView();
                tableView.setHeaderHeight(newValue.doubleValue());
            }
        });
        return vBox;
    }

    /// **
    // * 初始化模式2
    // *
    // * @param showComment 是否显示注释
    // * @return 结果
    // */
    //private FXHBox initMode2(boolean showComment) {
    //    // 核心：构建列头图形
    //    FXVBox vBox = this.initMode1(showComment);
    //    vBox.setPadding(Insets.EMPTY);
    //
    //    // 拖拽把手：一个透明的窄条
    //    Region handle = new Region();
    //    handle.setMinWidth(3);
    //    handle.setMaxWidth(3);
    //    handle.setPrefWidth(3);
    //    double translateX = FontUtil.textWidth("a", this.getFont()) + 1.5;
    //    handle.setTranslateX(translateX);
    //    handle.setCursor(Cursor.H_RESIZE);
    //    handle.setBackground(ControlUtil.background(Color.TRANSPARENT));
    //    handle.setPadding(Insets.EMPTY);
    //
    //    // 把手悬停时改变背景色以便视觉提示（可选）
    //    handle.setOnMouseEntered(e -> {
    //        handle.setBackground(ControlUtil.background(ThemeManager.currentForegroundColor()));
    //    });
    //    handle.setOnMouseExited(e -> {
    //        handle.setBackground(ControlUtil.background(Color.TRANSPARENT));
    //    });
    //    // 把手拖拽逻辑
    //    handle.setOnMousePressed(e -> {
    //        // 记录起始宽度，用于计算增量
    //        handle.setUserData(new double[]{e.getSceneX(), this.getWidth()});
    //        e.consume();
    //    });
    //    handle.setOnMouseDragged(e -> {
    //        double[] data = (double[]) handle.getUserData();
    //        if (data == null) {
    //            return;
    //        }
    //        double startSceneX = data[0];
    //        double startWidth = data[1];
    //        double delta = e.getSceneX() - startSceneX;
    //        double newWidth = startWidth + delta;
    //        // 应用宽度限制
    //        if (newWidth < this.getMinWidth()) {
    //            newWidth = this.getMinWidth();
    //        }
    //        if (newWidth > this.getMaxWidth()) {
    //            newWidth = this.getMaxWidth();
    //        }
    //        this.setPrefWidth(newWidth);
    //        e.consume();
    //    });
    //    handle.setOnMouseReleased(e -> {
    //        handle.setUserData(null);
    //        e.consume();
    //    });
    //
    //    // 把标签和把手放入 HBox
    //    FXHBox hBox = new FXHBox(vBox, handle);
    //    // 标签占用剩余空间，把手始终在右侧
    //    HBox.setHgrow(vBox, Priority.ALWAYS);
    //    hBox.setAlignment(Pos.CENTER_LEFT);
    //    // 关键：禁用原生列宽调整，避免冲突
    //    this.setResizable(false);
    //    return hBox;
    //}

    //    private FXHBox initMode2(boolean showComment) {
    //        // 核心：构建列头图形
    //        Label label = new Label(column.getName());
    //        label.setMaxWidth(Double.MAX_VALUE); // 让标签可伸缩
    //        label.setPadding(Insets.EMPTY);
    //
    //        // 拖拽把手：一个透明的窄条
    //        Region handle = new Region();
    //        handle.setPrefWidth(3);
    //        handle.setMinWidth(3);
    //        handle.setMaxWidth(3);
    //        handle.setTranslateX(12);
    //        handle.setStyle("-fx-cursor: h-resize; -fx-background-color: transparent;");
    //
    //        handle.setPadding(Insets.EMPTY);
    //
    //        // 把手悬停时改变背景色以便视觉提示（可选）
    //        handle.setOnMouseEntered(e -> handle.setStyle("-fx-cursor: h-resize; -fx-background-color: #aaa;"));
    //        handle.setOnMouseExited(e -> handle.setStyle("-fx-cursor: h-resize; -fx-background-color: transparent;"));
    //
    //        // 把手拖拽逻辑
    //        handle.setOnMousePressed(e -> {
    //            // 记录起始宽度，用于计算增量
    //            handle.setUserData(new double[]{e.getSceneX(), this.getWidth()});
    //            e.consume();
    //        });
    //        handle.setOnMouseDragged(e -> {
    //            double[] data = (double[]) handle.getUserData();
    //            if (data == null)
    //                return;
    //            double startSceneX = data[0];
    //            double startWidth = data[1];
    //            double delta = e.getSceneX() - startSceneX;
    //            double newWidth = startWidth + delta;
    //            // 应用宽度限制
    //            if (newWidth < this.getMinWidth())
    //                newWidth = this.getMinWidth();
    //            if (newWidth > this.getMaxWidth())
    //                newWidth = this.getMaxWidth();
    //            this.setPrefWidth(newWidth);
    //            e.consume();
    //        });
    //        handle.setOnMouseReleased(e -> {
    //            handle.setUserData(null);
    //            e.consume();
    //        });
    //
    //        // 把标签和把手放入 HBox
    //        HBox headerBox = new HBox(label, handle);
    //        HBox.setHgrow(label, Priority.ALWAYS); // 标签占用剩余空间，把手始终在右侧
    //        headerBox.setStyle("-fx-alignment: center-left;");
    //
    //        this.setGraphic(headerBox);
    //        // 关键：禁用原生列宽调整，避免冲突
    //        this.setResizable(false);
    //    }
    @Override
    public List<? extends MenuItem> getMenuItems() {
        FXMenuItem fieldInfo = MenuItemHelper.columnInfo(this::showColumnInfo);
        FXMenuItem copyFieldName = MenuItemHelper.copyColumnName(this::copyColumnName);
        return List.of(fieldInfo, copyFieldName);
    }

    /**
     * 显示字段信息
     */
    private void showColumnInfo() {
        PopupAdapter popup = PopupManager.parsePopup(ShellMysqlFieldInfoPopupController.class, Popover.ArrowLocation.TOP_LEFT, PopupWindow.AnchorLocation.CONTENT_TOP_LEFT);
        popup.setProp("column", this.column);
        popup.showPopup(this.getGraphic(), MouseUtil.getMouseX(), MouseUtil.getMouseY());
    }

    /**
     * 复制字段名称
     */
    private void copyColumnName() {
        ClipboardUtil.copy(this.getName());
    }

    public Font getFont() {
        //        FXVBox vBox = (FXVBox) this.getGraphic();
        //        if (vBox == null) {
        return FontManager.currentFont();
        //        }
        //        FXLabel label = (FXLabel) vBox.getFirstChild();
        //        return label.getFont();
    }

    public String getName() {
        return this.column.getName();
    }

    public String getType() {
        return this.column.getType();
    }

    public boolean supportSize() {
        return this.column.supportSize();
    }

    public Integer getSize() {
        return this.column.getSize();
    }

    @Override
    protected boolean autoInitGraphic() {
        return false;
    }
}
