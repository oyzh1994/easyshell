package cn.oyzh.easyshell.controller.mariadb.function;

import cn.oyzh.easyshell.mariadb.function.MariadbFunction;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mariadb.function.ShellMariadbFunctionTreeItem;
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
 * MariaDB函数信息业务
 *
 * @author oyzh
 * @since 2026-10-09
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "mariadb/function/shellMariadbFunctionInfo.fxml"
)
public class ShellMariadbFunctionInfoController extends StageController {

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
    private ShellMariadbFunctionTreeItem treeItem;

    /**
     * 初始化信息
     */
    private void initInfo() {
        ShellMariadbDatabaseTreeItem dbItem = this.treeItem.dbItem();
        MariadbFunction function = dbItem.selectFunction(this.treeItem.functionName());
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

    @Override
    public void destroy() {
        this.definition.destroy();
        this.createDefinition.destroy();
        super.destroy();
    }
}
