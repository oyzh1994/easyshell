package cn.oyzh.easyshell.controller.redis.key;

import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.redis.RedisDatabaseComboBox;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.trees.redis.key.RedisKeyTreeItem;
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

import java.util.List;


/**
 * redis键复制业务
 *
 * @author oyzh
 * @since 2023/08/09
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.WINDOW_MODAL,
        value = FXConst.FXML_PATH + "redis/key/redisKeyCopy.fxml"
)
public class ShellRedisKeyCopyController extends StageController {

    /**
     * 键
     */
    @FXML
    private DisabledTextField key;

    /**
     * 存在时替换
     */
    @FXML
    private FXCheckBox replace;

    /**
     * 目标数据库
     */
    @FXML
    private RedisDatabaseComboBox targetDB;

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
    private void copyKey() {
        String key = this.treeItem.key();
        int fromDBIndex = this.treeItem.dbIndex();
        int targetDBIndex = this.targetDB.getDB();
        try {
            this.client.throwClusterException();
            this.client.throwSentinelException();
            this.client.throwCommandException("copy");
            if (targetDBIndex == fromDBIndex) {
                MessageBox.warn(ShellI18nHelper.redisMoveTip1());
                return;
            }
            // 移动键
            boolean result = this.client.copy(fromDBIndex, key, key, targetDBIndex, this.replace.isSelected());
            if (result) {
                ShellEventUtil.redisKeyCopied(this.client.shellConnect(), List.of(key), fromDBIndex, targetDBIndex);
                MessageBox.okToast(I18nHelper.operationSuccess());
                this.closeWindow();
            } else {
                MessageBox.warn(ShellI18nHelper.redisCopyTip1());
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
    public String getViewTitle() {
        return I18nHelper.copyKey();
    }
}
