package cn.oyzh.easyshell.s3;

public class ShellS3Path {

    private final String path;

    public ShellS3Path(String path) {
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

    public String filePath() {
        int index = path.indexOf("/", 1);
        if (index != -1) {
            return path.substring(index);
        }
        return "/";
    }

    public String fileName() {
        int index = path.lastIndexOf("/");
        return path.substring(index + 1);
    }

    public String bucketName() {
        int index = path.indexOf("/", 1);
        if (index == -1) {
            return path.substring(1);
        }
        return path.substring(1, index);
    }

    public String parentPath() {
        int index = path.indexOf("/", 1);
        String fPath = path.substring(index + 1);
        index = fPath.indexOf("/");
        if (index == -1) {
            return "/";
        }
        int index2 = fPath.lastIndexOf("/");
        return fPath.substring(index, index2);
    }

    public static ShellS3Path of(String filePath) {
        return new ShellS3Path(filePath);
    }
}
