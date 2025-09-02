package cn.oyzh.easyshell.controller.redis.key;

import cn.oyzh.common.Const;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.easyshell.trees.redis.key.RedisKeyTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.sql.Date;


/**
 * redis ttl设置业务
 *
 * @author oyzh
 * @since 2023/07/09
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.WINDOW_MODAL,
        value = FXConst.FXML_PATH + "redis/key/redisKeyTTL.fxml"
)
public class RedisKeyTTLController extends StageController {

    /**
     * 当前窗口显示时间
     */
    private Long showTime;

    /**
     * ttl
     */
    @FXML
    private NumberTextField ttl;

    /**
     * redis客户端
     */
    private RedisClient client;

    /**
     * 到期预览
     */
    @FXML
    private FXLabel expirePreview;

    /**
     * 树键
     */
    private RedisKeyTreeItem treeItem;

    /**
     * 添加redis键
     */
    @FXML
    private void ttlSetting() {
        // 获取键值
        Number ttlValue = this.ttl.getValue();
        try {
            if (ttlValue.longValue() <= -1) {
                this.client.persist(this.treeItem.dbIndex(), this.treeItem.key());
            } else if (ttlValue.longValue() == 0) {
                if (MessageBox.confirm(ShellI18nHelper.redisTtlTip1())) {
                    this.client.del(this.treeItem.dbIndex(), this.treeItem.key());
                }
            } else {
                this.client.expire(this.treeItem.dbIndex(), this.treeItem.key(), ttlValue.longValue(), null);
            }
            ShellEventUtil.redisKeyTTLUpdated(this.treeItem.shellConnect(), ttlValue.longValue(), this.treeItem.key(), this.treeItem.dbIndex());
            MessageBox.okToast(I18nHelper.operationSuccess());
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void persistKey() {
        this.ttl.setValue(-1L);
    }

    @FXML
    private void expireAt1D() {
        this.ttl.setValue(24 * 3600);
    }

    @FXML
    private void expireAt1H() {
        this.ttl.setValue(3600);
    }

    @FXML
    private void expireAt1M() {
        this.ttl.setValue(60);
    }

    @FXML
    private void appendWith1M() {
        long ttl = this.ttl.getValue();
        if (ttl <= -1) {
            this.expireAt1M();
        } else {
            this.ttl.setValue(ttl + 60);
        }
    }

    @FXML
    private void appendWith1H() {
        long ttl = this.ttl.getValue();
        if (ttl <= -1) {
            this.expireAt1H();
        } else {
            this.ttl.setValue(ttl + 3600);
        }
    }

    @FXML
    private void appendWith1D() {
        long ttl = this.ttl.getValue();
        if (ttl <= -1) {
            this.expireAt1D();
        } else {
            this.ttl.setValue(ttl + 24 * 3600);
        }
    }

    @FXML
    private void resetTTL() {
        this.ttl.setValue(this.treeItem.ttl());
    }

    @Override
    protected void bindListeners() {
        this.ttl.addTextChangeListener((observable, oldValue, newValue) -> {
            long ttl = this.ttl.getValue();
            if (ttl <= -1) {
                this.expirePreview.setText(I18nHelper.neverExpire());
            } else {
                this.expirePreview.setText(Const.DATE_FORMAT.format(new Date(this.showTime + ttl * 1000)));
            }
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.showTime = System.currentTimeMillis();
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
        this.treeItem = this.getProp("treeItem");
        this.client = this.treeItem.client();
        Long ttl = this.treeItem.ttl();
        if (ttl == null || ttl <= -1) {
            this.ttl.setValue(-1);
            this.expirePreview.setText(I18nHelper.neverExpire());
        } else {
            this.ttl.setValue(ttl);
            this.expirePreview.setText(Const.DATE_FORMAT.format(new Date(System.currentTimeMillis() + ttl * 1000)));
        }
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("shell.redis.title.key.ttlUpdate");
    }
}
