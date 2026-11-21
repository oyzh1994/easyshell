package cn.oyzh.easyshell.llm.skill;

import cn.oyzh.common.compress.ArchiveUtil;
import cn.oyzh.common.compress.CompressUtil;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.util.StringUtil;
import dev.langchain4j.skills.FileSystemSkillLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Skill 压缩包准备、校验与释放工具。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMSkillArchiveUtil {

    public static final long MAX_FILE_BYTES = 10L * 1024 * 1024;

    private static final int MAX_ENTRIES = 512;

    private static final int BUFFER_SIZE = 8192;

    private LLMSkillArchiveUtil() {
    }

    public static PreparedArchive prepare(String sourcePath, Path workDirectory) throws IOException {
        if (StringUtil.isBlank(sourcePath)) {
            throw new IllegalArgumentException("Skill source is required");
        }
        Files.createDirectories(workDirectory);
        Path source = Path.of(sourcePath).toAbsolutePath().normalize();
        Path zip = workDirectory.resolve("skill.zip");
        if (Files.isDirectory(source)) {
            zipDirectory(source, zip);
        } else if (Files.isRegularFile(source) && isZip(source)) {
            requireFileSize(source);
            Files.copy(source, zip, StandardCopyOption.REPLACE_EXISTING);
        } else if (Files.isRegularFile(source) && isMarkdown(source)) {
            requireFileSize(source);
            zipSingleFile(source, zip, workDirectory.resolve(".single-markdown"));
        } else {
            throw new IllegalArgumentException(
                    "Skill source must be a ZIP, Markdown file, or a directory containing SKILL.md");
        }
        requireFileSize(zip);
        String prefix = inspectArchive(zip);
        Path validation = workDirectory.resolve("validation");
        extract(zip, validation, prefix);
        try {
            FileSystemSkillLoader.loadSkill(validation);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid SKILL.md: " + ex.getMessage(), ex);
        }
        return new PreparedArchive(zip, sha256(zip));
    }

    public static void extract(Path zip, Path destination, String prefix) throws IOException {
        Path root = destination.toAbsolutePath().normalize();
        Files.createDirectories(root);
        Path raw = Files.createTempDirectory(root.getParent(), ".skill-unzip-");
        try {
            try {
                CompressUtil.unzip(zip.toFile(), raw.toFile());
            } catch (RuntimeException ex) {
                throw new IOException("Failed to unzip skill archive", ex);
            }
            Path contentRoot = prefix.isEmpty()
                    ? raw
                    : raw.resolve(prefix.substring(0, prefix.length() - 1));
            if (!Files.isDirectory(contentRoot)) {
                throw new IOException("Skill archive root is missing: " + prefix);
            }
            try (var children = Files.list(contentRoot)) {
                for (Path child : children.toList()) {
                    String name = child.getFileName().toString().replace('\\', '/');
                    if (isMacMetadata(name)) {
                        continue;
                    }
                    Files.move(child, root.resolve(child.getFileName()),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
            validateExtractedContent(root);
        } finally {
            deleteTree(raw);
        }
        if (!Files.isRegularFile(root.resolve("SKILL.md"))) {
            throw new IOException("Skill archive does not contain SKILL.md");
        }
    }

    public static void validateIntegrity(Path zip, String expectedHash) throws IOException {
        if (!Files.isRegularFile(zip)) {
            throw new IOException("Skill archive does not exist: " + zip);
        }
        requireFileSize(zip);
        if (StringUtil.isBlank(expectedHash)
                || !expectedHash.equalsIgnoreCase(sha256(zip))) {
            throw new IOException("Skill archive hash mismatch: " + zip);
        }
    }

    public static String sha256(Path file) throws IOException {
        try (InputStream input = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    public static void deleteTree(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            return;
        }
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void copy(Path source, Path target) throws IOException {
        Files.createDirectories(target.toAbsolutePath().getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void zipDirectory(Path source, Path zip) throws IOException {
        long[] total = {0};
        int[] count = {0};
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Files.isSymbolicLink(file)) {
                    throw new IOException("Symbolic links are not supported in skill folders");
                }
                if (isMacMetadata(file.getFileName().toString())) {
                    return FileVisitResult.CONTINUE;
                }
                if (!attrs.isRegularFile()) {
                    throw new IOException("Unsupported skill folder entry: " + file);
                }
                if (++count[0] > MAX_ENTRIES) {
                    throw new IOException("Skill folder contains too many files");
                }
                total[0] += attrs.size();
                if (total[0] > MAX_FILE_BYTES) {
                    throw new IOException("Skill content exceeds 10 MB");
                }
                return FileVisitResult.CONTINUE;
            }
        });
        if (!Files.isRegularFile(source.resolve("SKILL.md"))) {
            throw new IOException("Skill folder does not contain SKILL.md");
        }
        ArchiveUtil.createZip(source, zip);
    }

    private static void zipSingleFile(Path source, Path zip, Path workDirectory) throws IOException {
        Files.createDirectories(workDirectory);
        Path skillMd = workDirectory.resolve("SKILL.md");
        try {
            Files.copy(source, skillMd, StandardCopyOption.REPLACE_EXISTING);
            ArchiveUtil.createZip(workDirectory, zip);
        } finally {
            deleteTree(workDirectory);
        }
    }

    public static String inspectArchive(Path zip) throws IOException {
        Set<String> names = new LinkedHashSet<>();
        String prefix = null;
        int count = 0;
        long declaredBytes = 0;
        try (ZipFile archive = new ZipFile(zip.toFile())) {
            var entries = archive.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (++count > MAX_ENTRIES) {
                    throw new IOException("Skill archive contains too many entries");
                }
                String name = normalizeEntryName(entry.getName());
                if (name == null || isMacMetadata(name)) {
                    continue;
                }
                if (!names.add(name)) {
                    throw new IOException("Duplicate skill archive entry: " + name);
                }
                if (!entry.isDirectory() && entry.getSize() > 0) {
                    declaredBytes += entry.getSize();
                    if (declaredBytes > MAX_FILE_BYTES) {
                        throw new IOException("Skill content exceeds 10 MB");
                    }
                }
                if (!entry.isDirectory()) {
                    String[] parts = name.split("/");
                    if ("SKILL.md".equalsIgnoreCase(parts[parts.length - 1])) {
                        String candidate = parts.length == 1
                                ? ""
                                : parts.length == 2 ? parts[0] + "/" : null;
                        if (candidate == null) {
                            throw new IOException("SKILL.md must be at the archive root or one directory deep");
                        }
                        if (prefix != null && !prefix.equals(candidate)) {
                            throw new IOException("Skill archive contains multiple SKILL.md files");
                        }
                        prefix = candidate;
                    }
                }
            }
        }
        if (prefix == null) {
            throw new IOException("Skill archive does not contain SKILL.md");
        }
        if (!prefix.isEmpty()) {
            for (String name : names) {
                if (!isMacMetadata(name) && !belongsToPrefix(name, prefix)) {
                    throw new IOException("Skill archive contains files outside " + prefix);
                }
            }
        }
        return prefix;
    }

    private static boolean belongsToPrefix(String name, String prefix) {
        return prefix.isEmpty() || name.equals(prefix) || name.startsWith(prefix);
    }

    private static String normalizeEntryName(String value) throws IOException {
        if (value == null || value.isBlank()) {
            return null;
        }
        String name = value.replace('\\', '/');
        while (name.startsWith("./")) {
            name = name.substring(2);
        }
        if (name.startsWith("/") || name.contains("../")
                || name.startsWith("../") || name.endsWith("/..")) {
            throw new IOException("Invalid skill archive entry: " + value);
        }
        return name;
    }

    private static void requireFileSize(Path file) throws IOException {
        if (Files.size(file) > MAX_FILE_BYTES) {
            throw new IOException("Skill file exceeds 10 MB");
        }
    }

    private static boolean isZip(Path file) {
        return FileNameUtil.isZipType(FileNameUtil.extName(file.toFile()));
    }

    private static boolean isMarkdown(Path file) {
        return FileNameUtil.isMarkdownType(FileNameUtil.extName(file.toFile()));
    }

    private static boolean isMacMetadata(String name) {
        return name.equals("__MACOSX") || name.startsWith("__MACOSX/")
                || name.equals(".DS_Store") || name.endsWith("/.DS_Store");
    }

    private static void validateExtractedContent(Path root) throws IOException {
        long[] total = {0};
        int[] count = {0};
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Files.isSymbolicLink(file)) {
                    throw new IOException("Symbolic links are not supported in skill archives");
                }
                if (++count[0] > MAX_ENTRIES) {
                    throw new IOException("Skill archive contains too many files");
                }
                total[0] += attrs.size();
                if (total[0] > MAX_FILE_BYTES) {
                    throw new IOException("Skill content exceeds 10 MB");
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public record PreparedArchive(Path zip, String hash) {
    }
}
