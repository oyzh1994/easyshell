package cn.oyzh.easyshell.tabs.s3;

import cn.oyzh.easyshell.fx.s3.ShellS3BucketTableView;
import cn.oyzh.easyshell.s3.ShellS3Client;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.fxml.FXML;

/**
 * ftp组件
 *
 * @author oyzh
 * @since 2025/04/25
 */
public class ShellS3BucketTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXVBox root;

    /**
     * 桶表格
     */
    @FXML
    private ShellS3BucketTableView bucketTable;

    @Override
    public ShellS3TabController parent() {
        return (ShellS3TabController) super.parent();
    }

    public ShellS3Client client() {
        return this.parent().client();
    }

    /**
     * 初始化
     */
    public void init() {
        this.bucketTable.setClient(this.client());
    }

    @Override
    public void onTabInit(RichTab tab) {
        try {
            this.bucketTable.setClient(this.client());
            tab.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    this.refreshBucket();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void refreshBucket() {
        try {
            this.bucketTable.loadBucket();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void deleteBucket() {
        try {
            this.bucketTable.deleteBucket(this.bucketTable.getSelectedItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void addBucket() {
        this.bucketTable.addBucket();
    }

}
