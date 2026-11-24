package cn.oyzh.easyshell.store;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.ShellConst;
import org.sqlite.SQLiteJDBCLoader;
import org.sqlite.util.LibraryLoaderUtil;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * sqlite原生库缓存
 *
 * @author oyzh
 * @since 2026-11-25
 */
public final class SQLiteNativeCache {

    /**
     * 原生库路径属性
     */
    private static final String LIB_PATH = "org.sqlite.lib.path";

    /**
     * 原生库名称属性
     */
    private static final String LIB_NAME = "org.sqlite.lib.name";

    /**
     * 构造方法
     */
    private SQLiteNativeCache() {
    }

    /**
     * 计算文件摘要
     *
     * @param file 文件
     * @return 摘要
     * @throws Exception 异常
     */
    private static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, count);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /**
     * 释放并配置sqlite原生库
     */
    public static void prepare() {
        if (System.getProperty(LIB_PATH) != null) {
            return;
        }
        try {
            String resourcePath = LibraryLoaderUtil.getNativeLibResourcePath();
            String libName = LibraryLoaderUtil.getNativeLibName();
            if (!LibraryLoaderUtil.hasNativeLib(resourcePath, libName)) {
                return;
            }
            String platform = resourcePath.replace('\\', '/').replaceAll("^/+", "").replace('/', '_');
            Path versionDir = Path.of(ShellConst.getSqliteNativePath(), SQLiteJDBCLoader.getVersion(), platform);
            Path libFile = versionDir.resolve(libName);
            Path hashFile = versionDir.resolve(libName + ".sha256");
            if (!isValid(libFile, hashFile)) {
                extract(resourcePath + "/" + libName, libFile, hashFile);
            }
            System.setProperty(LIB_PATH, versionDir.toString());
            System.setProperty(LIB_NAME, libName);
            cleanupOldVersions(versionDir);
        } catch (Exception ex) {
            JulLog.warn("prepare sqlite native library failed", ex);
        }
    }

    /**
     * 校验缓存文件
     *
     * @param libFile  原生库
     * @param hashFile 摘要文件
     * @return 结果
     */
    private static boolean isValid(Path libFile, Path hashFile) {
        try {
            return Files.isRegularFile(libFile)
                    && Files.isRegularFile(hashFile)
                    && Files.readString(hashFile).trim().equalsIgnoreCase(sha256(libFile));
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 释放原生库
     *
     * @param resourcePath 资源路径
     * @param libFile      原生库
     * @param hashFile     摘要文件
     * @throws Exception 异常
     */
    private static void extract(String resourcePath, Path libFile, Path hashFile) throws Exception {
        Files.createDirectories(libFile.getParent());
        Path tempFile = libFile.resolveSibling(libFile.getFileName() + "." + UUID.randomUUID() + ".tmp");
        Path tempHash = hashFile.resolveSibling(hashFile.getFileName() + "." + UUID.randomUUID() + ".tmp");
        try {
            try (InputStream input = SQLiteNativeCache.class.getResourceAsStream(resourcePath)) {
                if (input == null) {
                    throw new IllegalStateException("sqlite native library not found: " + resourcePath);
                }
                Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            tempFile.toFile().setReadable(true, true);
            tempFile.toFile().setWritable(true, true);
            tempFile.toFile().setExecutable(true, true);
            Files.writeString(tempHash, sha256(tempFile));
            move(tempFile, libFile);
            move(tempHash, hashFile);
        } finally {
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempHash);
        }
    }

    /**
     * 移动文件
     *
     * @param source 源文件
     * @param target 目标文件
     * @throws Exception 异常
     */
    private static void move(Path source, Path target) throws Exception {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * 清理旧版本缓存
     *
     * @param versionDir 当前版本目录
     */
    private static void cleanupOldVersions(Path versionDir) {
        try (Stream<Path> stream = Files.list(Path.of(ShellConst.getSqliteNativePath()))) {
            stream.filter(Files::isDirectory)
                    .filter(path -> !path.getFileName().equals(versionDir.getParent().getFileName()))
                    .forEach(FileUtil::del);
        } catch (Exception ignored) {
        }
    }
}
