package cn.oyzh.easyshell.tabs.mongo.terminal;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.terminal.mongo.MongoTerminalPane;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.event.Event;
import javafx.fxml.FXML;

/**
 * mongodb命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ShellMongoTerminalTabController extends RichTabController {

    /**
     * mongodb命令行文本域
     */
    @FXML
    private MongoTerminalPane terminal;

    private String dbName;

    /**
     * 初始化
     *
     * @param client mongodb客户端
     */
    public void init(ShellMongoClient client, String dbName) {
        this.terminal.init(client,dbName);
        this.dbName = dbName;
    }

    public String getDbName() {
        return dbName;
    }

    /**
     * mongodb信息
     *
     * @return 当前mongodb信息
     */
    protected ShellConnect shellConnect() {
        return this.terminal.shellConnect();
    }

    public ShellMongoClient client() {
        return this.terminal.getClient();
    }

    @Override
    public void onTabClosed(Event event) {
        if (this.terminal.isTemporary()) {
            this.client().close();
        }
        super.onTabClosed(event);
    }
}
