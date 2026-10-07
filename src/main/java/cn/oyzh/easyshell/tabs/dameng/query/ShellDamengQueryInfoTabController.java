package cn.oyzh.easyshell.tabs.dameng.query;

import cn.oyzh.fx.db.query.DBQueryResult;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * 达梦查询信息标签页控制器，负责将SQL执行结果信息输出到信息区域
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengQueryInfoTabController extends RichTabController {

    /**
     * 信息展示区域
     */
    @FXML
    private Editor infoArea;

    /**
     * 初始化
     *
     * @param results 结果集
     */
    public void init(DBQueryResults<?> results) {
        this.infoArea.clear();
        if (results.isSuccess()) {
            for (DBQueryResult result : results.getResults()) {
                this.infoArea.appendLine(result.getContent());
                if (result.isSuccess()) {
                    if (result.getUpdateCount() > 0) {
                        this.infoArea.appendLine("> Affected rows: " + result.getUpdateCount());
                    } else {
                        this.infoArea.appendLine("> OK");
                    }
                } else {
                    this.infoArea.appendLine("> " + result.getMsg());
                }
                this.infoArea.appendLine("> " + I18nHelper.time() + ": " + result.getUsedMs() + "ms");
                this.infoArea.appendLine("");
            }
        } else {
            this.infoArea.appendLine(results.getErrMsg());
        }
    }
}
