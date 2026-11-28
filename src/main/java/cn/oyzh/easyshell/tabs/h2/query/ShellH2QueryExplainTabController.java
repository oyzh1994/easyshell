package cn.oyzh.easyshell.tabs.h2.query;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.fx.h2.H2ResultTableView;
import cn.oyzh.easyshell.query.h2.H2ExecuteResult;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * H2 查询执行计划标签页控制器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2QueryExplainTabController extends RichTabController {

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
    private H2ResultTableView recordTable;

    /**
     * 执行结果
     */
    private H2ExecuteResult result;

    /**
     * 执行初始化
     *
     * @param result 执行结果
     */
    public void init(H2ExecuteResult result) {
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
