package cn.oyzh.easyshell.llm;

import cn.oyzh.easyshell.ShellConst;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Copies clipboard files and images into EasyShell's cache before attaching them.
 *
 * @author oyzh
 * @since 2026-11-09
 */
public final class LLMAttachmentCache {

    private static final String ATTACHMENT_DIRECTORY = "llm_attachments";

    private LLMAttachmentCache() {
    }

    /**
     * Extracts the first pasted file or image and copies it into the attachment cache.
     *
     * @param clipboard system clipboard
     * @return cached attachment, or {@code null} when the clipboard has no supported attachment
     * @throws IOException when the attachment cannot be cached
     */
    public static File fromClipboard(Clipboard clipboard) throws IOException {
        if (clipboard == null) {
            return null;
        }
        List<File> files = clipboard.getFiles();
        if (files != null) {
            for (File file : files) {
                if (file != null && file.isFile() && file.canRead()) {
                    return cacheFile(file);
                }
            }
        }
        File uriFile = uriListFile(clipboard);
        if (uriFile != null) {
            return cacheFile(uriFile);
        }
        if (clipboard.hasImage()) {
            return cacheImage(clipboard.getImage());
        }
        return null;
    }

    /**
     * Checks whether the clipboard contains a supported file or image.
     *
     * @param clipboard system clipboard
     * @return whether a supported attachment is available
     */
    public static boolean hasAttachment(Clipboard clipboard) {
        if (clipboard == null) {
            return false;
        }
        try {
            List<File> files = clipboard.getFiles();
            if (files != null && files.stream().anyMatch(file -> file != null && file.isFile() && file.canRead())) {
                return true;
            }
            return uriListFile(clipboard) != null || clipboard.hasImage();
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Copies a file into the attachment cache.
     *
     * @param source source file
     * @return cached file
     * @throws IOException when the file cannot be copied
     */
    public static File cacheFile(File source) throws IOException {
        return cacheFile(cacheRoot(), source);
    }

    static File cacheFile(Path root, File source) throws IOException {
        if (source == null || !source.isFile() || !source.canRead()) {
            throw new IOException("Attachment is not a readable file: " + source);
        }
        Path directory = Files.createDirectories(root.resolve(UUID.randomUUID().toString()));
        Path target = directory.resolve(safeFileName(source.getName()));
        Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toFile();
    }

    /**
     * Writes a clipboard image as PNG in the attachment cache.
     *
     * @param image clipboard image
     * @return cached PNG file
     * @throws IOException when the image cannot be written
     */
    public static File cacheImage(Image image) throws IOException {
        return cacheImage(cacheRoot(), image);
    }

    static File cacheImage(Path root, Image image) throws IOException {
        if (image == null || image.isError()) {
            throw new IOException("Clipboard image is unavailable");
        }
        int width = (int) Math.round(image.getWidth());
        int height = (int) Math.round(image.getHeight());
        PixelReader reader = image.getPixelReader();
        if (width <= 0 || height <= 0 || reader == null) {
            throw new IOException("Clipboard image is empty");
        }
        int[] pixels = new int[width * height];
        reader.getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);

        Path directory = Files.createDirectories(root.resolve(UUID.randomUUID().toString()));
        Path target = directory.resolve("pasted-image.png");
        if (!ImageIO.write(bufferedImage, "png", target.toFile())) {
            throw new IOException("No PNG image writer is available");
        }
        return target.toFile();
    }

    static Path cacheRoot() {
        return Path.of(ShellConst.getCachePath(), ATTACHMENT_DIRECTORY);
    }

    private static File uriListFile(Clipboard clipboard) {
        DataFormat format = DataFormat.lookupMimeType("text/uri-list");
        if (format == null) {
            return null;
        }
        Object value = clipboard.getContent(format);
        if (!(value instanceof String text)) {
            return null;
        }
        for (String line : text.lines().toList()) {
            String candidate = line.trim();
            if (candidate.isEmpty() || candidate.startsWith("#")) {
                continue;
            }
            try {
                File file = new File(URI.create(candidate));
                if (file.isFile() && file.canRead()) {
                    return file;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static String safeFileName(String fileName) {
        String result = fileName == null ? "" : fileName
                .replace('\\', '_')
                .replace('/', '_')
                .replaceAll("[\\p{Cntrl}<>:\"|?*]", "_")
                .trim();
        return result.isEmpty() ? "attachment" : result;
    }
}
