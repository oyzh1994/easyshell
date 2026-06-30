package cn.oyzh.easyshell.query.mongo;

/**
 * @author oyzh
 * @since 2024/02/19
 */
public class ShellMongoExecuteResult extends ShellMongoQueryResult {

    @Override
    public boolean hasResult() {
        if (this.updateCount > 0) {
            return false;
        }
        return super.hasResult();
    }
}
