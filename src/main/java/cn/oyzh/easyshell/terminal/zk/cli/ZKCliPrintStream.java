package cn.oyzh.easyshell.terminal.zk.cli;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author oyzh
 * @since 2023/9/20
 */
public abstract class ZKCliPrintStream extends PrintStream {

    public ZKCliPrintStream() {
        super(OutputStream.nullOutputStream(), true);
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
            this.onResponse(x + "\n");
        }
    }

    @Override
    public void println(Object x) {
        if (x != null) {
            this.onResponse(x + "\n");
        }
    }

    @Override
    public void println(int x) {
        this.onResponse(x + "\n");
    }

    @Override
    public void println(long x) {
        this.onResponse(x + "\n");
    }

    @Override
    public void println(float x) {
        this.onResponse(x + "\n");
    }

    @Override
    public void println(double x) {
        this.onResponse(x + "\n");
    }

    /**
     * 响应处理
     *
     * @param str 内容
     */
    public abstract void onResponse(String str);
}
