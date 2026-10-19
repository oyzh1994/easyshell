package cn.oyzh.easyshell.tabs.mariadb.query;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.fx.mariadb.record.ShellMariadbRecordColumn;
import cn.oyzh.easyshell.fx.mariadb.record.ShellMariadbRecordTableView;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.easyshell.query.mariadb.ShellMariadbExplainResult;
import cn.oyzh.fx.db.ui.DBStatusColumn;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;

/**
 * MariaDB 查询执行计划标签页控制器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbQueryExplainTabController extends RichTabController {

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
    private ShellMariadbRecordTableView recordTable;

    /**
     * 执行结果
     */
    private ShellMariadbExplainResult result;

    /**
     * 执行初始化
     *
     * @param result 执行结果
     */
    public void init(ShellMariadbExplainResult result) {
        this.result = result;
        this.initDataList();
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
            this.sql.text(TextUtil.toSingleLine(this.result.getContent()));
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
    private void initColumns(List<MariadbColumn> columns) {
        // 数据列集合
        List<FXTableColumn<MariadbRecord, Object>> columnList = new ArrayList<>();
        DBStatusColumn<MariadbRecord> statusColumn = new DBStatusColumn<>();
        columnList.add(statusColumn);
        for (MariadbColumn column : columns) {
            ShellMariadbRecordColumn tableColumn = new ShellMariadbRecordColumn(column, false);
            tableColumn.setPrefWidth(DBUtil.suitableColumnWidth(column));
            columnList.add(tableColumn);
        }
        this.recordTable.getColumns().setAll(columnList);
    }

    /**
     * 初始化记录
     *
     * @param records 数据
     */
    private void initRecords(List<MariadbRecord> records) {
        this.recordTable.setItem(records);
    }
}
