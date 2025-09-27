package cn.oyzh.easyshell.controller.redis.row;

import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.trees.redis.ShellRedisStringKeyTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
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

import java.util.List;
import java.util.stream.Collectors;


/**
 * redis添加hyLog元素
 *
 * @author oyzh
 * @since 2023/06/27
 */
@StageAttribute(
        modality = Modality.WINDOW_MODAL,
        value = FXConst.FXML_PATH + "redis/row/shellRedisHylogElementsAdd.fxml"
)
public class ShellRedisHylogElementsAddController extends StageController {

    /**
     * 行数据
     */
    @FXML
    private Editor rowValue;

    /**
     * redis键
     */
    private ShellRedisStringKeyTreeItem treeItem;

    /**
     * 添加行
     */
    @FXML
    private void addRow() {
        try {
            // 行数据
            String rowValue = this.rowValue.getText();
            if (StringUtil.isEmpty(rowValue) || StringUtil.isBlank(rowValue)) {
                MessageBox.tipMsg(I18nHelper.contentCanNotEmpty(), this.rowValue);
                return;
            }
            List<String> elements = rowValue.lines().collect(Collectors.toList());
            CollectionUtil.removeBlank(elements);
            if (elements.isEmpty()) {
                MessageBox.tipMsg(I18nHelper.contentCanNotEmpty(), this.rowValue);
                return;
            }
            // redis键
            String key = this.treeItem.key();
            // 获取键值
            int dbIndex = this.treeItem.dbIndex();
            // redis客户端
            ShellRedisClient client = this.treeItem.client();
            String[] array = ArrayUtil.toArray(elements, String.class);
            if (client.pfadd(dbIndex, key, array) <= 0) {
                MessageBox.warn(ShellI18nHelper.redisAddTip3());
                return;
            }
            // 结果
            this.setProp("result", true);
            // // 发送事件
            // ShellEventUtil.redisHyLogElementsAdded(this.treeItem, key, array);
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
        return I18nResourceBundle.i18nString("shell.redis.title.hyLogElementsAdd");
    }
}
