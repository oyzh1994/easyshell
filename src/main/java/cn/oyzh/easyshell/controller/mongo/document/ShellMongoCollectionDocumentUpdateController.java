package cn.oyzh.easyshell.controller.mongo.document;

import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.util.mongo.ShellMongoDataUtil;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 添加记录库业务
 *
 * @author oyzh
 * @since 2026/06/03
 */
@StageAttribute(
        modality = Modality.WINDOW_MODAL,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "mongo/document/mongoCollectionDocumentUpdate.fxml"
)
public class ShellMongoCollectionDocumentUpdateController extends StageController {

    /**
     * 文档
     */
    @FXML
    private Editor doc;

    /**
     * 更新文档
     */
    @FXML
    private void update() {
        try {
            // 检查字段是否存在
            String doc = this.doc.getText();
            //            if (JSONUtil.isJson(doc)) {
            this.setProp("doc", doc);
            this.closeWindow();
            //            } else {
            //                MessageBox.warn(I18nHelper.documentInvalid());
            //            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.updateDocument();
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        MongoRecord record = this.getProp("document");
        String text = ShellMongoDataUtil.getRecordScript(record, true);
        this.doc.setText(text);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }
}
