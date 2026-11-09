package cn.oyzh.easyshell.llm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LLMDurationUtilTest {

    @Test
    public void formatsMinutesAndSeconds() {
        assertEquals("00:00", LLMDurationUtil.format(0));
        assertEquals("00:01", LLMDurationUtil.format(1_999));
        assertEquals("12:34", LLMDurationUtil.format(754_000));
    }

    @Test
    public void formatsHours() {
        assertEquals("01:02:03", LLMDurationUtil.format(3_723_000));
    }

    @Test
    public void normalizesNegativeValues() {
        assertEquals("00:00", LLMDurationUtil.format(-1));
    }
}
