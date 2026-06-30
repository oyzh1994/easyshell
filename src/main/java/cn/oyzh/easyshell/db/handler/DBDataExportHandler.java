package cn.oyzh.easyshell.db.handler;

import cn.oyzh.easyshell.data.ShellDataExportHandler;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public abstract class DBDataExportHandler extends ShellDataExportHandler {

    /**
     * 库名称
     */
    protected String dbName;

    /**
     * 文件类型
     * sql
     * json
     */
    protected String fileType;

//    /**
//     * db客户端
//     */
//    private ShellMysqlClient dbClient;

    /**
     * 查询限制
     */
    protected int queryLimit = 1000;

//    /**
//     * 导出表
//     */
//    private List<ShellMysqlDataExportTable> tables;

    // /**
    //  * xls行记录
    //  */
    // private int xlsRowIndex = 1;
    //
    // /**
    //  * xls工作薄
    //  */
    // private Workbook workbook;
    //
    // /**
    //  * 文件写入器
    //  */
    // private FastFileWriter writer;

//    /**
//     * 导出配置
//     */
//    private final ShellMysqlDataExportConfig config;

    public DBDataExportHandler( String dbName) {
        this.dbName = dbName;
    }

    /**
     * 是否sql类型
     *
     * @return 结果
     */
    public boolean isSqlType() {
        return "sql".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否xml类型
     *
     * @return 结果
     */
    public boolean isXmlType() {
        return "xml".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否csv类型
     *
     * @return 结果
     */
    public boolean isCsvType() {
        return "csv".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否html类型
     *
     * @return 结果
     */
    public boolean isHtmlType() {
        return "html".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否xls类型
     *
     * @return 结果
     */
    public boolean isXlsType() {
        return "xls".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否xlsx类型
     *
     * @return 结果
     */
    public boolean isXlsxType() {
        return "xlsx".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否excel类型
     *
     * @return 结果
     */
    public boolean isExcelType() {
        return this.isXlsType() || this.isXlsxType();
    }

    /**
     * 是否json类型
     *
     * @return 结果
     */
    public boolean isJsonType() {
        return "json".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否txt类型
     *
     * @return 结果
     */
    public boolean isTxtType() {
        return "txt".equalsIgnoreCase(this.fileType);
    }


    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getQueryLimit() {
        return queryLimit;
    }

    public void setQueryLimit(int queryLimit) {
        this.queryLimit = queryLimit;
    }
}

