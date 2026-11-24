package cn.oyzh.easyshell.query.mongo;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.script.MongoScriptUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static cn.oyzh.easyshell.util.ShellClientUtil.isAvailable;

/**
 * mongo查询工具类
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoQueryUtil {

    /**
     * 关键字
     */
    private static final Set<String> DB_KEYWORDS = new HashSet<>();

    /**
     * 函数
     */
    private static final Set<String> DB_FUNCTIONS = new HashSet<>();

    /**
     * 集合
     */
    private static final List<String> DB_COLLECTIONS = new CopyOnWriteArrayList<>();

    static {
        DB_KEYWORDS.add("db");
        DB_FUNCTIONS.addAll(MongoScriptUtil.databaseFuncions());
        DB_FUNCTIONS.addAll(MongoScriptUtil.collectionFuncions());
    }

    /**
     * 获取关键字集合
     *
     * @return 关键字集合
     */
    public static Set<String> getKeywords() {
        return DB_KEYWORDS;
    }

    /**
     * 获取函数集合
     *
     * @return 函数集合
     */
    public static Set<String> getFunctions() {
        return DB_FUNCTIONS;
    }

    /**
     * 获取集合列表
     *
     * @return 集合列表
     */
    public static List<String> getCollections() {
        return DB_COLLECTIONS;
    }

    /**
     * 获取数据库关键字集合
     *
     * @return 结果
     */
    public static Set<String> getDbKeywords() {
        Set<String> set = new HashSet<>(getKeywords());
//        set.addAll(DB_FUNCTIONS);
//        set.addAll(DB_COLLECTIONS);
        return set;
    }

    /**
     * 更新索引
     *
     * @param client 客户端
     * @param dbName 数据库名称
     */
    public static void updateIndex(ShellMongoClient client, String dbName) {
        if (!isAvailable(client) || dbName == null) {
            return;
        }
        ThreadUtil.start(() -> refreshCollections(client, dbName));
    }

    /**
     * 刷新集合索引
     *
     * @param client 客户端
     * @param dbName 数据库名称
     */
    public static void refreshCollections(ShellMongoClient client, String dbName) {
        if (!isAvailable(client) || dbName == null) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<String> collections = client.listCollectionNames(dbName);
                if (!isAvailable(client)) {
                    return;
                }
                DB_COLLECTIONS.clear();
                DB_COLLECTIONS.addAll(collections);
            } catch (Exception ex) {
                if (isAvailable(client)) {
                    JulLog.warn("Refresh mongo collection query index failed", ex);
                } else {
                    JulLog.debug("Refresh mongo collection query index failed");
                }
            }
        });
    }
}
