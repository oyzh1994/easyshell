package cn.oyzh.easyshell.controller.mongo.document;

import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.column.MongoColumns;
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
        value = FXConst.FXML_PATH + "mongo/document/mongoCollectionDocumentAdd.fxml"
)
public class ShellMongoCollectionDocumentAddController extends StageController {

    /**
     * 文档
     */
    @FXML
    private Editor doc;

    /**
     * 添加文档
     */
    @FXML
    private void add() {
        try {
            // 检查字段是否存在
            String doc = this.doc.getText();
            this.setProp("doc", doc);
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addDocument();
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        MongoColumns columns = this.getProp("columns");
        if (columns.isEmpty()) {
            this.doc.setText("""
                    {
                    
                    }""");
        } else {
            //            JSONObject object = new JSONObject();
            //            for (MongoColumn column : columns) {
            //                object.put(column.getName(), column.defaultValue());
            //            }
            //            this.doc.setText(JSONUtil.toPretty(object));
            MongoRecord record = new MongoRecord(columns);
            for (MongoColumn column : columns) {
                record.putValue(column.getName(), column.defaultValue());
            }
            this.doc.setText(ShellMongoDataUtil.getRecordScript(record, false));
        }
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }
}
