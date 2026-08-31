package cn.oyzh.easyshell.data.mysql.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.data.db.dto.DBDataTransportObject;
import cn.oyzh.easyshell.data.db.ui.DBDataTransportObjectListView;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ShellMysqlDataTransportProcedureListView extends DBDataTransportObjectListView {

    public void of(List<MysqlProcedure> procedures) {
        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
        for (MysqlProcedure procedure : procedures) {
            DBDataTransportObject obj = new DBDataTransportObject();
            obj.setName(procedure.getName());
            list.add(obj);
        }
        this.init(list);
    }

}
