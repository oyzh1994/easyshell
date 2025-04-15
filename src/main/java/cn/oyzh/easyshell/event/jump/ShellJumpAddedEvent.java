//package cn.oyzh.easyshell.event.jump;
//
//import cn.oyzh.easyshell.domain.ShellJumpConfig;
//import cn.oyzh.event.Event;
//import cn.oyzh.event.EventFormatter;
//import cn.oyzh.i18n.I18nHelper;
//
///**
// * @author oyzh
// * @since 2023/9/18
// */
//public class ShellJumpAddedEvent extends Event<ShellJumpConfig> implements EventFormatter {
//
//    @Override
//    public String eventFormat() {
//        return String.format("[%s:%s] added", I18nHelper.connect(), this.data().getName());
//    }
//}
