package cn.oyzh.easyshell.tabs.redis.terminal;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.terminal.redis2.RedisTerminalPane;
import cn.oyzh.easyshell.trees.redis.database.ShellRedisDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * redis命令行tab内容组件
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisTerminalTabController extends RichTabController {

    /**
     * redis命令行文本域
     */
    @FXML
    private RedisTerminalPane terminal;

    /**
     * 数据库节点
     */
    private ShellRedisDatabaseTreeItem dbItem;

    /**
     * 初始化
     *
     * @param dbItem 数据库节点
     */
    public void init(ShellRedisDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
        // 如果tab已被选中，则直接初始化终端
        if (this.getTab() != null && this.getTab().isSelected()) {
            this.initTerminal();
        }
    }

    /**
     * 初始化终端
     */
    private void initTerminal() {
        if (this.dbItem != null && this.terminal.getClient() == null) {
            this.terminal.init(this.dbItem.client(), this.dbItem.dbIndex());
        }
    }

    /**
     * 获取数据库节点
     *
     * @return 数据库节点
     */
    public ShellRedisDatabaseTreeItem dbItem() {
        return dbItem;
    }

    /**
     * 获取数据库索引
     *
     * @return 数据库索引
     */
    public Integer dbIndex() {
        return this.terminal.getDbIndex();
    }

    /**
     * redis信息
     *
     * @return 当前redis信息
     */
    protected ShellConnect shellConnect() {
        return this.terminal.shellConnect();
    }

    /**
     * 获取redis客户端
     *
     * @return redis客户端
     */
    public ShellRedisClient getClient() {
        return this.terminal.getClient();
    }

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        tab.selectedProperty().subscribe((oldValue, newValue) -> {
            if (newValue) {
                this.initTerminal();
            }
        });
    }

    @Override
    public void destroy() {
        this.terminal.destroy();
        super.destroy();
    }
}
