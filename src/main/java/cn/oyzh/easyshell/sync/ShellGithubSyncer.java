package cn.oyzh.easyshell.sync;

/**
 * github同步器
 *
 * @author oyzh
 * @since 2025-10-11
 */
public class ShellGithubSyncer extends ShellGistSyncer {

    @Override
    protected ShellGistOperator getOperator(String accessToken) throws Exception {
        return new ShellGitHubGistOperator(this.setting.getSyncToken());
    }
}
