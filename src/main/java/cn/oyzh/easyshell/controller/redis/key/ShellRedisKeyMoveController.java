package cn.oyzh.easyshell.controller.redis.key;

import cn.oyzh.easyshell.fx.redis.ShellRedisDatabaseComboBox;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.trees.redis.RedisKeyTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.button.SubmitButton;
import cn.oyzh.fx.gui.text.field.DisabledTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;


/**
 * redis键移动业务
 *
 * @author oyzh
 * @since 2023/07/08
 */
@StageAttribute(
        modality = Modality.WINDOW_MODAL,
        value = FXConst.FXML_PATH + "redis/key/shellRedisKeyMove.fxml"
)
public class ShellRedisKeyMoveController extends StageController {

    /**
     * 键
     */
    @FXML
    private DisabledTextField key;

    /**
     * 保留ttl
     */
    @FXML
    private FXCheckBox retainTTL;

    /**
     * 目标数据库
     */
    @FXML
    private ShellRedisDatabaseComboBox targetDB;

    /**
     * redis客户端
     */
    private ShellRedisClient client;

    /**
     * 提交按钮
     */
    @FXML
    private SubmitButton submit;

    /**
     * 树节点
     */
    private RedisKeyTreeItem treeItem;

    /**
     * 转移键
     */
    @FXML
    private void moveKey() {
        String key = this.treeItem.key();
        int fromDBIndex = this.treeItem.dbIndex();
        int targetDBIndex = this.targetDB.getDB();
        try {
            this.client.throwClusterException();
            this.client.throwSentinelException();
            if (targetDBIndex == fromDBIndex) {
                MessageBox.warn(ShellI18nHelper.redisMoveTip1());
                return;
            }
            if (this.client.exists(targetDBIndex, key)) {
                MessageBox.warn(ShellI18nHelper.redisMoveTip2());
                return;
            }
            // 保留ttl
            long ttl = -3;
            if (this.retainTTL.isSelected()) {
                ttl = this.client.ttl(fromDBIndex, key);
            }
            // 移动键
            long count = this.client.move(key, fromDBIndex, targetDBIndex);
            if (count <= 0) {
                MessageBox.warn(I18nHelper.operationFail());
            } else {
                // 设置新键的ttl
                if (ttl > 0) {
                    this.client.expire(targetDBIndex, key, ttl, null);
                }
                this.setProp("dbIndex", targetDBIndex);
                // ShellEventUtil.redisKeyMoved(this.treeItem, targetDBIndex);
                // MessageBox.okToast(I18nHelper.operationSuccess());
                this.closeWindow();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    protected void bindListeners() {
        // 权限变化处理
        this.targetDB.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (newValue.intValue() == this.treeItem.dbIndex()) {
                this.submit.disable();
            } else {
                this.submit.enable();
            }
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
        super.onWindowShown(event);
        this.treeItem = this.getProp("treeItem");
        this.client = this.treeItem.client();
        this.key.setText(this.treeItem.key() + "（db" + this.treeItem.dbIndex() + "）");
        this.targetDB.setDbCount(this.client.databases());
        this.targetDB.selectFirst();
        this.targetDB.requestFocus();
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.moveKey();
    }
}
