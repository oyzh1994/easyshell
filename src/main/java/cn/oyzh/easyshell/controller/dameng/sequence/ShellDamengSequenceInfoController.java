package cn.oyzh.easyshell.controller.dameng.sequence;

import cn.oyzh.easyshell.dameng.sequence.DamengSequence;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.sequence.ShellDamengSequenceTreeItem;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * dameng序列信息业务
 *
 * @author oyzh
 * @since 2025-11-21
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "dameng/sequence/shellDamengSequenceInfo.fxml"
)
public class ShellDamengSequenceInfoController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ReadOnlyTextField name;

    /**
     * 最小值
     */
    @FXML
    private ReadOnlyTextField minVal;

    /**
     * 最大值
     */
    @FXML
    private ReadOnlyTextField maxVal;

    /**
     * 当前值
     */
    @FXML
    private ReadOnlyTextField currentVal;

    /**
     * 递增值
     */
    @FXML
    private ReadOnlyTextField incrementBy;

    /**
     * 饱序
     */
    @FXML
    private ReadOnlyTextField order;

    /**
     * 循环
     */
    @FXML
    private ReadOnlyTextField cycle;

    /**
     * ddl
     */
    @FXML
    private Editor createDefinition;

    /**
     * 序列节点
     */
    private ShellDamengSequenceTreeItem treeItem;

    /**
     * 初始化信息
     */
    private void initInfo() {
        ShellDamengSchemaTreeItem dbItem = this.treeItem.dbItem();
        DamengSequence sequence = dbItem.selectSequence(this.treeItem.sequenceName());
        this.name.setText(sequence.getName());
        if (sequence.getMinVal() != null) {
            this.minVal.setText(sequence.getMinVal() + "");
        }
        if (sequence.getMaxVal() != null) {
            this.maxVal.setText(sequence.getMaxVal() + "");
        }
        if (sequence.getCurrentVal() != null) {
            this.currentVal.setText(sequence.getCurrentVal() + "");
        }
        if (sequence.getIncrementBy() != null) {
            this.incrementBy.setText(sequence.getIncrementBy() + "");
        }
        this.order.setText(sequence.isOrder() ? "Y" : "N");
        this.cycle.setText(sequence.isCycle() ? "Y" : "N");
        this.createDefinition.setText(sequence.getCreateDefinition());
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.treeItem = this.getProp("item");
        StageManager.showMask(this.getWindow(), this::initInfo);
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.sequenceInfo();
    }

    @Override
    public void destroy() {
        this.createDefinition.destroy();
        super.destroy();
    }
}
