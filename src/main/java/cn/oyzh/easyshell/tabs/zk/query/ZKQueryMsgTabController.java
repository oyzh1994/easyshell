package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.easyshell.query.zk.ZKQueryParam;
import cn.oyzh.easyshell.query.zk.ZKQueryResult;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ZKQueryMsgTabController extends RichTabController {

    @FXML
    private ReadOnlyTextArea msg;

    public void init(ZKQueryParam param, ZKQueryResult result) {
        this.msg.appendLine(param.getContent());
        this.msg.appendLine("> " + result.getMessage());
        this.msg.appendLine("> " + I18nHelper.cost() + " " + result.costSeconds());
        if (result.isSuccess()) {
            // 兜底指令
            if (param.isGetAllChildrenNumber() || param.isRuok() || param.isCrst()
                    || param.isCons() || param.isSrst() || param.isWchc() || param.isWchs()
                    || param.isWchp() || param.isDump() || param.isReqs() || param.isDirs()) {
                this.msg.appendLine("" + result.getResult());
            }
        }
    }
}