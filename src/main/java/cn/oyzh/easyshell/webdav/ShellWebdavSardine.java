package cn.oyzh.easyshell.webdav;

import com.github.sardine.impl.SardineImpl;
import com.github.sardine.impl.handler.ExistsResponseHandler;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

/**
 *
 * @author oyzh
 * @since 2025-10-10
 */
public class ShellWebdavSardine extends SardineImpl {

    private String authorization;

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public ShellWebdavSardine(HttpClientBuilder builder, String user, String password) {
        super(builder, user, password);
    }

    @Override
    public boolean exists(String url) throws IOException {
        if (this.authorization != null) {
            HttpHead head = new HttpHead(url);
            head.setHeader("Authorization", this.authorization);
            return this.execute(head, new ExistsResponseHandler());
        }
        return super.exists(url);
    }
}
