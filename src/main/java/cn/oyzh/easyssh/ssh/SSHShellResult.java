//package cn.oyzh.easyssh.ssh;
//
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easyssh.util.SSHShellUtil;
//import lombok.Getter;
//import lombok.Setter;
//
///**
// * @author oyzh
// * @since 2023/8/16
// */
//public class SSHShellResult {
//
//    @Getter
//    private String prompt;
//
//    @Getter
//    @Setter
//    private String command;
//
//    @Getter
//    private String result;
//
//    @Getter
//    private String clipResult;
//
//    public SSHShellResult(String result) {
//        this.setFirstResult(result);
//    }
//
//    public SSHShellResult(String command, String result) {
//        this.command = command;
//        this.setResult(result);
//    }
//
//    public void setResult(String result) {
//        this.result = result;
//        if (result != null) {
//            String[] lines = result.split("\n");
//            StringBuilder builder = new StringBuilder();
//            for (String str : lines) {
//                if (StringUtil.equals(str.replace("\r", ""), this.command)) {
//                    continue;
//                }
//                if (SSHShellUtil.isPrompt(str)) {
//                    this.prompt = str;
//                    break;
//                }
//                builder.append(str);
//                if (!str.endsWith("\n")) {
//                    builder.append("\n");
//                }
//            }
//            this.clipResult = builder.toString();
//        }
//    }
//
//    public void setFirstResult(String result) {
//        this.result = result;
//        if (result != null) {
//            String[] lines = result.split("\n");
//            StringBuilder builder = new StringBuilder();
//            for (String str : lines) {
//                if (SSHShellUtil.isPrompt(str)) {
//                    this.prompt = str;
//                    break;
//                }
//                builder.append(str);
//                if (!str.endsWith("\n")) {
//                    builder.append("\n");
//                }
//            }
//            this.clipResult = builder.toString();
//        }
//    }
//
//    public boolean hasPrompt() {
//        return this.prompt != null;
//    }
//}
