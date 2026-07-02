package cn.oyzh.easyshell.fx.mongo;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.bucket.MongoBucketFile;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.util.mongo.ShellMongoViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import org.bson.types.ObjectId;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellMongoBucketFileTableView extends FXTableView<MongoBucketFile> {


    private String dbName;

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    private String bucketName;

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    private ShellMongoClient client;

    public void setClient(ShellMongoClient client) {
        this.client = client;
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            List<? extends MenuItem> items = this.getMenuItems();
            if (CollectionUtil.isNotEmpty(items)) {
                this.showContextMenu(items, e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        MenuItem uploadFile = MenuItemHelper.uploadFile(() -> this.uploadFile(null));
        menuItems.add(uploadFile);
        List<MongoBucketFile> files = new ArrayList<>(this.getSelectedItems());
        if (CollectionUtil.isNotEmpty(files)) {
            if (files.size() == 1) {
                MongoBucketFile file = files.getFirst();
                MenuItem viewDocument = MenuItemHelper.view1Document(() -> this.viewDocument(file));
                menuItems.add(viewDocument);
                MenuItem editDocument = MenuItemHelper.editDocument(() -> this.editDocument(file));
                menuItems.add(editDocument);
            }
            MenuItem deleteFile = MenuItemHelper.deleteDocument(() -> this.deleteDocuments(files, null));
            menuItems.add(deleteFile);
        }
        return menuItems;
    }

    /**
     * 上传文件
     */
    public void uploadFile(Runnable callback) {
        File file = FileChooserHelper.choose(I18nHelper.pleaseSelectFile(), FXChooser.allExtensionFilter());
        if (file == null) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                ObjectId _id = this.client.uploadBucketRecord(this.dbName, this.bucketName, file);
                if (_id == null) {
                    MessageBox.warn(I18nHelper.uploadFileFailed());
                    return;
                }
                MongoBucketFile record = this.client.selectBucketRecord(this.dbName, this.bucketName, _id);
                if (record == null) {
                    MessageBox.warn(I18nHelper.uploadFileFailed());
                    return;
                }
                this.addItem(record);
                this.selectLast();
            } catch (Exception ex) {
                MessageBox.exception(ex);
            } finally {
                if (callback != null) {
                    callback.run();
                }
            }
        });
    }

    /**
     * 查看文档
     */
    public void viewDocument(MongoBucketFile record) {
        try {
            if (record == null) {
                return;
            }
            String filename = record.getFileName();
            String extName = FileNameUtil.extName(filename);
            String type = ShellFileUtil.fileViewable(extName);
            ShellMongoViewFactory.fileView(record, this.client, type);
            this.refresh();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 编辑文档
     */
    public void editDocument(MongoBucketFile record) {
        try {
            if (record == null) {
                return;
            }
            StageAdapter adapter = ShellMongoViewFactory.bucketDocumentUpdate(record);
            if (adapter == null) {
                return;
            }
            MongoBucketFile r = adapter.getProp("document");
            if (r == null) {
                return;
            }
            // 修改数据
            if (this.client.updateBucketRecord(r) != 1) {
                MessageBox.warn(I18nHelper.updateDocumentFail());
            } else {
                record.setFileName(r.getFileName());
                record.setMetadata(r.getMetadata());
                this.refresh();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 删除文档
     *
     * @param records  记录
     * @param callback 回调
     */
    public void deleteDocuments(List<MongoBucketFile> records, Runnable callback) {
        if (!MessageBox.confirm(I18nHelper.deleteDocument() + "?")) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                boolean success = false;
                for (MongoBucketFile record : records) {
                    success = this.deleteDocument(record);
                    if (!success) {
                        break;
                    }
                }
                // 操作成功
                if (success) {
                    this.removeItem(records);
                } else {// 操作失败
                    MessageBox.warnToast(I18nHelper.operationFail());
                }
            } catch (Exception ex) {
                MessageBox.exception(ex);
            } finally {
                if (callback != null) {
                    callback.run();
                }
            }
        });
    }

    /**
     * 删除文档
     *
     * @param record 记录
     * @return 结果
     */
    public boolean deleteDocument(MongoBucketFile record) {
        boolean success = this.client.deleteBucketRecord(record.getDbName(), record.getBucketName(), record.getId()) == 1;
        // 操作成功
        if (success) {
            this.removeItem(record);
        } else {// 操作失败
            MessageBox.warnToast(I18nHelper.operationFail());
        }
        return success;
    }

}
