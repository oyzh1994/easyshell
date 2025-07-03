package cn.oyzh.easyshell.tabs.s3;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.s3.ShellS3Client;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.util.List;

/**
 * s3组件
 *
 * @author oyzh
 * @since 2025/06/14
 */
public class ShellS3TabController extends ParentTabController {

    /**
     * 文件
     */
    @FXML
    private ShellS3FileTabController fileTabController;

    /**
     * 桶
     */
    @FXML
    private ShellS3BucketTabController bucketTabController;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * s3客户端
     */
    private ShellS3Client client;

    public ShellS3Client client() {
        return this.client;
    }

    public ShellConnect shellConnect() {
        return this.client.getShellConnect();
    }

    /**
     * 初始化
     */
    public void init(ShellConnect shellConnect) {
        this.client = new ShellS3Client(shellConnect);
        StageManager.showMask(() -> {
            try {
                if (!this.client.isConnected()) {
                    this.client.start();
                }
                if (!this.client.isConnected()) {
                    MessageBox.warn(I18nHelper.connectFail());
                    this.closeTab();
                    return;
                }
                // 收起左侧
                if (this.setting.isHiddenLeftAfterConnected()) {
                    ShellEventUtil.layout1();
                }
                this.fileTabController.init();
                this.bucketTabController.init();
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.client.close();
        // 展开左侧
        if (this.setting.isHiddenLeftAfterConnected()) {
            ShellEventUtil.layout2();
        }
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(this.fileTabController, this.bucketTabController);
    }
}
