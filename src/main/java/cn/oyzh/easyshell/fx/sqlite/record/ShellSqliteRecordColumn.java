package cn.oyzh.easyshell.fx.sqlite.record;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.font.FontUtil;
import javafx.scene.control.OverrunStyle;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

/**
 * SQLite记录表格列。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteRecordColumn extends FXTableColumn<SQLiteRecord, Object> {

    /**
     * 字段信息
     */
    private final SQLiteColumn column;

    public ShellSqliteRecordColumn(SQLiteColumn column) {
        this(column, true);
    }

    public ShellSqliteRecordColumn(SQLiteColumn column, boolean showComment) {
        this.column = column;
        this.setReorderable(true);
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
        this.setGraphic(this.initContent(showComment));
        this.text(column.getName());
    }

    /**
     * 初始化内容
     *
     * @param showComment 是否显示附加信息
     * @return 结果
     */
    private FXVBox initContent(boolean showComment) {
        FXVBox vBox = new FXVBox();

        FXLabel colName = new FXLabel(this.column.getName());
        colName.setTextOverrun(OverrunStyle.ELLIPSIS);
        colName.setFont(FontUtil.newFontByWeight(colName.getFont(), FontWeight.BOLD));
        vBox.addChild(colName);

        FXLabel colType = new FXLabel(this.column.getType());
        colType.setTextFill(Color.GREEN);
        colType.setTextOverrun(OverrunStyle.ELLIPSIS);
        vBox.addChild(colType);

        if (showComment) {
            FXLabel colComment = new FXLabel(this.column.isPrimaryKey() ? "PK" : "");
            colComment.setTextFill(Color.GRAY);
            colComment.setTextOverrun(OverrunStyle.ELLIPSIS);
            vBox.addChild(colComment);
        }

        vBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && this.getTableView() instanceof ShellSqliteRecordTableView tableView) {
                tableView.setHeaderHeight(newValue.doubleValue());
            }
        });
        return vBox;
    }
}
