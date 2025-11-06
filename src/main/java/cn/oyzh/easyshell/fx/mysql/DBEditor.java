package cn.oyzh.easyshell.fx.mysql;

import cn.oyzh.easyshell.mysql.DBDialect;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.editor.incubator.EditorFormatType;

/**
 * db编辑器
 *
 * @author oyzh
 * @since 2025/10/29
 */
public class DBEditor extends Editor {

    @Override
    public void initNode() {
        super.initNode();
        super.setFormatType(EditorFormatType.SQL);
    }

    /**
     * 方言
     */
    private DBDialect dialect;

    public DBDialect getDialect() {
        return dialect;
    }

    public void setDialect(DBDialect dialect) {
        this.dialect = dialect;
    }
}
