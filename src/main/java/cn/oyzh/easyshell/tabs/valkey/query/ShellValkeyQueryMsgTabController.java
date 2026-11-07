package cn.oyzh.easyshell.tabs.valkey.query;

import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.query.valkey.ShellValkeyQueryParam;
import cn.oyzh.easyshell.query.valkey.ShellValkeyQueryResult;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import javafx.fxml.FXML;

/**
 * redis查询消息tab内容组件
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyQueryMsgTabController extends RichTabController {

    /**
     * 消息内容文本域
     */
    @FXML
    private ShellDataEditor msg;

    /**
     * 初始化查询消息
     *
     * @param param  查询参数
     * @param result 查询结果
     */
    public void init(ShellValkeyQueryParam param, ShellValkeyQueryResult result) {
        this.msg.appendLine(param.getContent());
        this.msg.appendLine("> " + result.getMessage());
        this.msg.appendLine(ShellI18nHelper.queryCostLine(result.costSeconds()));
    }
}
