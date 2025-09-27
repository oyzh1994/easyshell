package cn.oyzh.easyshell.controller.redis.row;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.trees.redis.RedisHashKeyTreeItem;
import cn.oyzh.fx.editor.tm4javafx.Editor;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;


/**
 * redis添加hash字段
 *
 * @author oyzh
 * @since 2023/06/27
 */
@StageAttribute(
        modality = Modality.WINDOW_MODAL,
        value = FXConst.FXML_PATH + "redis/row/shellRedisHashFieldAdd.fxml"
)
public class ShellRedisHashFieldAddController extends StageController {

    /**
     * 字段
     */
    @FXML
    private Editor fieldValue;

    /**
     * 行数据
     */
    @FXML
    private Editor rowValue;

    /**
     * redis键
     */
    private RedisHashKeyTreeItem treeItem;

    /**
     * 添加行
     */
    @FXML
    private void addRow() {
        try {
            String fieldValue = this.fieldValue.getText();
            if (fieldValue == null) {
                MessageBox.tipMsg(I18nHelper.contentCanNotEmpty(), this.fieldValue);
                return;
            }
            // 行数据
            String rowValue = this.rowValue.getText();
            if (StringUtil.isEmpty(rowValue)) {
                MessageBox.tipMsg(I18nHelper.contentCanNotEmpty(), this.rowValue);
                return;
            }
            // redis键
            String key = this.treeItem.key();
            // 获取键值
            int dbIndex = this.treeItem.dbIndex();
            // redis客户端
            ShellRedisClient client = this.treeItem.client();
            if (client.hexists(dbIndex, key, fieldValue)) {
                MessageBox.warn(I18nHelper.alreadyExists());
                return;
            }
            // 添加元素
            client.hset(dbIndex, key, fieldValue, rowValue);
            // 结果
            this.setProp("result", true);
            // // 发送事件
            // ShellEventUtil.redisHashFieldAdded(this.treeItem, key, fieldValue, rowValue);
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
        return I18nResourceBundle.i18nString("shell.redis.title.hashFieldAdd");
    }
}
