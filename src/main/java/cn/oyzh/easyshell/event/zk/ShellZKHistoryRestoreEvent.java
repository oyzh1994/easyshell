 package cn.oyzh.easyshell.event.zk;

 import cn.oyzh.easyshell.zk.ShellZKClient;
 import cn.oyzh.event.Event;
 import cn.oyzh.event.EventFormatter;
import cn.oyzh.easyshell.util.ShellI18nHelper;

 /**
  * zk历史恢复事件
  *
  * @author oyzh
  * @since 2025-09-04
  */
 public class ShellZKHistoryRestoreEvent extends Event<ShellZKClient> implements EventFormatter {

    /**
     * 节点路径
     */
    private String nodePath;

     public String getNodePath() {
         return nodePath;
     }

     public void setNodePath(String nodePath) {
         this.nodePath = nodePath;
     }

     @Override
     public String eventFormat() {
return ShellI18nHelper.zkHistoryRestored(this.data().connectName(), this.nodePath);
     }

 }
