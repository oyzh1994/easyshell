package cn.oyzh.easyshell.s3;

/**
 * s3路径
 * 格式 /桶/路径
 *
 * @author oyzh
 * @since 2025-06-14
 */
public class ShellS3Path {

    /**
     * 文件路径
     */
    private final String path;

    public ShellS3Path(String path) {
        // 处理路径
        if ("".equals(path)) {
            path = "/";
        } else if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        this.path = path;
    }

    /**
     * 获取文件路径
     *
     * @return 文件路径
     */
    public String filePath() {
        int index = this.path.indexOf("/", 1);
        if (index != -1) {
            return this.path.substring(index + 1);
        }
        return "/";
    }

    /**
     * 获取前缀
     *
     * @return 前缀
     */
    public String prefix() {
        return ShellS3Util.toPrefix(this.filePath());
    }

    /**
     * 获取文件名
     *
     * @return 文件名
     */
    public String fileName() {
        int index = this.path.lastIndexOf("/");
        return this.path.substring(index + 1);
    }

    /**
     * 获取桶名称
     *
     * @return 桶名称
     */
    public String bucketName() {
        int index = this.path.indexOf("/", 1);
        if (index == -1) {
            if (this.path.length() > 1) {
                return this.path.substring(1);
            }
            return null;
        }
        return this.path.substring(1, index);
    }

    /**
     * 获取父目录
     *
     * @return 父目录
     */
    public String parentPath() {
        int index = this.path.indexOf("/", 1);
        String fPath = this.path.substring(index + 1);
        index = fPath.indexOf("/");
        if (index == -1) {
            return "/";
        }
        int index2 = fPath.lastIndexOf("/");
        return fPath.substring(0, index2);
    }

    /**
     * 创建s3路径
     *
     * @param filePath 文件路径
     * @return ShellS3Path
     */
    public static ShellS3Path of(String filePath) {
        return new ShellS3Path(filePath);
    }
}
