package cn.oyzh.easyshell.data.dameng.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class DamengDataTransportProcedureListView extends DBDataTransportObjectListView {

    public void of(List<DamengProcedure> procedures) {
        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
        for (DamengProcedure procedure : procedures) {
            DBDataTransportObject obj = new DBDataTransportObject();
            obj.setName(procedure.getName());
            list.add(obj);
        }
        this.init(list);
    }
}
