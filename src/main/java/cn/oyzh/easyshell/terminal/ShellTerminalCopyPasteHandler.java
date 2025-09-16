package cn.oyzh.easyshell.terminal;

import cn.oyzh.ssh.util.SSHUtil;
import com.jediterm.terminal.DefaultTerminalCopyPasteHandler;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @author oyzh
 * @since 2025-09-15
 */
public class ShellTerminalCopyPasteHandler extends DefaultTerminalCopyPasteHandler {

    @Override
    public @Nullable String getContents(boolean useSystemSelectionClipboardIfAvailable) {
        String contents= super.getContents(useSystemSelectionClipboardIfAvailable);
        if(contents==null){
            return "";
        }
        return SSHUtil.removeAnsi(contents);
    }
}
