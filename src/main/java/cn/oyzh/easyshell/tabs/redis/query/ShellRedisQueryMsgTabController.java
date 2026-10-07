package cn.oyzh.easyshell.tabs.redis.query;

import cn.oyzh.easyshell.query.redis.ShellRedisQueryParam;
import cn.oyzh.easyshell.query.redis.ShellRedisQueryResult;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * redis查询消息tab内容组件
 *
 * @author oyzh
 * @since 2025/01/20
 */
public class ShellRedisQueryMsgTabController extends RichTabController {

    /**
     * 消息内容文本域
     */
    @FXML
    private ReadOnlyTextArea msg;

    /**
     * 初始化查询消息
     *
     * @param param  查询参数
     * @param result 查询结果
     */
    public void init(ShellRedisQueryParam param, ShellRedisQueryResult result) {
        this.msg.appendLine(param.getContent());
        this.msg.appendLine("> " + result.getMessage());
        this.msg.appendLine("> " + I18nHelper.cost() + ": " + result.costSeconds());
    }
}