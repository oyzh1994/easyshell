package cn.oyzh.easyshell.controller.sqlserver.view;

import cn.oyzh.easyshell.sqlserver.view.SqlServerView;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.view.ShellSqlServerViewTreeItem;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
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
 * sqlserver视图信息业务
 *
 * @author oyzh
 * @since 2025-11-21
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "sqlserver/view/shellSqlServerViewInfo.fxml"
)
public class ShellSqlServerViewInfoController extends StageController {

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
     * 定义
     */
    @FXML
    private Editor definition;

    /**
     * ddl
     */
    @FXML
    private Editor createDefinition;

    /**
     * 视图节点
     */
    private ShellSqlServerViewTreeItem treeItem;

    /**
     * 初始化信息
     */
    private void initInfo() {
        ShellSqlServerSchemaTreeItem dbItem = this.treeItem.dbItem();
        SqlServerView view = dbItem.selectView(this.treeItem.viewName());
        this.name.setText(view.getName());
        this.comment.setText(view.getComment());
        this.definition.setText(view.getDefinition());
        this.createDefinition.setText(view.getCreateDefinition());
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

//    @Override
//    public void destroy() {
//        this.definition.destroy();
//        this.createDefinition.destroy();
//        super.destroy();
//    }
}
