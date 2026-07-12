package cn.oyzh.easyshell.test.ipmi;

import org.junit.Test;
import org.metricshub.ipmi.core.api.async.ConnectionHandle;
import org.metricshub.ipmi.core.api.sol.SerialOverLan;
import org.metricshub.ipmi.core.api.sol.SpecificCipherSuiteSelector;
import org.metricshub.ipmi.core.api.sync.IpmiConnector;

import java.net.InetAddress;

public class IpmiTest {

    @Test
    public void test() throws Exception {
        System.out.println("test");


        String bmcIp = "120.24.176.61";
        int port = 2623;
        InetAddress address = InetAddress.getByName(bmcIp);
        String username = "ADMIN";
        String password = "ADMIN";

        IpmiConnector connector = new IpmiConnector(13369);
        ConnectionHandle handle = connector.createConnection(address, port);
//        Session session= connector.openSession(handle,username,password,null);
//        System.out.println(session);
//        CipherSuite cipherSuite = handle.getCipherSuite(); // 获取默认加密套件[reference:8]
//        //connection.openSession(username, password, cipherSuite);
//
//// 2. 构建一个具体的 IPMI 命令，例如获取传感器读数
//// 注意：GetSensorReading 的构造可能需要具体参数，请查阅 Javadoc[reference:9]
//        PayloadCoder command = new GetSensorReading(IpmiVersion.V20, cipherSuite, AuthenticationType.RMCPPlus,);
//
//// 3. 发送命令并获取响应
//        ResponseData messageId = connector.sendMessage(handle, command);

        SpecificCipherSuiteSelector suiteSelector = new SpecificCipherSuiteSelector(handle.getCipherSuite());

        SerialOverLan sol = new SerialOverLan(connector, bmcIp, port, username, password, suiteSelector);
        sol.writeString("sdr list");
        byte[] data = sol.readBytes(1024, 5000);
        System.out.println("收到数据: " + new String(data));
    }
}
