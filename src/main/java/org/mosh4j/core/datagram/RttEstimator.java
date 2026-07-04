package org.mosh4j.core.datagram;

/**
 * Smoothed RTT and RTT variance (TCP-style), used for retransmission timeout.
 */
public final class RttEstimator {

    private static final long RTO_MIN_MS = 50;
    private static final long RTO_MAX_MS = 10_000;

    private long srttMs = 0;
    private long rttvarMs = 0;
    private boolean initialized = false;

    public void update(long rttMs) {
        if (rttMs <= 0) return;
        if (!initialized) {
            srttMs = rttMs;
            rttvarMs = rttMs / 2;
            initialized = true;
            return;
        }
        long delta = Math.abs(rttMs - srttMs);
        rttvarMs = (3 * rttvarMs + delta) / 4;
        srttMs = (7 * srttMs + rttMs) / 8;
    }

    public long getSrttMs() {
        return srttMs;
    }

    public long getRtoMs() {
        if (!initialized) return 1000;
        long rto = srttMs + 4 * rttvarMs;
        return Math.max(RTO_MIN_MS, Math.min(RTO_MAX_MS, rto));
    }
}
