package org.mosh4j.core;

import org.mosh4j.terminal.StatefulAnsiRenderer;

import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Terminal frontend wrapper around {@link MoshClientSession}.
 * <p>
 * It combines receive/polling with a stateful ANSI renderer and provides a
 * queue of terminal-ready ANSI frames that can be consumed by external UIs.
 */
public final class MoshTerminalFrontend implements Closeable {
    private static final Logger LOG = Logger.getLogger(MoshTerminalFrontend.class.getName());

    private final MoshClientSession session;
    private final StatefulAnsiRenderer renderer;
    private final LinkedBlockingQueue<String> renderedOutputQueue;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile Thread receiveThread;

    public MoshTerminalFrontend(MoshClientSession session) {
        this(session, new StatefulAnsiRenderer(), 256);
    }

    public MoshTerminalFrontend(MoshClientSession session, StatefulAnsiRenderer renderer, int queueCapacity) {
        this.session = Objects.requireNonNull(session, "session");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.renderedOutputQueue = new LinkedBlockingQueue<>(Math.max(8, queueCapacity));
    }

    /**
     * Start background receive loop. Call {@link #takeRenderedOutput(long)} or
     * {@link #pollRenderedOutput()} to consume rendered ANSI frames.
     */
    public void start() {
        if (running.getAndSet(true)) {
            return;
        }
        receiveThread = new Thread(this::receiveLoop, "mosh4j-frontend-receive");
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    /**
     * Poll once without background thread. Useful for single-threaded integrations.
     *
     * @return true if at least one datagram was processed
     */
    public boolean pollOnce() {
        if (!session.isRunning()) {
            return false;
        }
        boolean progressed = session.receiveOnce();
        if (progressed) {
            enqueueRenderedFrame();
        }
        return progressed;
    }

    /**
     * Request initial server wake-up packet.
     */
    public void sendInitialWakeUp() {
        session.sendInitialWakeUp();
    }

    /**
     * Send user input bytes to the remote mosh-server.
     */
    public void sendUserInput(byte[] bytes) {
        session.sendUserInput(bytes);
    }

    /**
     * Send terminal resize event to remote mosh-server.
     */
    public void sendResize(int width, int height) {
        session.sendResize(width, height);
    }

    /**
     * Send a protocol heartbeat (ack-only) without user keystroke bytes.
     */
    public void sendHeartbeat() {
        session.sendHeartbeat();
    }

    /**
     * Non-blocking output poll.
     *
     * @return rendered ANSI frame or null if none available
     */
    public String pollRenderedOutput() {
        return renderedOutputQueue.poll();
    }

    /**
     * Blocking output poll with timeout.
     *
     * @return rendered ANSI frame or null on timeout
     */
    public String takeRenderedOutput(long timeoutMs) throws InterruptedException {
        return renderedOutputQueue.poll(Math.max(1, timeoutMs), TimeUnit.MILLISECONDS);
    }

    /**
     * Non-blocking poll for raw host byte chunks from the session.
     */
    public byte[] pollHostBytes() {
        return session.pollHostBytes();
    }

    /**
     * Blocking poll for raw host byte chunks from the session.
     */
    public byte[] takeHostBytes(long timeoutMs) throws InterruptedException {
        return session.takeHostBytes(timeoutMs);
    }

    /**
     * Number of queued rendered frames waiting for consumption.
     */
    public int pendingFrames() {
        return renderedOutputQueue.size();
    }

    /**
     * Returns true while frontend loop is running.
     */
    public boolean isRunning() {
        return running.get() && session.isRunning();
    }

    /**
     * Stop frontend loop and close underlying session.
     */
    @Override
    public void close() {
        running.set(false);
        Thread thread = receiveThread;
        receiveThread = null;
        if (thread != null) {
            thread.interrupt();
        }
        session.close();
    }

    private void receiveLoop() {
        try {
            while (running.get() && session.isRunning()) {
                try {
                    boolean progressed = session.receiveOnce();
                    if (progressed) {
                        enqueueRenderedFrame();
                    }
                } catch (Throwable t) {
                    // Keep frontend alive across transient network/runtime faults.
                    if (!running.get() || !session.isRunning()) {
                        break;
                    }
                    LOG.log(Level.FINE, "Transient receive-loop failure, continuing", t);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } finally {
            running.set(false);
        }
    }

    private void enqueueRenderedFrame() {
        String frame = renderer.render(session.getFramebuffer());
        if (frame == null || frame.isEmpty()) {
            return;
        }
        if (!renderedOutputQueue.offer(frame)) {
            renderedOutputQueue.poll();
            renderedOutputQueue.offer(frame);
        }
    }
}
