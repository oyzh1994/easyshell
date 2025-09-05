package cn.oyzh.easyshell.internal;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;

import java.nio.charset.Charset;

/**
 * 基础客户端
 *
 * @author oyzh
 * @since 2025-04-25
 */
public interface ShellBaseClient extends AutoCloseable {

    /**
     * 连接
     */
    default void start() throws Throwable {
        this.start(this.connectTimeout());
    }

    /**
     * 连接
     *
     * @param timeout 超时时间
     */
    void start(int timeout) throws Throwable;

    /**
     * 获取连接
     *
     * @return 连接
     */
    ShellConnect getShellConnect();

    /**
     * 获取连接名称
     *
     * @return 名称
     */
    default String connectName() {
        return this.getShellConnect().getName();
    }

    /**
     * 获取连接超时
     *
     * @return 连接超时
     */
    default int connectTimeout() {
        return this.getShellConnect().connectTimeOutMs();
    }

    /**
     * 获取字符集
     *
     * @return 字符集
     */
    default Charset getCharset() {
        String charset = this.getShellConnect().getCharset();
        if (StringUtil.isBlank(charset)) {
            return Charset.defaultCharset();
        }
        return Charset.forName(charset);
    }

    /**
     * 是否已关闭
     *
     * @return 结果
     */
    default boolean isClosed() {
        return !this.isConnected();
    }

    /**
     * 是否已连接
     *
     * @return 结果
     */
    boolean isConnected();

    /**
     * 连接状态属性
     *
     * @return 连接状态属性
     */
    ObjectProperty<ShellConnState> stateProperty();

    /**
     * 添加连接状态监听器
     *
     * @param stateListener 监听器
     */
    default void addStateListener(ChangeListener<ShellConnState> stateListener) {
        if (stateListener != null && this.stateProperty() != null) {
            this.stateProperty().addListener(stateListener);
        }
    }

    /**
     * 移除连接状态监听器
     *
     * @param stateListener 监听器
     */
    default void removeStateListener(ChangeListener<ShellConnState> stateListener) {
        if (stateListener != null && this.stateProperty() != null) {
            this.stateProperty().removeListener(stateListener);
        }
    }

    /**
     * 获取状态
     *
     * @return 状态
     */
    default ShellConnState getState() {
        return this.stateProperty() == null ? null : this.stateProperty().get();
    }

    /**
     * 状态变更事件
     *
     * @param state 状态
     */
    default void onStateChanged(ShellConnState state) {
        if (state == ShellConnState.CLOSED) {
            ShellEventUtil.connectionClosed(this);
        } else if (state == ShellConnState.CONNECTED) {
            ShellEventUtil.connectionConnected(this);
        }
    }

    /**
     * 检查状态
     */
    default void checkState() {
        ShellConnState state = this.getState();
        if (state == ShellConnState.CONNECTED && !this.isConnected()) {
            this.stateProperty().set(ShellConnState.INTERRUPT);
        }
    }

    /**
     * 部分场景下，例如上传、下载、删除、传输会占用客户端，可能需要fork一个子客户端去操作
     * 如果不需要fork，则直接返回自己即可
     * 如果fork失败，则建议返回自己
     *
     * @return fork出来的子客户端
     * @see #isForked() 配合这个方法这是个子客户端
     */
    default <T extends ShellBaseClient> T forkClient() throws Throwable {
        return (T) this;
    }

    /**
     * 是否子客户端
     *
     * @return 结果
     */
    default boolean isForked() {
        return false;
    }
}