package cn.oyzh.easyshell.rdp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.rdp.RdpView;
import com.tangluobo.rdp4j.frontend.FxRdpFrontend;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RDP 全屏键盘捕获。只转发当前选中 RDP 标签页在全屏状态下的键盘输入。
 *
 * @author oyzh
 * @since 2026-10-29
 */
final class ShellRDPFullScreenKeyboardCapture implements AutoCloseable {

    /**
     * F11 保留为本机进入/退出全屏的快捷键，不转发到远端。
     */
    private static final int SCANCODE_F11 = 0x57;

    private final RdpView view;

    private final FxRdpFrontend frontend;

    private final ShellWindowsKeyboardHook keyboardHook = ShellWindowsKeyboardHook.instance();

    private final ShellWindowsKeyboardHook.KeySink keySink = this::forwardKey;

    private final ChangeListener<Scene> sceneListener = (observable, oldScene, newScene) -> this.bindScene(newScene);

    private final ChangeListener<Window> windowListener = (observable, oldWindow, newWindow) -> this.bindWindow(newWindow);

    private final ChangeListener<Boolean> fullScreenListener = (observable, oldValue, newValue) -> this.refresh();

    private final ChangeListener<Boolean> focusedListener = (observable, oldValue, newValue) -> this.refresh();

    private final Set<Integer> nativePressedKeys = ConcurrentHashMap.newKeySet();

    private Scene scene;

    private Window window;

    private boolean active;

    private boolean closed;

    private boolean activationWarningLogged;

    ShellRDPFullScreenKeyboardCapture(RdpView view, FxRdpFrontend frontend) {
        this.view = view;
        this.frontend = frontend;
        this.keyboardHook.prepare();
        this.view.sceneProperty().addListener(this.sceneListener);
        this.bindScene(this.view.getScene());
    }

    void setActive(boolean active) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        this.refresh();
    }

    private void bindScene(Scene nextScene) {
        if (this.scene == nextScene) {
            return;
        }
        if (this.scene != null) {
            this.scene.windowProperty().removeListener(this.windowListener);
        }
        this.scene = nextScene;
        if (nextScene != null) {
            nextScene.windowProperty().addListener(this.windowListener);
        }
        this.bindWindow(nextScene == null ? null : nextScene.getWindow());
    }

    private void bindWindow(Window nextWindow) {
        if (this.window == nextWindow) {
            return;
        }
        if (this.window instanceof Stage oldStage) {
            oldStage.focusedProperty().removeListener(this.focusedListener);
            oldStage.fullScreenProperty().removeListener(this.fullScreenListener);
        }
        this.window = nextWindow;
        if (nextWindow instanceof Stage nextStage) {
            nextStage.focusedProperty().addListener(this.focusedListener);
            nextStage.fullScreenProperty().addListener(this.fullScreenListener);
        }
        this.refresh();
    }

    private void refresh() {
        if (this.closed) {
            return;
        }
        boolean capture = this.active
                && this.window instanceof Stage stage
                && stage.isFullScreen()
                && stage.isFocused();
        if (capture) {
            if (!this.keyboardHook.activate(this.keySink) && !this.activationWarningLogged) {
                this.activationWarningLogged = true;
                JulLog.warn("enable RDP full-screen keyboard capture failed.");
            }
        } else {
            this.releaseNativeKeys();
            this.keyboardHook.deactivate(this.keySink);
        }
    }

    private boolean forwardKey(int scanCode, boolean extended, boolean release) {
        if (scanCode == SCANCODE_F11 && !extended) {
            return false;
        }
        if (scanCode <= 0 || scanCode > 0xff) {
            return false;
        }
        int nativeKey = scanCode | (extended ? 0x8000 : 0);
        if (release) {
            this.nativePressedKeys.remove(nativeKey);
        } else {
            this.nativePressedKeys.add(nativeKey);
        }
        this.frontend.forwardNativeKey(scanCode, extended, release);
        return true;
    }

    private void releaseNativeKeys() {
        for (Integer nativeKey : this.nativePressedKeys.toArray(Integer[]::new)) {
            if (this.nativePressedKeys.remove(nativeKey)) {
                this.frontend.forwardNativeKey(nativeKey & 0xff, (nativeKey & 0x8000) != 0, true);
            }
        }
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        this.releaseNativeKeys();
        this.keyboardHook.deactivate(this.keySink);
        this.view.sceneProperty().removeListener(this.sceneListener);
        this.bindScene(null);
    }
}
