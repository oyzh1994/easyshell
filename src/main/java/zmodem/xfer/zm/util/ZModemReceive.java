package zmodem.xfer.zm.util;

import cn.oyzh.common.util.IOUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zmodem.FileCopyStreamEvent;
import zmodem.util.EmptyFileAdapter;
import zmodem.util.FileAdapter;
import zmodem.xfer.util.InvalidChecksumException;
import zmodem.xfer.zm.packet.Cancel;
import zmodem.xfer.zm.packet.DataPacket;
import zmodem.xfer.zm.packet.Finish;
import zmodem.xfer.zm.packet.Format;
import zmodem.xfer.zm.packet.Header;
import zmodem.zm.io.ZMPacketInputStream;
import zmodem.zm.io.ZMPacketOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;


public class ZModemReceive {

    private static final Logger log = LoggerFactory.getLogger(ZModemReceive.class);

    private final CopyStreamAdapter adapter = new CopyStreamAdapter();
    private final Supplier<FileAdapter> destinationSupplier;
    private FileAdapter destination;

    private FileAdapter file;
    private int fOffset = 0;
    private Long filesize;
    private int remaining = 0;
    private int index = 0;

    private OutputStream fileOs = null;

    private final InputStream netIs;
    private final OutputStream netOs;

    private enum Expect {
        FILENAME, DATA, NOTHING;
    }

    public ZModemReceive(Supplier<FileAdapter> destDir, InputStream netin, OutputStream netout) throws IOException {
        destinationSupplier = destDir;
        netIs = netin;
        netOs = netout;
    }

    private void open(int offset) throws IOException {
        boolean append = false;

        if (offset != 0) {
            if (file.exists() && file.length() == offset)
                append = true;
            else
                offset = 0;
        }

        IOUtil.closeQuietly(fileOs);

        fileOs = file.getOutputStream(append);
        fOffset = offset;

    }

    private void decodeFileNameData(DataPacket p) {
        ByteArrayOutputStream filename = new ByteArrayOutputStream();
        StringBuilder extract = new StringBuilder();
        byte[] data = p.data();
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            if (b == 0) {
                for (int j = i + 1; j < data.length; j++) {
                    b = data[j];
                    if (b == 0) {
                        break;
                    }
                    extract.append((char) b);
                }
                break;
            }
            filename.write(b);
        }

        final String[] segments = extract.toString().split(StringUtils.SPACE);
        if (ArrayUtils.isNotEmpty(segments)) {
            // filesize
            if (segments.length >= 1) {
                this.filesize = NumberUtils.toLong(segments[0]);
            }
            // remaining
            if (segments.length >= 5) {
                this.remaining = NumberUtils.toInt(segments[4]);
            }
        }

        file = destination.getChild(filename.toString());
        fOffset = 0;

        index++;

        adapter.bytesTransferred(new FileCopyStreamEvent(this, file.getName(), remaining - index, index,
                this.filesize, fOffset, 0, false));
    }

    public void addCopyStreamListener(CopyStreamListener listener) {
        adapter.addCopyStreamListener(listener);
    }

    public void removeCopyStreamListener(CopyStreamListener listener) {
        adapter.removeCopyStreamListener(listener);
    }

    private void writeData(DataPacket p) throws IOException {
        final byte[] data = p.data();

        fileOs.write(data);
        fOffset += data.length;

        // 开始传输
        adapter.bytesTransferred(new FileCopyStreamEvent(this, file.getName(), remaining, index,
                this.filesize, fOffset, 0, false));
    }

    private boolean initDestination() {
        if (destination != null) {
            return true;
        }
        destination = destinationSupplier.get();
        return !(destination instanceof EmptyFileAdapter);
    }

    public void receive(Supplier<Boolean> isCancelled) {
        ZMPacketInputStream is = new ZMPacketInputStream(netIs);
        ZMPacketOutputStream os = new ZMPacketOutputStream(netOs);

        Expect expect = Expect.NOTHING;

        byte[] recvOpt = {0, 4, 0, zmodem.xfer.zm.util.ZMOptions.with(zmodem.xfer.zm.util.ZMOptions.ESCCTL, ZMOptions.ESC8)};

        try {

            boolean end = false;
            int errorCount = 0;
            ZMPacket packet = null;
            while (!end) {
                try {
                    packet = is.read();
                } catch (InvalidChecksumException ice) {
                    if (log.isErrorEnabled()) {
                        log.error(ice.getMessage(), ice);
                    }
                    ++errorCount;
                    if (errorCount >= 3) {
                        os.write(new Cancel());
                        end = true;
                    }
                }

                if (packet instanceof Cancel) {
                    end = true;
                } else if (packet instanceof Finish) {
                    end = true;
                }

                if (isCancelled.get()) {
                    break;
                }

                // 如果重定向为空，则终止传输
                if (destination instanceof EmptyFileAdapter) {
                    os.write(new Cancel());
                    break;
                }

                if (packet instanceof Header header) {
                    switch (header.type()) {
                        case ZRQINIT:
                            os.write(new Header(Format.HEX, zmodem.xfer.zm.util.ZModemCharacter.ZRINIT, recvOpt));
                            break;
                        case ZFILE:
                            expect = Expect.FILENAME;
                            break;
                        case ZEOF:
                            os.write(new Header(Format.HEX, zmodem.xfer.zm.util.ZModemCharacter.ZRINIT, recvOpt));
                            expect = Expect.NOTHING;
                            file = null;
                            fileOs.flush();
                            IOUtil.closeQuietly(fileOs);
                            fileOs = null;
                            break;
                        case ZDATA:
                            open(header.getPos());
                            expect = Expect.DATA;
                            break;
                        case ZFIN:
                            os.write(new Header(Format.HEX, zmodem.xfer.zm.util.ZModemCharacter.ZFIN));
                            end = true;
                            break;
                        default:
                            end = true;
                            os.write(new Cancel());
                            break;
                    }
                }

                if (packet instanceof DataPacket data) {
                    switch (expect) {
                        case NOTHING:
                            os.write(new Header(Format.HEX, zmodem.xfer.zm.util.ZModemCharacter.ZRINIT, recvOpt));
                            break;
                        case FILENAME:
                            if (!initDestination()) {
                                end = true;
                                os.write(new Cancel());
                                break;
                            }
                            decodeFileNameData(data);
                            if (file.length() == filesize) {
                                os.write(new Header(Format.HEX, zmodem.xfer.zm.util.ZModemCharacter.ZSKIP));
                                adapter.bytesTransferred(new FileCopyStreamEvent(this, file.getName(), remaining, index,
                                        this.filesize, fOffset, 0, true));
                            } else {
                                os.write(new Header(Format.HEX, zmodem.xfer.zm.util.ZModemCharacter.ZRPOS, (int) file.length()));
                            }
                            expect = Expect.NOTHING;
                            break;
                        case DATA:
                            writeData(data);
                            switch (data.type()) {
                                case ZCRCW:
                                    expect = Expect.NOTHING;
                                case ZCRCQ:
                                    os.write(new Header(Format.HEX, ZModemCharacter.ZACK, fOffset));
                                    break;
                                case ZCRCE:
                                    expect = Expect.NOTHING;
                                    break;
                            }
                    }
                }
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        } finally {
            IOUtil.closeQuietly(fileOs);
        }

    }
}
