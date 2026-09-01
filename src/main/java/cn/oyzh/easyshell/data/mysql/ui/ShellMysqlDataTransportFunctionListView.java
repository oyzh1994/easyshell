package cn.oyzh.easyshell.data.mysql.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.db.dto.DBTransportObject;
import cn.oyzh.fx.db.ui.DBDataTransportObjectListView;
import cn.oyzh.easyshell.mysql.function.MysqlFunction;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ShellMysqlDataTransportFunctionListView extends DBDataTransportObjectListView {

    public void of(List<MysqlFunction> functions) {
        List<DBTransportObject> list = CollectionUtil.newArrayList();
        for (MysqlFunction function : functions) {
            DBTransportObject obj = new DBTransportObject();
            obj.setName(function.getName());
            list.add(obj);
        }
        this.init(list);
    }
}
