package cn.oyzh.easyssh.parser;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.common.Parser;
import com.jcraft.jsch.JSchException;
import lombok.NonNull;

/**
 * ssh异常信息解析
 *
 * @author oyzh
 * @since 2023/7/2
 */
public class SSHExceptionParser implements Parser<Exception, String> {

    /**
     * 当前实例
     */
    public final static SSHExceptionParser INSTANCE = new SSHExceptionParser();

    @Override
    public String parse(@NonNull Exception e) {
        String message = e.getMessage();

        if (e instanceof JSchException) {
            if (StrUtil.contains(message, "java.net.UnknownHostException")) {
                return "主机地址异常";
            }
            if (StrUtil.contains(message, "Auth fail")) {
                return "认证失败";
            }
            if (StrUtil.contains(message, "connection is closed by foreign host")) {
                return "连接被外部主机关闭";
            }
            if (StrUtil.contains(message, "Connection refused: connect")) {
                return "拒绝连接";
            }
            if (StrUtil.contains(message, "socket is not established")) {
                return "连接未建立";
            }
            if (StrUtil.contains(message, "session is down")) {
                return "会话已断开";
            }
            return message;
        }

        if (e instanceof UnsupportedOperationException) {
            return message;
        }

        if (e instanceof IllegalArgumentException) {
            return message;
        }

        e.printStackTrace();
        return "未知错误！";
    }
}
