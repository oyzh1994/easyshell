package cn.oyzh.easyshell.data;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public abstract class ShellDataExportHandler extends ShellDataHandler {

    /**
     * 执行导出
     *
     * @throws Exception 异常
     */
    public abstract void doExport() throws Exception ;
}

