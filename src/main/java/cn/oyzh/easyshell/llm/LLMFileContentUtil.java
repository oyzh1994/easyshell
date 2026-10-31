package cn.oyzh.easyshell.llm;

import cn.oyzh.i18n.I18nHelper;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.VideoContent;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * LLM文件内容转换工具
 *
 * @author oyzh
 * @since 2026-10-31
 */
public final class LLMFileContentUtil {

    /**
     * 单个文本文件最大字节数
     */
    private static final long MAX_TEXT_FILE_SIZE = 5L * 1024 * 1024;

    /**
     * 单个多媒体文件最大字节数
     */
    private static final long MAX_MEDIA_FILE_SIZE = 32L * 1024 * 1024;

    private static final int BINARY_SNIFF_SIZE = 8192;

    private static final double BINARY_CONTROL_RATIO = 0.3;

    private static final Map<String, String> IMAGE_MIME_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "webp", "image/webp",
            "bmp", "image/bmp"
    );

    private static final Map<String, String> AUDIO_MIME_TYPES = Map.of(
            "mp3", "audio/mpeg",
            "wav", "audio/wav",
            "m4a", "audio/mp4",
            "aac", "audio/aac",
            "ogg", "audio/ogg",
            "flac", "audio/flac",
            "opus", "audio/opus"
    );

    private static final Map<String, String> VIDEO_MIME_TYPES = Map.of(
            "mp4", "video/mp4",
            "m4v", "video/mp4",
            "mov", "video/quicktime",
            "webm", "video/webm",
            "mpeg", "video/mpeg",
            "mpg", "video/mpeg",
            "avi", "video/x-msvideo",
            "mkv", "video/x-matroska"
    );

    private LLMFileContentUtil() {
    }

    /**
     * 将用户输入和附件转换为LangChain4j消息内容
     *
     * @param prompt 用户输入
     * @param file   附件
     * @return 消息内容
     */
    public static List<Content> toUserContents(String prompt, File file) {
        String text = prompt == null ? "" : prompt.trim();
        if (file == null) {
            return text.isEmpty() ? List.of() : List.of(TextContent.from(text));
        }
        validateFile(file);
        Path path = file.toPath();
        String extension = extension(file);

        String imageMimeType = IMAGE_MIME_TYPES.get(extension);
        if (imageMimeType != null) {
            validateMediaSize(file);
            return List.of(
                    TextContent.from(mediaDescription(text, file, imageMimeType)),
                    ImageContent.from(path, imageMimeType)
            );
        }
        if ("pdf".equals(extension)) {
            validateMediaSize(file);
            return List.of(
                    TextContent.from(mediaDescription(text, file, "application/pdf")),
                    PdfFileContent.from(path)
            );
        }
        String audioMimeType = AUDIO_MIME_TYPES.get(extension);
        if (audioMimeType != null) {
            validateMediaSize(file);
            return List.of(
                    TextContent.from(mediaDescription(text, file, audioMimeType)),
                    AudioContent.from(path, audioMimeType)
            );
        }
        String videoMimeType = VIDEO_MIME_TYPES.get(extension);
        if (videoMimeType != null) {
            validateMediaSize(file);
            return List.of(
                    TextContent.from(mediaDescription(text, file, videoMimeType)),
                    VideoContent.from(path, videoMimeType)
            );
        }

        byte[] bytes = readTextBytes(file);
        if (isBinary(bytes)) {
            throw new IllegalArgumentException(I18nHelper.notSupport() + ": " + file.getName());
        }
        return List.of(TextContent.from(textDescription(text, file, decode(bytes))));
    }

    private static void validateFile(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException(I18nHelper.fileNotExists() + ": " + file.getName());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException(I18nHelper.notSupportFolder() + ": " + file.getName());
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException(I18nHelper.read() + I18nHelper.fail() + ": " + file.getName());
        }
    }

    private static void validateMediaSize(File file) {
        try {
            long size = Files.size(file.toPath());
            if (size <= 0) {
                throw new IllegalArgumentException(I18nHelper.invalidFile() + ": " + file.getName());
            }
            checkSize(file, size, MAX_MEDIA_FILE_SIZE);
        } catch (IOException ex) {
            throw readException(file, ex);
        }
    }

    private static byte[] readTextBytes(File file) {
        try {
            long size = Files.size(file.toPath());
            checkSize(file, size, MAX_TEXT_FILE_SIZE);
            return Files.readAllBytes(file.toPath());
        } catch (IOException ex) {
            throw readException(file, ex);
        }
    }

    private static void checkSize(File file, long size, long maxSize) {
        if (size > maxSize) {
            throw new IllegalArgumentException(I18nHelper.dataTooLarge() + ": " + file.getName()
                    + " (" + formatSize(size) + " > " + formatSize(maxSize) + ")");
        }
    }

    private static IllegalArgumentException readException(File file, Exception ex) {
        return new IllegalArgumentException(I18nHelper.read() + I18nHelper.fail() + ": " + file.getName(), ex);
    }

    private static String formatSize(long size) {
        if (size >= 1024L * 1024) {
            return String.format(Locale.ROOT, "%.1fMB", size / (1024.0 * 1024.0));
        }
        if (size >= 1024L) {
            return String.format(Locale.ROOT, "%.1fKB", size / 1024.0);
        }
        return size + "B";
    }

    private static String mediaDescription(String prompt, File file, String mimeType) {
        StringBuilder builder = new StringBuilder();
        if (!prompt.isEmpty()) {
            builder.append(prompt).append("\n\n");
        }
        builder.append(I18nHelper.file()).append(": ").append(file.getName()).append('\n');
        builder.append("MIME: ").append(mimeType);
        return builder.toString();
    }

    private static String textDescription(String prompt, File file, String content) {
        StringBuilder builder = new StringBuilder();
        if (!prompt.isEmpty()) {
            builder.append(prompt).append("\n\n");
        }
        builder.append(I18nHelper.file()).append(": ").append(file.getName()).append('\n');
        builder.append("```").append(codeFenceLanguage(file)).append('\n');
        builder.append(content);
        if (!content.isEmpty() && !content.endsWith("\n")) {
            builder.append('\n');
        }
        builder.append("```");
        return builder.toString();
    }

    private static String extension(File file) {
        String name = file.getName();
        int index = name.lastIndexOf('.');
        return index < 0 || index == name.length() - 1
                ? "" : name.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private static String codeFenceLanguage(File file) {
        return switch (extension(file)) {
            case "java" -> "java";
            case "kt", "kts" -> "kotlin";
            case "js", "mjs", "cjs" -> "javascript";
            case "ts", "mts", "cts" -> "typescript";
            case "jsx" -> "jsx";
            case "tsx" -> "tsx";
            case "vue" -> "vue";
            case "py" -> "python";
            case "sh", "bash", "zsh" -> "bash";
            case "bat", "cmd" -> "batch";
            case "ps1" -> "powershell";
            case "json", "jsonl" -> "json";
            case "xml" -> "xml";
            case "yml", "yaml" -> "yaml";
            case "toml" -> "toml";
            case "ini", "conf", "cfg", "properties" -> "ini";
            case "md", "markdown" -> "markdown";
            case "html", "htm" -> "html";
            case "css" -> "css";
            case "scss", "sass" -> "scss";
            case "less" -> "less";
            case "c", "h" -> "c";
            case "cc", "cpp", "cxx", "hpp", "hh", "hxx" -> "cpp";
            case "cs" -> "csharp";
            case "go" -> "go";
            case "rs" -> "rust";
            case "rb" -> "ruby";
            case "php" -> "php";
            case "lua" -> "lua";
            case "swift" -> "swift";
            case "sql" -> "sql";
            case "csv" -> "csv";
            case "log", "txt", "text" -> "text";
            default -> "";
        };
    }

    private static boolean isBinary(byte[] bytes) {
        if (startsWith(bytes, 0xEF, 0xBB, 0xBF)
                || startsWith(bytes, 0xFF, 0xFE)
                || startsWith(bytes, 0xFE, 0xFF)) {
            return false;
        }
        int limit = Math.min(bytes.length, BINARY_SNIFF_SIZE);
        if (limit == 0) {
            return false;
        }
        int controls = 0;
        for (int i = 0; i < limit; i++) {
            int value = bytes[i] & 0xFF;
            if (value == 0) {
                return true;
            }
            if (value < 0x20 && value != '\t' && value != '\n' && value != '\r'
                    && value != '\b' && value != '\f') {
                controls++;
            }
        }
        return controls > limit * BINARY_CONTROL_RATIO;
    }

    private static String decode(byte[] bytes) {
        if (startsWith(bytes, 0xEF, 0xBB, 0xBF)) {
            return decode(bytes, 3, StandardCharsets.UTF_8);
        }
        if (startsWith(bytes, 0xFF, 0xFE)) {
            return decode(bytes, 2, StandardCharsets.UTF_16LE);
        }
        if (startsWith(bytes, 0xFE, 0xFF)) {
            return decode(bytes, 2, StandardCharsets.UTF_16BE);
        }
        String result = decodeStrict(bytes, 0, StandardCharsets.UTF_8);
        if (result != null) {
            return result;
        }
        Charset defaultCharset = Charset.defaultCharset();
        if (!StandardCharsets.UTF_8.equals(defaultCharset)) {
            result = decodeStrict(bytes, 0, defaultCharset);
            if (result != null) {
                return result;
            }
        }
        result = decodeStrict(bytes, 0, Charset.forName("GB18030"));
        if (result != null) {
            return result;
        }
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    private static String decode(byte[] bytes, int offset, Charset charset) {
        String result = decodeStrict(bytes, offset, charset);
        return result == null ? new String(bytes, offset, bytes.length - offset, charset) : result;
    }

    private static String decodeStrict(byte[] bytes, int offset, Charset charset) {
        try {
            return charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes, offset, bytes.length - offset))
                    .toString();
        } catch (CharacterCodingException ex) {
            return null;
        }
    }

    private static boolean startsWith(byte[] bytes, int... prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if ((bytes[i] & 0xFF) != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
