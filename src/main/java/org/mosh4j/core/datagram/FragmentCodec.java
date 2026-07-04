package org.mosh4j.core.datagram;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Handles the mosh fragment layer that sits between the transport instruction
 * (protobuf) and the encrypted datagram payload. Matches the C++ implementation
 * in src/network/network.cc.
 *
 * Wire format per fragment payload (after timestamp header, before encryption):
 * <pre>
 *   [8B] instruction_id   (uint64 big-endian)
 *   [2B] fragment_num      bits 0-14: fragment index, bit 15: final flag
 *   [NB] chunk             portion of zlib-compressed protobuf
 * </pre>
 */
public final class FragmentCodec {

    private static final int HEADER_SIZE = 10;
    private static final int FINAL_FLAG = 0x8000;
    private static final int MAX_FRAGMENT_COUNT = 0x8000;
    private static final int MAX_FRAGMENT_INDEX = 0x7FFF;

    /**
     * Upper bound on the size of a single reassembled/decompressed transport instruction.
     * Bounds both the buffered compressed fragments and the zlib output so a small
     * malicious payload cannot expand to exhaust the heap (decompression bomb). A mosh
     * terminal state/diff is far smaller than this; 16 MiB leaves generous headroom.
     */
    public static final int MAX_DECOMPRESSED_BYTES = 16 * 1024 * 1024;

    private final ConcurrentHashMap<Long, FragmentAssembly> assemblies = new ConcurrentHashMap<>();

    /**
     * Encode a serialized protobuf instruction into one or more fragment payloads.
     * Each returned byte[] is ready to be used as the payload in SspDatagramCodec.encode().
     *
     * @param instructionId unique id for this instruction (reuse for retransmission)
     * @param protobufBytes serialized TransportBuffers.Instruction
     * @param mtu           max payload size per fragment (excluding timestamps/nonce/tag)
     * @throws IllegalArgumentException if the payload would require more than 32768 fragments
     */
    public static List<byte[]> encode(long instructionId, byte[] protobufBytes, int mtu) {
        byte[] compressed = zlibCompress(protobufBytes);
        int chunkSize = mtu - HEADER_SIZE;
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("MTU too small for fragment header");
        }
        int totalFragments = (compressed.length + chunkSize - 1) / chunkSize;
        if (totalFragments > MAX_FRAGMENT_COUNT) {
            throw new IllegalArgumentException(
                    "Payload requires " + totalFragments + " fragments, exceeding the maximum of " + MAX_FRAGMENT_COUNT);
        }

        List<byte[]> fragments = new ArrayList<>(totalFragments);
        int offset = 0;
        for (int i = 0; i < totalFragments; i++) {
            int end = Math.min(offset + chunkSize, compressed.length);
            int dataLen = end - offset;
            boolean isFinal = (i == totalFragments - 1);

            byte[] fragment = new byte[HEADER_SIZE + dataLen];
            ByteBuffer buf = ByteBuffer.wrap(fragment).order(ByteOrder.BIG_ENDIAN);
            buf.putLong(instructionId);
            int fragField = i & MAX_FRAGMENT_INDEX;
            if (isFinal) fragField |= FINAL_FLAG;
            buf.putShort((short) fragField);
            if (dataLen > 0) {
                System.arraycopy(compressed, offset, fragment, HEADER_SIZE, dataLen);
            }
            fragments.add(fragment);
            offset = end;
        }
        return fragments;
    }

    /**
     * Convenience: encode as a single fragment (most common case for small instructions).
     */
    public static byte[] encodeSingle(long instructionId, byte[] protobufBytes) {
        byte[] compressed = zlibCompress(protobufBytes);
        byte[] fragment = new byte[HEADER_SIZE + compressed.length];
        ByteBuffer buf = ByteBuffer.wrap(fragment).order(ByteOrder.BIG_ENDIAN);
        buf.putLong(instructionId);
        buf.putShort((short) (FINAL_FLAG)); // fragment 0, final
        System.arraycopy(compressed, 0, fragment, HEADER_SIZE, compressed.length);
        return fragment;
    }

    /**
     * Decode a fragment payload. Returns the reassembled protobuf bytes when all
     * fragments for an instruction have arrived, or null if still waiting.
     */
    public byte[] decode(byte[] fragmentPayload) {
        if (fragmentPayload == null || fragmentPayload.length < HEADER_SIZE) {
            return null;
        }
        ByteBuffer buf = ByteBuffer.wrap(fragmentPayload).order(ByteOrder.BIG_ENDIAN);
        long id = buf.getLong();
        int fragField = buf.getShort() & 0xFFFF;
        int fragNum = fragField & MAX_FRAGMENT_INDEX;
        boolean isFinal = (fragField & FINAL_FLAG) != 0;

        byte[] data = new byte[fragmentPayload.length - HEADER_SIZE];
        if (data.length > 0) {
            System.arraycopy(fragmentPayload, HEADER_SIZE, data, 0, data.length);
        }

        FragmentAssembly assembly = assemblies.get(id);
        boolean isNew = false;
        if (assembly == null) {
            FragmentAssembly newAssembly = new FragmentAssembly(id);
            assembly = assemblies.putIfAbsent(id, newAssembly);
            if (assembly == null) {
                assembly = newAssembly;
                isNew = true;
            }
        }

        final long currentId = id;
        synchronized (assembly) {
            if (isNew) {
                assemblies.keySet().removeIf(k -> k != currentId);
            }
            assembly.addFragment(fragNum, data, isFinal);

            if (assembly.isComplete()) {
                byte[] compressed = assembly.assemble();
                assemblies.remove(currentId, assembly);
                return zlibDecompress(compressed);
            }
        }
        return null;
    }

    private static byte[] zlibCompress(byte[] input) {
        Deflater deflater = new Deflater();
        try {
            deflater.setInput(input);
            deflater.finish();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
            byte[] tmp = new byte[1024];
            while (!deflater.finished()) {
                int n = deflater.deflate(tmp);
                baos.write(tmp, 0, n);
            }
            return baos.toByteArray();
        } finally {
            deflater.end();
        }
    }

    private static byte[] zlibDecompress(byte[] input) {
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(input);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length * 2);
            byte[] tmp = new byte[1024];
            while (!inflater.finished()) {
                int n = inflater.inflate(tmp);
                if (n == 0) {
                    if (inflater.needsDictionary()) {
                        throw new DataFormatException(
                                "Decompression requires a preset dictionary that was not provided");
                    } else if (inflater.needsInput()) {
                        throw new IOException("Compressed input is truncated or incomplete");
                    } else {
                        break;
                    }
                }
                if (baos.size() + n > MAX_DECOMPRESSED_BYTES) {
                    throw new IOException("Decompressed size exceeds limit of " + MAX_DECOMPRESSED_BYTES + " bytes");
                }
                baos.write(tmp, 0, n);
            }
            return baos.toByteArray();
        } catch (DataFormatException | IOException e) {
            throw new IllegalArgumentException("zlib decompression failed", e);
        } finally {
            inflater.end();
        }
    }

    private static class FragmentAssembly {
        final long id;
        final Map<Integer, byte[]> fragments = new HashMap<>();
        int totalFragments = -1;
        long bufferedBytes = 0;

        FragmentAssembly(long id) {
            this.id = id;
        }

        void addFragment(int num, byte[] data, boolean isFinal) {
            byte[] prev = fragments.put(num, data);
            if (prev != null) {
                bufferedBytes -= prev.length;
            }
            bufferedBytes += data.length;
            if (bufferedBytes > MAX_DECOMPRESSED_BYTES) {
                throw new IllegalArgumentException(
                        "Buffered fragment bytes exceed limit of " + MAX_DECOMPRESSED_BYTES);
            }
            if (isFinal) {
                totalFragments = num + 1;
            }
        }

        boolean isComplete() {
            if (totalFragments <= 0) return false;
            if (fragments.size() != totalFragments) return false;
            for (int i = 0; i < totalFragments; i++) {
                if (!fragments.containsKey(i)) return false;
            }
            return true;
        }

        byte[] assemble() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int i = 0; i < totalFragments; i++) {
                byte[] part = fragments.get(i);
                if (part != null) {
                    baos.write(part, 0, part.length);
                }
            }
            return baos.toByteArray();
        }
    }
}
