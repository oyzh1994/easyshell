package cn.oyzh.easyshell.file;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文件协议通用 LLM 工具。
 *
 * <p>FTP、SFTP、S3、WebDAV、SMB 均通过 {@link ShellFileClient} 提供统一文件能力，
 * 因此可以使用同一套工具适配器绑定到不同协议客户端。</p>
 *
 * @author oyzh
 * @since 2026-11-01
 */
public class ShellFileLLMTools<E extends ShellFile> implements LLMTools {

    private static final int MAX_LIST_FILES = 1000;

    private static final int MAX_READ_BYTES = 64 * 1024;

    private static final int MAX_WRITE_BYTES = 1024 * 1024;

    private ShellFileClient<E> client;

    public ShellFileLLMTools() {
    }

    public ShellFileLLMTools(ShellFileClient<E> client) {
        this.client = client;
    }

    public void setClient(ShellFileClient<E> client) {
        this.client = client;
    }

    @Tool(name = "listFiles", value = "List files and directories at a remote path")
    public String listFiles(
            @P(name = "path", value = "Absolute remote directory path") String path) {
        try {
            String remotePath = this.normalizePath(path);
            List<E> files = new ArrayList<>(this.client.lsFile(remotePath));
            files.removeIf(file -> file == null || !file.isNormal());
            files.sort((left, right) -> {
                if (left.isDirectory() != right.isDirectory()) {
                    return left.isDirectory() ? -1 : 1;
                }
                return String.CASE_INSENSITIVE_ORDER.compare(left.getFilePath(), right.getFilePath());
            });
            int shown = Math.min(files.size(), MAX_LIST_FILES);
            StringBuilder result = new StringBuilder();
            result.append("path: ").append(remotePath).append('\n');
            result.append("count: ").append(files.size()).append('\n');
            for (int index = 0; index < shown; index++) {
                result.append(this.formatFileLine(files.get(index))).append('\n');
            }
            if (files.size() > shown) {
                result.append("... ").append(files.size() - shown).append(" more entries omitted");
            }
            return result.toString();
        } catch (Exception ex) {
            return this.failure("List files", path, ex);
        }
    }

    @Tool(name = "getFileInfo", value = "Get metadata for a remote file or directory")
    public String getFileInfo(
            @P(name = "path", value = "Absolute remote file or directory path") String path) {
        try {
            String remotePath = this.normalizePath(path);
            E file = this.fileInfoIfPresent(remotePath);
            if (file == null) {
                return "Not found: " + remotePath;
            }
            return this.formatFile(file);
        } catch (Exception ex) {
            return this.failure("Get file info", path, ex);
        }
    }

    @Tool(name = "checkRemotePathExists", value = "Check whether a remote path exists")
    public String exists(
            @P(name = "path", value = "Absolute remote path") String path) {
        try {
            String remotePath = this.normalizePath(path);
            return "path: " + remotePath + "\nexists: " + this.client.exist(remotePath);
        } catch (Exception ex) {
            return this.failure("Check path", path, ex);
        }
    }

    @Tool(name = "getRemoteConnectionInfo", value = "Get read-only metadata for the bound file connection and its current working directory")
    public String getRemoteConnectionInfo() {
        try {
            ShellConnect connect = this.client.getShellConnect();
            StringBuilder result = new StringBuilder();
            result.append("name: ").append(this.value(connect == null ? null : connect.getName())).append('\n');
            result.append("type: ").append(this.value(connect == null ? null : connect.getType())).append('\n');
            result.append("host: ").append(this.value(connect == null ? null : connect.getHost())).append('\n');
            result.append("charset: ").append(this.value(this.client.getCharset().name())).append('\n');
            result.append("workingDirectorySupported: ").append(this.client.isWorkDirSupport()).append('\n');
            result.append("workingDirectory: ").append(this.value(this.workingDirectory()));
            return result.toString();
        } catch (Exception ex) {
            return this.failure("Get remote connection info", null, ex);
        }
    }

    @Tool(name = "resolveRemotePath", value = "Resolve a relative or absolute remote path against the bound connection")
    public String resolveRemotePath(
            @P(name = "path", value = "Remote path to resolve; empty resolves to the remote root") String path) {
        try {
            String remotePath = this.normalizePath(path);
            return "path: " + remotePath;
        } catch (Exception ex) {
            return this.failure("Resolve remote path", path, ex);
        }
    }

    @Tool(name = "createRemoteFile", value = "Create an empty remote file")
    public String createFile(
            @P(name = "path", value = "Absolute remote file path") String path) {
        try {
            String remotePath = this.normalizePath(path);
            this.client.touch(remotePath);
            return "Created file: " + remotePath;
        } catch (Exception ex) {
            return this.failure("Create file", path, ex);
        }
    }

    @Tool(name = "createRemoteDirectory", value = "Create one remote directory")
    public String createDirectory(
            @P(name = "path", value = "Absolute remote directory path") String path) {
        try {
            String remotePath = this.normalizePath(path);
            if (!this.client.isCreateDirSupport()) {
                return "Directory creation is not supported by this connection.";
            }
            boolean created = this.client.createDir(remotePath);
            return created ? "Created directory: " + remotePath : "Directory creation failed: " + remotePath;
        } catch (Exception ex) {
            return this.failure("Create directory", path, ex);
        }
    }

    @Tool(name = "createRemoteDirectoryRecursive", value = "Create all missing remote directories in a path")
    public String createDirectoryRecursive(
            @P(name = "path", value = "Absolute remote directory path") String path) {
        try {
            String remotePath = this.normalizePath(path);
            if (!this.client.isCreateDirRecursiveSupport()) {
                return "Recursive directory creation is not supported by this connection.";
            }
            this.client.createDirRecursive(remotePath);
            return "Created directory path: " + remotePath;
        } catch (Exception ex) {
            return this.failure("Create directory path", path, ex);
        }
    }

    @Tool(name = "renameRemoteFileOrDirectory", value = "Rename a remote file or directory within the same parent directory")
    public String rename(
            @P(name = "path", value = "Absolute current remote path") String path,
            @P(name = "newName", value = "New name without path separators") String newName) {
        try {
            if (newName == null || newName.isBlank()) {
                return "New name is required.";
            }
            String name = newName.trim();
            if (name.indexOf('/') >= 0 || name.indexOf((char) 92) >= 0) {
                return "New name must not contain path separators.";
            }
            String remotePath = this.normalizePath(path);
            E file = this.fileInfoIfPresent(remotePath);
            if (file == null) {
                return "Not found: " + remotePath;
            }
            boolean renamed = this.client.rename(file, name);
            return renamed ? "Renamed " + remotePath + " to " + name : "Rename failed: " + remotePath;
        } catch (Exception ex) {
            return this.failure("Rename", path, ex);
        }
    }

    @Tool(name = "deleteRemoteFile", value = "Delete one remote file")
    public String deleteFile(
            @P(name = "path", value = "Absolute remote file path") String path) {
        try {
            String remotePath = this.normalizePath(path);
            E file = this.fileInfoIfPresent(remotePath);
            if (file == null) {
                return "Not found: " + remotePath;
            }
            if (!file.isFile() && !file.isLink()) {
                return "Not a file: " + remotePath;
            }
            this.client.delete(remotePath);
            return "Deleted file: " + remotePath;
        } catch (Exception ex) {
            return this.failure("Delete file", path, ex);
        }
    }

    @Tool(name = "deleteRemoteDirectory", value = "Delete one remote directory using the connection protocol's non-recursive semantics")
    public String deleteDirectory(
            @P(name = "path", value = "Absolute remote directory path") String path) {
        try {
            String remotePath = this.normalizePath(path);
            E file = this.fileInfoIfPresent(remotePath);
            if (file == null) {
                return "Not found: " + remotePath;
            }
            if (!file.isDirectory()) {
                return "Not a directory: " + remotePath;
            }
            this.client.deleteDir(remotePath);
            return "Deleted directory: " + remotePath;
        } catch (Exception ex) {
            return this.failure("Delete directory", path, ex);
        }
    }

    @Tool(name = "deleteRemoteDirectoryRecursive", value = "Recursively delete a remote directory and all contents")
    public String deleteDirectoryRecursive(
            @P(name = "path", value = "Absolute remote directory path") String path) {
        try {
            String remotePath = this.normalizePath(path);
            E file = this.fileInfoIfPresent(remotePath);
            if (file == null) {
                return "Not found: " + remotePath;
            }
            if (!file.isDirectory()) {
                return "Not a directory: " + remotePath;
            }
            this.client.deleteDirRecursive(remotePath);
            return "Recursively deleted directory: " + remotePath;
        } catch (Exception ex) {
            return this.failure("Recursively delete directory", path, ex);
        }
    }

    @Tool(name = "readRemoteTextFile", value = "Read up to 64 KiB of text from a remote file")
    public String readTextFile(
            @P(name = "path", value = "Absolute remote file path") String path,
            @P(name = "maxBytes", value = "Maximum bytes to return, up to 65536") int maxBytes) {
        try {
            String remotePath = this.normalizePath(path);
            E file = this.fileInfoIfPresent(remotePath);
            if (file == null) {
                return "Not found: " + remotePath;
            }
            if (!file.isFile() && !file.isLink()) {
                return "Not a readable file: " + remotePath;
            }
            int limit = maxBytes <= 0 ? MAX_READ_BYTES : Math.min(maxBytes, MAX_READ_BYTES);
            ReadResult result = this.readRemote(file, limit);
            if (this.isBinary(result.bytes())) {
                return "Binary file detected. Use downloadRemoteFile for binary content: " + remotePath;
            }
            StringBuilder response = new StringBuilder();
            response.append("path: ").append(remotePath).append('\n');
            response.append("size: ").append(file.getFileSize()).append('\n');
            response.append("bytesRead: ").append(result.bytes().length).append('\n');
            response.append("truncated: ").append(result.truncated()).append('\n');
            response.append("content:\n");
            response.append(this.decodeText(result.bytes()));
            return response.toString();
        } catch (Exception ex) {
            return this.failure("Read file", path, ex);
        }
    }

    @Tool(name = "writeRemoteTextFile", value = "Create or overwrite a remote text file")
    public String writeTextFile(
            @P(name = "path", value = "Absolute remote file path") String path,
            @P(name = "content", value = "Complete text content to write") String content) {
        try {
            if (content == null) {
                return "Content is required.";
            }
            byte[] bytes = content.getBytes(this.client.getCharset());
            if (bytes.length > MAX_WRITE_BYTES) {
                return "Content exceeds the 1 MiB write limit.";
            }
            String remotePath = this.normalizePath(path);
            E existing = this.fileInfoIfPresent(remotePath);
            if (existing != null && existing.isDirectory()) {
                return "Cannot write text to a directory: " + remotePath;
            }
            try (InputStream input = new ByteArrayInputStream(bytes)) {
                this.client.put(input, remotePath, null);
            }
            return "Wrote " + bytes.length + " bytes to " + remotePath;
        } catch (Exception ex) {
            return this.failure("Write file", path, ex);
        }
    }

    @Tool(name = "setRemoteFilePermissions", value = "Set Unix-style permissions on a remote file or directory")
    public String setPermissions(
            @P(name = "path", value = "Absolute remote path") String path,
            @P(name = "permissions", value = "Permissions such as rwxr-xr-x or 755") String permissions) {
        try {
            if (!this.client.isChmodSupport()) {
                return "Permission changes are not supported by this connection.";
            }
            String remotePath = this.normalizePath(path);
            int permissionValue = this.parsePermissions(permissions);
            boolean changed = this.client.chmod(permissionValue, remotePath);
            return changed ? "Updated permissions on " + remotePath : "Permission change failed: " + remotePath;
        } catch (Exception ex) {
            return this.failure("Set permissions", path, ex);
        }
    }

    @Tool(name = "uploadLocalFile", value = "Upload one local file to the bound remote connection")
    public String uploadLocalFile(
            @P(name = "localPath", value = "Absolute local file path") String localPath,
            @P(name = "remotePath", value = "Absolute remote file path") String remotePath) {
        try {
            Path localFile = this.localFile(localPath);
            String targetPath = this.normalizePath(remotePath);
            try (InputStream input = Files.newInputStream(localFile)) {
                this.client.put(input, targetPath, null);
            }
            return "Uploaded " + Files.size(localFile) + " bytes to " + targetPath;
        } catch (Exception ex) {
            return this.failure("Upload file", remotePath, ex);
        }
    }

    @Tool(name = "downloadRemoteFile", value = "Download one remote file to a local path")
    public String downloadRemoteFile(
            @P(name = "remotePath", value = "Absolute remote file path") String remotePath,
            @P(name = "localPath", value = "Absolute local file path") String localPath) {
        try {
            String sourcePath = this.normalizePath(remotePath);
            E file = this.fileInfoIfPresent(sourcePath);
            if (file == null) {
                return "Not found: " + sourcePath;
            }
            if (!file.isFile() && !file.isLink()) {
                return "Not a downloadable file: " + sourcePath;
            }
            Path localFile = this.localFile(localPath);
            Path parent = localFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            long copied;
            try (InputStream input = this.client.getStream(file, null);
                 OutputStream output = Files.newOutputStream(localFile,
                         StandardOpenOption.CREATE,
                         StandardOpenOption.TRUNCATE_EXISTING,
                         StandardOpenOption.WRITE)) {
                copied = input.transferTo(output);
            }
            return "Downloaded " + copied + " bytes from " + sourcePath + " to " + localFile;
        } catch (Exception ex) {
            return this.failure("Download file", remotePath, ex);
        }
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        String base = LLMUtil.readLlmText("file_tool.txt");
        ShellConnect connect = this.client.getShellConnect();
        if (connect != null && connect.isS3Type()) {
            base += "\n\n" + LLMUtil.readLlmText("s3_file_tool.txt");
        }
        String environment = LLMUtil.readLlmText("file_environment.txt");
        String connectionName = connect == null ? "-" : connect.getName();
        String protocol = connect == null ? "-" : connect.getType();
        String charset = this.client.getCharset().name();
        String workingDirectory = this.workingDirectory();
        environment = environment
                .replace("{name}", this.value(connectionName))
                .replace("{type}", this.value(protocol))
                .replace("{charset}", this.value(charset))
                .replace("{workingDirectory}", this.value(workingDirectory));
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + environment + "\n\n" + languageRule + "\n\n" + executionModeText;
    }

    private String normalizePath(String path) throws Exception {
        if (path == null || path.isBlank()) {
            return "/";
        }
        String resolved = this.client.resolvePath(path.trim());
        return resolved == null || resolved.isBlank() ? "/" : resolved;
    }

    private E fileInfoIfPresent(String remotePath) throws Exception {
        return this.client.exist(remotePath) ? this.client.fileInfo(remotePath) : null;
    }

    private Path localFile(String localPath) {
        if (localPath == null || localPath.isBlank()) {
            throw new IllegalArgumentException("Local path is required.");
        }
        return Path.of(localPath.trim()).toAbsolutePath().normalize();
    }

    private ReadResult readRemote(E file, int limit) throws Exception {
        try (InputStream input = this.client.getStream(file, null)) {
            byte[] bytes = input.readNBytes(limit + 1);
            boolean truncated = bytes.length > limit;
            return new ReadResult(Arrays.copyOf(bytes, Math.min(bytes.length, limit)), truncated);
        }
    }

    private boolean isBinary(byte[] bytes) {
        int count = Math.min(bytes.length, 8192);
        for (int index = 0; index < count; index++) {
            if (bytes[index] == 0) {
                return true;
            }
        }
        return false;
    }

    private String decodeText(byte[] bytes) {
        Charset charset = this.client.getCharset();
        int offset = 0;
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xEF
                && (bytes[1] & 0xFF) == 0xBB
                && (bytes[2] & 0xFF) == 0xBF) {
            charset = StandardCharsets.UTF_8;
            offset = 3;
        } else if (bytes.length >= 2
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xFE) {
            charset = StandardCharsets.UTF_16LE;
            offset = 2;
        } else if (bytes.length >= 2
                && (bytes[0] & 0xFF) == 0xFE
                && (bytes[1] & 0xFF) == 0xFF) {
            charset = StandardCharsets.UTF_16BE;
            offset = 2;
        }
        return new String(bytes, offset, bytes.length - offset, charset);
    }

    private int parsePermissions(String permissions) {
        if (permissions == null || permissions.isBlank()) {
            throw new IllegalArgumentException("Permissions are required.");
        }
        String value = permissions.trim();
        if (value.chars().allMatch(Character::isDigit)) {
            if (value.length() < 3 || value.length() > 4) {
                throw new IllegalArgumentException("Numeric permissions must contain 3 or 4 digits.");
            }
            for (char item : value.toCharArray()) {
                if (item > '7') {
                    throw new IllegalArgumentException("Numeric permissions must use digits 0-7.");
                }
            }
            return Integer.parseInt(value);
        }
        int result = ShellFileUtil.toPermissionInt(value);
        if (result <= 0) {
            throw new IllegalArgumentException("Invalid Unix permissions.");
        }
        return result;
    }

    private String workingDirectory() {
        if (this.client.isWorkDirSupport()) {
            try {
                return this.client.workDir();
            } catch (Exception ignored) {
            }
        }
        return "-";
    }

    private String formatFile(E file) {
        StringBuilder result = new StringBuilder();
        result.append("path: ").append(this.value(file.getFilePath())).append('\n');
        result.append("name: ").append(this.value(file.getFileName())).append('\n');
        result.append("type: ").append(this.fileType(file)).append('\n');
        result.append("size: ").append(file.getFileSize()).append('\n');
        result.append("modified: ").append(this.value(file.getModifyTime())).append('\n');
        result.append("owner: ").append(this.value(file.getOwner())).append('\n');
        result.append("group: ").append(this.value(file.getGroup())).append('\n');
        result.append("permissions: ").append(this.value(file.getPermissions()));
        return result.toString();
    }

    private String formatFileLine(E file) {
        return this.fileType(file)
                + " | " + file.getFileSize()
                + " | " + this.value(file.getModifyTime())
                + " | " + this.value(file.getFilePath());
    }

    private String fileType(E file) {
        if (file.isLink()) {
            return "link";
        }
        if (file.isDirectory()) {
            return "directory";
        }
        if (file.isFile()) {
            return "file";
        }
        return "other";
    }

    private String failure(String action, String path, Exception ex) {
        String message = ex.getMessage();
        return action + " failed for " + this.value(path) + ": "
                + (message == null || message.isBlank() ? ex.getClass().getSimpleName() : message);
    }

    private String value(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private record ReadResult(byte[] bytes, boolean truncated) {
    }
}
