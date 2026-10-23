package cn.oyzh.easyshell.util.redis;

import cn.oyzh.easyshell.controller.redis.data.ShellRedisDataExportController;
import cn.oyzh.easyshell.controller.redis.data.ShellRedisDataImportController;
import cn.oyzh.easyshell.controller.redis.data.ShellRedisDataTransportController;
import cn.oyzh.easyshell.controller.redis.key.ShellRedisKeyAddController;
import cn.oyzh.easyshell.controller.redis.key.ShellRedisKeyBatchOperationController;
import cn.oyzh.easyshell.controller.redis.key.ShellRedisKeyCopyController;
import cn.oyzh.easyshell.controller.redis.key.ShellRedisKeyMoveController;
import cn.oyzh.easyshell.controller.redis.key.ShellRedisKeyTTLController;
import cn.oyzh.easyshell.controller.redis.row.ShellRedisHashFieldAddController;
import cn.oyzh.easyshell.controller.redis.row.ShellRedisHylogElementsAddController;
import cn.oyzh.easyshell.controller.redis.row.ShellRedisListElementAddController;
import cn.oyzh.easyshell.controller.redis.row.ShellRedisSetMemberAddController;
import cn.oyzh.easyshell.controller.redis.row.ShellRedisStreamMessageAddController;
import cn.oyzh.easyshell.controller.redis.row.ShellRedisZSetCoordinateAddController;
import cn.oyzh.easyshell.controller.redis.row.ShellRedisZSetMemberAddController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisHashKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisListKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisSetKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisStreamKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisStringKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisZSetKeyTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * shell页面工厂
 *
 * @author oyzh
 * @since 2025-11-21
 */
public class ShellRedisViewFactory {

    /**
     * 添加键
     *
     * @param client  客户端
     * @param dbIndex db库节点
     * @param type    键类型
     */
    public static StageAdapter addKey(ShellRedisClient client, Integer dbIndex, ShellRedisKeyType type) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisKeyAddController.class, StageManager.getPrimaryStage());
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
    public static void batchOperation(ShellRedisClient client, Integer dbIndex) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisKeyBatchOperationController.class, StageManager.getPrimaryStage());
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
    public static StageAdapter moveKey(ShellRedisKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisKeyMoveController.class, StageManager.getPrimaryStage());
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
    public static StageAdapter copyKey(ShellRedisKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisKeyCopyController.class, StageManager.getPrimaryStage());
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
    public static void ttlKey(ShellRedisKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisKeyTTLController.class, StageManager.getPrimaryStage());
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
    public static StageAdapter zSetCoordinateAdd(ShellRedisZSetKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisZSetCoordinateAddController.class, StageManager.getPrimaryStage());
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
    public static StageAdapter zSetMemberAdd(ShellRedisZSetKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisZSetMemberAddController.class, StageManager.getPrimaryStage());
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
    public static StageAdapter setMemberAdd(ShellRedisSetKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisSetMemberAddController.class, StageManager.getPrimaryStage());
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
    public static StageAdapter hashFieldAdd(ShellRedisHashKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisHashFieldAddController.class, StageManager.getPrimaryStage());
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
    public static StageAdapter listElementAdd(ShellRedisListKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisListElementAddController.class, StageManager.getPrimaryStage());
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
    public static StageAdapter streamMessageAdd(ShellRedisStreamKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisStreamMessageAddController.class, StageManager.getPrimaryStage());
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
    public static StageAdapter hylogElementsAdd(ShellRedisStringKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisHylogElementsAddController.class, StageManager.getPrimaryStage());
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
            Class<?> clazz = ShellRedisDataImportController.class;
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
            Class<?> clazz = ShellRedisDataExportController.class;
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
            Class<?> clazz = ShellRedisDataTransportController.class;
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
