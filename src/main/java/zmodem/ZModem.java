package zmodem;


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
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);

    public ZModem(InputStream netin, OutputStream netout) {
        netIs = netin;
        netOs = netout;
    }

    public void receive(Supplier<FileAdapter> dstDir, CopyStreamListener listener) throws IOException {
        ZModemReceive sender = new ZModemReceive(dstDir, netIs, netOs);
        sender.addCopyStreamListener(listener);
        sender.receive(this.isCancelled::get);
        this.netOs.write("\r".getBytes());
        this.netOs.flush();
        // this.connector.resetTtyConnector();
    }

    public void send(Supplier<List<FileAdapter>> filesSupplier, CopyStreamListener listener) throws Exception {
        ZModemSend sender = new ZModemSend(filesSupplier, netIs, netOs);
        sender.addCopyStreamListener(listener);
        sender.send(this.isCancelled::get);
        this.netOs.write("\r".getBytes());
        this.netOs.flush();
        // this.connector.resetTtyConnector();
    }

    public void cancel() throws IOException {
        this.isCancelled.compareAndSet(false, true);
        // this.netOs.write("\r".getBytes());
        // this.netOs.flush();
        // this.connector.resetTtyConnector();
    }
}
