package cn.oyzh.easyshell.util.zk;

import cn.oyzh.easyshell.controller.zk.acl.ShellZKAddACLController;
import cn.oyzh.easyshell.controller.zk.acl.ShellZKUpdateACLController;
import cn.oyzh.easyshell.controller.zk.auth.ShellZKAddAuthController;
import cn.oyzh.easyshell.controller.zk.data.ShellZKDataExportController;
import cn.oyzh.easyshell.controller.zk.data.ShellZKDataImportController;
import cn.oyzh.easyshell.controller.zk.data.ShellZKDataTransportController;
import cn.oyzh.easyshell.controller.zk.history.ShellZKHistoryDataController;
import cn.oyzh.easyshell.controller.zk.node.ShellZKAddNodeController;
import cn.oyzh.easyshell.controller.zk.node.ShellZKAuthNodeController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.dto.zk.ShellZKACL;
import cn.oyzh.easyshell.trees.zk.node.ShellZKNodeTreeItem;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * shell页面工厂
 *
 * @author oyzh
 * @since 2025-11-21
 */
public class ShellZKViewFactory {

    /**
     * 认证节点
     *
     * @param nodeItem zk节点
     * @param client   zk客户端
     * @return 页面
     */
    public static StageAdapter authNode(ShellZKNodeTreeItem nodeItem, ShellZKClient client) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKAuthNodeController.class, StageManager.getPrimaryStage());
            adapter.setProp("zkItem", nodeItem);
            adapter.setProp("zkClient", client);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 添加zk子节点
     *
     * @param nodeItem zk节点
     * @param client   zk客户端
     * @return StageAdapter
     */
    public static StageAdapter addNode(ShellZKNodeTreeItem nodeItem, ShellZKClient client) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKAddNodeController.class, StageManager.getPrimaryStage());
            adapter.setProp("zkItem", nodeItem);
            adapter.setProp("zkClient", client);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 添加权限
     *
     * @param nodeItem zk节点
     * @param client   zk客户端
     */
    public static StageAdapter addACL(ShellZKNodeTreeItem nodeItem, ShellZKClient client) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKAddACLController.class, StageManager.getPrimaryStage());
            adapter.setProp("zkItem", nodeItem);
            adapter.setProp("zkClient", client);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 修改权限
     *
     * @param nodeItem zk节点
     * @param client   zk客户端
     * @param acl      权限
     */
    public static StageAdapter updateACL(ShellZKNodeTreeItem nodeItem, ShellZKClient client, ShellZKACL acl) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKUpdateACLController.class, StageManager.getPrimaryStage());
            adapter.setProp("acl", acl);
            adapter.setProp("zkItem", nodeItem);
            adapter.setProp("zkClient", client);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 导入数据
     *
     * @param connect zk连接
     */
    public static void importData(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKDataImportController.class, StageManager.getPrimaryStage());
            adapter.setProp("connect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 导出数据
     *
     * @param connect  zk连接
     * @param nodePath 节点路径
     */
    public static void exportData(ShellConnect connect, String nodePath) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKDataExportController.class);
            adapter.setProp("connect", connect);
            adapter.setProp("nodePath", nodePath);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 传输数据
     *
     * @param connect zk连接
     */
    public static void transportData(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKDataTransportController.class);
            adapter.setProp("sourceConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增zk认证
     *
     * @param connect 连接
     * @return 页面
     */
    public static StageAdapter authAdd(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKAddAuthController.class, StageManager.getPrimaryStage());
            adapter.setProp("connect", connect);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * zk数据历史
     *
     * @param client   客户端
     * @param nodePath 路径
     */
    public static void historyData(ShellZKClient client, String nodePath) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKHistoryDataController.class);
            adapter.setProp("client", client);
            adapter.setProp("nodePath", nodePath);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }
}
