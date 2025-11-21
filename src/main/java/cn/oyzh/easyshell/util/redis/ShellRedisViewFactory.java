package cn.oyzh.easyshell.util.redis;

import cn.oyzh.easyshell.controller.redis.data.ShellRedisExportDataController;
import cn.oyzh.easyshell.controller.redis.data.ShellRedisImportDataController;
import cn.oyzh.easyshell.controller.redis.data.ShellRedisTransportDataController;
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
import cn.oyzh.easyshell.trees.redis.ShellRedisHashKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisListKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisSetKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisStreamKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisStringKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisZSetKeyTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * shell页面工厂
 *
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellRedisViewFactory {

    /**
     * 添加键
     *
     * @param client  客户端
     * @param dbIndex db库节点
     * @param type    键类型
     */
    public static StageAdapter addRedisKey(ShellRedisClient client, Integer dbIndex, ShellRedisKeyType type) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisKeyAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("type", type);
            adapter.setProp("client", client);
            adapter.setProp("dbIndex", dbIndex);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 批量操作
     *
     * @param client  客户端
     * @param dbIndex db索引
     */
    public static void redisBatchOperation(ShellRedisClient client, Integer dbIndex) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisKeyBatchOperationController.class, StageManager.getPrimaryStage());
            adapter.setProp("client", client);
            adapter.setProp("dbIndex", dbIndex);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 移动键
     *
     * @param treeItem 键节点
     * @return 页面
     */
    public static StageAdapter redisMoveKey(ShellRedisKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisKeyMoveController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 复制键
     *
     * @param treeItem 键节点
     * @return 页面
     */
    public static StageAdapter redisCopyKey(ShellRedisKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisKeyCopyController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 键ttl
     *
     * @param treeItem 键节点
     */
    public static void redisTtlKey(ShellRedisKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisKeyTTLController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 添加zset坐标
     *
     * @param treeItem 键节点
     */
    public static StageAdapter redisZSetCoordinateAdd(ShellRedisZSetKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisZSetCoordinateAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 添加zset成员
     *
     * @param treeItem 键节点
     */
    public static StageAdapter redisZSetMemberAdd(ShellRedisZSetKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisZSetMemberAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 添加set成员
     *
     * @param treeItem 键节点
     */
    public static StageAdapter redisSetMemberAdd(ShellRedisSetKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisSetMemberAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 添加hash字段
     *
     * @param treeItem 键节点
     */
    public static StageAdapter redisHashFieldAdd(ShellRedisHashKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisHashFieldAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 添加list元素
     *
     * @param treeItem 键节点
     */
    public static StageAdapter redisListElementAdd(ShellRedisListKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisListElementAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 添加stream消息
     *
     * @param treeItem 键节点
     */
    public static StageAdapter redisStreamMessageAdd(ShellRedisStreamKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisStreamMessageAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 添加hylog元素
     *
     * @param treeItem 键节点
     */
    public static StageAdapter redisHylogElementsAdd(ShellRedisStringKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisHylogElementsAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
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
     * @param connect redis连接
     */
    public static void redisImportData(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisImportDataController.class, StageManager.getPrimaryStage());
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
     * @param connect redis连接
     * @param dbIndex db索引
     */
    public static void redisExportData(ShellConnect connect, Integer dbIndex) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisExportDataController.class, StageManager.getPrimaryStage());
            adapter.setProp("connect", connect);
            adapter.setProp("dbIndex", dbIndex);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 传输数据
     *
     * @param connect redis连接
     * @param dbIndex db索引
     */
    public static void redisTransportData(ShellConnect connect, Integer dbIndex) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellRedisTransportDataController.class, StageManager.getPrimaryStage());
            adapter.setProp("sourceConnect", connect);
            adapter.setProp("dbIndex", dbIndex);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
