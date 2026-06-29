package cn.oyzh.easyshell.query.mongo;

/**
 * @author oyzh
 * @since 2024/02/19
 */
public class MongoExecuteResult extends MongoQueryResult {

//    /**
//     * 是否全字段
//     */
//    private boolean fullColumn;

    @Override
    public boolean hasResult() {
        if (this.updateCount > 0) {
            return false;
        }
        return super.hasResult();
    }

//    public void setFullColumn(boolean fullColumn) {
//        this.fullColumn = fullColumn;
//    }
//
//    public boolean isFullColumn() {
//        return fullColumn;
//    }
}
