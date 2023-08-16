package cn.oyzh.easyssh.ssh;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.easyssh.domain.SSHInfo;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * ssh终端
 *
 * @author oyzh
 * @since 2023/08/16
 */
@Slf4j
public class SSHClient {

    /**
     * JSch对象
     */
    private static final JSch JSCH = new JSch();

    /**
     * ssh信息
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    private final SSHInfo sshInfo;

    private Session session;

    /**
     * 连接状态
     */
    private ReadOnlyObjectWrapper<SSHConnState> connState;

    /**
     * 连接状态监听器列表
     */
    private final List<ChangeListener<SSHConnState>> connStateListeners = new ArrayList<>();

    public SSHClient(@NonNull SSHInfo sshInfo) {
        this.sshInfo = sshInfo;
    }

    /**
     * 连接状态属性
     *
     * @return 连接状态属性
     */
    private ReadOnlyObjectWrapper<SSHConnState> connState() {
        if (this.connState == null) {
            this.connState = new ReadOnlyObjectWrapper<>(SSHConnState.NOT_INITIALIZED);
        }
        return this.connState;
    }

    /**
     * 连接状态属性
     *
     * @return 连接状态属性
     */
    private ReadOnlyObjectProperty<SSHConnState> connStateProperty() {
        return this.connState().getReadOnlyProperty();
    }

    /**
     * 添加连接状态监听器
     *
     * @param listener 监听器
     */
    public void addConnStateListener(@NonNull ChangeListener<SSHConnState> listener) {
        if (!this.connStateListeners.contains(listener)) {
            this.connStateListeners.add(listener);
            this.connStateProperty().addListener(listener);
        }
    }

    /**
     * 移除连接状态监听器
     *
     * @param listener 监听器
     */
    public void removeConnStateListener(ChangeListener<SSHConnState> listener) {
        if (listener != null) {
            this.connStateListeners.remove(listener);
            this.connStateProperty().removeListener(listener);
        }
    }

    /**
     * 初始化客户端
     */
    private void initClient() throws JSchException {
        if (log.isInfoEnabled()) {
            log.info("initClient user:{} password:{} host:{}", this.sshInfo.getUser(), this.sshInfo.getPassword(), this.sshInfo.getHost());
        }
        // 创建会话
        this.session = JSCH.getSession(this.sshInfo.getUser(), this.sshInfo.hostIp(), this.sshInfo.hostPort());
        // 主机密码
        if (StrUtil.isNotBlank(this.sshInfo.getPassword())) {
            this.session.setPassword(this.sshInfo.getPassword());
        }
        // 去掉首次连接确认
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        this.session.setConfig(config);
        // 超时连接时间为3秒
        this.session.setTimeout(this.sshInfo.connectTimeOutMs());
    }

    /**
     * 关闭客户端
     */
    public void close() {
        try {
            if (this.session != null && this.session.isConnected()) {
                this.session.disconnect();
                this.connState().set(SSHConnState.CLOSED);
            }
            this.session = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 重置客户端
     */
    public void reset() {
        // 移除监听器
        if (!this.connStateListeners.isEmpty()) {
            for (ChangeListener<SSHConnState> listener : connStateListeners) {
                this.connStateProperty().removeListener(listener);
            }
            this.connStateListeners.clear();
        }
        this.close();
        this.connState().set(SSHConnState.NOT_INITIALIZED);
    }

    /**
     * 开始连接客户端
     */
    public void start() throws Exception {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        try {
            // 关闭旧连接
            this.close();
            // 初始化连接池
            this.connState().set(SSHConnState.CONNECTING);
            // 初始化客户端
            this.initClient();
            // 执行连接
            this.session.connect();
            // 判断连接结果
            if (this.session.isConnected()) {
                this.connState().set(SSHConnState.CONNECTED);
            } else {
                this.connState().set(SSHConnState.FAILED);
            }
        } catch (Exception ex) {
            this.connState().set(SSHConnState.FAILED);
            throw ex;
        }
    }

    /**
     * 是否连接中
     *
     * @return 结果
     */
    public boolean isConnecting() {
        if (!this.isClosed()) {
            return this.connState().get() == SSHConnState.CONNECTING;
        }
        return false;
    }

    /**
     * 是否已连接
     *
     * @return 结果
     */
    public boolean isConnected() {
        if (!this.isClosed()) {
            return this.connState().get().isConnected();
        }
        return false;
    }

    /**
     * 是否已关闭
     *
     * @return 结果
     */
    public boolean isClosed() {
        return this.session == null || !this.session.isConnected() || !this.connState().get().isConnected();
    }

    /**
     * 当前连接名称
     *
     * @return 名称
     */
    public String infoName() {
        return this.sshInfo.getName();
    }

    // /**
    //  * 执行命令
    //  *
    //  * @param command 命令
    //  * @return 命令执行结果
    //  */
    // public SSHExecResult exec(@NonNull String command) throws Exception {
    //     if (!this.isConnected()) {
    //         return null;
    //     }
    //     ChannelExec exec = null;
    //     try {
    //         // 执行连接
    //         exec = (ChannelExec) this.session.openChannel("exec");
    //         exec.setCommand(command);
    //         exec.setInputStream(null);
    //         exec.connect(this.sshInfo.connectTimeOutMs());
    //         // 获取命令执行结果
    //         return SSHConnectUtil.readExecInput(exec);
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         throw ex;
    //     } finally {
    //         if (exec != null) {
    //             exec.disconnect();
    //         }
    //     }
    // }

    private SSHShell shell;

    /**
     * 获取交互式终端
     *
     * @return 交互式终端
     */
    public SSHShell shell() throws Exception {
        if (!this.isConnected()) {
            return null;
        }
        try {
            // 执行连接
            if (this.shell == null) {
                ChannelShell shell = (ChannelShell) this.session.openChannel("shell");
                this.shell = new SSHShell(shell);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return this.shell;
    }

}
