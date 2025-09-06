package cn.oyzh.easyshell.zk;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.thread.ThreadLocalUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.zk.ShellZKAuth;
import cn.oyzh.easyshell.dto.zk.ShellZKACL;
import cn.oyzh.easyshell.dto.zk.ShellZKEnvNode;
import cn.oyzh.easyshell.dto.zk.ShellZKClusterNode;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.exception.ShellReadonlyOperationException;
import cn.oyzh.easyshell.exception.zk.ShellZKNoAdminPermException;
import cn.oyzh.easyshell.exception.zk.ShellZKNoChildPermException;
import cn.oyzh.easyshell.exception.zk.ShellZKNoCreatePermException;
import cn.oyzh.easyshell.exception.zk.ShellZKNoDeletePermException;
import cn.oyzh.easyshell.exception.zk.ShellZKNoReadPermException;
import cn.oyzh.easyshell.exception.zk.ShellZKNoWritePermException;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.query.zk.ShellZKQueryParam;
import cn.oyzh.easyshell.query.zk.ShellZKQueryResult;
import cn.oyzh.easyshell.util.zk.ShellZKAuthUtil;
import cn.oyzh.easyshell.util.zk.ShellZKClientActionUtil;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.ssh.jump.SSHJumpForwarder2;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Quotas;
import org.apache.zookeeper.StatsTrack;
import org.apache.zookeeper.Version;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.cli.DelQuotaCommand;
import org.apache.zookeeper.cli.SetQuotaCommand;
import org.apache.zookeeper.client.FourLetterWordMain;
import org.apache.zookeeper.client.ZooKeeperSaslClient;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.ClientInfo;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.quorum.QuorumPeer;
import org.apache.zookeeper.server.quorum.flexible.QuorumVerifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * zk客户端封装
 *
 * @author oyzh
 * @since 2020/6/8
 */
public class ShellZKClient implements ShellBaseClient {

    static {
        // 注册sasl处理器
        ShellZKSASLUtil.registerConfiguration();
    }

    /**
     * 最后的创建节点
     */
    private String lastCreate;

    /**
     * 最后的修改节点
     */
    private String lastUpdate;

    /**
     * 最后的删除节点
     */
    private String lastDelete;

    /**
     * zk信息
     */
    private final ShellConnect shellConnect;

    ///**
    // * 树监听对象
    // */
    //private TreeCache treeCache;

    /**
     * ssh端口转发器
     */
    private SSHJumpForwarder2 jumpForwarder;

    // /**
    //  * 是否已初始化
    //  */
    // private volatile boolean initialized;

    /**
     * 重试策略
     */
    private RetryPolicy retryPolicy;

    /**
     * zookeeper对象
     */
    private ZooKeeper zooKeeper;

    /**
     * zk客户端
     */
    private CuratorFramework framework;

    ///**
    // * zk消息监听器
    // */
    //private volatile ShellZKTreeListener cacheListener;

    ///**
    // * 认证列表
    // */
    //private List<ShellZKAuth> auths;

    ///**
    // * 静默关闭标志位
    // */
    //private boolean closeQuietly;

    ///**
    // * 缓存选择器
    // */
    //private final ShellZKTreeCacheSelector cacheSelector = new ShellZKTreeCacheSelector();

    // /**
    //  * 连接状态
    //  */
    // private final ReadOnlyObjectWrapper<ZKConnState> state = new ReadOnlyObjectWrapper<>();

//    /**
//     * ssh配置储存
//     */
//    private final ZKSSHConfigStore sshConfigStore = ZKSSHConfigStore.INSTANCE;

    // /**
    //  * 跳板配置存储
    //  */
    // private final ZKJumpConfigStore jumpConfigStore = ZKJumpConfigStore.INSTANCE;

//    /**
//     * 代理配置存储
//     */
//    private final ZKProxyConfigStore proxyConfigStore = ZKProxyConfigStore.INSTANCE;

    // /**
    //  * 获取连接状态
    //  *
    //  * @return 连接状态
    //  */
    // public ZKConnState state() {
    //     return this.stateProperty().get();
    // }
    //
    // /**
    //  * 连接状态属性
    //  *
    //  * @return 连接状态属性
    //  */
    // public ReadOnlyObjectProperty<ZKConnState> stateProperty() {
    //     return this.state.getReadOnlyProperty();
    // }

    /**
     * 连接状态
     */
    private final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> ShellBaseClient.super.onStateChanged(state3);

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    public ShellZKClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.addStateListener(this.stateListener);
        //// 监听连接状态
        //this.stateProperty().addListener((observable, oldValue, newValue) -> {
        //    if (newValue == null || !newValue.isConnected()) {
        //        this.closeTreeCache();
        //    } else {
        //        ThreadUtil.startVirtual(this::startTreeCache);
        //    }
        //    // if (newValue != null) {
        //    //     switch (newValue) {
        //    //         case LOST -> ShellZKEventUtil.connectionLost(this);
        //    //         case CLOSED -> {
        //    //             if (!this.closeQuietly) {
        //    //                 ShellZKEventUtil.connectionClosed(this);
        //    //             }
        //    //             this.closeQuietly = false;
        //    //         }
        //    //         case CONNECTED -> ShellZKEventUtil.connectionConnected(this);
        //    //     }
        //    // }
        //});
    }

    /**
     * 是否只读模式
     *
     * @return 结果
     */
    public boolean isReadonly() {
        return this.shellConnect.isReadonly();
    }

    /**
     * 如果只读模式不支持操作，则抛出异常
     */
    public void throwReadonlyException() {
        if (this.isReadonly()) {
            throw new ShellReadonlyOperationException();
        }
    }

    ///**
    // * 开启zk树监听
    // */
    //private void startTreeCache() {
    //    try {
    //        // 关闭旧的zk树监听
    //        this.closeTreeCache();
    //        // 创建zk树监听
    //        if (this.cacheListener != null) {
    //            this.treeCache = ShellZKTreeCacheUtil.build(this.framework, this.cacheListener.getPath(), this.cacheSelector);
    //            this.treeCache.getListenable().addListener(this.cacheListener);
    //            // this.treeCache.getListenable().addListener(this.initializedListener);
    //            this.treeCache.start();
    //            // } else if (!this.isEnableListen()) {// 未开启监听则只创建连接状态初始化监听器
    //            //     this.treeCache = ShellZKTreeCacheUtil.build(this.framework, this.cacheSelector);
    //            //     this.treeCache.getListenable().addListener(this.initializedListener);
    //            //     this.treeCache.start();
    //        }
    //    } catch (Exception ex) {
    //        ex.printStackTrace();
    //    }
    //}

    ///**
    // * 关闭zk树监听
    // */
    //private void closeTreeCache() {
    //    try {
    //        if (this.treeCache != null) {
    //            if (this.cacheListener != null) {
    //                this.cacheListener.destroy();
    //                this.treeCache.getListenable().removeListener(this.cacheListener);
    //            }
    //            this.treeCache.close();
    //            this.treeCache = null;
    //        }
    //    } catch (Exception ex) {
    //        ex.printStackTrace();
    //    }
    //}

    ///**
    // * 是否开启了节点监听
    // *
    // * @return 结果
    // */
    //public boolean isEnableListen() {
    //    return this.shellConnect.isListen();
    //}

    /// **
    // * 连接zk以监听模式
    // */
    //public void startWithListener() throws Throwable {
    //    if (this.isEnableListen()) {
    //        this.cacheListener = new ShellZKTreeListener(this);
    //    } else {
    //        this.cacheListener = null;
    //    }
    //    this.start();
    //}

    // /**
    //  * 连接zk
    //  */
    // public void start() {
    //     this.start(this.shellConnect.connectTimeOutMs());
    // }
    @Override
    public void start(int timeout) {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        // 初始化客户端
        this.initClient();
        try {
            // 开始连接时间
            final AtomicLong starTime = new AtomicLong();
            // 设置连接监听事件
            this.framework.getConnectionStateListenable().addListener((c, s) -> {
                this.state.set(ShellConnState.valueOfZK(s));
                // 连接成功
                if (s == ConnectionState.CONNECTED) {
                    long endTime = System.currentTimeMillis();
                    JulLog.info("zkClient connected used:{}ms.", (endTime - starTime.get()));
                }
                JulLog.info("ConnectionState changed:{}==============================", s);
            });
            // 开始连接时间
            starTime.set(System.currentTimeMillis());
            // 更新连接状态
            this.state.set(ShellConnState.CONNECTING);
            // 开始连接
            this.framework.start();
            // 连接成功前阻塞线程
            if (this.framework.blockUntilConnected(timeout, TimeUnit.MILLISECONDS)) {
                // 更新连接状态
                this.state.set(ShellConnState.CONNECTED);
                // // 设置认证信息为已认证
                // ShellZKAuthUtil.setAuthed(this, ShellZKAuthUtil.loadAuths(this.iid()));
            } else {// 连接未成功则关闭
                this.closeInner();
                if (this.state.get() == ShellConnState.FAILED) {
                    this.state.set(null);
                } else {
                    this.state.set(ShellConnState.FAILED);
                }
            }
        } catch (Exception ex) {
            this.state.set(ShellConnState.FAILED);
            JulLog.warn("zkClient start error", ex);
            throw new ShellException(ex);
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    /**
     * 初始化连接
     *
     * @return 连接
     */
    private String initHost() {
        // 连接地址
        String host;
//        // 初始化跳板配置
//        List<ZKJumpConfig> jumpConfigs = this.shellConnect.getJumpConfigs();
//        // 从数据库获取
//        if (jumpConfigs == null) {
//            jumpConfigs = this.jumpConfigStore.loadByIid(this.shellConnect.getId());
//        }
//        // 过滤配置
//        jumpConfigs = jumpConfigs == null ? Collections.emptyList() : jumpConfigs.stream().filter(ZKJumpConfig::isEnabled).collect(Collectors.toList());
        // 初始化跳板转发
        if (this.shellConnect.isEnableJump()) {
            if (this.jumpForwarder == null) {
                this.jumpForwarder = new SSHJumpForwarder2();
            }
            // 初始化跳板配置
            List<ShellJumpConfig> jumpConfigs = this.shellConnect.getJumpConfigs();
            // 转换为目标连接
            SSHConnect target = new SSHConnect();
            target.setHost(this.shellConnect.hostIp());
            target.setPort(this.shellConnect.hostPort());
            // 执行连接
            int localPort = this.jumpForwarder.forward(jumpConfigs, target);
            // 连接信息
            host = "127.0.0.1:" + localPort;
        } else {// 直连
            if (this.jumpForwarder != null) {
                IOUtil.close(this.jumpForwarder);
                this.jumpForwarder = null;
            }
            // 连接信息
            host = this.shellConnect.hostIp() + ":" + this.shellConnect.hostPort();
        }
        return host;
    }

//    /**
//     * 初始化代理
//     */
//    private void initProxy() {
//        // 开启了代理
//        if (this.shellConnect.isEnableProxy()) {
//            // 初始化ssh转发器
//            ZKProxyConfig proxyConfig = this.shellConnect.getProxyConfig();
//            // 从数据库获取
//            if (proxyConfig == null) {
//                proxyConfig = this.proxyConfigStore.getByIid(this.shellConnect.getId());
//            }
//            if (proxyConfig == null) {
//                JulLog.warn("proxy is enable but proxy config is null");
//                throw new SSHException("proxy is enable but proxy config is null");
//            }
//            // 初始化代理
//            Proxy proxy = SSHUtil.newProxy(proxyConfig);
//            // 设置代理
//            this.session.setProxy(proxy);
//        }
//    }

    /**
     * 初始化客户端
     */
    private void initClient() {
//        // 连接地址
//        String host;
//        // 初始化ssh端口转发
//        if (this.shellConnect.isSSHForward()) {
//            // 初始化ssh转发器
//            ZKSSHConfig sshConfig = this.shellConnect.getSshConfig();
//            // 从数据库获取
//            if (sshConfig == null) {
//                sshConfig = this.sshConfigStore.getByIid(this.shellConnect.getId());
//            }
//            if (sshConfig != null) {
//                if (this.sshJumper == null) {
//                    this.sshJumper = new SSHJumpForwarder();
//                }
//                // ssh配置
//                // 执行连接
//                int localPort = this.sshJumper.forward(null, null);
//                // 连接信息
//                host = "127.0.0.1:" + localPort;
//            } else {
//                JulLog.warn("ssh forward is enable but ssh config is null");
//                throw new ZKException("ssh forward is enable but ssh config is null");
//            }
//        } else {// 直连
//            // 连接信息
//            host = this.shellConnect.hostIp() + ":" + this.shellConnect.hostPort();
//        }
        // 连接信息
        String host = this.initHost();
        //// 加载认证
        //this.auths = ShellZKAuthUtil.loadAuths(this.iid());
        // 认证信息列表
        List<AuthInfo> authInfos = ShellZKAuthUtil.toAuthInfo(this.getEnabledAuths());
        JulLog.info("auto authorization, auths: {}.", authInfos);
        // 重试策略
        if (this.retryPolicy == null) {
            this.retryPolicy = new RetryOneTime(3_000);
        }
        // 创建客户端
        this.framework = ShellZKClientUtil.build(
                host,
                this.shellConnect,
                this.retryPolicy,
                authInfos,
                zoo -> this.zooKeeper = zoo
        );
    }

    @Override
    public void close() {
        this.closeInner();
        this.state.set(ShellConnState.CLOSED);
        this.removeStateListener(this.stateListener);
    }

    ///**
    // * 关闭zk，静默模式
    // */
    //public void closeQuiet() {
    //    this.closeQuietly = true;
    //    this.close();
    //}

    /**
     * 关闭zk实际业务
     */
    private void closeInner() {
        try {
            //// 关闭树监听
            //this.closeTreeCache();
            //// 清理变量
            //this.auths = null;
            // 销毁端口转发
            if (this.jumpForwarder != null) {
                IOUtil.close(this.jumpForwarder);
            }
            // 关闭连接
            if (this.framework != null) {
                // 关闭连接
                TaskManager.startTimeout(this.framework::close, 500);
                this.framework = null;
            }
            // 关闭zk连接及sasl客户端
            ZooKeeper zooKeeper = this.getZooKeeper();
            if (zooKeeper != null) {
                ZooKeeperSaslClient saslClient = zooKeeper.getSaslClient();
                if (saslClient != null) {
                    saslClient.shutdown();
                }
                zooKeeper.close();
            }
            JulLog.info("zkClient closed.");
        } catch (Exception ex) {
            JulLog.warn("zkClient close error.", ex);
        }
    }

    /**
     * 是否最后创建的节点
     *
     * @param path 节点路径
     * @return 结果
     */
    public boolean isLastCreate(String path) {
        return Objects.equals(this.lastCreate, path);
    }

    /**
     * 清除最后创建的节点
     */
    public void clearLastCreate() {
        this.lastCreate = null;
    }

    /**
     * 是否最后修改的节点
     *
     * @param path 节点路径
     * @return 结果
     */
    public boolean isLastUpdate(String path) {
        return Objects.equals(this.lastUpdate, path);
    }

    /**
     * 清除最后修改的节点
     */
    public void clearLastUpdate() {
        this.lastUpdate = null;
    }

    /**
     * 是否最后删除的节点
     *
     * @param path 节点路径
     * @return 结果
     */
    public boolean isLastDelete(String path) {
        return Objects.equals(this.lastDelete, path);
    }

    /**
     * 清除最后删除的节点
     */
    public void clearLastDelete() {
        this.lastDelete = null;
    }

    // /**
    //  * zk是否已连接
    //  *
    //  * @return 结果
    //  */
    // public boolean isClosed() {
    //     return this.state() == ZKConnState.CLOSED;
    // }

    @Override
    public boolean isConnected() {
        if (this.framework == null || this.framework.getState() != CuratorFrameworkState.STARTED) {
            return false;
        }
        return this.getState().isConnected();
    }

    /**
     * zk是否连接中
     *
     * @return 结果
     */
    public boolean isConnecting() {
        if (this.framework == null || this.isConnected()) {
            return false;
        }
        if (this.framework.getState() == CuratorFrameworkState.LATENT) {
            return true;
        }
        return this.getState() == ShellConnState.CONNECTING;
    }

    /**
     * 设置权限
     *
     * @param path    路径
     * @param aclList 权限列表
     * @return Stat 节点状态
     * @throws Exception 异常
     */
    public Stat setACL(String path, List<ACL> aclList) throws Exception {
        return this.setACL(path, aclList, null);
    }

    /**
     * 设置权限
     *
     * @param path    路径
     * @param aclList 权限列表
     * @param version 数据版本
     * @return Stat 节点状态
     * @throws Exception 异常
     */
    public Stat setACL(String path, List<ACL> aclList, Integer version) throws Exception {
        this.throwReadonlyException();
        try {
            ShellZKClientActionUtil.forSetAclAction(this.connectName(), path, false, false, version, aclList);
            return this.framework.setACL().withVersion(version == null ? -1 : version).withACL(aclList).forPath(path);
        } catch (Exception ex) {
            if (ex instanceof KeeperException.NoAuthException) {
                JulLog.error("path:{} NoAuth!", path);
                throw new ShellZKNoAdminPermException(path);
            }
            throw ex;
        }
    }

    /**
     * 设置权限
     *
     * @param paths   路径列表
     * @param aclList 权限列表
     * @throws Exception 异常
     */
    public void setACL(List<String> paths, List<ACL> aclList) throws Exception {
        this.throwReadonlyException();
        String path = null;
        try {
            ThreadLocalUtil.setVal("connectName", this.connectName());
            for (String s : paths) {
                path = s;
                // this.doAction("setAcl", path, aclList);
                this.framework.setACL().withVersion(-1).withACL(aclList).forPath(path);
            }
        } catch (Exception ex) {
            if (ex instanceof KeeperException.NoAuthException) {
                JulLog.error("path:{} NoAuth!", path);
                throw new ShellZKNoAdminPermException(path);
            }
            throw ex;
        }
    }

    /**
     * 设置权限
     *
     * @param path 路径
     * @param acl  权限
     * @return Stat
     * @throws Exception 异常
     */
    public Stat addACL(String path, ACL acl) throws Exception {
        List<ACL> aclList = this.getACL(path);
        aclList.add(acl);
        return this.setACL(path, aclList);
    }

    /**
     * 设置权限
     *
     * @param path 路径
     * @param list 权限列表
     * @return Stat
     * @throws Exception 异常
     */
    public Stat addACL(String path, List<? extends ACL> list) throws Exception {
        List<ACL> aclList = this.getACL(path);
        aclList.addAll(list);
        return this.setACL(path, aclList);
    }

    /**
     * 设置权限
     *
     * @param path 路径
     * @param acl  权限列表
     * @return Stat
     * @throws Exception 异常
     */
    public Stat deleteACL(String path, ACL acl) throws Exception {
        List<ACL> aclList = this.getACL(path);
        aclList.remove(acl);
        return this.setACL(path, aclList);
    }

    /**
     * 添加认证信息
     *
     * @param user     用户名
     * @param password 密码
     * @throws Exception 异常
     */
    public void addAuth(String user, String password) throws Exception {
        ZooKeeper zooKeeper = this.getZooKeeper();
        if (zooKeeper != null) {
            String data = user + ":" + password;
            ThreadLocalUtil.setVal("connectName", this.connectName());
            ShellZKClientActionUtil.forAddAuthAction(this.connectName(), "digest", data);
            zooKeeper.addAuthInfo("digest", data.getBytes());
            // ShellZKAuthUtil.setAuthed(this, user, password);
            this.setAuthed(user, password);
        } else {
            JulLog.warn("zooKeeper is null!");
        }
    }

    /**
     * 检查节点是否存在
     *
     * @param path 节点路径
     * @return 结果
     */
    public boolean exists(String path) {
        try {
            return this.checkExists(path) != null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 创建节点及父节点
     *
     * @param path 节点路径
     * @param data 节点值
     */
    public String createIncludeParents(String path, byte[] data) throws Exception {
        return this.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, null, CreateMode.PERSISTENT, true);
    }

    /**
     * 创建节点及父节点
     *
     * @param path       节点路径
     * @param data       节点值
     * @param createMode 节点模式
     */
    public String createIncludeParents(String path, byte[] data, CreateMode createMode) throws Exception {
        return this.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, null, createMode, true);
    }

    /**
     * 创建节点
     *
     * @param path       节点路径
     * @param data       节点值
     * @param createMode 创建模式
     */
    public String create(String path, byte[] data, CreateMode createMode) throws Exception {
        return this.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, null, createMode, false);
    }

    /**
     * 创建节点
     *
     * @param path       节点路径
     * @param data       节点值
     * @param aclList    权限集合
     * @param ttl        生存时间
     * @param createMode 创建模式
     * @param cParents   在需要的时候是否创建父节点
     */
    public String create(String path, byte[] data, List<ACL> aclList, Long ttl, CreateMode createMode, boolean cParents) throws Exception {
        this.throwReadonlyException();
        String old = this.lastCreate;
        try {
            this.lastCreate = path;
            CreateBuilder builder = this.framework.create();
            if (cParents) {
                builder.creatingParentsIfNeeded();
            }
            if (ttl != null) {
                builder.withTtl(ttl);
            }
            builder.withMode(createMode).withACL(aclList);
            String nodePath;
            if (data == null) {
                nodePath = builder.forPath(path);
            } else {
                nodePath = builder.forPath(path, data);
            }
            ShellZKClientActionUtil.forCreateAction(this.connectName(), path, data, createMode, aclList, ttl);
            return nodePath;
        } catch (Exception ex) {
            this.lastCreate = old;
            if (ex instanceof KeeperException.NoAuthException) {
                JulLog.error("path:{} NoAuth!", path);
                throw new ShellZKNoCreatePermException(path);
            }
            throw ex;
        }
    }

    /**
     * 创建节点
     *
     * @param path       节点路径
     * @param data       节点值
     * @param aclList    权限集合
     * @param createMode 创建模式
     */
    public String create(String path, String data, List<ACL> aclList, CreateMode createMode) throws Exception {
        return this.create(path, data, aclList, null, createMode, false);
    }

    /**
     * 创建节点
     *
     * @param path       节点路径
     * @param data       节点值
     * @param aclList    权限集合
     * @param createMode 创建模式
     * @param cParents   在需要的时候是否创建父节点
     */
    public String create(String path, String data, List<ACL> aclList, CreateMode createMode, boolean cParents) throws Exception {
        return this.create(path, data, aclList, null, createMode, cParents);
    }

    /**
     * 创建节点
     *
     * @param path       节点路径
     * @param data       节点值
     * @param aclList    权限集合
     * @param ttl        生存时间
     * @param createMode 创建模式
     * @param cParents   在需要的时候是否创建父节点
     */
    public String create(String path, String data, List<ACL> aclList, Long ttl, CreateMode createMode, boolean cParents) throws Exception {
        byte[] bytes;
        if (data != null) {
            bytes = data.getBytes();
        } else {
            bytes = new byte[0];
        }
        return this.create(path, bytes, aclList, ttl, createMode, cParents);
    }

    /**
     * 获取子节点
     *
     * @param path 路径
     * @return 子节点列表
     */
    public List<String> getChildren(String path) throws Exception {
        try {
            ShellZKClientActionUtil.forGetAction(this.connectName(), path, false, false);
            return this.framework.getChildren().forPath(path);
        } catch (Exception ex) {
            if (ex instanceof KeeperException.NoAuthException) {
                JulLog.error("path:{} NoAuth!", path);
                throw new ShellZKNoChildPermException(path);
            }
            throw ex;
        }
    }

//    /**
//     * 获取子节点(异步)
//     *
//     * @param path     路径
//     * @param callback 回调函数
//     */
//    public void getChildren( String path,  BackgroundCallback callback) throws Exception {
//        try {
//            ShellZKClientActionUtil.forLsAction(this.connectName(), path, false, false, false);
//            this.framework.getChildren().inBackground(callback).forPath(path);
//        } catch (IllegalStateException ignore) {
//        } catch (Exception ex) {
//            if (ex instanceof KeeperException.NoAuthException) {
//                JulLog.error("path:{} NoAuth!", path);
//                throw new ShellZKNoChildPermException(path);
//            }
//            throw ex;
//        }
//    }

    /**
     * 获取节点数据
     *
     * @param path 路径
     * @return 节点数据
     */
    public byte[] getData(String path) throws Exception {
        if (this.framework == null) {
            return null;
        }
        try {
            ShellZKClientActionUtil.forGetAction(this.connectName(), path, false, false);
            return this.framework.getData().forPath(path);
        } catch (Exception ex) {
            if (ex instanceof KeeperException.NoAuthException) {
                JulLog.error("path:{} NoAuth!", path);
                throw new ShellZKNoReadPermException(path);
            }
            throw ex;
        }
    }

    /**
     * 获取节点数据(异步)
     *
     * @param path     路径
     * @param callback 回调函数
     */
    public void getData(String path, BackgroundCallback callback) throws Exception {
        try {
            ShellZKClientActionUtil.forGetAction(this.connectName(), path, false, false);
            this.framework.getData().inBackground(callback).forPath(path);
        } catch (IllegalStateException ignore) {
        } catch (Exception ex) {
            if (ex instanceof KeeperException.NoAuthException) {
                JulLog.error("path:{} NoAuth!", path);
                throw new ShellZKNoReadPermException(path);
            }
            throw ex;
        }
    }

    /**
     * 获取节点数据，字符串形式
     *
     * @param path 路径
     * @return 节点数据
     */
    public String getDataString(String path) throws Exception {
        byte[] bytes = this.getData(path);
        if (bytes != null) {
            return new String(bytes);
        }
        return null;
    }

    /**
     * 获取节点权限
     *
     * @param path 路径
     * @return 权限数据
     */
    public List<ACL> getACL(String path) throws Exception {
        try {
            ShellZKClientActionUtil.forGetAclAction(this.connectName(), path, false);
            return this.framework.getACL().forPath(path);
        } catch (Exception ex) {
            if (ex instanceof KeeperException.NoAuthException) {
                JulLog.error("path:{} NoAuth!", path);
                throw new ShellZKNoAdminPermException(path);
            }
            throw ex;
        }
    }

    /**
     * 获取节点权限(异步)
     *
     * @param path     路径
     * @param callback 回调函数
     */
    public void getACL(String path, BackgroundCallback callback) throws Exception {
        try {
            ShellZKClientActionUtil.forGetAclAction(this.connectName(), path, false);
            this.framework.getACL().inBackground(callback).forPath(path);
        } catch (IllegalStateException ignore) {
        } catch (Exception ex) {
            if (ex instanceof KeeperException.NoAuthException) {
                JulLog.error("path:{} NoAuth!", path);
                throw new ShellZKNoAdminPermException(path);
            }
            throw ex;
        }
    }

    /**
     * 设置节点数据
     *
     * @param path 路径
     * @param data 数据
     * @return Stat 状态
     */
    public Stat setData(String path, String data) throws Exception {
        byte[] bytes = Objects.requireNonNullElse(data, "").getBytes();
        return this.setData(path, bytes, null);
    }

    /**
     * 设置节点数据
     *
     * @param path    路径
     * @param data    数据
     * @param version 版本
     * @return Stat 状态
     */
    public Stat setData(String path, String data, Integer version) throws Exception {
        byte[] bytes = Objects.requireNonNullElse(data, "").getBytes();
        return this.setData(path, bytes, version);
    }

    /**
     * 设置节点数据
     *
     * @param path 路径
     * @param data 数据
     * @return Stat 状态
     */
    public Stat setData(String path, byte[] data) throws Exception {
        return this.setData(path, data, null);
    }

    /**
     * 设置节点数据
     *
     * @param path    路径
     * @param data    数据
     * @param version 版本
     * @return Stat 状态
     */
    public Stat setData(String path, byte[] data, Integer version) throws Exception {
        this.throwReadonlyException();
        String old = this.lastUpdate;
        try {
            this.lastUpdate = path;
            ShellZKClientActionUtil.forSetAction(this.connectName(), path, data, version, false);
            Stat stat = this.framework.setData().withVersion(version == null ? -1 : version).forPath(path, data);
            //if (stat != null) {
            //    ShellZKEventUtil.nodeUpdated(this, path);
            //}
            return stat;
        } catch (Exception ex) {
            this.lastUpdate = old;
            if (ex instanceof KeeperException.NoAuthException) {
                JulLog.error("path:{} NoAuth!", path);
                throw new ShellZKNoWritePermException(path);
            }
            throw ex;
        }
    }

    /**
     * 同步节点数据
     *
     * @param path 路径
     */
    public void sync(String path) throws Exception {
        this.throwReadonlyException();
        ShellZKClientActionUtil.forSyncAction(this.connectName(), path);
        this.framework.sync().forPath(path);
    }

    /**
     * 删除节点
     *
     * @param path 路径
     */
    public void delete(String path) throws Exception {
        this.delete(path, null, false);
    }

    /**
     * 删除节点
     *
     * @param path        路径
     * @param version     版本
     * @param delChildren 删除子节点
     */
    public void delete(String path, Integer version, boolean delChildren) throws Exception {
        this.throwReadonlyException();
        String old = this.lastDelete;
        try {
            this.lastDelete = path;
            DeleteBuilder builder = this.framework.delete();
            builder.guaranteed().withVersion(version == null ? -1 : version);
            if (delChildren) {
                builder.deletingChildrenIfNeeded();
            }
            builder.forPath(path);
            ShellZKClientActionUtil.forDeleteAction(this.connectName(), path, version);
            //ShellZKEventUtil.nodeDeleted(this, path, delChildren);
        } catch (Exception ex) {
            this.lastDelete = old;
            if (ex instanceof KeeperException.NoAuthException) {
                JulLog.error("path:{} NoAuth!", path);
                throw new ShellZKNoDeletePermException(path);
            }
            throw ex;
        }
    }

    /**
     * 获取状态
     *
     * @param path 路径
     * @return 状态
     */
    public Stat checkExists(String path) throws Exception {
        if (this.framework == null) {
            return null;
        }
        ShellZKClientActionUtil.forStatAction(this.connectName(), path, false);
        return this.framework.checkExists().forPath(path);
    }

//    /**
//     * 获取状态(异步)
//     *
//     * @param path     路径
//     * @param callback 回调函数
//     */
//    public void checkExists( String path,  BackgroundCallback callback) throws Exception {
//        try {
//            ShellZKClientActionUtil.forStatAction(this.connectName(), path, false);
//            this.framework.checkExists().inBackground(callback).forPath(path);
//        } catch (IllegalStateException ignore) {
//        }
//    }

    /**
     * 创建配额
     *
     * @param path  路径
     * @param bytes 限制数据大小
     * @param num   限制子节点数量
     */
    public boolean createQuota(String path, long bytes, int num) throws Exception {
        this.throwReadonlyException();
        if (path.equals("/")) {
            return false;
        }
        ShellZKClientActionUtil.forSetQuotaAction(this.connectName(), path, bytes, num);
        return SetQuotaCommand.createQuota(this.getZooKeeper(), path, bytes, num);
    }

    /**
     * 删除配额
     *
     * @param path  路径
     * @param bytes 删除数据大小配额
     * @param count 删除子节点数量配额
     */
    public boolean delQuota(String path, boolean bytes, boolean count) throws Exception {
        this.throwReadonlyException();
        if (path.equals("/")) {
            return false;
        }
        ShellZKClientActionUtil.forDelQuotaAction(this.connectName(), path, bytes, count);
        return DelQuotaCommand.delQuota(this.getZooKeeper(), path, bytes, count);
    }

    /**
     * 列举配额
     *
     * @param path 路径
     */
    public StatsTrack listQuota(String path) throws Exception {
        if (path.equals("/")) {
            return null;
        }
        String absolutePath = Quotas.quotaZookeeper + path + "/" + Quotas.limitNode;
        byte[] data = this.getData(absolutePath);
        ShellZKClientActionUtil.forListQuotaAction(this.connectName(), path);
        return new StatsTrack(new String(data));
    }

    /**
     * 获取临时节点
     */
    public List<String> getEphemerals() throws Exception {
        return this.zooKeeper.getEphemerals();
    }

    /**
     * 获取临时节点
     *
     * @param path 路径
     */
    public List<String> getEphemerals(String path) throws Exception {
        return this.zooKeeper.getEphemerals(path);
    }

    /**
     * 获取全部节点数量
     */
    public int getAllChildrenNumber(String path) throws Exception {
        return this.zooKeeper.getAllChildrenNumber(path);
    }

    /**
     * 获取当前配置
     *
     * @return 配置
     */
    public QuorumVerifier getCurrentConfig() {
        return this.framework.getCurrentConfig();
    }

    /**
     * 获取集群服务列表
     *
     * @return 集群服务列表
     */
    public List<ShellZKClusterNode> clusterNodes() {
        List<ShellZKClusterNode> servers = new ArrayList<>(8);
        try {
            QuorumVerifier verifier = this.getCurrentConfig();
            // 老版本实现
            if (this.shellConnect.compatibility34() || verifier == null) {
                if (this.exists("/zookeeper/config")) {
                    String data = this.getDataString("/zookeeper/config");
                    if (data != null) {
                        for (String str : data.lines().toList()) {
                            if (str.startsWith("server.")) {
                                servers.add(new ShellZKClusterNode(str));
                            }
                        }
                    }
                }
            } else {// 新版本实现
                Collection<QuorumPeer.QuorumServer> quorumServers = verifier.getVotingMembers().values();
                for (QuorumPeer.QuorumServer quorumServer : quorumServers) {
                    ShellZKClusterNode serverNode = new ShellZKClusterNode(quorumServer);
                    serverNode.setWeight(verifier.getWeight(quorumServer.id));
                    servers.add(serverNode);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return servers;
    }

    // /**
    //  * 是否已初始化完成
    //  *
    //  * @return 结果
    //  */
    // public boolean initialized() {
    //     return this.initialized && this.isConnected();
    // }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String connectName() {
        return this.shellConnect.getName();
    }

    /**
     * 获取zookeeper对象
     *
     * @return ZooKeeper
     * @throws Exception 异常
     */
    public ZooKeeper getZooKeeper() throws Exception {
        if (this.zooKeeper != null) {
            return this.zooKeeper;
        }
        if (this.framework == null) {
            return null;
        }
        CuratorZookeeperClient zookeeperClient = this.framework.getZookeeperClient();
        if (zookeeperClient == null) {
            return null;
        }
        return zookeeperClient.getZooKeeper();
    }

//     /**
//      * 添加状态监听器
//      *
//      * @param stateListener 状态监听器
//      */
//     public void addStateListener(ChangeListener<ZKConnState> stateListener) {
//         if (stateListener != null) {
//             this.state.addListener(stateListener);
// //            this.state.addListener(new WeakChangeListener<>(stateListener));
//         }
//     }

    /**
     * 用户信息
     *
     * @return 结果
     */
    public List<ClientInfo> whoami() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "whoami");
            return this.zooKeeper.whoAmI();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 获取本地环境
     *
     * @return 结果
     */
    public List<ShellZKEnvNode> localNodes() {
        List<ShellZKEnvNode> list = new ArrayList<>(8);
        ShellZKEnvNode host = new ShellZKEnvNode("host", this.shellConnect.getHost());
        ShellZKEnvNode connection = new ShellZKEnvNode("connection", this.shellConnect.getName());
        ShellZKEnvNode version = new ShellZKEnvNode("sdkVersion", Version.getFullVersion());
        ShellZKEnvNode jdkVersion = new ShellZKEnvNode("jdkVersion", System.getProperty("java.vm.version"));
        list.add(host);
        list.add(version);
        list.add(connection);
        list.add(jdkVersion);
        return list;
    }

    /**
     * 四字命令envi
     *
     * @return 结果
     */
    public String envi() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "envi");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "envi");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 执行四字命令envi
     *
     * @return 结果
     */
    public List<ShellZKEnvNode> enviNodes() {
        String envi = this.envi();
        if (envi != null) {
            List<ShellZKEnvNode> list = new ArrayList<>();
            envi.lines().skip(1).forEach(l -> {
                int index = l.indexOf("=");
                if (index != -1) {
                    String name = l.substring(0, index);
                    String value = l.substring(index + 1);
                    ShellZKEnvNode envNode = new ShellZKEnvNode(name, value);
                    list.add(envNode);
                }
            });
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * 四字命令srvr
     *
     * @return 结果
     */
    public String srvr() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "srvr");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "srvr");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 执行四字命令srvr
     *
     * @return 结果
     */
    public List<ShellZKEnvNode> srvrNodes() {
        String srvr = this.srvr();
        if (srvr != null) {
            List<ShellZKEnvNode> list = new ArrayList<>();
            srvr.lines().forEach(l -> {
                int index = l.indexOf(":");
                if (index != -1) {
                    String name = l.substring(0, index);
                    String value = l.substring(index + 1).trim();
                    ShellZKEnvNode envNode = new ShellZKEnvNode(name, value);
                    list.add(envNode);
                }
            });
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * 四字命令mntr
     *
     * @return 结果
     */
    public String mntr() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "mntr");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "mntr");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 执行四字命令mntr
     *
     * @return 结果
     */
    public List<ShellZKEnvNode> mntrNodes() {
        String mntr = this.mntr();
        if (mntr != null) {
            List<ShellZKEnvNode> list = new ArrayList<>();
            mntr.lines().forEach(l -> {
                int index = l.indexOf("\t");
                if (index != -1) {
                    String name = l.substring(0, index).trim();
                    String value = l.substring(index + 1).trim();
                    ShellZKEnvNode envNode = new ShellZKEnvNode(name, value);
                    list.add(envNode);
                }
            });
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * 四字命令stat
     *
     * @return 结果
     */
    public String stat() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "stat");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "stat");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 执行四字命令stat
     *
     * @return 结果
     */
    public List<ShellZKEnvNode> statNodes() {
        String stat = this.stat();
        if (stat != null) {
            List<ShellZKEnvNode> list = new ArrayList<>();
            stat.lines().forEach(l -> {
                int index = l.indexOf("(");
                if (index != -1) {
                    String name = l.substring(0, index).trim();
                    String value = l.substring(index + 1, l.length() - 1).trim();
                    ShellZKEnvNode envNode = new ShellZKEnvNode(name, value);
                    list.add(envNode);
                }
            });
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * 四字命令conf
     *
     * @return 结果
     */
    public String conf() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "conf");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "conf");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 执行四字命令conf
     *
     * @return 结果
     */
    public List<ShellZKEnvNode> confNodes() {
        String conf = this.conf();
        if (conf != null) {
            List<ShellZKEnvNode> list = new ArrayList<>();
            conf.lines().forEach(l -> {
                int index = l.indexOf("=");
                if (index != -1) {
                    String name = l.substring(0, index);
                    String value = l.substring(index + 1);
                    ShellZKEnvNode envNode = new ShellZKEnvNode(name, value);
                    list.add(envNode);
                }
            });
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * 四字命令cons
     *
     * @return 结果
     */
    public String cons() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "cons");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "cons");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

//    /**
//     * 执行四字命令cons
//     *
//     * @return 结果
//     */
//    public List<ShellZKEnvNode> consNodes() {
//        String cons = this.cons();
//        if (cons != null) {
//            List<ShellZKEnvNode> list = new ArrayList<>();
//            cons.lines().forEach(l -> {
//                int index = l.indexOf("(");
//                if (index != -1) {
//                    String name = l.substring(0, index).trim();
//                    String value = l.substring(index + 1, l.length() - 1).trim();
//                    ShellZKEnvNode envNode = new ShellZKEnvNode(name, value);
//                    list.add(envNode);
//                }
//            });
//            return list;
//        }
//        return Collections.emptyList();
//    }

    /**
     * 四字命令ruok
     *
     * @return 结果
     */
    public String ruok() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "ruok");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "ruok");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 四字命令crst
     *
     * @return 结果
     */
    public String crst() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "crst");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "crst");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 四字命令srst
     *
     * @return 结果
     */
    public String srst() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "srst");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "srst");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 四字命令wchc
     *
     * @return 结果
     */
    public String wchc() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "wchc");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "wchc");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 四字命令wchs
     *
     * @return 结果
     */
    public String wchs() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "wchs");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "wchs");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 四字命令wchp
     *
     * @return 结果
     */
    public String wchp() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "wchp");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "wchp");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 四字命令dump
     *
     * @return 结果
     */
    public String dump() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "dump");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "dump");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 四字命令reqs
     *
     * @return 结果
     */
    public String reqs() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "reqs");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "reqs");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 四字命令dirs
     *
     * @return 结果
     */
    public String dirs() {
        try {
            ShellZKClientActionUtil.forAction(this.connectName(), "dirs");
            return FourLetterWordMain.send4LetterWord(this.shellConnect.hostIp(), this.shellConnect.hostPort(), "dirs");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 连接id
     *
     * @return 结果
     */
    public String iid() {
        return this.shellConnect.getId();
    }

    /**
     * 是否已认证
     *
     * @param auth 认证信息
     * @return 结果
     */
    public boolean isAuthed(ShellZKAuth auth) {
        if (auth != null) {
            for (ShellZKAuth auth1 : this.getAuths()) {
                if (auth1.compare(auth)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 设置已认证
     *
     * @param user     用户名
     * @param password 密码
     */
    public void setAuthed(String user, String password) {
        if (user != null && password != null) {
            this.addAuth(new ShellZKAuth(this.iid(), user, password));
        }
    }

    /**
     * 是否任意权限已认证
     *
     * @param aclList 权限列表
     * @return 结果
     */
    public boolean isAnyAuthed(List<ShellZKACL> aclList) {
        if (CollectionUtil.isNotEmpty(aclList) && CollectionUtil.isNotEmpty(this.getAuths())) {
            for (ShellZKACL zkacl : aclList) {
                for (ShellZKAuth auth : this.getAuths()) {
                    if (auth.digest().equals(zkacl.idVal())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 是否需要认证
     *
     * @param node zk节点
     * @return 结果
     */
    public boolean isNeedAuth(ShellZKNode node) {
        if (node.aclEmpty() && node.lackPerm()) {
            return true;
        }
        if (node.hasWorldACL()) {
            return false;
        }
        if (node.hasIPACL()) {
            return false;
        }
        return !isAnyAuthed(node.acl());
    }

    /**
     * 是否摘要认证已认证
     *
     * @param digestVal 摘要值
     * @return 结果
     */
    public boolean isDigestAuthed(String digestVal) {
        for (ShellZKAuth auth : this.getAuths()) {
            if (auth.digest().equals(digestVal)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 状态是否无效
     *
     * @return 结果
     */
    public boolean isInvalid() {
        return this.framework == null || this.framework.getState() == CuratorFrameworkState.STOPPED;
    }

    /**
     * 执行查询
     *
     * @param param 查询参数
     * @return 查询结果
     */
    public ShellZKQueryResult query(ShellZKQueryParam param) {
        ShellZKQueryResult result = new ShellZKQueryResult();
        long start = System.currentTimeMillis();
        try {
            String nodePath = param.getPath();
            if (param.isLs() || param.isLs2()) {
                result.setResult(this.getChildren(nodePath));
            } else if (param.isGet()) {
                result.setResult(this.getData(nodePath));
            } else if (param.isSet()) {
                this.setData(nodePath, param.getData());
            } else if (param.isGetACL()) {
                result.setResult(this.getACL(nodePath));
            } else if (param.isSync()) {
                this.sync(nodePath);
            } else if (param.isCreate()) {
                this.create(nodePath, param.getData(), param.getACL(), param.getCreateMode());
            } else if (param.isGetEphemerals()) {
                if (nodePath == null) {
                    result.setResult(this.getEphemerals());
                } else {
                    result.setResult(this.getEphemerals(nodePath));
                }
            } else if (param.isGetAllChildrenNumber()) {
                result.setResult(this.getAllChildrenNumber(nodePath));
//            } else if (param.isStat()) {
            } else if (param.isSetQuota()) {
                this.createQuota(nodePath, param.getParamB(), param.getParamN());
            } else if (param.isListquota()) {
                result.setResult(this.listQuota(nodePath));
            } else if (param.isRmr() || param.isDeleteall()) {
                this.delete(nodePath, null, true);
            } else if (param.isDelete()) {
                this.delete(nodePath, null, false);
            } else if (param.isSetACL()) {
                this.setACL(nodePath, param.getACL(), param.getParamV());
            } else if (param.isWhoami()) {
                result.setResult(this.whoami());
            } else if (param.isSrvr()) {
                result.setResult(this.srvrNodes());
            } else if (param.isMntr()) {
                result.setResult(this.mntrNodes());
            } else if (param.isEnvi()) {
                result.setResult(this.enviNodes());
            } else if (param.isConf()) {
                result.setResult(this.confNodes());
            } else if (param.isCons()) {
                result.setResult(this.cons());
            } else if (param.isRuok()) {
                result.setResult(this.ruok());
            } else if (param.isCrst()) {
                result.setResult(this.crst());
            } else if (param.isSrst()) {
                result.setResult(this.srst());
            } else if (param.isStat4()) {
                result.setResult(this.statNodes());
            } else if (param.isWchc()) {
                result.setResult(this.wchc());
            } else if (param.isWchs()) {
                result.setResult(this.wchs());
            } else if (param.isWchp()) {
                result.setResult(this.wchp());
            } else if (param.isDump()) {
                result.setResult(this.dump());
            } else if (param.isReqs()) {
                result.setResult(this.reqs());
            } else if (param.isDirs()) {
                result.setResult(this.dirs());
            } else {
                throw new UnsupportedOperationException("unsupported command: " + param.getCommand());
            }
            if (param.hasParamStat()) {
                result.setStat(this.checkExists(nodePath));
            }
            result.setSuccess(true);
            result.setMessage("OK");
        } catch (Exception ex) {
            result.setSuccess(false);
            result.setMessage(ex.getMessage());
        }
        long end = System.currentTimeMillis();
        result.setCost(end - start);
        return result;
    }

    // public ShellConnect shellConnect() {
    //     return this.shellConnect;
    // }

    @Override
    public ShellZKClient forkClient() throws Throwable {
        ShellZKClient zkClient = new ShellZKClient(this.shellConnect) {
            @Override
            public boolean isForked() {
                return true;
            }
        };
        zkClient.start();
        return zkClient;
    }

    /**
     * 获取认证
     *
     * @return 认证列表
     */
    public List<ShellZKAuth> getAuths() {
        return this.getShellConnect().getAuths();
    }

    /**
     * 添加认证
     *
     * @param auth 认证
     */
    public void addAuth(ShellZKAuth auth) {
        this.getShellConnect().addAuth(auth);
    }

    /**
     * 移除认证
     *
     * @param auth 认证
     */
    public void removeAuth(ShellZKAuth auth) {
        if (CollectionUtil.isNotEmpty(this.getAuths())) {
            this.getAuths().remove(auth);
        }
    }

    /**
     * 获取启用的认证
     *
     * @return 启用的认证
     */
    public List<ShellZKAuth> getEnabledAuths() {
        if (CollectionUtil.isNotEmpty(this.getAuths())) {
            return this.getAuths().stream().filter(ShellZKAuth::isEnable).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
