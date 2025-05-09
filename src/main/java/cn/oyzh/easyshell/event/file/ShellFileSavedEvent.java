package cn.oyzh.easyshell.event.file;


import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/3/29
 */
public class ShellFileSavedEvent extends Event<ShellFile> {

    public String fileName() {
        return this.data().getFileName();
    }
}
