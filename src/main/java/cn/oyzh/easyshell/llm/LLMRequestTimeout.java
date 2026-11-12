package cn.oyzh.easyshell.llm;

import java.time.Duration;

public enum LLMRequestTimeout {

    MINUTES_10(10),
    MINUTES_20(20),
    MINUTES_30(30),
    MINUTES_60(60),
    UNLIMITED(0);

    public static final int DEFAULT_MINUTES = MINUTES_30.minutes;

    private final int minutes;

    LLMRequestTimeout(int minutes) {
        this.minutes = minutes;
    }

    public int getMinutes() {
        return this.minutes;
    }

    public Duration getDuration() {
        return this == UNLIMITED ? null : Duration.ofMinutes(this.minutes);
    }

    public static LLMRequestTimeout fromMinutes(Integer minutes) {
        if (minutes != null) {
            for (LLMRequestTimeout timeout : values()) {
                if (timeout.minutes == minutes) {
                    return timeout;
                }
            }
        }
        return MINUTES_30;
    }
}
