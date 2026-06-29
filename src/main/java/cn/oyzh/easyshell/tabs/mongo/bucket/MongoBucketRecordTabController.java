package cn.oyzh.easyshell.tabs.mongo.bucket;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.fx.mongo.MongoRecordColumn;
import cn.oyzh.easyshell.fx.mongo.MongoRecordTableView;
import cn.oyzh.easyshell.mongo.MongoColumn;
import cn.oyzh.easyshell.mongo.MongoColumns;
import cn.oyzh.easyshell.mongo.MongoRecord;
import cn.oyzh.easyshell.mongo.MongoRecordFilter;
import cn.oyzh.easyshell.popups.mongo.MongoPageSettingPopupController;
import cn.oyzh.easyshell.popups.mongo.MongoRecordFilterPopupController;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.trees.mongo.bucket.MongoBucketTreeItem;
import cn.oyzh.easyshell.util.mongo.MongoRecordUtil;
import cn.oyzh.easyshell.util.mongo.MongoViewFactory;
import cn.oyzh.easyshell.util.mongo.ShellFileUtil;
import cn.oyzh.fx.gui.page.PageBox;
import cn.oyzh.fx.gui.page.PageEvent;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import org.bson.types.ObjectId;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * db表tab内容组件
 *
 * @author oyzh
 * @since 2023/12/24
 */
public class MongoBucketRecordTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private FXVBox root;

    /**
     * db树表节点
     */
    private ObjectProperty<MongoBucketTreeItem> itemProperty;

    /**
     * 分页数据
     */
    private Paging<MongoRecord> pageData;

    /**
     * 记录过滤按钮
     */
    @FXML
    private SVGGlyph filter;

    /**
     * 数据分页组件
     */
    @FXML
    private PageBox<MongoRecord> pageBox;

    /**
     * 数据表单组件
     */
    @FXML
    private MongoRecordTableView recordTable;

    /**
     * 过滤列表
     */
    private List<MongoRecordFilter> filters;

    /**
     * 字段列表
     */
    private MongoColumns columns;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 执行初始化
     *
     * @param item db树表节点
     */
    public void init(MongoBucketTreeItem item) {
        this.itemProperty = new SimpleObjectProperty<>(item);
        this.itemProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                this.closeTab();
            }
        });
        item.parentProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                this.closeTab();
            }
        });
        this.reload();
    }

    public MongoBucketTreeItem getItem() {
        return this.itemProperty.get();
    }

    /**
     * 初始化数据列表
     *
     * @param pageNo 数据页码
     */
    private void initDataList(long pageNo) {
        try {
            this.pageData = this.getItem().recordPage(pageNo, this.setting.getRecordPageLimit(), this.enabledFilters(), this.columns);
            this.pageBox.setPaging(this.pageData);
            List<MongoRecord> records = this.pageData.dataList();
            //  初始化字段
            if (records.isEmpty()) {
                this.initColumns(this.getItem().bucketColumns());
            } else {
                this.initColumns(records.getFirst().getColumns());
            }
            // 初始化数据
            this.initRecords(records);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 初始化数据列表，带遮罩板
     *
     * @param pageNo 数据页码
     */
    private void initDataListByMask(long pageNo) {
        StageManager.showMask(() -> this.initDataList(pageNo));
    }

    /**
     * 获取已启用的表过滤条件
     *
     * @return 已启用的表过滤条件
     */
    private List<MongoRecordFilter> enabledFilters() {
        if (CollectionUtil.isNotEmpty(this.filters)) {
            return this.filters.stream().filter(MongoRecordFilter::isEnabled).toList();
        }
        return null;
    }

    /**
     * 初始化计数
     *
     * @param count 计数
     */
    private void initCount(long count) {
        this.pageData = new Paging<>(this.recordTable.itemList(), this.pageData.limit(), count);
        this.pageBox.setPaging(this.pageData);
    }

    /**
     * 初始化列
     *
     * @param columns 列数据
     */
    private void initColumns(MongoColumns columns) {
        // 非首次，忽略
        if (this.columns != null) {
            return;
        }
        // 设置字段列表
        this.columns = columns;
        // 数据列集合
        List<FXTableColumn<MongoRecord, Object>> columnList = new ArrayList<>();
        for (MongoColumn column : columns) {
            MongoRecordColumn recordColumn = new MongoRecordColumn(column, 0);
            if (recordColumn.getName().equals("uploadDate")) {
                recordColumn.setPrefWidth(170);
            } else if (recordColumn.getName().equals("filename")) {
                recordColumn.setPrefWidth(220);
            } else if (recordColumn.getName().equals("metadata")) {
                recordColumn.setPrefWidth(150);
                //            } else if (recordColumn.getName().equals("md5")) {
                //                recordColumn.setPrefWidth(240);
            } else {
                recordColumn.setPrefWidth(MongoRecordUtil.suitableColumnWidth(column));
            }
            columnList.add(recordColumn);
        }
        this.recordTable.setColumn(columnList);
    }

    /**
     * 初始化记录
     *
     * @param records 数据
     */
    private void initRecords(List<MongoRecord> records) {
        this.recordTable.setItem(records);
    }

    /**
     * 编辑文档
     */
    @FXML
    private void editDocument() {
        try {
            MongoRecord record = this.recordTable.getSelectedItem();
            if (record == null) {
                return;
            }
            StageAdapter adapter = MongoViewFactory.bucketDocumentUpdate(record);
            if (adapter == null) {
                return;
            }
            MongoRecord r = adapter.getProp("document");
            if (r == null) {
                return;
            }
            // 修改数据
            if (this.getItem().updateRecord(r) != 1) {
                MessageBox.warn(I18nHelper.updateDocumentFail());
            } else {
                record.putValue("filename", r.getValue("filename"));
                record.putValue("metadata", r.getProperty("metadata").getOriginal());
                //                record.putValue("contentType", r.getValue("contentType"));
                this.recordTable.refresh();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 查看文档
     *
     */
    @FXML
    public void viewDocument() {
        MongoRecord record = this.recordTable.getSelectedItem();
        if (record == null) {
            return;
        }
        String filename = (String) record.getValue("filename");
        String extName = FileNameUtil.extName(filename);
        String type = ShellFileUtil.fileViewable(extName);
        MongoViewFactory.fileView(record, this.getItem().client(), type);
        this.recordTable.refresh();
    }


    /**
     * 刷新记录
     */
    @FXML
    public void reload() {
        StageManager.showMask(this::doReload);
    }

    /**
     * 刷新记录，实际业务
     */
    private void doReload() {
        try {
            // 初始化数据
            this.initDataListByMask(0);
            // 设置过滤激活
            this.filter.setActive(CollectionUtil.isNotEmpty(this.enabledFilters()));
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 过滤记录
     */
    @FXML
    private void filter() {
        try {
            PopupAdapter popup = PopupManager.parsePopup(MongoRecordFilterPopupController.class);
            popup.setProp("item", this.getItem());
            popup.setProp("filters", this.filters);
            popup.setProp("columns", this.columns);
            popup.showPopup(this.filter);
            popup.setSubmitHandler(filters -> {
                this.setFilters((List<MongoRecordFilter>) filters);
                this.reload();
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 下一页
     */
    @FXML
    private void nextPage() {
        this.initDataListByMask(this.pageData.nextPage());
    }

    /**
     * 上一页
     */
    @FXML
    private void prevPage() {
        this.initDataListByMask(this.pageData.prevPage());
    }

    /**
     * 尾页
     */
    @FXML
    private void lastPage() {
        this.initDataListByMask(this.pageData.lastPage());
    }

    /**
     * 首页
     */
    @FXML
    private void firstPage() {
        this.initDataListByMask(0);
    }

    /**
     * 跳页
     */
    @FXML
    private void pageJump(PageEvent.PageJumpEvent event) {
        this.initDataListByMask(event.getPage());
    }

    /**
     * 页码设置
     */
    @FXML
    private void pageSetting() {
        PopupAdapter popup = PopupManager.parsePopup(MongoPageSettingPopupController.class);
        popup.showPopup(this.pageBox.getSettingBtn());
        int limit = this.setting.getRecordPageLimit();
        popup.setSubmitHandler(o -> {
            if (o instanceof Integer l && l != limit) {
                this.firstPage();
            }
        });
    }

    /**
     * 删除记录
     */
    @FXML
    private void deleteRecord() {
        if (!MessageBox.confirm(I18nHelper.deleteDocument() + "?")) {
            return;
        }
        List<MongoRecord> records = new ArrayList<>(this.recordTable.getSelectedItems());
        StageManager.showMask(() -> this.deleteRecords(records));
    }

    /**
     * 删除记录
     *
     * @param records 记录
     */
    private void deleteRecords(List<MongoRecord> records) {
        try {
            boolean success = false;
            for (MongoRecord record : records) {
                success = this.deleteRecord(record);
                if (!success) {
                    break;
                }
            }
            // 操作成功
            if (success) {
                this.recordTable.removeItem(records);
                this.initCount(this.pageData.count() - records.size());
            } else {// 操作失败
                MessageBox.warnToast(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 删除记录
     *
     * @param record 记录
     * @return 结果
     */
    private boolean deleteRecord(MongoRecord record) {
        boolean success;
        // 如果是新增的数据，直接删除
        if (record.isCreated()) {
            success = true;
        } else {
            success = this.getItem().deleteRecord(record) == 1;
        }
        // 操作成功
        if (success) {
            this.recordTable.removeItem(record);
        } else {// 操作失败
            MessageBox.warnToast(I18nHelper.operationFail());
        }
        return success;
    }

    /**
     * 上传记录
     */
    @FXML
    private void uploadRecord() {
        File file = FileChooserHelper.choose(I18nHelper.pleaseSelectFile(), FXChooser.allExtensionFilter());
        if (file == null) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                ObjectId _id = this.getItem().uploadRecord(file);
                if (_id == null) {
                    MessageBox.warn(I18nHelper.uploadFileFailed());
                    return;
                }
                MongoRecord record = this.getItem().selectRecord(_id);
                if (record == null) {
                    MessageBox.warn(I18nHelper.uploadFileFailed());
                    return;
                }
                this.recordTable.addItem(record);
                this.recordTable.selectLast();
                this.initCount(this.recordTable.getItemSize());
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 下载记录
     */
    @FXML
    private void downloadRecord() {
        MongoRecord record = this.recordTable.getSelectedItem();
        if (record == null) {
            return;
        }
        String fileName = (String) record.getValue("filename");
        String extName = FileNameUtil.extName(fileName);
        FileExtensionFilter extensionFilter;
        if (StringUtil.isNotBlank(extName)) {
            extensionFilter = FXChooser.newExtensionFilter(extName);
        } else {
            extensionFilter = FXChooser.allExtensionFilter();
        }
        File file = FileChooserHelper.save(I18nHelper.pleaseSelectFile(), fileName, extensionFilter);
        if (file == null) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                Object _id = record._idValue();
                this.getItem().downloadRecord(_id, file.getPath());
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }

    public List<MongoRecordFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<MongoRecordFilter> filters) {
        this.filters = filters;
    }
}
