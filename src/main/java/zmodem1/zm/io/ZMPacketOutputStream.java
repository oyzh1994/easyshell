package zmodem1.zm.io;

import zmodem1.xfer.io.ObjectOutputStream;
import zmodem1.xfer.util.ASCII;
import zmodem1.xfer.util.Buffer;
import zmodem1.xfer.zm.packet.DataPacket;
import zmodem1.xfer.zm.packet.Format;
import zmodem1.xfer.zm.packet.Header;
import zmodem1.xfer.zm.util.ZMPacket;
import zmodem1.xfer.zm.util.ZModemCharacter;

import java.io.IOException;
import java.io.OutputStream;


public class ZMPacketOutputStream extends ObjectOutputStream<ZMPacket> {

    private final OutputStream os;

    public ZMPacketOutputStream(OutputStream netOs) {
        os = netOs;
    }

    public void implWrite(byte b) throws IOException {
        //System.out.printf("%02x",b);
        os.write(b);
    }

    @Override
    public void write(ZMPacket o) throws IOException {
        Buffer buff = o.marshall();
        Format fmt = null;

        if (o instanceof Header)
            fmt = ((Header) o).format();


        if (fmt != null) {
            for (int i = 0; i < fmt.width(); i++)
                implWrite(ZModemCharacter.ZPAD.value());

            implWrite(ZModemCharacter.ZDLE.value());
            implWrite(fmt.character());
        }


        if (buff.hasRemaining()) {
            byte[] buf = new byte[buff.remaining()];
            buff.get(buf);
            os.write(buf);
        }

        if (fmt != null) if (fmt.hex()) {
            implWrite(ASCII.CR.value());
            implWrite(ASCII.LF.value());
            implWrite(ASCII.XON.value());
        }

        if (o instanceof DataPacket) if (((DataPacket) o).type() == ZModemCharacter.ZCRCW)
            implWrite(ASCII.XON.value());


        os.flush();

    }

}
