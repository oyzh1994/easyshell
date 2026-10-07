package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.easyshell.query.zk.ShellZKQueryParam;
import cn.oyzh.easyshell.query.zk.ShellZKQueryResult;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * zk查询消息内容组件
 *
 * @author oyzh
 * @since 2025/01/20
 */
public class ShellZKQueryMsgTabController extends RichTabController {

    /**
     * 消息文本域
     */
    @FXML
    private ReadOnlyTextArea msg;

    /**
     * 初始化消息数据
     *
     * @param param  查询参数
     * @param result 查询结果
     */
    public void init(ShellZKQueryParam param, ShellZKQueryResult result) {
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