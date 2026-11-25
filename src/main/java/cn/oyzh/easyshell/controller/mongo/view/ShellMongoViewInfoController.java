package cn.oyzh.easyshell.controller.mongo.view;

import cn.oyzh.easyshell.mongo.view.MongoView;
import cn.oyzh.easyshell.trees.mongo.view.ShellMongoViewTreeItem;
import cn.oyzh.easyshell.util.mongo.ShellMongoViewDesignUtil;
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
 * MongoDB 视图信息。
 *
 * @author oyzh
 * @since 2026-11-26
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "mongo/view/shellMongoViewInfo.fxml"
)
public class ShellMongoViewInfoController extends StageController {

    @FXML
    private ReadOnlyTextField name;

    @FXML
    private ReadOnlyTextField database;

    @FXML
    private ReadOnlyTextField viewOn;

    @FXML
    private Editor pipeline;

    @FXML
    private Editor createDefinition;

    private ShellMongoViewTreeItem treeItem;

    private void initInfo() {
        MongoView view = this.treeItem.dbItem()
                .selectView(this.treeItem.viewName());
        this.name.setText(view.getName());
        this.database.setText(this.treeItem.dbName());
        this.viewOn.setText(view.getViewOn());
        this.pipeline.setText(ShellMongoViewDesignUtil.pipelineText(view.getPipeline()));
        this.createDefinition.setText(ShellMongoViewDesignUtil.toScript(view, true));
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
        this.pipeline.destroy();
        this.createDefinition.destroy();
        super.destroy();
    }
}
