package cn.oyzh.easyshell.llm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LLMModelContextWindowUtilTest {

    @Test
    public void defaultsTo256KAndSupportsExpectedOptions() {
        assertEquals(256 * 1024, LLMModelContextWindowUtil.DEFAULT_TOKENS);
        assertEquals(256 * 1024, LLMModelContextWindowUtil.normalize(0));
        assertEquals(256 * 1024, LLMModelContextWindowUtil.normalize(100));
        assertEquals(64 * 1024, LLMModelContextWindowUtil.normalize(64 * 1024));
        assertTrue(LLMModelContextWindowUtil.OPTIONS.containsAll(
                java.util.List.of(
                        64 * 1024,
                        128 * 1024,
                        256 * 1024,
                        384 * 1024,
                        512 * 1024,
                        1024 * 1024)));
        assertEquals("1M", LLMModelContextWindowUtil.label(1024 * 1024));
    }
}
