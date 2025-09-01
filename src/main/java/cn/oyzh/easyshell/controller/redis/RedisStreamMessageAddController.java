package cn.oyzh.easyshell.controller.redis;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.redis.RedisEventUtil;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.easyshell.trees.redis.RedisStreamKeyTreeItem;
import cn.oyzh.easyshell.util.RedisI18nHelper;
import cn.oyzh.fx.editor.tm4javafx.Editor;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import com.alibaba.fastjson2.JSONObject;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import redis.clients.jedis.params.XAddParams;

import java.util.Map;


/**
 * redis添加stream消息
 *
 * @author oyzh
 * @since 2023/07/07
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.WINDOW_MODAL,
        value = FXConst.FXML_PATH + "row/redisStreamMessageAdd.fxml"
)
public class RedisStreamMessageAddController extends StageController {

    /**
     * 消息内容
     */
    @FXML
    private Editor rowValue;

    /**
     * 消息id
     */
    @FXML
    private ClearableTextField streamID;

    /**
     * redis键
     */
    private RedisStreamKeyTreeItem treeItem;

    /**
     * 添加行
     */
    @FXML
    private void addRow() {
        try {
            // 行数据
            String rowValue = this.rowValue.getText();
            if (StringUtil.isEmpty(rowValue)) {
                MessageBox.tipMsg(I18nHelper.contentCanNotEmpty(), this.rowValue);
                return;
            }
            if (!JSONUtil.isJson(rowValue)) {
                MessageBox.warn(RedisI18nHelper.addTip4());
                return;
            }
            JSONObject fields = JSONUtil.parseObject(rowValue);
            if (fields.isEmpty()) {
                MessageBox.tipMsg(I18nHelper.contentCanNotEmpty(), this.rowValue);
                return;
            }
            String streamIDText = this.streamID.getText();
            if (streamIDText == null) {
                MessageBox.tipMsg(I18nHelper.contentCanNotEmpty(), this.streamID);
                return;
            }
            // redis键
            String key = this.treeItem.key();
            // 获取键值
            int dbIndex = this.treeItem.dbIndex();
            // redis客户端
            RedisClient client = this.treeItem.client();
            // 流添加参数
            XAddParams params = new XAddParams();
            params.id(streamIDText);
            // 添加流
            client.xadd(dbIndex, key, (Map) fields, params);
            // 结果
            this.setProp("result", true);
            // 发送事件
            RedisEventUtil.streamMessageAdded(this.treeItem, key, rowValue);
            this.closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 粘贴数据
     */
    @FXML
    private void pasteData() {
        this.rowValue.paste();
        this.rowValue.requestFocus();
    }

    /**
     * 清空数据
     */
    @FXML
    private void clearData() {
        this.rowValue.clear();
        this.rowValue.requestFocus();
    }

    /**
     * 解析为json
     */
    @FXML
    private void parseToJson() {
        String text = this.rowValue.getTextTrim();
        try {
            if ("json".equals(this.rowValue.getUserData())) {
                String jsonStr = JSONUtil.toJson(this.rowValue);
                this.rowValue.setText(jsonStr);
                this.rowValue.setUserData("text");
            } else if (text.contains("{") || text.contains("[") || "text".equals(this.rowValue.getUserData())) {
                String jsonStr = JSONUtil.toPretty(this.rowValue);
                this.rowValue.setText(jsonStr);
                this.rowValue.setUserData("json");
            }
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.treeItem = this.getProp("treeItem");
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
        this.rowValue.requestFocus();
        super.onWindowShown(event);
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("redis.title.streamMessageAdd");
    }
}
