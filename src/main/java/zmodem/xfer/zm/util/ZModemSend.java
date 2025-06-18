package zmodem.xfer.zm.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zmodem.FileCopyStreamEvent;
import zmodem.util.FileAdapter;
import zmodem.xfer.util.ASCII;
import zmodem.xfer.util.InvalidChecksumException;
import zmodem.xfer.zm.packet.Cancel;
import zmodem.xfer.zm.packet.DataPacket;
import zmodem.xfer.zm.packet.Finish;
import zmodem.xfer.zm.packet.Format;
import zmodem.xfer.zm.packet.Header;
import zmodem.zm.io.ZMPacketInputStream;
import zmodem.zm.io.ZMPacketOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;


public class ZModemSend {

    private static final int packLen = 1024 * 8;
    private static final Logger log = LoggerFactory.getLogger(ZModemSend.class);

    private final byte[] data = new byte[packLen];
    private final CopyStreamAdapter adapter = new CopyStreamAdapter();
    private final Supplier<List<FileAdapter>> destinationSupplier;
    private final InputStream netIs;
    private final OutputStream netOs;

    private List<FileAdapter> files;
    private Iterator<FileAdapter> iter;

    private FileAdapter file;
    private int fOffset = 0;
    private int index = 0;
    private int filesize = 0;
    private boolean atEof = false;
    private InputStream fileIs;


    public ZModemSend(Supplier<List<FileAdapter>> destinationSupplier, InputStream netin, OutputStream netout) throws IOException {
        this.destinationSupplier = destinationSupplier;
        netIs = netin;
        netOs = netout;
    }

    public boolean nextFile() throws IOException {

        IOUtils.closeQuietly(fileIs);

        if (files == null) {
            files = destinationSupplier.get();
            iter = files.iterator();
        }

        if (!iter.hasNext())
            return false;


        file = iter.next();
        fileIs = file.getInputStream();
        filesize = fileIs.available();
        fOffset = 0;
        atEof = false;
        index++;

        return true;
    }


    public void addCopyStreamListener(CopyStreamListener listener) {
        adapter.addCopyStreamListener(listener);
    }

    public void removeCopyStreamListener(CopyStreamListener listener) {
        adapter.removeCopyStreamListener(listener);
    }


    private void position(int offset) throws IOException {
        if (offset != fOffset) {
            fileIs.skipNBytes(offset);
            fOffset = offset;
        }
    }

    private byte[] getNextBlock() throws IOException {
        final int len = fileIs.read(data);

        /* we know it is a file: all the data is locally available.*/
        if (len < data.length)
            atEof = true;
        else if (fileIs.available() == 0)
            atEof = true;

        if (len == -1) {
            return null;
        }

        fOffset += len;

        if (len != data.length)
            return ArrayUtils.subarray(data, 0, len);
        else
            return data;
    }

    private DataPacket getNextDataPacket() throws IOException {
        byte[] data = getNextBlock();

        ZModemCharacter fe = ZModemCharacter.ZCRCW;
        if (atEof) {
            fe = ZModemCharacter.ZCRCE;
            fileIs.close();
        }

        if (data == null) {
            return new DataPacket(fe);
        }

        return new DataPacket(fe, data);
    }

    public void send(Supplier<Boolean> isCancelled) {
        ZMPacketFactory factory = new ZMPacketFactory();

        ZMPacketInputStream is = new ZMPacketInputStream(netIs);
        ZMPacketOutputStream os = new ZMPacketOutputStream(netOs);


        try {

            boolean end = false;
            int errorCount = 0;
            ZMPacket packet = null;

            while (!end) {
                try {
                    packet = is.read();
                    System.out.println(packet);
                    // System.out.println("------------");
                } catch (InvalidChecksumException ice) {
                    ++errorCount;
                    if (errorCount > 20) {
                        os.write(new Cancel());
                        end = true;
                    }
                }

                if (packet instanceof Cancel) {
                    end = true;
                } else if (isCancelled.get()) {
                    os.write(new Cancel());
                    continue;
                }

                if (packet instanceof Header header) {
                    switch (header.type()) {
                        case ZSKIP:
                            fireBytesTransferred(true);
                        case ZRINIT:
                            if (!nextFile()) {
                                os.write(new Header(Format.BIN, ZModemCharacter.ZFIN));
                            } else {
                                os.write(new Header(Format.BIN, ZModemCharacter.ZFILE, new byte[]{0, 0, 0, ZMOptions.with(ZMOptions.ZCBIN)}));
                                os.write(factory.createZFilePacket(file.getName(), filesize));
                                fireBytesTransferred(false);
                            }
                            break;
                        case ZRPOS:
                            if (!atEof)
                                position(header.getPos());
                        case ZACK:
                            os.write(new Header(Format.BIN, ZModemCharacter.ZDATA, fOffset));
                            os.write(getNextDataPacket());
                            if (atEof) {
                                os.write(new Header(Format.HEX, ZModemCharacter.ZEOF, fOffset));
                            }
                            fireBytesTransferred(false);
                            break;
                        case ZFIN:
                            end = true;
                            os.write(new Finish());
                            break;
                        default:
                            end = true;
                            os.write(new Cancel());
                            netOs.write(ASCII.CR.value());
                            netOs.write(ASCII.LF.value());
                            netOs.write(ASCII.XON.value());
                            break;
                    }

                }
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        } finally {
            IOUtils.closeQuietly(fileIs);
        }

    }

    private void fireBytesTransferred(boolean skip) {
        if (this.filesize == fOffset) {
            System.out.println();
        }
        adapter.bytesTransferred(new FileCopyStreamEvent(this, file.getName(), files.size() - index + 1, index,
                this.filesize, fOffset, 0, skip));
    }
}
