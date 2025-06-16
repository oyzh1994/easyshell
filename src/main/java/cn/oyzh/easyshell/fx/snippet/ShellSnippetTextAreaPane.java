package cn.oyzh.easyshell.fx.snippet;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.rich.richtextfx.data.RichDataTextAreaPane;
import javafx.scene.text.Font;

import java.util.HashSet;
import java.util.Set;

/**
 * @author oyzh
 * @since 2025-03-26
 */
public class ShellSnippetTextAreaPane extends RichDataTextAreaPane {

    {
        Set<String> prompts=new HashSet<>();
        // linux、macos、unix
        prompts.add("cd");
        prompts.add("sudo");
        prompts.add("ls");
        prompts.add("ll");
        prompts.add("uname");
        prompts.add("top");
        prompts.add("df");
        prompts.add("free");
        prompts.add("ps");
        prompts.add("cp");
        prompts.add("mv");
        prompts.add("rm");
        prompts.add("mkdir");
        prompts.add("cat");
        prompts.add("touch");
        prompts.add("chmod");
        prompts.add("chown");
        prompts.add("find");
        prompts.add("ifconfig");
        prompts.add("ip");
        prompts.add("netstat");
        prompts.add("traceroute");
        prompts.add("tracert");
        prompts.add("ss");
        prompts.add("grep");
        prompts.add("awk");
        prompts.add("sed");
        prompts.add("head");
        prompts.add("tail");
        prompts.add("vi");
        prompts.add("vim");
        prompts.add("kill");
        prompts.add("pkill");
        prompts.add("service");
        prompts.add("systemctl");
        prompts.add("system");
        prompts.add("net");
        prompts.add("tar");
        prompts.add("passwd");
        prompts.add("useradd");
        prompts.add("userdel");
        prompts.add("clear");
        prompts.add("history");
        prompts.add("date");
        prompts.add("man");
        prompts.add("which");
        prompts.add("lsof");
        prompts.add("htop");
        prompts.add("iostat");
        prompts.add("vmstat");
        prompts.add("dmesg");
        prompts.add("iftop");
        prompts.add("mtr");
        prompts.add("tcpdump");
        prompts.add("route");
        prompts.add("host");
        prompts.add("nmap");
        prompts.add("su");
        prompts.add("id");
        prompts.add("mount");
        prompts.add("umount");
        prompts.add("fdisk");
        prompts.add("mkfs");
        prompts.add("brew");
        // windows
        prompts.add("cls");
        prompts.add("taskkill");
        prompts.add("tasklist");
        prompts.add("dir");
        prompts.add("copy");
        prompts.add("move");
        prompts.add("type");
        prompts.add("ping");
        prompts.add("ipconfig");
        prompts.add("edit");
        prompts.add("help");
        prompts.add("where");
        // 脚本
        prompts.add("for");
        prompts.add("if");
        prompts.add("do");
        prompts.add("while");
        prompts.add("set");
        prompts.add("call");
        prompts.add("pause");
        prompts.add("start");
        this.setContentPrompts(prompts);
    }

    @Override
    protected Font initFont() {
        ShellSetting setting = ShellSettingStore.SETTING;
        return FontManager.toFont(setting.editorFontConfig());
    }


}
