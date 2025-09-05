package cn.oyzh.easyshell.handler.redis;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2024/08/29
 */
public class ShellRedisDataHandler {

    /**
     * 中断标志位
     */
    protected AtomicBoolean interrupt;

    /**
     * 消息处理
     */
    private Consumer<String> messageHandler;

    public Consumer<String> getMessageHandler() {
        return messageHandler;
    }

    public void setMessageHandler(Consumer<String> messageHandler) {
        this.messageHandler = messageHandler;
    }

    public Consumer<Integer> getProcessedHandler() {
        return processedHandler;
    }

    public void setProcessedHandler(Consumer<Integer> processedHandler) {
        this.processedHandler = processedHandler;
    }

    /**
     * 进度处理
     */
    private Consumer<Integer> processedHandler;

    /**
     * 设置中断
     *
     * @param interrupt 中断标志位
     */
    public void interrupt(boolean interrupt) {
        if (this.interrupt == null) {
            this.interrupt = new AtomicBoolean(interrupt);
        } else {
            this.interrupt.set(interrupt);
        }
    }

    /**
     * 中断
     */
    public void interrupt() {
        this.interrupt(true);
    }

    /**
     * 检查中断，如果中断则抛出中断异常
     *
     * @throws InterruptedException 中断异常
     */
    protected void checkInterrupt() throws InterruptedException {
        if (this.interrupt != null && this.interrupt.get()) {
            throw new InterruptedException();
        }
    }

    /**
     * 发送异常
     *
     * @param ex 异常
     */
    protected void exception(Exception ex) throws Exception {
        if (ex instanceof InterruptedException) {
            throw ex;
        }
        if (this.messageHandler != null) {
            this.messageHandler.accept(ex.getMessage());
        } else {
            ex.printStackTrace();
        }
    }

    /**
     * 发送消息
     *
     * @param message 消息
     */
    protected void message(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.accept(message);
        }
    }

    /**
     * 更新忽略进度
     *
     */
    protected void processedSkip( ) {
      this.processed(0);
    }

    /**
     * 更新进度
     *
     * @param processed 进度
     */
    protected void processed(int processed) {
        if (this.processedHandler != null) {
            this.processedHandler.accept(processed);
        }
    }

    /**
     * 递增进度
     */
    protected void processedIncr() {
        this.processedIncr(1);
    }

    /**
     * 递增进度
     */
    protected void processedIncr(int incr) {
        if (incr < 0) {
            incr = Math.abs(incr);
        }
        this.processed(incr);
    }

    /**
     * 递减进度
     */
    protected void processedDecr() {
        this.processedDecr(-1);
    }

    /**
     * 递减进度
     */
    protected void processedDecr(int decr) {
        if (decr > 0) {
            this.processed(-decr);
        } else {
            this.processed(decr);
        }
    }
}
