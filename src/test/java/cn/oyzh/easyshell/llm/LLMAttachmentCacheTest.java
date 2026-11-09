package cn.oyzh.easyshell.llm;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LLMAttachmentCacheTest {

    private Path root;

    @After
    public void cleanup() throws IOException {
        if (this.root != null && Files.exists(this.root)) {
            try (var files = Files.walk(this.root)) {
                for (Path path : files.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    @Test
    public void copiesFileIntoCacheWithoutChangingContent() throws IOException {
        this.root = Files.createTempDirectory("llm-attachment-cache-");
        Path source = Files.createTempFile("pasted-file-", ".txt");
        byte[] content = "cached attachment".getBytes(StandardCharsets.UTF_8);
        Files.write(source, content);

        File cached = LLMAttachmentCache.cacheFile(this.root, source.toFile());

        assertTrue(cached.isFile());
        assertTrue(cached.toPath().startsWith(this.root));
        assertEquals(source.getFileName().toString(), cached.getName());
        assertArrayEquals(content, Files.readAllBytes(cached.toPath()));
        Files.deleteIfExists(source);
    }
}
