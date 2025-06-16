package cn.oyzh.easyshell.fx.s3;

import cn.oyzh.easyshell.s3.ShellS3Bucket;
import cn.oyzh.easyshell.s3.ShellS3Client;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.window.StageAdapter;
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
    public List<? extends MenuItem> getMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>(super.getMenuItems());
        menuItems.add(MenuItemHelper.separator());
        // 上传文件
        return menuItems;
    }

    public void deleteBucket(ShellS3Bucket selectedItem) {
    }

    public void loadBucket() {
        if (this.client != null) {
            List<ShellS3Bucket> buckets = this.client.listBuckets();
            this.setItem(buckets);
        }
    }

    public void addBucket() {
        StageAdapter adapter = ShellViewFactory.addS3Bucket(this.client);
        ShellS3Bucket bucket = adapter.getProp("bucket");
        if (bucket != null) {
            this.loadBucket();
        }
    }
}
