package zmodem1.xfer.zm.packet;


import zmodem1.xfer.util.Buffer;
import zmodem1.xfer.util.ByteBuffer;
import zmodem1.xfer.zm.util.ZMPacket;

public class Finish extends ZMPacket {

    @Override
    public Buffer marshall() {
        ByteBuffer buff = ByteBuffer.allocate(16);

        for (int i = 0; i < 2; i++)
            buff.put((byte) 'O');

        buff.flip();

        return buff;
    }

    @Override
    public String toString() {
        return "Finish: OO";
    }

}
