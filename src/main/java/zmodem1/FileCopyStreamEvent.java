package zmodem1;

import org.apache.commons.net.io.CopyStreamEvent;

/**
 * 如果一共两个文件，并且传输第一个文件时：
 *
 * remaining = 1
 * index = 1
 */
public class FileCopyStreamEvent extends CopyStreamEvent {
    // 本次传输的文件名
    private final String filename;
    // 剩余未传输的文件数量
    private final int remaining;
    // 第几个文件
    private final int index;
    /**
     * 这个文件被跳过了
     */
    private final boolean skip;

    public FileCopyStreamEvent(
            Object source,
            String filename,
            int remaining,
            int index,
            long totalBytesTransferred,
            int bytesTransferred,
            long streamSize,
            boolean skip
    ) {
        super(source, totalBytesTransferred, bytesTransferred, streamSize);
        this.filename = filename;
        this.remaining = remaining;
        this.index = index;
        this.skip = skip;
    }

    // 构造函数重载，skip默认为false
    public FileCopyStreamEvent(
            Object source,
            String filename,
            int remaining,
            int index,
            long totalBytesTransferred,
            int bytesTransferred,
            long streamSize
    ) {
        this(source, filename, remaining, index, totalBytesTransferred, bytesTransferred, streamSize, false);
    }

    // Getter方法
    public String getFilename() {
        return filename;
    }

    public int getRemaining() {
        return remaining;
    }

    public int getIndex() {
        return index;
    }

    public boolean isSkip() {
        return skip;
    }
}