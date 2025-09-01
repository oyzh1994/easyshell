package cn.oyzh.easyshell.controller.redis.row;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.redis.RedisEventUtil;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.easyshell.trees.redis.RedisZSetKeyTreeItem;
import cn.oyzh.fx.editor.tm4javafx.Editor;
import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;


/**
 * redis添加set成员
 *
 * @author oyzh
 * @since 2023/06/27
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.WINDOW_MODAL,
        value = FXConst.FXML_PATH + "redis/row/redisZSetMemberAdd.fxml"
)
public class RedisZSetMemberAddController extends StageController {

    /**
     * 行数据
     */
    @FXML
    private Editor rowValue;

    /**
     * 分数
     */
    @FXML
    private DecimalTextField score;

    /**
     * redis键
     */
    private RedisZSetKeyTreeItem treeItem;

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
            Number scoreValue = this.score.getValue();
            if (scoreValue == null) {
                MessageBox.tipMsg(I18nHelper.contentCanNotEmpty(), this.score);
                return;
            }
            // redis键
            String key = this.treeItem.key();
            // 获取键值
            int dbIndex = this.treeItem.dbIndex();
            // redis客户端
            RedisClient client = this.treeItem.client();
            if (client.zrank(dbIndex, key, rowValue) != null) {
                MessageBox.warn(I18nHelper.alreadyExists());
                return;
            }
            double score = scoreValue.doubleValue();
            // 添加元素
            client.zadd(dbIndex, key, score, rowValue);
            // 结果
            this.setProp("result", true);
            // 发送事件
            RedisEventUtil.zSetMemberAdded(this.treeItem, key, rowValue, score);
            this.closeWindow();
        } catch (Exception ex) {
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
        super.onWindowShown(event);
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("redis.title.zSetMemberAdd");
    }
}
