package cn.oyzh.easyshell.controller.mongo.collection;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.mongo.collection.MongoCollectionOptions;
import cn.oyzh.easyshell.mongo.index.MongoIndex;
import cn.oyzh.easyshell.trees.mongo.collection.ShellMongoCollectionTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.mongo.ShellMongoCollectionDesignUtil;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import org.bson.Document;

import java.util.Collections;
import java.util.List;

/**
 * MongoDB 集合信息。
 *
 * @author oyzh
 * @since 2026-11-26
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "mongo/collection/shellMongoCollectionInfo.fxml"
)
public class ShellMongoCollectionInfoController extends StageController {

    @FXML
    private ReadOnlyTextField name;

    @FXML
    private ReadOnlyTextField database;

    @FXML
    private ReadOnlyTextField documentCount;

    @FXML
    private ReadOnlyTextField indexCount;

    @FXML
    private ReadOnlyTextField storageSize;

    @FXML
    private ReadOnlyTextField capped;

    @FXML
    private ReadOnlyTextField maxDocuments;

    @FXML
    private Editor createDefinition;

    private ShellMongoCollectionTreeItem treeItem;

    private void initInfo() {
        String dbName = this.treeItem.dbName();
        String collectionName = this.treeItem.collectionName();
        MongoCollectionOptions options = this.treeItem.client()
                .selectCollectionOptions(dbName, collectionName);
        List<MongoIndex> indexes = Collections.emptyList();
        Document stats = null;
        try {
            indexes = this.treeItem.client().listCollectionIndexes(dbName, collectionName);
        } catch (Exception ignored) {
        }
        try {
            stats = this.treeItem.client().collectionStats(dbName, collectionName);
        } catch (Exception ignored) {
        }

        long count = getLong(stats, "count", -1);
        if (count < 0) {
            try {
                count = this.treeItem.client().collectionCount(dbName, collectionName);
            } catch (Exception ignored) {
                count = 0;
            }
        }
        long indexSize = getLong(stats, "nindexes", indexes.size());
        long size = getLong(stats, "storageSize", getLong(stats, "size", 0));

        this.name.setText(collectionName);
        this.database.setText(dbName);
        this.documentCount.setText(String.valueOf(count));
        this.indexCount.setText(String.valueOf(indexSize));
        this.storageSize.setText(NumberUtil.formatSize(size, 2));
        this.capped.setText(options.isCapped() ? "true" : "false");
        this.maxDocuments.setText(options.getMaxDocuments() == null
                ? "-" : String.valueOf(options.getMaxDocuments()));
        this.createDefinition.setText(ShellMongoCollectionDesignUtil
                .toCreateCollectionScript(options, indexes));
    }

    private static long getLong(Document document, String key, long defaultValue) {
        if (document == null) {
            return defaultValue;
        }
        Object value = document.get(key);
        return value instanceof Number number ? number.longValue() : defaultValue;
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
        return ShellI18nHelper.mongoCollectionInfo();
    }

    @Override
    public void destroy() {
        this.createDefinition.destroy();
        super.destroy();
    }
}
