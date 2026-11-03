package cn.oyzh.easyshell.tabs.sqlite.home;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.sqlite.database.SQLiteDatabase;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.fxml.FXML;

/**
 * SQLite 主页控制器。
 *
 * @since 2026-11-02
 */
public class ShellSqliteHomeTabController extends RichTabController {

    @FXML
    private FXLabel product;

    @FXML
    private FXLabel version;

    @FXML
    private FXLabel filePath;

    @FXML
    private FXLabel fileSize;

    @FXML
    private FXLabel pageSize;

    @FXML
    private FXLabel encoding;

    @FXML
    private FXLabel journalMode;

    @FXML
    private FXLabel readOnly;

    public void init(ShellSqliteClient client) {
        try {
            SQLiteDatabase info = client.database();
            this.product.text("SQLite");
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
