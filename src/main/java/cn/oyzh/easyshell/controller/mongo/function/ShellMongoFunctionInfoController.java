package cn.oyzh.easyshell.controller.mongo.function;

import cn.oyzh.easyshell.mongo.function.MongoFunction;
import cn.oyzh.easyshell.trees.mongo.function.ShellMongoFunctionTreeItem;
import cn.oyzh.easyshell.util.mongo.ShellMongoDataUtil;
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
 * MongoDB 函数信息。
 *
 * @author oyzh
 * @since 2026-11-26
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "mongo/function/shellMongoFunctionInfo.fxml"
)
public class ShellMongoFunctionInfoController extends StageController {

    @FXML
    private ReadOnlyTextField name;

    @FXML
    private ReadOnlyTextField database;

    @FXML
    private Editor definition;

    @FXML
    private Editor createDefinition;

    private ShellMongoFunctionTreeItem treeItem;

    private void initInfo() {
        MongoFunction function = this.treeItem.dbItem()
                .selectFunction(this.treeItem.functionName());
        this.name.setText(function.getName());
        this.database.setText(this.treeItem.dbName());
        this.definition.setText(function.getCode());
        this.createDefinition.setText(ShellMongoDataUtil.toReplaceScript(function));
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
        return I18nHelper.functionInfo();
    }

    @Override
    public void destroy() {
        this.definition.destroy();
        this.createDefinition.destroy();
        super.destroy();
    }
}
