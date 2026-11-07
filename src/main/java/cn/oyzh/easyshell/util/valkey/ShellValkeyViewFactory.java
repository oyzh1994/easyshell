package cn.oyzh.easyshell.util.valkey;

import cn.oyzh.easyshell.controller.valkey.data.ShellValkeyDataExportController;
import cn.oyzh.easyshell.controller.valkey.data.ShellValkeyDataImportController;
import cn.oyzh.easyshell.controller.valkey.data.ShellValkeyDataTransportController;
import cn.oyzh.easyshell.controller.valkey.key.ShellValkeyKeyAddController;
import cn.oyzh.easyshell.controller.valkey.key.ShellValkeyKeyBatchOperationController;
import cn.oyzh.easyshell.controller.valkey.key.ShellValkeyKeyCopyController;
import cn.oyzh.easyshell.controller.valkey.key.ShellValkeyKeyMoveController;
import cn.oyzh.easyshell.controller.valkey.key.ShellValkeyKeyTTLController;
import cn.oyzh.easyshell.controller.valkey.row.ShellValkeyHashFieldAddController;
import cn.oyzh.easyshell.controller.valkey.row.ShellValkeyHylogElementsAddController;
import cn.oyzh.easyshell.controller.valkey.row.ShellValkeyListElementAddController;
import cn.oyzh.easyshell.controller.valkey.row.ShellValkeySetMemberAddController;
import cn.oyzh.easyshell.controller.valkey.row.ShellValkeyStreamMessageAddController;
import cn.oyzh.easyshell.controller.valkey.row.ShellValkeyZSetCoordinateAddController;
import cn.oyzh.easyshell.controller.valkey.row.ShellValkeyZSetMemberAddController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.easyshell.valkey.ShellValkeyKeyType;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyHashKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyListKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeySetKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyStreamKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyStringKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyZSetKeyTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * shell页面工厂
 *
 * @author oyzh
 * @since 2025-11-21
 */
public class ShellValkeyViewFactory {

    /**
     * 添加键
     *
     * @param client  客户端
     * @param dbIndex db库节点
     * @param type    键类型
     */
    public static StageAdapter addKey(ShellValkeyClient client, Integer dbIndex, ShellValkeyKeyType type) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellValkeyKeyAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("type", type);
            adapter.setProp("client", client);
            adapter.setProp("dbIndex", dbIndex);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 批量操作
     *
     * @param client  客户端
     * @param dbIndex db索引
     */
    public static void batchOperation(ShellValkeyClient client, Integer dbIndex) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellValkeyKeyBatchOperationController.class, StageManager.getPrimaryStage());
            adapter.setProp("client", client);
            adapter.setProp("dbIndex", dbIndex);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 移动键
     *
     * @param treeItem 键节点
     * @return 页面
     */
    public static StageAdapter moveKey(ShellValkeyKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellValkeyKeyMoveController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 复制键
     *
     * @param treeItem 键节点
     * @return 页面
     */
    public static StageAdapter copyKey(ShellValkeyKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellValkeyKeyCopyController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 键ttl
     *
     * @param treeItem 键节点
     */
    public static void ttlKey(ShellValkeyKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellValkeyKeyTTLController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 添加zset坐标
     *
     * @param treeItem 键节点
     */
    public static StageAdapter zSetCoordinateAdd(ShellValkeyZSetKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellValkeyZSetCoordinateAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 添加zset成员
     *
     * @param treeItem 键节点
     */
    public static StageAdapter zSetMemberAdd(ShellValkeyZSetKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellValkeyZSetMemberAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 添加set成员
     *
     * @param treeItem 键节点
     */
    public static StageAdapter setMemberAdd(ShellValkeySetKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellValkeySetMemberAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 添加hash字段
     *
     * @param treeItem 键节点
     */
    public static StageAdapter hashFieldAdd(ShellValkeyHashKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellValkeyHashFieldAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 添加list元素
     *
     * @param treeItem 键节点
     */
    public static StageAdapter listElementAdd(ShellValkeyListKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellValkeyListElementAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 添加stream消息
     *
     * @param treeItem 键节点
     */
    public static StageAdapter streamMessageAdd(ShellValkeyStreamKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellValkeyStreamMessageAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 添加hylog元素
     *
     * @param treeItem 键节点
     */
    public static StageAdapter hylogElementsAdd(ShellValkeyStringKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellValkeyHylogElementsAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
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
     * @param connect redis连接
     * @param dbIndex db索引
     */
    public static void importData(ShellConnect connect, Integer dbIndex) {
        try {
            Class<?> clazz = ShellValkeyDataImportController.class;
            if (StageManager.frontStage(clazz, "connect", connect)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("connect", connect);
            adapter.setProp("dbIndex", dbIndex);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 导出数据
     *
     * @param connect redis连接
     * @param dbIndex db索引
     */
    public static void exportData(ShellConnect connect, Integer dbIndex) {
        try {
            Class<?> clazz = ShellValkeyDataExportController.class;
            if (StageManager.frontStage(clazz, "connect", connect)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("connect", connect);
            adapter.setProp("dbIndex", dbIndex);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 传输数据
     *
     * @param connect redis连接
     * @param dbIndex db索引
     */
    public static void transportData(ShellConnect connect, Integer dbIndex) {
        try {
            Class<?> clazz = ShellValkeyDataTransportController.class;
            if (StageManager.frontStage(clazz, "sourceConnect", connect)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("sourceConnect", connect);
            adapter.setProp("dbIndex", dbIndex);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }
}
