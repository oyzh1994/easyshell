package cn.oyzh.easyshell.local;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.BaseClient;
import cn.oyzh.easyshell.internal.ShellConnState;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2025-06-11
 */
public class ShellLocalClient implements BaseClient {

    /**
     * 连接
     */
    private final ShellConnect shellConnect;

    /**
     * 连接状态
     */
    private final ReadOnlyObjectWrapper<ShellConnState> state = new ReadOnlyObjectWrapper<>();

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> BaseClient.super.onStateChanged(state3);

    @Override
    public ReadOnlyObjectProperty<ShellConnState> stateProperty() {
        return this.state.getReadOnlyProperty();
    }

    public ShellLocalClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.addStateListener(this.stateListener);
    }

    @Override
    public void start(int timeout) throws IOException {
        if (this.isConnected()) {
            return;
        }
        try {
            this.state.set(ShellConnState.CONNECTING);
                this.state.set(ShellConnState.CONNECTED);
        } catch (Throwable ex) {
            ex.printStackTrace();
            this.state.set(ShellConnState.FAILED);
            throw ex;
        } finally {
            // 执行一次gc，快速回收内存
            SystemUtil.gc();
        }
    }

    @Override
    public void close() {
        try {
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
