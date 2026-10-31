package cn.oyzh.easyshell.llm;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.TextContent;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LLMFileContentUtilTest {

    private final List<Path> tempFiles = new ArrayList<>();

    @After
    public void cleanup() throws IOException {
        for (Path file : this.tempFiles) {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void convertsTextFileWithPrompt() throws IOException {
        Path file = this.temp("sample.txt", "hello\n世界".getBytes(StandardCharsets.UTF_8));

        List<Content> contents = LLMFileContentUtil.toUserContents("分析一下", file.toFile());

        assertEquals(1, contents.size());
        String text = ((TextContent) contents.get(0)).text();
        assertTrue(text.contains("分析一下"));
        assertTrue(text.contains("sample.txt"));
        assertTrue(text.contains("hello\n世界"));
    }

    @Test
    public void convertsAttachmentWithoutPrompt() throws IOException {
        Path file = this.temp("only-file.txt", "attachment".getBytes(StandardCharsets.UTF_8));

        List<Content> contents = LLMFileContentUtil.toUserContents("", file.toFile());

        assertEquals(1, contents.size());
        String text = ((TextContent) contents.get(0)).text();
        assertTrue(text.contains("only-file.txt"));
        assertTrue(text.contains("attachment"));
    }

    @Test
    public void decodesGbkTextFile() throws IOException {
        Path file = this.temp("server.log", "中文日志".getBytes(Charset.forName("GB18030")));

        List<Content> contents = LLMFileContentUtil.toUserContents(null, file.toFile());

        String text = ((TextContent) contents.get(0)).text();
        assertTrue(text.contains("中文日志"));
    }

    @Test
    public void decodesUtf16TextFileWithBom() throws IOException {
        byte[] body = "UTF-16文本".getBytes(StandardCharsets.UTF_16LE);
        byte[] content = new byte[body.length + 2];
        content[0] = (byte) 0xFF;
        content[1] = (byte) 0xFE;
        System.arraycopy(body, 0, content, 2, body.length);
        Path file = this.temp("utf16.txt", content);

        List<Content> contents = LLMFileContentUtil.toUserContents(null, file.toFile());

        String text = ((TextContent) contents.get(0)).text();
        assertTrue(text.contains("UTF-16文本"));
    }

    @Test
    public void convertsImageFile() throws IOException {
        byte[] png = new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0};
        Path file = this.temp("image.png", png);

        List<Content> contents = LLMFileContentUtil.toUserContents("看图", file.toFile());

        assertEquals(2, contents.size());
        assertTrue(contents.get(0) instanceof TextContent);
        assertTrue(contents.get(1) instanceof ImageContent);
    }

    @Test
    public void convertsPdfFile() throws IOException {
        Path file = this.temp("document.pdf", "%PDF-1.4\n%%EOF".getBytes(StandardCharsets.US_ASCII));

        List<Content> contents = LLMFileContentUtil.toUserContents("总结", file.toFile());

        assertEquals(2, contents.size());
        assertTrue(contents.get(0) instanceof TextContent);
        assertTrue(contents.get(1) instanceof PdfFileContent);
    }

    @Test
    public void rejectsUnknownBinaryFile() throws IOException {
        Path file = this.temp("archive.bin", new byte[]{0, 1, 2, 3, 4});

        try {
            LLMFileContentUtil.toUserContents(null, file.toFile());
            fail("binary file should be rejected");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("archive.bin"));
        }
    }

    private Path temp(String suffix, byte[] content) throws IOException {
        Path file = Files.createTempFile("llm-file-", "-" + suffix);
        Files.write(file, content);
        this.tempFiles.add(file);
        return file;
    }
}
