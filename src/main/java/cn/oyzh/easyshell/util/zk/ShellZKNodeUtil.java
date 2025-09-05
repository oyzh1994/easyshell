package cn.oyzh.easyshell.util.zk;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.zk.ZKFilter;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.easyshell.zk.ZKNode;
import cn.oyzh.i18n.I18nHelper;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * zk节点工具类
 *
 * @author oyzh
 * @since 2020/11/10
 */

public class ShellZKNodeUtil {

    /**
     * 权限属性
     */
    public static final String ACL_PROPERTIES = "a";

    /**
     * 数据属性
     */
    public static final String DATA_PROPERTIES = "d";

    /**
     * 状态属性
     */
    public static final String STAT_PROPERTIES = "s";

    /**
     * 全部属性
     */
    public static final String FULL_PROPERTIES = ACL_PROPERTIES + DATA_PROPERTIES + STAT_PROPERTIES;

    /**
     * 获取zk节点
     *
     * @param path   路径
     * @param client zk操作器
     * @return zk节点
     */
    public static ZKNode getNode( ZKClient client,  String path) throws Exception {
        return getNode(client, path, FULL_PROPERTIES);
    }

    /**
     * 获取zk节点
     *
     * @param path   路径
     * @param client zk操作器
     * @return zk节点
     */
    public static ZKNode getNode( ZKClient client,  String path,  String properties) throws Exception {
        if (!path.contains("/")) {
            throw new ShellException("path:[" + path + "]" + I18nHelper.invalid());
        }
        long start = System.currentTimeMillis();
        // zk节点
        ZKNode node = new ZKNode();
        // 设置节点路径
        node.nodePath(path);
//        // 异常
//        AtomicReference<Exception> exceptionReference = new AtomicReference<>();
//        // 设置zk状态
//        if (properties.contains("s")) {
//            try {
//                node.stat(client.checkExists(path));
//            } catch (KeeperException.NoAuthException ignored) {
//            } catch (Exception ex) {
//                exceptionReference.set(ex);
//            }
//        }
//        // 设置zk访问控制
//        if (properties.contains("a")) {
//            try {
//                node.acl(client.getACL(path));
//            } catch (KeeperException.NoAuthException ignored) {
//            } catch (Exception ex) {
//                exceptionReference.set(ex);
//            }
//        }
//        // 设置zk数据
//        if (properties.contains("d")) {
//            try {
//                node.setNodeData(client.getData(path));
//            } catch (KeeperException.NoAuthException ignored) {
//            } catch (Exception ex) {
//                exceptionReference.set(ex);
//            }
//        }
//        // 抛出异常
//        if (exceptionReference.get() != null) {
//            throw exceptionReference.get();
//        }
        // 执行任务
        Exception exception = ShellZKNodeTask.of(node, client, path, properties);
        // 抛出异常
        if (exception != null) {
            throw exception;
        }
        // 设置加载时间
        long end = System.currentTimeMillis();
        long loadTime = end - start;
        node.loadTime((short) loadTime);
        // 返回节点
        return node;
    }

    /**
     * 刷新节点数据
     *
     * @param client zk客户端
     * @param node   zk节点
     */
    public static void refreshData( ZKClient client,  ZKNode node) throws Exception {
        long start = System.currentTimeMillis();
        node.setNodeData(client.getData(node.nodePath()));
        long end = System.currentTimeMillis();
        long loadTime = end - start;
        node.loadTime((short) loadTime);
    }

    /**
     * 刷新节点权限
     *
     * @param client zk客户端
     * @param node   zk节点
     */
    public static void refreshAcl( ZKClient client,  ZKNode node) throws Exception {
        node.acl(client.getACL(node.nodePath()));
    }

    /**
     * 刷新节点配额
     *
     * @param client zk客户端
     * @param node   zk节点
     */
    public static void refreshQuota( ZKClient client,  ZKNode node) throws Exception {
        node.quota(client.listQuota(node.nodePath()));
    }

    /**
     * 刷新节点状态
     *
     * @param client zk客户端
     * @param node   zk节点
     */
    public static void refreshStat( ZKClient client,  ZKNode node) throws Exception {
        node.stat(client.checkExists(node.nodePath()));
    }

    /**
     * 刷新节点
     *
     * @param client zk客户端
     * @param node   zk节点
     */
    public static void refreshNode( ZKClient client,  ZKNode node) throws Exception {
        ZKNode n = ShellZKNodeUtil.getNode(client, node.nodePath());
        node.copy(n);
    }

    /**
     * 是否被过滤
     *
     * @param nodePath 节点路径
     * @param filters  zk过滤配置列表
     * @return 结果
     */
    public static boolean isFiltered(String nodePath, List<ZKFilter> filters) {
        if (CollectionUtil.isEmpty(filters) || nodePath == null) {
            return false;
        }
        // 匹配结果
        for (ZKFilter filter : filters) {
            // 未启用，不处理
            if (!filter.isEnable()) {
                continue;
            }
            // 模糊匹配
            if (filter.isPartMatch() && nodePath.contains(filter.getKw())) {
                return true;
            }
            // 完全匹配
            if (nodePath.equalsIgnoreCase(filter.getKw())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取父路径
     *
     * @param path 当前路径
     * @return 当前路径的父路径
     */
    public static String getParentPath(String path) {
        if (StringUtil.isBlank(path) || !path.contains("/")) {
            return "";
        }
        if (path.equals("/")) {
            return "/";
        }
        int index = path.lastIndexOf("/");
        if (index == 0) {
            return "/";
        }
        return path.substring(0, index);
    }

    /**
     * 连接路径
     *
     * @param parentPath 父路径
     * @param childPath  子路径
     * @return 连接后的路径
     */
    public static String concatPath( String parentPath,  String childPath) {
        if (parentPath.endsWith("/") && childPath.startsWith("/")) {
            return parentPath + childPath.substring(1);
        }
        if (parentPath.endsWith("/") || childPath.startsWith("/")) {
            return parentPath + childPath;
        }
        return parentPath + "/" + childPath;
    }

    /**
     * 获取名称
     *
     * @param path 路径
     * @return 名称
     */
    public static String getName(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        return "/".equals(path) ? "/" : ArrayUtil.last(path.split("/"));
    }

//    /**
//     * 获取子节点
//     *
//     * @param parentPath 父节点路径
//     * @return 子节点列表
//     */
//    public static List<ZKNode> getChildNode( ZKClient client,  String parentPath) throws Exception {
//        return getChildNode(client, parentPath, FULL_PROPERTIES, null);
//    }

//    /**
//     * 获取子节点
//     *
//     * @param client     zk客户端
//     * @param parentPath 父节点路径
//     * @param properties 查询属性
//     * @param filter     过滤器
//     * @return 子节点列表
//     */
//    public static List<ZKNode> getChildNode( ZKClient client,  String parentPath,  String properties, Predicate<String> filter) throws Exception {
//        List<ZKNode> list = new ArrayList<>();
//        // 获取子节点
//        List<String> children = client.getChildren(parentPath);
//        // 为空，直接返回
//        if (CollectionUtil.isEmpty(children)) {
//            return Collections.emptyList();
//        }
//        // 处理器核心数量
//        int pCount = RuntimeUtil.processorCount();
//        // 性能较差机器上同步执行
//        if (pCount < 4) {
//            // 获取节点数据
//            for (String sub : children) {
//                // 对节点路径做处理
//                String path = ShellZKNodeUtil.concatPath(parentPath, sub);
//                // 获取节点
//                if (filter == null || filter.test(path)) {
//                    list.add(getNode(client, path, properties));
//                }
//            }
////            children.clear();
//        } else {// 性能较好机器上异步执行
//            // 任务列表
//            List<Callable<ZKNode>> tasks = new ArrayList<>(children.size());
//            // 获取节点数据
//            for (String sub : children) {
//                // 对节点路径做处理
//                String path = ShellZKNodeUtil.concatPath(parentPath, sub);
//                // 获取节点
//                if (filter == null || filter.test(path)) {
//                    // 添加到任务列表
//                    tasks.add(() -> getNode(client, path, properties));
//                }
//                // 分批获取，避免机器爆炸
//                if (tasks.size() >= pCount) {
//                    list.addAll(ThreadUtil.invoke(tasks));
//                    tasks.clear();
//                }
//            }
//            // 处理尾部数据
//            if (!tasks.isEmpty()) {
//                list.addAll(ThreadUtil.invoke(tasks));
//            }
////            tasks.clear();
////            children.clear();
//        }
//        return list;
//    }

    /**
     * 获取子节点
     *
     * @param client        zk客户端
     * @param parentPath    父节点路径
     * @param existingNodes 查询属性
     * @param limit         过滤器
     * @return 子节点列表
     */
    public static List<ZKNode> getChildNode( ZKClient client,  String parentPath, List<String> existingNodes, int limit) throws Exception {
        // 获取子节点
        List<String> children = null;
        try {
            // 获取子节点
            children = client.getChildren(parentPath);
        } catch (Exception ignore) {

        }
        // 为空，直接返回
        if (CollectionUtil.isEmpty(children)) {
            return Collections.emptyList();
        }
        // 数据列表
        List<ZKNode> list = new CopyOnWriteArrayList<>();
        // 处理器核心数量
//        int pCount = RuntimeUtil.processorCount();
//        // 性能较差机器上同步执行
//        if (pCount < 4) {
//            // 获取节点数据
//            for (String sub : children) {
//                // 对节点路径做处理
//                String path = ShellZKNodeUtil.concatPath(parentPath, sub);
//                // 判断是否存在
//                if (existingNodes.contains(path)) {
//                    continue;
//                }
//                // 获取节点
//                try {
//                    list.add(getNode(client, path, FULL_PROPERTIES));
//                } catch (Exception ignore) {
//                }
//                if (limit > 0 && list.size() > limit) {
//                    break;
//                }
//            }
//        } else {// 性能较好机器上异步执行
        // 任务列表
//        List<Callable<ZKNode>> tasks = new ArrayList<>(children.size());
//        List<Callable<ZKNode>> tasks = new ArrayList<>(children.size());
//        DownLatch latch = new DownLatch(tasks.size());
//        // 获取节点数据
//        for (String sub : children) {
//            // 对节点路径做处理
//            String path = ShellZKNodeUtil.concatPath(parentPath, sub);
//            // 判断是否存在
//            if (existingNodes.contains(path)) {
//                continue;
//            }
//            // 添加到任务列表
//            tasks.add(() -> {
//                try {
//                    return getNode(client, path, FULL_PROPERTIES);
//                } catch (Exception ignore) {
//                }finally {
//                    latch.countDown();
//                }
//                return null;
//            });
//            if (limit > 0 && list.size() > limit) {
//                break;
//            }
//            // 如果任务列表达到核心数2倍，则开始执行此部分任务，并清空任务列表
//            if (tasks.size() >= pCount * 2) {
//                list.addAll(ThreadUtil.invoke(tasks));
//                tasks.clear();
//            }
//        }
//
//        // 处理尾部数据
//        if (!tasks.isEmpty()) {
//            list.addAll(ThreadUtil.invoke(tasks));
//        }
//        }
//        if (limit > 0 && list.size() > limit) {
//            return list.subList(0, limit);
//        }
        // 任务列表
        List<Runnable> tasks = new ArrayList<>(children.size());
        // 获取节点数据
        for (String sub : children) {
            // 对节点路径做处理
            String path = ShellZKNodeUtil.concatPath(parentPath, sub);
            // 判断是否存在
            if (existingNodes.contains(path)) {
                continue;
            }
            // 添加到集合
            tasks.add(() -> {
                try {
                    list.add(getNode(client, path));
                } catch (Exception ignore) {
                }
            });
            // 判断是否要限制
            if (limit > 0 && tasks.size() > limit) {
                break;
            }
        }
        // 提交任务
        ThreadUtil.submitVirtual(tasks);
        return list;
    }

    public static String decodePath(String nodePath) {
        if (StringUtil.containsAny(nodePath, "%", "+")) {
            return URLDecoder.decode(nodePath, Charset.defaultCharset());
        }
        return nodePath;
    }

    /**
     * 递归获取zk节点
     *
     * @param path       路径
     * @param client     zk操作器
     * @param filter     过滤器
     * @param success    成功处理
     * @param error      错误处理
     * @param includeACL 是否包含acl
     */
    public static void loopNode( ZKClient client,  String path, Predicate<String> filter,  Consumer<ZKNode> success, BiConsumer<String, Exception> error, boolean includeACL) throws Exception {
        // 获取节点
        try {
            if (filter == null || filter.test(path)) {
                ZKNode node = new ZKNode();
                node.nodePath(path);
                node.setNodeData(client.getData(path));
                if (includeACL) {
                    node.acl(client.getACL(node.nodePath()));
                }
                success.accept(node);
            }
        } catch (Exception ex) {
            if (error != null) {
                error.accept(path, ex);
            }
        }
        // 获取子节点
        try {
            List<String> children = client.getChildren(path);
            for (String child : children) {
                String nPath = ShellZKNodeUtil.concatPath(path, child);
                loopNode(client, nPath, filter, success, error, includeACL);
            }
        } catch (Exception ex) {
            if (error != null) {
                error.accept(path, ex);
            }
        }
    }
}
