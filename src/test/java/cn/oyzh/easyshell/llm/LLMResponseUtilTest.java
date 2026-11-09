package cn.oyzh.easyshell.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.FinishReason;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LLMResponseUtilTest {

    @Test
    public void usesCompleteResponseTextWhenThereWasNoPartialText() {
        ChatResponse response = ChatResponse.builder()
                .aiMessage(AiMessage.from("complete final answer"))
                .finishReason(FinishReason.STOP)
                .build();

        assertEquals("complete final answer", LLMResponseUtil.finalText(response));
        assertFalse(LLMResponseUtil.outputTruncated(response));
    }

    @Test
    public void ignoresBlankFinalText() {
        ChatResponse response = ChatResponse.builder()
                .aiMessage(AiMessage.from(" "))
                .build();

        assertNull(LLMResponseUtil.finalText(response));
    }

    @Test
    public void detectsLengthTruncation() {
        ChatResponse response = ChatResponse.builder()
                .aiMessage(AiMessage.from("partial"))
                .finishReason(FinishReason.LENGTH)
                .build();

        assertTrue(LLMResponseUtil.outputTruncated(response));
    }

    @Test
    public void mergesStreamedThinkingWithoutDuplicatingFinalRound() {
        assertEquals("first\n\nsecond",
                LLMResponseUtil.mergeThinking("first", "second"));
        assertEquals("first\n\nsecond",
                LLMResponseUtil.mergeThinking("first\n\nsecond", "second"));
        assertEquals("final", LLMResponseUtil.mergeThinking("", "final"));
        assertEquals("streamed", LLMResponseUtil.mergeThinking("streamed", null));
    }
}
