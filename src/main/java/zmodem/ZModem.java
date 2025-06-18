package zmodem;


import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;
import org.apache.commons.net.io.CopyStreamListener;
import zmodem.util.FileAdapter;
import zmodem.xfer.zm.util.ZModemReceive;
import zmodem.xfer.zm.util.ZModemSend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;


public class ZModem {

    private final InputStream netIs;
    private final OutputStream netOs;
    private final ShellSSHTtyConnector connector;
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);


    public ZModem(InputStream netin, OutputStream netout,ShellSSHTtyConnector connector) {
        netIs = netin;
        netOs = netout;
        this.connector=connector;
    }

    public void receive(Supplier<FileAdapter> destDir, CopyStreamListener listener) throws IOException {
        ZModemReceive sender = new ZModemReceive(destDir, netIs, netOs);
        sender.addCopyStreamListener(listener);
        sender.receive(isCancelled::get);
        netOs.flush();
    }

    public void send(Supplier<List<FileAdapter>> filesSupplier, CopyStreamListener listener) throws Exception {
        ZModemSend sender = new ZModemSend(filesSupplier, netIs, netOs);
        sender.addCopyStreamListener(listener);
        sender.send(isCancelled::get);
        ThreadUtil.sleep(300);
        netOs.flush();
        ThreadUtil.sleep(300);

        // connector.reset();
    }

    public void cancel() {
        isCancelled.compareAndSet(false, true);
    }

    public InputStream getNetIs() {
        return netIs;
    }

    public OutputStream getNetOs() {
        return netOs;
    }

    public AtomicBoolean getIsCancelled() {
        return isCancelled;
    }
}
