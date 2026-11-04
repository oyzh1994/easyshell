package cn.oyzh.easyshell.tabs.sqlite.query;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.fx.sqlite.SQLiteResultTableView;
import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * SQLite 查询执行计划标签页控制器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryExplainTabController extends RichTabController {

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
     * 执行结果
     */
    private SQLiteExecuteResult result;

    /**
     * 执行初始化
     *
     * @param result 执行结果
     */
    public void init(SQLiteExecuteResult result) {
        this.result = result;
        this.initDataList();
    }

    /**
     * 初始化数据列表
     */
    private void initDataList() {
        try {
            this.recordTable.setResult(this.result);
            this.sql.text(TextUtil.toSingleLine(this.result.getContent()));
            this.used.text(ShellI18nHelper.queryTime(this.result.getUsedMs()));
            this.count.text(ShellI18nHelper.queryTotal(this.result.getCount()));
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }
}
