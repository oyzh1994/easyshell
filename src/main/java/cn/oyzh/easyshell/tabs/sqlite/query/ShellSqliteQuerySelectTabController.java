package cn.oyzh.easyshell.tabs.sqlite.query;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.data.sqlite.dto.SQLiteDataExportTable;
import cn.oyzh.easyshell.fx.sqlite.SQLiteResultTableView;
import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteViewFactory;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SQLite 查询结果标签页控制器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQuerySelectTabController extends RichTabController {

    /**
     * SQL组件
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
    private SQLiteResultTableView recordTable;

    /**
     * 数据库树节点
     */
    private ShellSqliteRootTreeItem dbItem;

    /**
     * 执行结果
     */
    private SQLiteExecuteResult result;

    /**
     * 执行初始化
     *
     * @param result 执行结果
     * @param dbItem 数据库树节点
     */
    public void init(SQLiteExecuteResult result, ShellSqliteRootTreeItem dbItem) {
        this.result = result;
        this.dbItem = dbItem;
        this.initDataList();
    }

    /**
     * 初始化数据列表
     */
    private void initDataList() {
        try {
            this.recordTable.setResult(this.result);
            this.sql.text(TextUtil.toSingleLine(this.result.getContent()));
            this.used.text(I18nHelper.time() + ": " + this.result.getUsedMs() + "ms");
            this.count.text(I18nHelper.totalData() + ": " + this.result.getCount());
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 导出记录
     */
    @FXML
    private void exportRecord() {
        try {
            SQLiteDataExportTable exportTable = new SQLiteDataExportTable();
            exportTable.setSelected(true);
            exportTable.setName("query_result");
            exportTable.columns(this.result.getColumns());
            exportTable.setRecords(this.result.getRecords().stream().map(record -> {
                Map<String, Object> values = new LinkedHashMap<>();
                for (SQLiteColumn column : this.result.getColumns()) {
                    values.put(column.getName(), record.get(column));
                }
                return values;
            }).toList());
            ShellSqliteViewFactory.exportData(this.dbItem.client(), null, 1, exportTable);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }
}
