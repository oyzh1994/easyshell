package cn.oyzh.easyshell.util.zk;

import cn.oyzh.easyshell.controller.zk.acl.ShellZKAddACLController;
import cn.oyzh.easyshell.controller.zk.acl.ShellZKUpdateACLController;
import cn.oyzh.easyshell.controller.zk.auth.ShellZKAddAuthController;
import cn.oyzh.easyshell.controller.zk.data.ShellZKExportDataController;
import cn.oyzh.easyshell.controller.zk.data.ShellZKImportDataController;
import cn.oyzh.easyshell.controller.zk.data.ShellZKTransportDataController;
import cn.oyzh.easyshell.controller.zk.history.ShellZKHistoryDataController;
import cn.oyzh.easyshell.controller.zk.history.ShellZKHistoryViewController;
import cn.oyzh.easyshell.controller.zk.node.ShellZKAddNodeController;
import cn.oyzh.easyshell.controller.zk.node.ShellZKAuthNodeController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.dto.zk.ShellZKACL;
import cn.oyzh.easyshell.trees.zk.ShellZKNodeTreeItem;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.stage.Window;

/**
 * shell页面工厂
 *
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellZKViewFactory {

    /**
     * 认证节点
     *
     * @param nodeItem zk节点
     * @param client   zk客户端
     * @return 页面
     */
    public static StageAdapter zkAuthNode(ShellZKNodeTreeItem nodeItem, ShellZKClient client) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKAuthNodeController.class, StageManager.getPrimaryStage());
            adapter.setProp("zkItem", nodeItem);
            adapter.setProp("zkClient", client);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
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
    public static StageAdapter zkAddNode(ShellZKNodeTreeItem nodeItem, ShellZKClient client) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKAddNodeController.class, StageManager.getPrimaryStage());
            adapter.setProp("zkItem", nodeItem);
            adapter.setProp("zkClient", client);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 添加权限
     *
     * @param nodeItem zk节点
     * @param client   zk客户端
     */
    public static StageAdapter zkAddACL(ShellZKNodeTreeItem nodeItem, ShellZKClient client) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKAddACLController.class, StageManager.getPrimaryStage());
            adapter.setProp("zkItem", nodeItem);
            adapter.setProp("zkClient", client);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
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
    public static StageAdapter zkUpdateACL(ShellZKNodeTreeItem nodeItem, ShellZKClient client, ShellZKACL acl) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKUpdateACLController.class, StageManager.getPrimaryStage());
            adapter.setProp("acl", acl);
            adapter.setProp("zkItem", nodeItem);
            adapter.setProp("zkClient", client);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 导入数据
     *
     * @param connect zk连接
     */
    public static void zkImportData(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKImportDataController.class, StageManager.getPrimaryStage());
            adapter.setProp("connect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 导出数据
     *
     * @param connect  zk连接
     * @param nodePath 节点路径
     */
    public static void zkExportData(ShellConnect connect, String nodePath) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKExportDataController.class, StageManager.getPrimaryStage());
            adapter.setProp("connect", connect);
            adapter.setProp("nodePath", nodePath);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 传输数据
     *
     * @param connect zk连接
     */
    public static void zkTransportData(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKTransportDataController.class, StageManager.getPrimaryStage());
            adapter.setProp("sourceConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 新增zk认证
     *
     * @param connect 连接
     * @return 页面
     */
    public static StageAdapter zkAuthAdd(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKAddAuthController.class, StageManager.getFrontWindow());
            adapter.setProp("connect", connect);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * zk数据历史
     *
     * @param client   客户端
     * @param nodePath 路径
     */
    public static void zkHistoryData(ShellZKClient client, String nodePath) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKHistoryDataController.class, StageManager.getFrontWindow());
            adapter.setProp("client", client);
            adapter.setProp("nodePath", nodePath);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * zk查看历史
     *
     * @param data 数据
     * @param owner 父窗口
     */
    public static void zkHistoryView(byte[] data, Window owner) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellZKHistoryViewController.class, owner);
            adapter.setProp("data", data);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
