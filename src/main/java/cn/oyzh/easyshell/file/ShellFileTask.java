package cn.oyzh.easyshell.file;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.Competitor;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 文件任务
 *
 * @author oyzh
 * @since 2025-06-26
 */
public class ShellFileTask {

    /**
     * 工作线程
     */
    protected Thread worker;

    /**
     * 错误
     */
    protected Exception error;

    /**
     * 竞争器
     */
    protected final Competitor competitor;

    /**
     * 状态
     */
    protected transient ShellFileStatus status;

    public ShellFileTask(Competitor competitor) {
        this.competitor = competitor;
    }

    /**
     * 取消
     */
    public void cancel() {
        this.error = null;
        this.competitor.release(this);
        this.updateStatus(ShellFileStatus.CANCELED);
        ThreadUtil.interrupt(this.worker);
    }

    /**
     * 更新状态
     *
     * @param status 状态
     */
    protected void updateStatus(ShellFileStatus status) {
        this.status = status;
        JulLog.debug("status: {}", status);
    }

    /**
     * 是否失败
     *
     * @return 结果
     */
    public boolean isFailed() {
        return this.status == ShellFileStatus.FAILED;
    }

    /**
     * 是否取消
     *
     * @return 结果
     */
    public boolean isCanceled() {
        return this.status == ShellFileStatus.CANCELED;
    }

    /**
     * 获取错误信息
     *
     * @return 错误信息
     */
    public String getErrorMsg() {
        if (this.error == null) {
            return "";
        }
        return this.error.getMessage();
    }
}
