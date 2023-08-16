package cn.oyzh.easyssh.ssh;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2023/8/16
 */
public class SSHShellResult {

    @Getter
    private String prompt;

    @Setter
    private String command;

    @Getter
    private String result;

    @Getter
    private String clipResult;

    public SSHShellResult(String result) {
        this.setFirstResult(result);
    }

    public SSHShellResult(String command, String result) {
        this.command = command;
        this.setResult(result);
    }

    public void setResult(String result) {
        this.result = result;
        if (result != null) {
            String[] lines = result.split("\n");
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                String str = lines[i];
                if (i == 0 && StrUtil.containsIgnoreCase(str, this.command)) {
                    continue;
                }
                if (i == lines.length - 2) {
                    this.prompt = str;
                    break;
                }
                builder.append(str).append("\n");
            }
            this.clipResult = builder.toString();
        }
    }

    public void setFirstResult(String result) {
        this.result = result;
        System.out.println(result);
        if (result != null) {
            String[] lines = result.split("\n");
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                String str = lines[i];
                if (i == lines.length - 1) {
                    this.prompt = str;
                    break;
                }
                builder.append(str).append("\n");
            }
            this.clipResult = builder.toString();
        }
    }
}
