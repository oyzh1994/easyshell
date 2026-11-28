package cn.oyzh.easyshell.tabs.h2.home;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.h2.database.H2Database;
import cn.oyzh.easyshell.tabs.h2.ShellH2TabPane;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 * H2 主页标签页控制器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2HomeTabController extends RichTabController implements Initializable {

    /**
     * 类型
     */
    @FXML
    private FXLabel type;

    /**
     * 版本
     */
    @FXML
    private FXLabel version;

    /**
     * 数据库名称
     */
    @FXML
    private FXLabel name;

    /**
     * 用户名
     */
    @FXML
    private FXLabel user;

    /**
     * 默认 schema
     */
    @FXML
    private FXLabel schema;

    /**
     * 事务隔离级别
     */
    @FXML
    private FXLabel isolationLevel;

    /**
     * 文件路径
     */
    @FXML
    private FXLabel filePath;

    /**
     * 文件大小
     */
    @FXML
    private FXLabel fileSize;

    /**
     * 只读状态
     */
    @FXML
    private FXLabel readOnly;

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        tab.tabPaneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof ShellH2TabPane tabPane) {
                if (tabPane.getClient() != null) {
                    this.initInfo(tabPane.getClient());
                } else {
                    tabPane.clientProperty().addListener((observable1, oldValue1, newValue1) -> {
                        if (newValue1 != null) {
                            this.initInfo(newValue1);
                        }
                    });
                }
            }
        });
        super.flushTab();
    }

    /**
     * 初始化信息
     *
     * @param client 客户端
     */
    private void initInfo(ShellH2Client client) {
        try {
            if (client.isClosed()) {
                return;
            }
            H2Database info = client.database();
            this.type.text(client.selectProduct());
            this.version.text(info.getVersion());
            this.name.text(info.getName());
            this.user.text(info.getUser());
            this.schema.text(info.getSchema());
            this.isolationLevel.text(info.getIsolationLevel());
            this.filePath.text(info.getFilePath());
            this.fileSize.text(NumberUtil.formatSize(info.getFileSize(), 2));
            this.readOnly.text(String.valueOf(info.isReadOnly()));
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }
}
