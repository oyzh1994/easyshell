package cn.oyzh.easyshell.controller.redis.row;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.trees.redis.ShellRedisZSetKeyTreeItem;
import cn.oyzh.fx.editor.tm4javafx.Editor;
import cn.oyzh.fx.gui.text.field.DecimalTextField;
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
 * redis添加zset坐标
 *
 * @author oyzh
 * @since 2023/06/27
 */
@StageAttribute(
        modality = Modality.WINDOW_MODAL,
        value = FXConst.FXML_PATH + "redis/row/shellRedisZSetCoordinateAdd.fxml"
)
public class ShellRedisZSetCoordinateAddController extends StageController {

    /**
     * 坐标名称
     */
    @FXML
    private Editor rowValue;

    /**
     * 经度
     */
    @FXML
    private DecimalTextField longitude;

    /**
     * 纬度
     */
    @FXML
    private DecimalTextField latitude;

    /**
     * redis键
     */
    private ShellRedisZSetKeyTreeItem treeItem;

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
            Number longitudeValue = this.longitude.getValue();
            if (longitudeValue == null) {
                MessageBox.tipMsg(I18nHelper.contentCanNotEmpty(), this.latitude);
                return;
            }
            Number latitudeValue = this.latitude.getValue();
            if (latitudeValue == null) {
                MessageBox.tipMsg(I18nHelper.contentCanNotEmpty(), this.latitude);
                return;
            }
            // redis键
            String key = this.treeItem.key();
            // 获取键值
            int dbIndex = this.treeItem.dbIndex();
            // redis客户端
            ShellRedisClient client = this.treeItem.client();
            if (client.zrank(dbIndex, key, rowValue) != null) {
                MessageBox.warn(I18nHelper.alreadyExists());
                return;
            }
            double longitude = longitudeValue.doubleValue();
            double latitude = latitudeValue.doubleValue();
            // 添加元素
            client.geoadd(dbIndex, key, longitude, latitude, rowValue);
            // 结果
            this.setProp("result", true);
            // // 发送事件
            // ShellEventUtil.redisZSetCoordinateAdded(this.treeItem, key, rowValue, longitude, latitude);
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

    @Override
    public void onWindowShown(WindowEvent event) {
        this.treeItem = this.getProp("treeItem");
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
        super.onWindowShown(event);
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("shell.redis.title.zSetCoordinateAdd");
    }
}
