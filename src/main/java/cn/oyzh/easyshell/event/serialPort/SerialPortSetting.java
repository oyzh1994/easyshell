package cn.oyzh.easyshell.event.serialPort;

import com.fazecast.jSerialComm.SerialPort;

import java.nio.charset.Charset;

/**
 * @author Iammm
 * 2025/4/7 19:02
 */
public record SerialPortSetting(
        SerialPort serialPort,
        Integer baudRate,
        Integer dataBits,
        Integer stopBits,
        Integer parity,
        Integer flowControl,
        Charset outCharset,
        Charset inCharset
) {

}