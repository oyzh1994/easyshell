package cn.oyzh.easyshell.h2;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBConnConfig;

import java.nio.file.Path;

/**
 * H2 连接配置。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellH2ConnConfig extends DBConnConfig {

    /**
     * 连接模式：file=本地文件，mem=内存库，tcp=远程服务器
     */
    private String urlMode = "file";

    /**
     * 是否只读
     */
    private boolean readOnly;

    /**
     * 文件不存在时是否创建
     */
    private boolean createIfMissing;

    /**
     * 默认 schema
     */
    private String schema = "PUBLIC";

    /**
     * H2 兼容模式，如 MySQL、PostgreSQL 等，空表示不指定
     */
    private String mode = "";

    /**
     * 是否启用 AUTO_SERVER 多进程访问
     */
    private boolean autoServer;

    /**
     * 内存模式是否追加 DB_CLOSE_DELAY=-1 保持库存活
     */
    private boolean dbCloseDelay = true;

    /**
     * 获取连接模式。
     *
     * @return 连接模式：file/mem/tcp
     */
    public String getUrlMode() {
        return StringUtil.isBlank(this.urlMode) ? "file" : this.urlMode;
    }

    /**
     * 设置连接模式。
     *
     * @param urlMode 连接模式
     */
    public void setUrlMode(String urlMode) {
        this.urlMode = urlMode;
    }

    /**
     * 是否文件模式。
     *
     * @return 结果
     */
    public boolean isFileMode() {
        return "file".equalsIgnoreCase(this.getUrlMode());
    }

    /**
     * 是否内存模式。
     *
     * @return 结果
     */
    public boolean isMemMode() {
        return "mem".equalsIgnoreCase(this.getUrlMode());
    }

    /**
     * 是否远程模式。
     *
     * @return 结果
     */
    public boolean isTcpMode() {
        return "tcp".equalsIgnoreCase(this.getUrlMode());
    }

    /**
     * 获取规范化后的数据库文件路径。文件模式下 host 为文件路径，
     * 自动剥离用户误带的 .mv.db 后缀（H2 驱动会自行追加）。
     *
     * @return 文件路径
     */
    public Path resolveFilePath() {
        return ShellH2Helper.databasePath(this.getHost());
    }

    /**
     * 获取 H2 实际使用的 MVStore 文件。
     *
     * @return 实际文件路径
     */
    public Path actualFilePath() {
        return ShellH2Helper.actualFile(this.resolveFilePath());
    }

    /**
     * 是否只读。
     *
     * @return 结果
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * 设置只读。
     *
     * @param readOnly 是否只读
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * 文件不存在时是否创建。
     *
     * @return 结果
     */
    public boolean isCreateIfMissing() {
        return this.createIfMissing;
    }

    /**
     * 设置文件不存在时是否创建。
     *
     * @param createIfMissing 是否创建
     */
    public void setCreateIfMissing(boolean createIfMissing) {
        this.createIfMissing = createIfMissing;
    }

    /**
     * 获取默认 schema。
     *
     * @return schema 名称
     */
    public String getSchema() {
        return StringUtil.blankToDefault(this.schema, "PUBLIC");
    }

    /**
     * 设置默认 schema。
     *
     * @param schema schema 名称
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * 获取兼容模式。
     *
     * @return 兼容模式
     */
    public String getMode() {
        return StringUtil.blankToDefault(this.mode, "");
    }

    /**
     * 设置兼容模式。
     *
     * @param mode 兼容模式
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * 是否启用 AUTO_SERVER。
     *
     * @return 结果
     */
    public boolean isAutoServer() {
        return this.autoServer;
    }

    /**
     * 设置是否启用 AUTO_SERVER。
     *
     * @param autoServer 是否启用
     */
    public void setAutoServer(boolean autoServer) {
        this.autoServer = autoServer;
    }

    /**
     * 内存模式是否追加 DB_CLOSE_DELAY=-1。
     *
     * @return 结果
     */
    public boolean isDbCloseDelay() {
        return this.dbCloseDelay;
    }

    /**
     * 设置内存模式是否追加 DB_CLOSE_DELAY=-1。
     *
     * @param dbCloseDelay 是否保持库存活
     */
    public void setDbCloseDelay(boolean dbCloseDelay) {
        this.dbCloseDelay = dbCloseDelay;
    }
}
