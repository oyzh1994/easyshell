package cn.oyzh.easyshell.fx.mongo;

import cn.oyzh.common.thread.ThreadLocalUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.fx.file.ShellFileTableView;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.bucket.MongoBucketFile;
import cn.oyzh.easyshell.util.mongo.ShellMongoViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.i18n.I18nHelper;
import javafx.collections.ListChangeListener;
import javafx.scene.control.MenuItem;
import org.bson.types.ObjectId;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellMongoBucketFileTableView extends ShellFileTableView<ShellMongoClient, MongoBucketFile> {

    private ListChangeListener<ShellFileUploadTask> uploadTaskListener = change -> {
        change.next();
        if (change.wasRemoved()) {
            for (ShellFileUploadTask task : change.getRemoved()) {
                if (!task.isFailed() && !task.isCanceled()) {
                    ObjectId id = ThreadLocalUtil.getVal("id");
                    String dbName = ThreadLocalUtil.getVal("dbName");
                    String bucketName = ThreadLocalUtil.getVal("bucketName");
                    MongoBucketFile file = this.client.selectBucketRecord(dbName, bucketName, id);
                    this.addItem(file);
                }
            }
        }
    };

    private ListChangeListener<ShellFileDeleteTask> deleteTaskListener = change -> {
        change.next();
        if (change.wasRemoved()) {
            for (ShellFileDeleteTask task : change.getRemoved()) {
                if (!task.isFailed() && !task.isCanceled()) {
                    this.removeItem(task.getRemoteFile());
                }
            }
        }
    };

    @Override
    public void setClient(ShellMongoClient client) {
        super.setClient(client);
        this.client.uploadTasks().addListener(this.uploadTaskListener);
        this.client.deleteTasks().addListener(this.deleteTaskListener);
    }

    private String dbName;

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    private String bucketName;

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
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
        MenuItem uploadFile = MenuItemHelper.uploadFile(this::uploadFile);
        menuItems.add(uploadFile);
        List<MongoBucketFile> files = new ArrayList<>(this.getSelectedItems());
        if (CollectionUtil.isNotEmpty(files)) {
            if (files.size() == 1) {
                MongoBucketFile file = files.getFirst();
                MenuItem viewDocument = MenuItemHelper.view1File(() -> this.viewFile(file));
                menuItems.add(viewDocument);
                MenuItem editDocument = MenuItemHelper.editDocument(() -> this.editDocument(file));
                menuItems.add(editDocument);
            }
            MenuItem downloadFile = MenuItemHelper.downloadFile(() -> this.downloadFile(files));
            menuItems.add(downloadFile);
            MenuItem deleteFile = MenuItemHelper.deleteDocument(() -> this.deleteFile(files));
            menuItems.add(deleteFile);
        }
        return menuItems;
    }

    //    /**
    //     * 上传文件
    //     */
    //    public void uploadFile(Runnable callback) {
    //        File file = FileChooserHelper.choose(I18nHelper.pleaseSelectFile(), FXChooser.allExtensionFilter());
    //        if (file == null) {
    //            return;
    //        }
    //        StageManager.showMask(() -> {
    //            try {
    //                ObjectId _id = this.client.uploadBucketRecord(this.dbName, this.bucketName, file);
    //                if (_id == null) {
    //                    MessageBox.warn(I18nHelper.uploadFileFailed());
    //                    return;
    //                }
    //                MongoBucketFile record = this.client.selectBucketRecord(this.dbName, this.bucketName, _id);
    //                if (record == null) {
    //                    MessageBox.warn(I18nHelper.uploadFileFailed());
    //                    return;
    //                }
    //                this.addItem(record);
    //                this.selectLast();
    //            } catch (Exception ex) {
    //                MessageBox.exception(ex);
    //            } finally {
    //                if (callback != null) {
    //                    callback.run();
    //                }
    //            }
    //        });
    //    }

    //    /**
    //     * 查看文档
    //     */
    //    public void viewDocument(MongoBucketFile record) {
    //        try {
    //            if (record == null) {
    //                return;
    //            }
    //            String filename = record.getFileName();
    //            String extName = FileNameUtil.extName(filename);
    //            String type = ShellFileUtil.fileViewable(extName);
    //            ShellMongoViewFactory.fileView(record, this.client, type);
    //            this.refresh();
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            MessageBox.exception(ex);
    //        }
    //    }

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

    //    /**
    //     * 删除文档
    //     *
    //     * @param records  记录
    //     * @param callback 回调
    //     */
    //    public void deleteDocuments(List<MongoBucketFile> records, Runnable callback) {
    //        if (!MessageBox.confirm(I18nHelper.deleteDocument() + "?")) {
    //            return;
    //        }
    //        StageManager.showMask(() -> {
    //            try {
    //                boolean success = false;
    //                for (MongoBucketFile record : records) {
    //                    success = this.deleteDocument(record);
    //                    if (!success) {
    //                        break;
    //                    }
    //                }
    //                // 操作成功
    //                if (success) {
    //                    this.removeItem(records);
    //                } else {// 操作失败
    //                    MessageBox.warnToast(I18nHelper.operationFail());
    //                }
    //            } catch (Exception ex) {
    //                MessageBox.exception(ex);
    //            } finally {
    //                if (callback != null) {
    //                    callback.run();
    //                }
    //            }
    //        });
    //    }

    //    /**
    //     * 删除文档
    //     *
    //     * @param record 记录
    //     * @return 结果
    //     */
    //    public boolean deleteDocument(MongoBucketFile record) {
    //        boolean success = this.client.deleteBucketRecord(record.getDbName(), record.getBucketName(), record.getId()) == 1;
    //        // 操作成功
    //        if (success) {
    //            this.removeItem(record);
    //        } else {// 操作失败
    //            MessageBox.warnToast(I18nHelper.operationFail());
    //        }
    //        return success;
    //    }

    @Override
    public void uploadFile(List<File> files, Consumer<Boolean> callback) {
        super.uploadFile(files, callback);
    }

    //    @Override
    //    public void deleteFile(List<MongoBucketFile> files) {
    //        super.deleteFile(files);
    //        this.removeItem(files);
    //    }

    @Override
    public String getLocation() {
        return this.dbName + "@" + this.bucketName;
    }

    @Override
    public void setItem(Collection<?> items) {
        this.files = new ArrayList<>();
        for (Object item : items) {
            this.files.add((MongoBucketFile) item);
        }
        super.setItem(this.doFilter(files));
    }

    @Override
    public void refreshFile() {
        this.setItem(this.files);
    }

    @Override
    public void destroy() {
        if (this.client != null) {
            this.client.uploadTasks().removeListener(this.uploadTaskListener);
            this.client.deleteTasks().removeListener(this.deleteTaskListener);
            this.uploadTaskListener = null;
            this.deleteTaskListener = null;
        }
        super.destroy();
    }
}
