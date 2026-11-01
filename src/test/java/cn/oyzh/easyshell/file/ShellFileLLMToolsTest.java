package cn.oyzh.easyshell.file;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShellFileLLMToolsTest {

    @Test
    public void readsAndWritesThroughFileClient() {
        AtomicReference<byte[]> uploaded = new AtomicReference<>();
        ShellFile file = new TestFile("/hello.txt");
        ShellFileClient<ShellFile> client = client(file, uploaded);
        try (ShellFileLLMTools<ShellFile> tools = new ShellFileLLMTools<>(client)) {
            assertTrue(tools.readTextFile("/hello.txt", 100).contains("hello"));

            String result = tools.writeTextFile("/hello.txt", "updated");

            assertEquals("updated", new String(uploaded.get(), StandardCharsets.UTF_8));
            assertTrue(result.contains("Wrote"));
        }
    }

    @Test
    public void systemMessageReplacesFileEnvironment() {
        ShellFile file = new TestFile("/hello.txt");
        ShellConnect connect = new ShellConnect();
        connect.setName("demo");
        connect.setType("sftp");
        connect.setCharset("UTF-8");
        ShellFileClient<ShellFile> client = client(file, new AtomicReference<>());
        try (ShellFileLLMTools<ShellFile> tools = new ShellFileLLMTools<>(client)) {
            String message = tools.systemMessage(LLMExecutionMode.APPROVE);

            assertTrue(message.contains("文件连接工具"));
            assertTrue(message.contains("demo"));
            assertFalse(message.contains("{workingDirectory}"));
        }
    }

    private ShellFileClient<ShellFile> client(ShellFile file, AtomicReference<byte[]> uploaded) {
        return (ShellFileClient<ShellFile>) Proxy.newProxyInstance(
                ShellFileClient.class.getClassLoader(),
                new Class<?>[]{ShellFileClient.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "lsFile" -> List.of(file);
                    case "lsFileDynamic" -> {
                        ((Consumer<ShellFile>) args[1]).accept(file);
                        yield null;
                    }
                    case "fileInfo" -> file;
                    case "exist" -> true;
                    case "resolvePath" -> args[0];
                    case "getShellConnect" -> connect();
                    case "getCharset" -> StandardCharsets.UTF_8;
                    case "getStream" -> new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8));
                    case "put" -> {
                        uploaded.set(((InputStream) args[0]).readAllBytes());
                        yield null;
                    }
                    case "isWorkDirSupport", "isChmodSupport" -> false;
                    case "isCreateDirSupport", "isCreateDirRecursiveSupport" -> true;
                    case "rename", "chmod" -> true;
                    case "toString" -> "test-file-client";
                    case "hashCode" -> 1;
                    case "equals" -> proxy == args[0];
                    default -> defaultValue(method.getReturnType());
                });
    }

    private ShellConnect connect() {
        ShellConnect connect = new ShellConnect();
        connect.setName("demo");
        connect.setType("sftp");
        connect.setCharset("UTF-8");
        return connect;
    }

    private Object defaultValue(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        return null;
    }

    private static final class TestFile implements ShellFile {

        private final String path;

        private String fileName;

        private TestFile(String path) {
            this.path = path;
            this.fileName = path.substring(path.lastIndexOf('/') + 1);
        }

        @Override
        public boolean isFile() {
            return true;
        }

        @Override
        public boolean isLink() {
            return false;
        }

        @Override
        public String getOwner() {
            return "-";
        }

        @Override
        public String getGroup() {
            return "-";
        }

        @Override
        public long getFileSize() {
            return 5;
        }

        @Override
        public void setFileSize(long fileSize) {
        }

        @Override
        public String getFileName() {
            return this.fileName;
        }

        @Override
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public String getParentPath() {
            return "/";
        }

        @Override
        public String getPermissions() {
            return "rw-r--r--";
        }

        @Override
        public void setPermissions(String permissions) {
        }

        @Override
        public String getModifyTime() {
            return "2026-01-01 00:00:00";
        }

        @Override
        public void setModifyTime(String modifyTime) {
        }

        @Override
        public String getFilePath() {
            return this.path;
        }

        @Override
        public void copy(ShellFile file) {
        }

        @Override
        public void destroy() {
        }
    }
}
