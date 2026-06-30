package cn.oyzh.easyshell.tabs.mongo.query;

import cn.oyzh.easyshell.query.mongo.ShellMongoQueryResult;
import cn.oyzh.easyshell.query.mongo.ShellMongoQueryResults;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * @author oyzh
 * @since 2024/08/12
 */
public class MongoQueryInfoTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTextArea infoArea;

    public void init(ShellMongoQueryResults<?> results) {
        this.infoArea.clear();
        if (results.isSuccess()) {
            for (ShellMongoQueryResult result : results.getResults()) {
                this.infoArea.appendLine(result.getScript());
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
