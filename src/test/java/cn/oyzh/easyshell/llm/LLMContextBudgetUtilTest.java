package cn.oyzh.easyshell.llm;

import cn.oyzh.easyshell.llm.capability.LLMModelCapability;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LLMContextBudgetUtilTest {

    @Test
    public void allowsRequestsWithinConfiguredContext() {
        LLMModelCapability capability = capability(256 * 1024);
        LLMTokenUsageStats stats = new LLMTokenUsageStats(
                100_000, 2_000, 102_000,
                100_000, 2_000, 102_000,
                true, 1_000);

        LLMContextBudgetUtil.Result result = LLMContextBudgetUtil.check(
                stats, List.of(), "分析这段日志", null, 8_192, capability);

        assertTrue(result.allowed());
    }

    @Test
    public void blocksBeforeSendingWhenPromptAndOutputReserveExceedLimit() {
        LLMModelCapability capability = capability(64 * 1024);
        LLMTokenUsageStats stats = new LLMTokenUsageStats(
                50_000, 2_000, 52_000,
                50_000, 2_000, 52_000,
                true, 1_000);
        String prompt = "x".repeat(20_000);

        LLMContextBudgetUtil.Result result = LLMContextBudgetUtil.check(
                stats, List.of(), prompt, null, 8_192, capability);

        assertFalse(result.allowed());
        assertTrue(result.estimatedTokens() > result.limitTokens());
    }

    private static LLMModelCapability capability(int tokens) {
        LLMModelCapability capability = new LLMModelCapability();
        capability.setContextWindowTokens(tokens);
        return capability;
    }
}
