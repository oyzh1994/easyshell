package cn.oyzh.easyshell.sync;

/**
 * gitee同步器
 *
 * @author oyzh
 * @since 2025-10-11
 */
public class ShellGiteeSyncer extends ShellGistSyncer {

    @Override
    protected ShellGistOperator getOperator(String accessToken) throws Exception {
        return new ShellGiteeGistOperator(this.setting.getSyncToken());
    }
}
