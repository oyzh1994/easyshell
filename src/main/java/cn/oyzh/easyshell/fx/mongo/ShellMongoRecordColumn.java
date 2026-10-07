package cn.oyzh.easyshell.fx.mongo;

import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
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
 * MongoDB记录表格列
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoRecordColumn extends FXTableColumn<MongoRecord, Object> implements MenuItemAdapter, ContextMenuAdapter {

    /**
     * mongodb字段
     */
    private final MongoColumn column;

    /**
     * 构造器
     *
     * @param column 字段
     */
    public ShellMongoRecordColumn(MongoColumn column) {
        this(column, 1);
    }

    /**
     * 构造器
     *
     * @param column 字段
     * @param mode   模式
     */
    public ShellMongoRecordColumn(MongoColumn column, int mode) {
        this.column = column;
        this.setReorderable(true);
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
        if (mode == 0) {
            this.text(column.displayName());
        } else {
            FXVBox vBox = this.initContent();
            this.setGraphic(vBox);
            this.text(column.displayName());
            super.showGraphicOnlyLater();
//        } else {
//            FXVBox vBox = this.initContent();
//            FXHBox hBox = super.initGraphic(vBox);
//            this.setGraphic(hBox);
//            this.text(column.displayName());
//            super.showGraphicOnlyLater();
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
            this.showContextMenu(this.getMenuItems(), e.getScreenX() , e.getScreenY());
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

    /**
     * 获取字体
     *
     * @return 字体
     */
    public Font getFont() {
        return FontManager.currentFont();
    }

    /**
     * 获取字段名称
     *
     * @return 字段名称
     */
    public String getName() {
        return this.column.getName();
    }

    /**
     * 获取字段类型
     *
     * @return 字段类型
     */
    public String getType() {
        return this.column.getType();
    }

//    @Override
//    protected boolean autoInitGraphic() {
//        return false;
//    }
}
