package cn.oyzh.easyshell.tabs.dameng.query;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.query.DamengExplainResult;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.fx.dameng.record.DamengRecordColumn;
import cn.oyzh.easyshell.fx.dameng.record.DamengRecordTableView;
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
 * @author oyzh
 * @since 2024/08/16
 */
public class ShellDamengQueryExplainTabController extends RichTabController {

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
    private DamengRecordTableView recordTable;

    /**
     * 执行结果
     */
    private DamengExplainResult result;

    /**
     * 执行初始化
     *
     * @param result 执行结果
     */
    public void init(DamengExplainResult result ) {
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
    private void initColumns(List<DamengColumn> columns) {
        // 数据列集合
        List<FXTableColumn<DamengRecord, Object>> columnList = new ArrayList<>();
        DBStatusColumn<DamengRecord> statusColumn = new DBStatusColumn<>();
        columnList.add(statusColumn);
        for (DamengColumn column : columns) {
            DamengRecordColumn tableColumn = new DamengRecordColumn(column);
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
    private void initRecords(List<DamengRecord> records) {
        this.recordTable.setItem(records);
    }
}
