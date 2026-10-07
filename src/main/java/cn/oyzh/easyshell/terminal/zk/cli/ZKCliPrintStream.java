package cn.oyzh.easyshell.terminal.zk.cli;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * cli打印流
 *
 * @author oyzh
 * @since 2023/9/20
 */
public abstract class ZKCliPrintStream extends PrintStream {

    /**
     * 行结束文本
     */
    private final String lineEndingText;

    /**
     * 构造方法
     *
     * @param lineEndingText 行结束文本
     */
    public ZKCliPrintStream(String lineEndingText) {
        super(OutputStream.nullOutputStream(), true);
        this.lineEndingText = lineEndingText;
    }

    @Override
    public void print(String str) {
        if (str != null) {
            this.onResponse(str);
        }
    }

    @Override
    public void print(int x) {
        this.onResponse(String.valueOf(x));
    }

    @Override
    public void print(long x) {
        this.onResponse(String.valueOf(x));
    }

    @Override
    public void print(float x) {
        this.onResponse(String.valueOf(x));
    }

    @Override
    public void print(double x) {
        this.onResponse(String.valueOf(x));
    }

    @Override
    public void print(char x) {
        this.onResponse(String.valueOf(x));
    }

    @Override
    public void println(String x) {
        if (x != null) {
            this.onResponse(x + lineEndingText);
        }
    }

    @Override
    public void println(Object x) {
        if (x != null) {
            this.onResponse(x + lineEndingText);
        }
    }

    @Override
    public void println(int x) {
        this.onResponse(x + lineEndingText);
    }

    @Override
    public void println(long x) {
        this.onResponse(x + lineEndingText);
    }

    @Override
    public void println(float x) {
        this.onResponse(x + lineEndingText);
    }

    @Override
    public void println(double x) {
        this.onResponse(x + lineEndingText);
    }

    /**
     * 响应处理
     *
     * @param str 内容
     */
    public abstract void onResponse(String str);
}
