package cn.oyzh.easyshell.smb;

import com.hierynomus.security.Mac;
import com.hierynomus.security.SecurityException;
import com.hierynomus.security.jce.JceSecurityProvider;

/**
 * smb加密实现
 *
 * @author oyzh
 * @since 2025-10-09
 */
public class ShellSMBSecurityProvider extends JceSecurityProvider {

    @Override
    public Mac getMac(String name) throws SecurityException {
        // TODO: 因启用bc库，暂时注释这部分代码
//        if ("AesCmac".equalsIgnoreCase(name)) {
//            return new ShellSMBAesCmac();
//        }
        return super.getMac(name);
    }

}