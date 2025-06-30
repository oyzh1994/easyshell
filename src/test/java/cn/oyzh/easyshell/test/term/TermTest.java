package cn.oyzh.easyshell.test.term;

import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.easyshell.ssh2.ShellSSHUtil;
import org.junit.Test;

public class TermTest {

    @Test
    public void test1(){
        String sockFile = RuntimeUtil.execForStr("bash", "-c", "ls -l $SSH_AUTH_SOCK");
        String sockFile1 = RuntimeUtil.execForStr("zsh", "-c", "ls -l $SSH_AUTH_SOCK");
        String sockFile2 = RuntimeUtil.execForStr("zsh", "-c","echo $SSH_AUTH_SOCK && ssh-add -l");
        String sockFile3 = RuntimeUtil.execForStr("zsh", "-c","env");
        String sockFile4 = RuntimeUtil.execForStr("zsh", "-c","ssh-add -l");

        System.out.println(sockFile);
        System.out.println(sockFile1);
        System.out.println(sockFile2);
        System.out.println(sockFile3);
        System.out.println(sockFile4);
    }

    @Test
    public void test2(){
        String sockFile = ShellSSHUtil.getSSHAgentSockFile();

        System.out.println(sockFile);
    }
}
