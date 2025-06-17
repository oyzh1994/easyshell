package zmodem1.xfer.zm.packet;


import zmodem1.xfer.util.ASCII;
import zmodem1.xfer.util.Buffer;
import zmodem1.xfer.util.ByteBuffer;
import zmodem1.xfer.zm.util.ZMPacket;

public class Cancel extends ZMPacket {

    @Override
    public Buffer marshall() {
        ByteBuffer buff = ByteBuffer.allocate(16);

        for (int i = 0; i < 8; i++)
            buff.put(ASCII.CAN.value());
        for (int i = 0; i < 8; i++)
            buff.put(ASCII.BS.value());

        buff.flip();

        return buff;
    }

    @Override
    public String toString() {
        return "Cancel: CAN * 8 + BS * 8";
    }
}
