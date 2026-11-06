package cn.oyzh.easyshell.tabs.postgresql.query;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.query.postgresql.PostgresqlExplainResult;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.fx.postgresql.record.ShellPostgresqlRecordColumn;
import cn.oyzh.easyshell.fx.postgresql.record.ShellPostgresqlRecordTableView;
import cn.oyzh.fx.db.ui.DBStatusColumn;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQLSQL解释结果标签页控制器，负责解释结果列、记录与SQL信息的初始化展示
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlQueryExplainTabController extends RichTabController {

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
    private ShellPostgresqlRecordTableView recordTable;

    /**
     * 执行结果
     */
    private PostgresqlExplainResult result;

    /**
     * 执行初始化
     *
     * @param result 执行结果
     */
    public void init(PostgresqlExplainResult result ) {
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
            this.used.text(ShellI18nHelper.queryTime(this.result.getUsedMs()));
            this.count.text(ShellI18nHelper.queryTotal(this.result.getCount()));
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 初始化列
     *
     * @param columns 列数据
     */
    private void initColumns(List<PostgresqlColumn> columns) {
        // 数据列集合
        List<FXTableColumn<PostgresqlRecord, Object>> columnList = new ArrayList<>();
        DBStatusColumn<PostgresqlRecord> statusColumn = new DBStatusColumn<>();
        columnList.add(statusColumn);
        for (PostgresqlColumn column : columns) {
            ShellPostgresqlRecordColumn tableColumn = new ShellPostgresqlRecordColumn(column);
            tableColumn.setRealWidth(DBUtil.suitableColumnWidth(column));
            columnList.add(tableColumn);
        }
        this.recordTable.getColumns().setAll(columnList);
    }

    /**
     * 初始化记录
     *
     * @param records 数据
     */
    private void initRecords(List<PostgresqlRecord> records) {
        this.recordTable.setItem(records);
    }
}
