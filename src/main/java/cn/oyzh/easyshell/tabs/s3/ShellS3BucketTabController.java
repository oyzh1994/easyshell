package cn.oyzh.easyshell.tabs.s3;

import cn.oyzh.easyshell.fx.s3.ShellS3BucketTableView;
import cn.oyzh.easyshell.s3.ShellS3ClientV2;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.fxml.FXML;

/**
 * s3桶标签页内容组件
 *
 * @author oyzh
 * @since 2025-06-16
 */
public class ShellS3BucketTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * 桶表格
     */
    @FXML
    private ShellS3BucketTableView bucketTable;

    @Override
    public ShellS3TabController parent() {
        return (ShellS3TabController) super.parent();
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellS3ClientV2 client() {
        return this.parent().client();
    }

    /**
     * 初始化
     */
    public void init() {
        this.bucketTable.setClient(this.client());
        this.refreshBucket();
    }

    @Override
    public void onTabInit(FXTab tab) {
        try {
            this.root.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    this.init();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 刷新桶
     */
    @FXML
    private void refreshBucket() {
        this.bucketTable.loadBucket();
    }

    /**
     * 删除桶
     */
    @FXML
    private void deleteBucket() {
        this.bucketTable.deleteBucket(this.bucketTable.getSelectedItem(), false);
    }

    /**
     * 新增桶
     */
    @FXML
    private void addBucket() {
        this.bucketTable.addBucket();
    }

}
