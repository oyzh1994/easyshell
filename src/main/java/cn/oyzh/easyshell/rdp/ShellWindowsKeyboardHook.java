package cn.oyzh.easyshell.rdp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.ptr.IntByReference;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Windows 低级键盘钩子。钩子只在当前进程为前台进程且存在活动接收器时拦截按键。
 *
 * @author oyzh
 * @since 2026-10-29
 */
final class ShellWindowsKeyboardHook {

    /**
     * 按键接收器，返回 true 表示按键已被消费，不再传给本机系统。
     */
    @FunctionalInterface
    interface KeySink {

        boolean forward(int scanCode, boolean extended, boolean release);
    }

    private static final ShellWindowsKeyboardHook INSTANCE = new ShellWindowsKeyboardHook();

    private static final int WH_KEYBOARD_LL = 13;

    private static final int WM_KEYDOWN = 0x0100;

    private static final int WM_KEYUP = 0x0101;

    private static final int WM_SYSKEYDOWN = 0x0104;

    private static final int WM_SYSKEYUP = 0x0105;

    private static final int LLKHF_EXTENDED = 0x01;

    private final AtomicReference<KeySink> activeSink = new AtomicReference<>();

    private final AtomicBoolean startRequested = new AtomicBoolean();

    private final CountDownLatch started = new CountDownLatch(1);

    private volatile boolean available;

    private volatile HHOOK hook;

    private LowLevelKeyboardProc callback;

    private ShellWindowsKeyboardHook() {
    }

    static ShellWindowsKeyboardHook instance() {
        return INSTANCE;
    }

    /**
     * 提前启动钩子线程，避免首次进入全屏时阻塞 JavaFX 线程。
     */
    void prepare() {
        this.ensureStarted(0);
    }

    boolean activate(KeySink sink) {
        if (!OSUtil.isWindows() || sink == null || !this.ensureStarted(500)) {
            return false;
        }
        this.activeSink.set(sink);
        return true;
    }

    void deactivate(KeySink sink) {
        if (sink != null) {
            this.activeSink.compareAndSet(sink, null);
        }
    }

    private boolean ensureStarted(long waitMillis) {
        if (!OSUtil.isWindows()) {
            return false;
        }
        if (this.available) {
            return true;
        }
        if (this.startRequested.compareAndSet(false, true)) {
            Thread thread = new Thread(this::runHookLoop, "easyshell-rdp-keyboard-hook");
            thread.setDaemon(true);
            thread.start();
        }
        if (waitMillis <= 0) {
            return this.available;
        }
        try {
            this.started.await(waitMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return this.available;
    }

    private void runHookLoop() {
        try {
            this.callback = this::handleKey;
            this.hook = User32.INSTANCE.SetWindowsHookEx(WH_KEYBOARD_LL, this.callback,
                    Kernel32.INSTANCE.GetModuleHandle(null), 0);
            this.available = this.hook != null;
            this.started.countDown();
            if (!this.available) {
                JulLog.warn("install Windows full-screen keyboard hook failed.");
                return;
            }
            JulLog.info("Windows full-screen keyboard hook installed.");
            MSG message = new MSG();
            while (User32.INSTANCE.GetMessage(message, null, 0, 0) > 0) {
                User32.INSTANCE.TranslateMessage(message);
                User32.INSTANCE.DispatchMessage(message);
            }
        } catch (Throwable ex) {
            this.available = false;
            JulLog.warn("Windows full-screen keyboard hook error.", ex);
        } finally {
            this.started.countDown();
            this.available = false;
            HHOOK installed = this.hook;
            if (installed != null) {
                User32.INSTANCE.UnhookWindowsHookEx(installed);
                this.hook = null;
            }
        }
    }

    private LRESULT handleKey(int code, WPARAM message, KBDLLHOOKSTRUCT data) {
        HHOOK installed = this.hook;
        LPARAM dataPointer = data == null ? null
                : new LPARAM(Pointer.nativeValue(data.getPointer()));
        if (code < 0 || data == null) {
            return User32.INSTANCE.CallNextHookEx(installed, code, message, dataPointer);
        }

        KeySink sink = this.activeSink.get();
        int messageId = message.intValue();
        boolean release = messageId == WM_KEYUP || messageId == WM_SYSKEYUP;
        boolean keyboardMessage = release || messageId == WM_KEYDOWN || messageId == WM_SYSKEYDOWN;
        if (sink == null || !keyboardMessage || !this.isThisProcessInForeground()) {
            return User32.INSTANCE.CallNextHookEx(installed, code, message, dataPointer);
        }

        boolean extended = (data.flags & LLKHF_EXTENDED) != 0;
        boolean forwarded;
        try {
            forwarded = sink.forward(data.scanCode, extended, release);
        } catch (Throwable ex) {
            JulLog.warn("forward full-screen keyboard event failed.", ex);
            forwarded = false;
        }
        if (!forwarded) {
            return User32.INSTANCE.CallNextHookEx(installed, code, message, dataPointer);
        }
        return new LRESULT(1);
    }

    private boolean isThisProcessInForeground() {
        HWND foreground = User32.INSTANCE.GetForegroundWindow();
        if (foreground == null) {
            return false;
        }
        IntByReference processId = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(foreground, processId);
        return processId.getValue() == Kernel32.INSTANCE.GetCurrentProcessId();
    }
}
