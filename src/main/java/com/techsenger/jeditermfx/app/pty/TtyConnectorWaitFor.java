package com.techsenger.jeditermfx.app.pty;

import cn.oyzh.common.log.JulLog;
import com.techsenger.jeditermfx.terminal.TtyConnector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.function.IntConsumer;

public class TtyConnectorWaitFor {

    public TtyConnectorWaitFor(@NotNull TtyConnector ttyConnector, @NotNull ExecutorService executor,
                               @NotNull IntConsumer terminationCallback) {
        executor.submit(() -> {
            int exitCode = 0;
            try {
                while (true) {
                    try {
                        exitCode = ttyConnector.waitFor();
                        break;
                    } catch (InterruptedException e) {
                        JulLog.debug("", e);
                    }
                }
            } finally {
                terminationCallback.accept(exitCode);
            }
        });
    }
}
