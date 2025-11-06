package cn.oyzh.easyshell.mysql.query;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.query.MysqlQueryResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/02/19
 */
public class MysqlQueryResults<R extends MysqlQueryResult> {

    private String errMsg;

    private List<R> results;

    public void addResult(R result) {
        if (this.results == null) {
            this.results = new ArrayList<>();
        }
        this.results.add(result);
    }

    public boolean isEmpty() {
        return CollectionUtil.isEmpty(this.results);
    }

    public boolean isSuccess() {
        return StringUtil.isEmpty(this.errMsg);
    }

    public void parseError(Exception ex) {
        this.errMsg = ex.getMessage();
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public List<R> getResults() {
        return results;
    }

    public void setResults(List<R> results) {
        this.results = results;
    }
}
