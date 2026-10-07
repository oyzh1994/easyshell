 package cn.oyzh.easyshell.event.zk;

 import cn.oyzh.easyshell.zk.ShellZKClient;
 import cn.oyzh.event.Event;
 import cn.oyzh.event.EventFormatter;
 import cn.oyzh.i18n.I18nHelper;

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
         return String.format("[%s:%s path:%s restored data] ", I18nHelper.connect(), this.data().connectName(), this.nodePath);
     }

 }
