package cn.oyzh.easyshell.fx.s3;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.s3.ShellS3Bucket;
import cn.oyzh.easyshell.s3.ShellS3Client;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellS3BucketTableView extends FXTableView<ShellS3Bucket> {

    private ShellS3Client client;

    public void setClient(ShellS3Client client) {
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
        MenuItem add = MenuItemHelper.addBucket("12", this::addBucket);
        menuItems.add(add);
        MenuItem refresh = MenuItemHelper.refreshBucket("12", this::loadBucket);
        menuItems.add(refresh);
        ShellS3Bucket bucket = this.getSelectedItem();
        if (bucket != null) {
            menuItems.add(MenuItemHelper.separator());
            MenuItem update = MenuItemHelper.updateBucket("12", () -> this.updateBucket(bucket));
            menuItems.add(update);
            MenuItem delete = MenuItemHelper.deleteBucket("12", () -> this.deleteBucket(bucket, false));
            menuItems.add(delete);
            MenuItem forceDelete = MenuItemHelper.forceDeleteBucket("12", () -> this.deleteBucket(bucket, true));
            menuItems.add(forceDelete);
        }
        return menuItems;
    }

    /**
     * 加载桶
     */
    public void loadBucket() {
        try {
            if (this.client != null) {
                List<ShellS3Bucket> buckets = this.client.listBuckets();
                this.setItem(buckets);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 删除桶
     *
     * @param bucket 桶
     * @param force  强制
     */
    public void deleteBucket(ShellS3Bucket bucket, boolean force) {
        if (bucket != null && MessageBox.confirm(I18nHelper.deleteBucket() + " " + bucket.getName() + "?")) {
            StageManager.showMask(() -> {
                try {
                    this.client.deleteBucket(bucket, force);
                    this.loadBucket();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.exception(ex);
                }
            });
        }
    }

    /**
     * 添加桶
     */
    public void addBucket() {
        try {
            StageAdapter adapter = ShellViewFactory.addS3Bucket(this.client);
            ShellS3Bucket bucket = adapter.getProp("bucket");
            if (bucket != null) {
                this.loadBucket();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 修改桶
     *
     * @param bucket 桶
     */
    public void updateBucket(ShellS3Bucket bucket) {
        try {
            StageAdapter adapter = ShellViewFactory.updateS3Bucket(this.client, bucket);
            if (adapter != null) {
                this.refresh();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
