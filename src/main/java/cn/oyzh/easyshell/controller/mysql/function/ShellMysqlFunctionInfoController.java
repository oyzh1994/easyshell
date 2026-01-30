package cn.oyzh.easyshell.controller.mysql.function;

import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.function.ShellMysqlFunctionTreeItem;
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
 * mysql函数信息业务
 *
 * @author oyzh
 * @since 2024/01/30
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "mysql/function/shellMysqlFunctionInfo.fxml"
)
public class ShellMysqlFunctionInfoController extends StageController {

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
     * 函数节点
     */
    private ShellMysqlFunctionTreeItem treeItem;

    /**
     * 初始化信息
     */
    private void initInfo() {
        ShellMysqlDatabaseTreeItem dbItem = this.treeItem.dbItem();
        MysqlFunction function = dbItem.selectFunction(this.treeItem.functionName());
        this.name.setText(function.getName());
        this.comment.setText(function.getComment());
        this.definition.setText(function.getDefinition());
        this.createDefinition.setText(function.getCreateDefinition());
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
        return I18nHelper.functionInfo();
    }
}
