package cn.oyzh.easyshell.llm;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LLMSessionUtilTest {

    private final List<Path> tempFiles = new ArrayList<>();

    @After
    public void cleanup() throws IOException {
        for (Path file : this.tempFiles) {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void roundTripsMessagesWithAttachment() throws IOException {
        Path file = Files.createTempFile("llm-session-", ".txt");
        Files.writeString(file, "hello", StandardCharsets.UTF_8);
        this.tempFiles.add(file);

        LLMMessage user = LLMMessage.user("总结附件", file.toFile());
        user.setMessageId("user-1");
        LLMMessage answer = new LLMMessage(LLMMessageType.LLM, 1700000000000L);
        answer.setMessageId("llm-1");
        answer.setMessage("附件内容很少");
        answer.setModel("test-model");

        String json = LLMSessionUtil.toJson(List.of(user, answer));
        List<LLMMessage> messages = LLMSessionUtil.fromJson(json);

        assertEquals(2, messages.size());
        assertEquals("user-1", messages.get(0).getMessageId());
        assertEquals(LLMMessageType.USER, messages.get(0).getType());
        assertEquals("总结附件", messages.get(0).getMessage());
        assertEquals(file.toFile().getAbsolutePath(), messages.get(0).getFile().getAbsolutePath());
        assertEquals(LLMMessageType.LLM, messages.get(1).getType());
        assertEquals(1700000000000L, messages.get(1).getTime());
        assertEquals("test-model", messages.get(1).getModel());
    }

    @Test
    public void handlesEmptyAndInvalidJson() {
        assertTrue(LLMSessionUtil.fromJson(null).isEmpty());
        assertTrue(LLMSessionUtil.fromJson("").isEmpty());
        assertTrue(LLMSessionUtil.fromJson("[]").isEmpty());
        assertTrue(LLMSessionUtil.fromJson("not-json").isEmpty());
    }

    @Test
    public void createsSessionFromBoundTab() {
        LLMSessionTab tab = new LLMSessionTab("tab-1", "SSH server");

        LLMSession session = LLMSessionUtil.newSession(tab);

        assertTrue(session.getUid() != null && !session.getUid().isBlank());
        assertEquals("tab-1", session.getSessionTabId());
        assertEquals("SSH server", session.getTitle());
    }

    @Test
    public void countsSessionMessages() {
        LLMSession session = new LLMSession();
        session.setMessages(LLMSessionUtil.toJson(List.of(
                LLMMessage.user("hello"),
                new LLMMessage(LLMMessageType.LLM, 1700000000000L))));

        assertEquals(2, session.getMessageCount());
    }
}
