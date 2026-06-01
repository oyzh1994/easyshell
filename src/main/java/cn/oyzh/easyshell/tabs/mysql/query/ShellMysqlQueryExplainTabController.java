package cn.oyzh.easyshell.tabs.mysql.query;

import cn.oyzh.easyshell.fx.db.DBStatusColumn;
import cn.oyzh.easyshell.fx.mysql.record.ShellMysqlRecordColumn;
import cn.oyzh.easyshell.fx.mysql.record.ShellMysqlRecordTableView;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.query.MysqlExplainResult;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.util.mysql.ShellMysqlRecordUtil;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/16
 */
public class ShellMysqlQueryExplainTabController extends RichTabController {

    /**
     * sql组件
     */
    @FXML
    private FXText sql;

    /**
     * 耗时组件
     */
    @FXML
    private FXText used;

    /**
     * 计数组件
     */
    @FXML
    private FXText count;

    /**
     * 数据表单组件
     */
    @FXML
    private ShellMysqlRecordTableView recordTable;

    /**
     * 执行结果
     */
    private MysqlExplainResult result;

    /**
     * 执行初始化
     *
     * @param result 执行结果
     */
    public void init(MysqlExplainResult result) {
        this.result = result;
        this.initDataList();
        // 虽然设置了文字，但是不显示图标
//        FXUtil.runPulse(this.recordTable::showGraphicOnly);
//        this.recordTable.showGraphicOnlyLater();
    }

    /**
     * 初始化数据列表
     */
    private void initDataList() {
        try {
            // 初始化字段
            this.initColumns(this.result.columnList());
            // 初始化数据
            this.initRecords(this.result.getRecords());
            // 初始化sql信息
            this.sql.text(this.result.getSql());
            this.used.text(I18nHelper.time() + ": " + this.result.getUsedMs() + "ms");
            this.count.text(I18nHelper.totalData() + ": " + this.result.getCount());
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 初始化列
     *
     * @param columns 列数据
     */
    private void initColumns(List<MysqlColumn> columns) {
        // 数据列集合
        List<FXTableColumn<MysqlRecord, Object>> columnList = new ArrayList<>();
        DBStatusColumn<MysqlRecord> statusColumn = new DBStatusColumn<>();
        columnList.add(statusColumn);
        for (MysqlColumn column : columns) {
            //int mode = Objects.equals(column, columns.getLast()) ? 1 : 2;
            ShellMysqlRecordColumn tableColumn = new ShellMysqlRecordColumn(column, false, 2);
            tableColumn.setPrefWidth(ShellMysqlRecordUtil.suitableColumnWidth(tableColumn));
            columnList.add(tableColumn);
        }
        this.recordTable.getColumns().setAll(columnList);
    }

    /**
     * 初始化记录
     *
     * @param records 数据
     */
    private void initRecords(List<MysqlRecord> records) {
        this.recordTable.setItem(records);
    }

    //    @Override
    //    public void onTabClosed(Event event) {
    //        super.onTabClosed(event);
    //        this.recordTable.destroy();
    //    }

    @Override
    public void destroy() {
        this.recordTable.destroy();
        super.destroy();
    }
}
