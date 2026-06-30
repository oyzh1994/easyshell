package cn.oyzh.easyshell.fx.mongo;

import cn.oyzh.easyshell.mongo.MongoColumn;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.table.FakerResizeTableColumn;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.menu.ContextMenuAdapter;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;


/**
 * @author oyzh
 * @since 2024/7/17
 */
public class ShellMongoRecordColumn extends FakerResizeTableColumn<MongoRecord, Object> implements MenuItemAdapter, ContextMenuAdapter {

    private final MongoColumn column;

    public ShellMongoRecordColumn(MongoColumn column) {
        this(column, 1);
    }

    public ShellMongoRecordColumn(MongoColumn column, int mode) {
        this.column = column;
        this.setReorderable(true);
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
        if (mode == 0) {
            this.text(column.displayName());
        } else if (mode == 1) {
            FXVBox vBox = this.initContent();
            this.setGraphic(vBox);
            this.text(column.displayName());
            super.showGraphicOnlyLater();
        } else {
            FXVBox vBox = this.initContent();
            FXHBox hBox = super.initGraphic(vBox);
            this.setGraphic(hBox);
            this.text(column.displayName());
            super.showGraphicOnlyLater();
        }
    }

    /**
     * 初始化内容
     *
     * @return 结果
     */
    private FXVBox initContent() {
        FXVBox vBox = new FXVBox();

        // 字段名称
        FXLabel colName = new FXLabel(this.column.getName());
        colName.setTextOverrun(OverrunStyle.ELLIPSIS);
        colName.setFont(FontUtil.newFontByWeight(colName.getFont(), FontWeight.BOLD));
        vBox.addChild(colName);

        // 字段类型
        FXLabel colType = new FXLabel(this.column.getType());
        colType.setTextFill(Color.GREEN);
        colType.setTextOverrun(OverrunStyle.ELLIPSIS);
        vBox.addChild(colType);

        // 右键菜单
        vBox.setOnContextMenuRequested(e -> {
            this.showContextMenu(this.getMenuItems(), e.getScreenX() - 10, e.getScreenY() - 10);
        });

        // 实时更新行高
        vBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ShellMongoRecordTableView tableView = (ShellMongoRecordTableView) this.getTableView();
                tableView.setHeaderHeight(newValue.doubleValue());
            }
        });
        return vBox;
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        FXMenuItem copyFieldName = MenuItemHelper.copyColumnName(this::copyColumnName);
        return List.of(copyFieldName);
    }

    /**
     * 复制字段名称
     */
    private void copyColumnName() {
        ClipboardUtil.copy(this.getName());
    }

    public Font getFont() {
        return FontManager.currentFont();
    }

    public String getName() {
        return this.column.getName();
    }

    public String getType() {
        return this.column.getType();
    }

    @Override
    protected boolean autoInitGraphic() {
        return false;
    }
}
