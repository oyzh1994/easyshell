package cn.oyzh.easyshell.controller.sqlite.view;

import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewTreeItem;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * sqlite视图信息业务
 *
 * @author oyzh
 * @since 2026-11-02
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "sqlite/view/shellSqliteViewInfo.fxml"
)
public class ShellSqliteViewInfoController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ReadOnlyTextField name;

    /**
     * 注释
     */
    @FXML
    private ReadOnlyTextArea comment;

    /**
     * 查询定义
     */
    @FXML
    private Editor definition;

    /**
     * 创建定义
     */
    @FXML
    private Editor createDefinition;

    /**
     * 视图节点
     */
    private ShellSqliteViewTreeItem treeItem;

    /**
     * 初始化信息
     */
    private void initInfo() {
        SQLiteView view = this.treeItem.client().selectView(this.treeItem.viewName());
        if (view == null) {
            MessageBox.warn(I18nResourceBundle.i18nString(I18nHelper.NOT_FOUND), this.getWindow());
            this.stage.close();
            return;
        }
        this.name.setText(view.getName());
        this.comment.setText(view.getComment());
        this.definition.setText(view.getDefinition());
        this.createDefinition.setText(this.treeItem.client().showCreate(view.getName()));
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
        return I18nHelper.viewInfo();
    }

    @Override
    public void destroy() {
        this.definition.destroy();
        this.createDefinition.destroy();
        super.destroy();
    }
}
