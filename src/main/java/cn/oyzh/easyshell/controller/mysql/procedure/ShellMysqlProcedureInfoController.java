package cn.oyzh.easyshell.controller.mysql.procedure;

import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.procedure.ShellMysqlProcedureTreeItem;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * mysql过程信息业务
 *
 * @author oyzh
 * @since 2024/01/30
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "mysql/procedure/shellMysqlProcedureInfo.fxml"
)
public class ShellMysqlProcedureInfoController extends StageController {

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
     * 过程节点
     */
    private ShellMysqlProcedureTreeItem treeItem;

    /**
     * 初始化信息
     */
    private void initInfo() {
        ShellMysqlDatabaseTreeItem dbItem = this.treeItem.dbItem();
        MysqlProcedure procedure = dbItem.selectProcedure(this.treeItem.procedureName());
        this.name.setText(procedure.getName());
        this.comment.setText(procedure.getComment());
        this.definition.setText(procedure.getDefinition());
        this.createDefinition.setText(procedure.getCreateDefinition());
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.treeItem = this.getProp("item");
        StageManager.showMask(this::initInfo);
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.procedureInfo();
    }
}
