package org.mosh4j.transport;

import com.google.protobuf.CodedInputStream;
import org.mosh4j.protocol.transport.Transportinstruction;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * Wrapper for SSP transport Instruction (TransportBuffers.Instruction): parse and serialize.
 * Matches native C++ mosh wire format: protocol_version=2, random chaff bytes.
 */
public final class TransportInstruction {

    public static final int MOSH_PROTOCOL_VERSION = 2;
    private static final int MAX_FRAGMENT_SIZE = 1400;
    private static final int MAX_CHAFF_BYTES = 16;
    /** Upper bound on a serialized transport instruction; mirrors the fragment-layer decompression cap. */
    private static final int MAX_INSTRUCTION_BYTES = 16 * 1024 * 1024;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static int getMaxFragmentSize() {
        return MAX_FRAGMENT_SIZE;
    }

    public static Transportinstruction.Instruction parse(byte[] bytes) throws IOException {
        Objects.requireNonNull(bytes, "bytes");
        if (bytes.length > MAX_INSTRUCTION_BYTES) {
            throw new IOException("Transport instruction exceeds limit of " + MAX_INSTRUCTION_BYTES + " bytes");
        }
        return Transportinstruction.Instruction.parseFrom(bytes);
    }

    public static Transportinstruction.Instruction parseFrom(InputStream in) throws IOException {
        CodedInputStream cis = CodedInputStream.newInstance(in);
        cis.setSizeLimit(MAX_INSTRUCTION_BYTES);
        return Transportinstruction.Instruction.parseFrom(cis);
    }

    public static byte[] toBytes(Transportinstruction.Instruction instruction) {
        Objects.requireNonNull(instruction, "instruction");
        return instruction.toByteArray();
    }

    /**
     * Build an instruction with protocol_version=2 and random chaff bytes,
     * matching the native C++ mosh wire format.
     */
    public static Transportinstruction.Instruction create(
            long oldNum,
            long newNum,
            long ackNum,
            long throwawayNum,
            byte[] diff) {
        Transportinstruction.Instruction.Builder b = Transportinstruction.Instruction.newBuilder();
        b.setProtocolVersion(MOSH_PROTOCOL_VERSION);
        b.setOldNum(oldNum);
        b.setNewNum(newNum);
        b.setAckNum(ackNum);
        b.setThrowawayNum(throwawayNum);
        if (diff != null && diff.length > 0) {
            b.setDiff(com.google.protobuf.ByteString.copyFrom(diff));
        }
        byte[] chaff = new byte[RANDOM.nextInt(MAX_CHAFF_BYTES + 1)];
        if (chaff.length > 0) {
            RANDOM.nextBytes(chaff);
            b.setChaff(com.google.protobuf.ByteString.copyFrom(chaff));
        }
        return b.build();
    }

    /**
     * Build a trial/ack-only instruction (empty diff).
     */
    public static Transportinstruction.Instruction createAckOnly(long ackNum, long throwawayNum) {
        return create(0, 0, ackNum, throwawayNum, null);
    }

    /**
     * Build an ack-only instruction without changing sender state numbering.
     * This avoids state rollback (old/new jumping back to 0) after data packets
     * were already sent.
     */
    public static Transportinstruction.Instruction createAckOnly(
            long oldNum,
            long newNum,
            long ackNum,
            long throwawayNum) {
        return create(oldNum, newNum, ackNum, throwawayNum, null);
    }

    /**
     * Validate that a received instruction uses the expected protocol version.
     */
    public static boolean isProtocolVersionValid(Transportinstruction.Instruction inst) {
        Objects.requireNonNull(inst, "instruction");
        return inst.hasProtocolVersion() && inst.getProtocolVersion() == MOSH_PROTOCOL_VERSION;
    }
}
