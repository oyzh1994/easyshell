package cn.oyzh.easyshell.tabs.mongo.collection;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.mongo.collection.MongoCollection;
import cn.oyzh.easyshell.mongo.collection.MongoCollectionOptions;
import cn.oyzh.easyshell.mongo.index.MongoIndex;
import cn.oyzh.easyshell.mongo.index.MongoIndexControl;
import cn.oyzh.easyshell.trees.mongo.collection.ShellMongoCollectionsTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.mongo.ShellMongoCollectionDesignUtil;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.db.ui.DBStatusTableView;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * MongoDB 集合设计标签页业务。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class ShellMongoCollectionDesignTabController extends RichTabController {

    /**
     * 切换面板
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 是否固定大小集合
     */
    @FXML
    private FXCheckBox capped;

    /**
     * 集合容量
     */
    @FXML
    private NumberTextField sizeInBytes;

    /**
     * 最大文档数
     */
    @FXML
    private NumberTextField maxDocuments;

    /**
     * 校验级别
     */
    @FXML
    private FXComboBox<String> validationLevel;

    /**
     * 校验动作
     */
    @FXML
    private FXComboBox<String> validationAction;

    /**
     * 校验器
     */
    @FXML
    private Editor validator;

    /**
     * 索引表格
     */
    @FXML
    private DBStatusTableView<MongoIndexControl> indexTable;

    /**
     * 预览
     */
    @FXML
    private Editor preview;

    /**
     * 集合
     */
    private MongoCollection collection;

    /**
     * 数据库节点
     */
    private ShellMongoDatabaseTreeItem dbItem;

    /**
     * 已加载的集合参数
     */
    private MongoCollectionOptions loadedOptions;

    /**
     * 数据监听器
     */
    private DBStatusListener listener;

    /**
     * 未保存标志位
     */
    private boolean unsaved;

    /**
     * 新数据标志位
     */
    private boolean newData;

    /**
     * 初始化中标志位
     */
    private boolean initiating;

    /**
     * 新建集合名称
     */
    private String newCollectionName;

    /**
     * 初始化
     *
     * @param collection 集合
     * @param dbItem     数据库节点
     */
    public void init(MongoCollection collection, ShellMongoDatabaseTreeItem dbItem) {
        this.collection = collection;
        this.dbItem = dbItem;
        this.newData = collection.isNew();
        StageManager.showMask(this.getWindow(), this::doInit);
    }

    /**
     * 执行初始化
     */
    private void doInit() {
        this.initDBListener();
        this.initInfo();
    }

    /**
     * 初始化监听器
     */
    private void initDBListener() {
        if (this.listener != null) {
            return;
        }
        this.listener = new DBStatusListener(this.dbName() + ":collection-design:" + this.collection.getName()) {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                initChangedFlag();
            }
        };
        DBStatusListenerManager.bindListener(this.capped, this.listener);
        DBStatusListenerManager.bindListener(this.sizeInBytes, this.listener);
        DBStatusListenerManager.bindListener(this.maxDocuments, this.listener);
        DBStatusListenerManager.bindListener(this.validationLevel, this.listener);
        DBStatusListenerManager.bindListener(this.validationAction, this.listener);
        this.validator.addTextChangeListener((observable, oldValue, newValue) ->
                this.listener.changed(observable, oldValue, newValue));
        this.indexTable.setStatusListener(this.listener);
        this.indexTable.setCtrlSAction(this::save);

        this.capped.selectedChanged((observable, oldValue, newValue) -> this.updateCappedControls());
    }

    /**
     * 初始化变更标志
     */
    private void initChangedFlag() {
        if (!this.initiating) {
            this.unsaved = true;
            this.flushTab();
        }
    }

    /**
     * 初始化信息
     */
    private void initInfo() {
        this.initiating = true;
        try {
            if (this.newData) {
                this.initNew();
            } else {
                this.initNormal();
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            FXUtil.runPulse(() -> {
                this.initiating = false;
                this.updateActionVisibility();
                this.initPreview();
                this.flushTab();
            });
        }
    }

    /**
     * 初始化新建集合
     */
    private void initNew() throws Exception {
        this.unsaved = true;
        this.loadedOptions = null;
        this.newCollectionName = null;
        this.capped.setSelected(false);
        this.capped.setDisable(false);
        this.sizeInBytes.setValue(null);
        this.maxDocuments.setValue(null);
        this.validationLevel.select("strict");
        this.validationAction.select("error");
        this.validator.text("");
        this.validator.forgetHistory();
        this.indexTable.reset();
        this.indexTable.setItem(Collections.emptyList());
        this.updateCappedControls();
        NodeGroupUtil.disappear(this.getTab(), "action3");
    }

    /**
     * 初始化已有集合
     */
    private void initNormal() throws Exception {
        this.unsaved = false;
        MongoCollectionOptions options = this.dbItem.client()
                .selectCollectionOptions(this.dbName(), this.collection.getName());
        this.loadedOptions = options;
        if (StringUtil.isBlank(options.getValidationLevel())) {
            options.setValidationLevel("strict");
        }
        if (StringUtil.isBlank(options.getValidationAction())) {
            options.setValidationAction("error");
        }
        this.capped.setSelected(options.isCapped());
        this.capped.setDisable(true);
        this.sizeInBytes.setValue(options.getSizeInBytes());
        this.maxDocuments.setValue(options.getMaxDocuments());
        this.validationLevel.select(options.getValidationLevel());
        this.validationAction.select(options.getValidationAction());
        this.validator.text(ShellMongoCollectionDesignUtil.validatorText(options.getValidator()));
        this.validator.forgetHistory();
        this.updateCappedControls();

        List<MongoIndex> indexes = this.dbItem.client()
                .listCollectionIndexes(this.dbName(), this.collection.getName());
        List<MongoIndexControl> controls = new ArrayList<>();
        for (MongoIndex index : indexes) {
            controls.add(MongoIndexControl.of(index));
        }
        this.indexTable.reset();
        this.indexTable.setItem(controls);
        NodeGroupUtil.display(this.getTab(), "action3");
    }

    /**
     * 更新固定大小集合控件状态
     */
    private void updateCappedControls() {
        boolean editable = this.newData && this.capped.isSelected();
        this.sizeInBytes.setDisable(!editable);
        this.maxDocuments.setDisable(!editable);
    }

    /**
     * 保存
     */
    @FXML
    private void save() {
        StageManager.showMask(this.getWindow(), this::doSave);
    }

    /**
     * 执行保存
     */
    private void doSave() {
        try {
            if (this.newData) {
                String collectionName = MessageBox.prompt(I18nHelper.pleaseInputCollectionName(),
                        this.collectionName(), this.getWindow());
                if (collectionName == null) {
                    return;
                }
                this.newCollectionName = collectionName.trim();
            }
            MongoCollectionOptions options = this.currentOptions();
            if (StringUtil.isBlank(options.getName())) {
                MessageBox.warn(ShellI18nHelper.mongoInvalidCollectionName(), this.getWindow());
                this.tabPane.selectTab("optionsTab");
                return;
            }
            if (options.isCapped()
                    && (options.getSizeInBytes() == null || options.getSizeInBytes() <= 0)) {
                MessageBox.warn(ShellI18nHelper.mongoCappedSizeRequired(), this.getWindow());
                this.tabPane.selectTab("optionsTab");
                return;
            }
            if (this.newData && this.dbItem.client().existCollection(this.dbName(), options.getName())) {
                MessageBox.warn(ShellI18nHelper.mongoCollectionExists(options.getName()), this.getWindow());
                this.tabPane.selectTab("optionsTab");
                return;
            }

            if (this.newData) {
                this.createCollection(options, this.currentIndexes());
            } else {
                this.validateIndexText();
                this.alertCollection(options);
            }

            this.newData = false;
            this.unsaved = false;
            this.collection.setName(options.getName());
            this.collection.setDbName(this.dbName());
            this.initInfo();
        } catch (ShellException ex) {
            MessageBox.warn(ex.getMessage(), this.getWindow());
        } catch (IllegalArgumentException ex) {
            MessageBox.warn(ShellI18nHelper.mongoInvalidIndex(ex.getMessage()), this.getWindow());
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.flushTab();
        }
    }

    /**
     * 创建集合
     *
     * @param options 集合参数
     * @param indexes 索引列表
     */
    private void createCollection(MongoCollectionOptions options, List<MongoIndex> indexes) {
        boolean created = false;
        try {
            this.dbItem.client().createCollection(options);
            created = true;
            for (MongoIndex index : indexes) {
                if (!index.isIdIndex()) {
                    this.dbItem.client().createIndex(this.dbName(), options.getName(), index);
                }
            }
        } catch (RuntimeException ex) {
            if (created) {
                try {
                    this.dbItem.client().dropCollection(this.dbName(), options.getName());
                } catch (Exception ignored) {
                }
            }
            throw ex;
        }
        ShellMongoCollectionsTreeItem collectionsItem = this.dbItem.getCollectionTypeChild();
        if (collectionsItem != null) {
            MongoCollection collection = new MongoCollection();
            collection.setDbName(this.dbName());
            collection.setName(options.getName());
            collectionsItem.addCollection(collection);
        }
    }

    /**
     * 修改集合
     *
     * @param options 集合参数
     */
    private void alertCollection(MongoCollectionOptions options) {
        String collectionName = this.collection.getName();
        List<String> droppedNames = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(this.indexTable.getDeleteItems())) {
            for (MongoIndexControl index : this.indexTable.getDeleteItems()) {
                if (index.isIdIndex()) {
                    throw new ShellException(ShellI18nHelper.mongoIdIndexNotEditable());
                }
                droppedNames.add(index.getName());
            }
        }
        for (MongoIndexControl control : this.indexTable.getItems()) {
            if (control.isIdIndex()) {
                if (control.isChanged() || control.isCreated()) {
                    throw new ShellException(ShellI18nHelper.mongoIdIndexNotEditable());
                }
                continue;
            }
            if (control.isChanged() && !control.isCreated()) {
                droppedNames.add(control.originalName());
            }
        }
        if (this.isValidatorChanged(options)) {
            this.dbItem.client().updateCollectionOptions(this.dbName(), options);
        }
        for (String name : droppedNames) {
            if (StringUtil.isNotBlank(name)) {
                this.dbItem.client().dropIndex(this.dbName(), collectionName, name);
            }
        }
        for (MongoIndexControl control : this.indexTable.getItems()) {
            if (control.isIdIndex()) {
                continue;
            }
            if (control.isCreated() || control.isChanged()) {
                this.dbItem.client().createIndex(this.dbName(), collectionName, control.toIndex());
            }
        }
    }

    /**
     * 校验器是否变更
     *
     * @param options 当前集合参数
     * @return 结果
     */
    private boolean isValidatorChanged(MongoCollectionOptions options) {
        if (this.loadedOptions == null) {
            return true;
        }
        return !Objects.equals(this.loadedOptions.getValidator(), options.getValidator())
                || !StringUtil.equals(this.loadedOptions.getValidationLevel(), options.getValidationLevel())
                || !StringUtil.equals(this.loadedOptions.getValidationAction(), options.getValidationAction());
    }

    /**
     * 获取当前集合参数
     *
     * @return 集合参数
     */
    private MongoCollectionOptions currentOptions() {
        MongoCollectionOptions options = new MongoCollectionOptions();
        options.setDbName(this.dbName());
        options.setName(this.newData ? this.newCollectionName : this.collection.getName());
        options.setCapped(this.capped.isSelected());
        options.setSizeInBytes(this.sizeInBytes.getLongValue());
        options.setMaxDocuments(this.maxDocuments.getLongValue());
        try {
            options.setValidator(ShellMongoCollectionDesignUtil.parseValidator(this.validator.getTextTrim()));
        } catch (Exception ex) {
            throw new ShellException(ShellI18nHelper.mongoInvalidValidator());
        }
        options.setValidationLevel(this.validationLevel.getSelectedItem());
        options.setValidationAction(this.validationAction.getSelectedItem());
        return options;
    }

    /**
     * 获取当前索引列表
     *
     * @return 索引列表
     */
    private List<MongoIndex> currentIndexes() {
        List<MongoIndex> indexes = new ArrayList<>();
        for (MongoIndexControl control : this.indexTable.getItems()) {
            if (control.isIdIndex()) {
                continue;
            }
            try {
                indexes.add(control.toIndex());
            } catch (Exception ex) {
                throw new IllegalArgumentException(control.getName());
            }
        }
        return indexes;
    }

    /**
     * 校验索引编辑文本
     */
    private void validateIndexText() {
        for (MongoIndexControl control : this.indexTable.getItems()) {
            if (control.isIdIndex()) {
                continue;
            }
            try {
                control.validateText();
            } catch (Exception ex) {
                throw new IllegalArgumentException(control.getName());
            }
        }
    }

    /**
     * 新增索引
     */
    private void addIndex() {
        MongoIndexControl index = new MongoIndexControl();
        index.setName(DBUtil.genIndexName());
        index.setKeysText("");
        index.setPartialFilterExpressionText("");
        index.setCreated(true);
        this.indexTable.addItem(index);
        this.indexTable.selectLast();
    }

    /**
     * 删除索引
     */
    private void deleteIndex() {
        try {
            MongoIndexControl index = this.indexTable.getSelectedItem();
            if (index == null) {
                return;
            }
            if (index.isIdIndex()) {
                MessageBox.warn(ShellI18nHelper.mongoIdIndexNotEditable(), this.getWindow());
                return;
            }
            if (!index.isCreated()
                    && !MessageBox.confirm(ShellI18nHelper.confirmDeleteIndex(index.getName()), this.getWindow())) {
                return;
            }
            this.indexTable.removeItem(index);
            index.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 刷新
     */
    @FXML
    private void refresh() {
        if (this.newData) {
            return;
        }
        if (!MessageBox.confirm(ShellI18nHelper.confirmRefreshData(), this.getWindow())) {
            return;
        }
        StageManager.showMask(this.getWindow(), () -> {
            this.initInfo();
            this.flushTab();
        });
    }

    /**
     * 执行添加
     */
    @FXML
    private void doAdd() {
        if (this.tabPane.isSelectedTab("indexTab")) {
            this.addIndex();
        }
    }

    /**
     * 执行删除
     */
    @FXML
    private void doDelete() {
        if (this.tabPane.isSelectedTab("indexTab")) {
            this.deleteIndex();
        }
    }

    /**
     * 更新操作按钮可见性
     */
    private void updateActionVisibility() {
        if (this.tabPane.isSelectedTab("indexTab")) {
            NodeGroupUtil.display(this.getTab(), "indexAction");
        } else {
            NodeGroupUtil.disappear(this.getTab(), "indexAction");
        }
    }

    /**
     * 初始化预览
     */
    private void initPreview() {
        try {
            MongoCollectionOptions options = this.currentOptions();
            if (this.newData) {
                this.preview.text(ShellMongoCollectionDesignUtil.toCreateCollectionScript(
                        options, this.currentIndexes()));
                return;
            }
            StringBuilder builder = new StringBuilder();
            if (this.isValidatorChanged(options)) {
                builder.append(ShellMongoCollectionDesignUtil.toCollModScript(options));
            }
            if (CollectionUtil.isNotEmpty(this.indexTable.getDeleteItems())) {
                for (MongoIndexControl index : this.indexTable.getDeleteItems()) {
                    if (!index.isIdIndex()) {
                        builder.append(ShellMongoCollectionDesignUtil.toDropIndexScript(
                                this.collection.getName(), index.getName())).append('\n');
                    }
                }
            }
            for (MongoIndexControl control : this.indexTable.getItems()) {
                if (control.isIdIndex()) {
                    continue;
                }
                if (control.isChanged() && !control.isCreated()) {
                    builder.append(ShellMongoCollectionDesignUtil.toDropIndexScript(
                            this.collection.getName(), control.originalName())).append('\n');
                }
                if (control.isCreated() || control.isChanged()) {
                    builder.append(ShellMongoCollectionDesignUtil.toCreateIndexScript(
                            this.collection.getName(), control.toIndex())).append('\n');
                }
            }
            this.preview.text(builder.toString());
        } catch (Exception ex) {
            this.preview.text("// " + ex.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);
        this.validationLevel.addItems(List.of("strict", "moderate", "off"));
        this.validationAction.addItems(List.of("error", "warn"));

        NodeUtil.nodeOnCtrlS(this.getTab() == null ? null : this.getTab().getContent(), this::save);
        NodeUtil.nodeOnCtrlS(this.validator, this::save);

        this.tabPane.selectedItemChanged((observable, oldValue, newValue) -> {
            this.updateActionVisibility();
            if (newValue != null && "previewTab".equals(newValue.getId())) {
                this.initPreview();
            }
        });
    }

    /**
     * 获取集合名称
     *
     * @return 集合名称
     */
    public String collectionName() {
        return this.newData ? this.newCollectionName : this.collection.getName();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.dbItem.dbName();
    }

    /**
     * 获取数据库节点
     *
     * @return 数据库节点
     */
    public ShellMongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    /**
     * 是否未保存
     *
     * @return 是否未保存
     */
    public boolean isUnsaved() {
        return unsaved;
    }

    @Override
    public void destroy() {
        if (this.listener != null) {
            this.listener.destroy();
            this.listener = null;
        }
        this.preview.destroy();
        this.validator.destroy();
        super.destroy();
    }
}
