package cn.oyzh.easyshell.test.ipmi;

import org.junit.Test;
import org.metricshub.ipmi.client.IpmiClient;
import org.metricshub.ipmi.client.IpmiClientConfiguration;
import org.metricshub.ipmi.core.api.async.ConnectionHandle;
import org.metricshub.ipmi.core.api.sol.SerialOverLan;
import org.metricshub.ipmi.core.api.sol.SpecificCipherSuiteSelector;
import org.metricshub.ipmi.core.api.sync.IpmiConnector;

import java.net.InetAddress;

/**
 * IPMI 测试
 * <p>
 * 使用 metricshub ipmi-java 1.2.02 的高级客户端 API
 * IpmiClient 封装了连接、会话管理、命令发送，直接返回解析后的结果。
 *
 * @author oyzh
 * @since 2026/06/19
 */
public class IpmiTest {

    // ====== 测试配置（替换为实际 BMC 信息）======
    private static final String BMC_HOST = "127.0.0.1";
    private static final int BMC_PORT = 2623;          // IPMI 默认 UDP 端口 623
    private static final String BMC_USER = "admin";
    private static final String BMC_PASS = "admin";
    private static final long TIMEOUT_MS = 10000;     // 10s 超时


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
    
    /**
     * 使用高级 API 获取 FRU 和传感器信息（推荐）
     */
    @Test
    public void testGetFrusAndSensors() throws Exception {
        IpmiClientConfiguration config = new IpmiClientConfiguration(
                BMC_HOST, BMC_PORT, BMC_USER, BMC_PASS.toCharArray(), null, false, TIMEOUT_MS);

        String result = IpmiClient.getFrusAndSensorsAsStringResult(config);
        System.out.println("===== FRU & Sensors =====");
        System.out.println(result);
    }

    /**
     * 使用高级 API 获取机箱状态
     */
    @Test
    public void testGetChassisStatus() throws Exception {
        IpmiClientConfiguration config = new IpmiClientConfiguration(
                BMC_HOST, BMC_PORT, BMC_USER, BMC_PASS.toCharArray(), null, false, TIMEOUT_MS);

        String result = IpmiClient.getChassisStatusAsStringResult(config);
        System.out.println("===== Chassis Status =====");
        System.out.println(result);
    }

    ///**
    // * 获取传感器列表（结构化数据）
    // */
    //@Test
    //public void testGetSensors() throws Exception {
    //    IpmiClientConfiguration config = new IpmiClientConfiguration(
    //            BMC_HOST, BMC_PORT, BMC_USER, BMC_PASS.toCharArray(), null, false, TIMEOUT_MS);
    //
    //    List<Sensor> sensors = IpmiClient.getSensors(config);
    //    System.out.println("===== Sensors (" + sensors.size() + ") =====");
    //    for (Sensor sensor : sensors) {
    //        System.out.printf("  [%s] %s = %s %s (status: %s)%n",
    //                sensor.getSensorType(), sensor.getEntityName(),
    //                sensor.getValue(), sensor.getUnit(), sensor.getStatus());
    //    }
    //}
    //
    ///**
    // * 获取 FRU 列表（结构化数据）
    // */
    //@Test
    //public void testGetFrus() throws Exception {
    //    IpmiClientConfiguration config = new IpmiClientConfiguration(
    //            BMC_HOST, BMC_PORT, BMC_USER, BMC_PASS.toCharArray(), null, false, TIMEOUT_MS);
    //
    //    List<Fru> frus = IpmiClient.getFrus(config);
    //    System.out.println("===== FRUs (" + frus.size() + ") =====");
    //    for (Fru fru : frus) {
    //        System.out.println("  FRU: " + fru.getName());
    //        for (FruDevice device : fru.getDevices()) {
    //            System.out.println("    Device: " + device.getDeviceName());
    //        }
    //    }
    //}

    /**
     * 一次性获取全部信息
     */
    @Test
    public void testGetAll() throws Exception {
        IpmiClientConfiguration config = new IpmiClientConfiguration(
                BMC_HOST, BMC_PORT, BMC_USER, BMC_PASS.toCharArray(), null, false, TIMEOUT_MS);

        // 机箱状态
        System.out.println("===== Chassis Status =====");
        System.out.println(IpmiClient.getChassisStatusAsStringResult(config));

        // FRU 和传感器
        System.out.println("===== FRU & Sensors =====");
        System.out.println(IpmiClient.getFrusAndSensorsAsStringResult(config));
    }
}
