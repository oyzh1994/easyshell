package cn.oyzh.easyshell.tabs.sqlite.home;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.sqlite.database.SQLiteDatabase;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteTabPane;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 * SQLite 主页标签页控制器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteHomeTabController extends RichTabController implements Initializable {

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
     * 页面大小
     */
    @FXML
    private FXLabel pageSize;

    /**
     * 编码
     */
    @FXML
    private FXLabel encoding;

    /**
     * 日志模式
     */
    @FXML
    private FXLabel journalMode;

    /**
     * 只读状态
     */
    @FXML
    private FXLabel readOnly;

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        tab.tabPaneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof ShellSqliteTabPane tabPane) {
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
    private void initInfo(ShellSqliteClient client) {
        try {
            if (client.isClosed()) {
                return;
            }
            SQLiteDatabase info = client.database();
            this.type.text(client.selectProduct());
            this.version.text(info.getVersion());
            this.filePath.text(info.getFilePath());
            this.fileSize.text(NumberUtil.formatSize(info.getFileSize(), 2));
            this.pageSize.text(info.getPageSize() + " / " + info.getPageCount() + " pages");
            this.encoding.text(info.getEncoding());
            this.journalMode.text(info.getJournalMode());
            this.readOnly.text(String.valueOf(info.isReadOnly()));
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }
}
