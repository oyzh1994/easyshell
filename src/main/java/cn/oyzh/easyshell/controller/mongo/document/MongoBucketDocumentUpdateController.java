package cn.oyzh.easyshell.controller.mongo.document;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.mongo.script.MongoScriptUtil;
import cn.oyzh.easyshell.util.mongo.ShellMongoUtil;
import cn.oyzh.fx.editor.incubator.control.JsonEditor;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import org.bson.Document;

/**
 * 添加db信息业务
 *
 * @author oyzh
 * @since 2023/12/22
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "mongo/document/mongoBucketDocumentUpdate.fxml"
)
public class MongoBucketDocumentUpdateController extends StageController {

    /**
     * 文件名
     */
    @FXML
    private ClearableTextField filename;

//    /**
//     * 内容类型
//     */
//    @FXML
//    private ClearableTextField contentType;

    /**
     * 元数据
     */
    @FXML
    private JsonEditor metadata;

    /**
     * 数据
     */
    private MongoRecord record;

    /**
     * 添加db信息
     */
    @FXML
    private void update() {
        try {
            String metadata = this.metadata.getTextTrim();
            metadata = metadata.strip();
            Document metadataDocument;
            if (metadata.isBlank()) {
                metadataDocument = null;
            } else if (metadata.startsWith("{") && JSONUtil.isJson(metadata)) {
                metadataDocument =MongoScriptUtil.toDocument(JSONUtil.parseObject(metadata));
            } else {
                MessageBox.warn(I18nHelper.invalidMetadata());
                return;
            }
//            String contentType = this.contentType.getTextTrim();
            String filename = this.filename.getTextTrim();
            MongoRecord record = new MongoRecord(this.record.getColumns());
            record.putValue(ShellMongoUtil.ID, this.record._idValue());
            record.putValue("filename", filename);
            record.putValue("metadata", metadataDocument);
            record.getProperty("metadata").setOriginal(metadata);
//            record.putValue("contentType", contentType);
            this.setProp("document", record);
            this.closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.record = this.removeProp("document");
        this.filename.setText((String) this.record.getValue("filename"));
        this.metadata.setText((String) this.record.getValue("metadata"));
//        this.contentType.setText((String) this.record.getValue("contentType"));
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.updateDocument();
    }
}
