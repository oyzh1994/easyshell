package cn.oyzh.easyshell.test.rdp;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellConnState;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;

import java.io.EOFException;

/**
 * 原生 RDP 客户端实现
 * 基于 JavaFX Canvas 的 RDP 协议实现
 * 
 * @author oyzh
 * @since 2026-02-25
 */
public class NativeRDPClient implements ShellBaseClient {
    
    private ShellConnect shellConnect;
    private Canvas canvas;
    private RDPConnection0 rdpConnection;
    private BitmapRenderer bitmapRenderer;
    private RDPInputHandler inputHandler;
    
    private ObjectProperty<ShellConnState> stateProperty = 
        new SimpleObjectProperty<>(ShellConnState.NOT_INITIALIZED);
    
    private volatile boolean running = false;
    private Thread rdpMainLoop;
    
    public NativeRDPClient(ShellConnect shellConnect, Canvas canvas) {
        this.shellConnect = shellConnect;
        this.canvas = canvas;
    }
    
    @Override
    public void start(int timeout) throws Throwable {
        String host = shellConnect.hostIp();
        int port = shellConnect.hostPort();
        String username = shellConnect.getUser();
        String password = shellConnect.getPassword();
        
        // 设置默认分辨率
        int screenWidth = 1024;
        int screenHeight = 768;
        if (shellConnect.getResolution() != null) {
            String[] res = shellConnect.getResolution().split("x");
            if (res.length == 2) {
                screenWidth = Integer.parseInt(res[0]);
                screenHeight = Integer.parseInt(res[1]);
            }
        }
        
        // 初始化渲染器
        bitmapRenderer = new BitmapRenderer(canvas, screenWidth, screenHeight);
        
        // 创建 RDP 连接
        rdpConnection = new RDPConnection0(host, port, username, password);
        
        // 更新状态为连接中
        stateProperty.set(ShellConnState.CONNECTING);
        
        try {
            // 执行连接
            rdpConnection.connect();
            stateProperty.set(ShellConnState.CONNECTED);
            
            // 初始化输入处理器
            inputHandler = new RDPInputHandler(rdpConnection.getOutputStream());
            
            // 绑定事件处理
            setupInputBindings();
            
            // 启动主循环线程
            running = true;
            rdpMainLoop = new Thread(this::rdpMainLoop);
            rdpMainLoop.setName("RDP-MainLoop");
            rdpMainLoop.setDaemon(true);
            rdpMainLoop.start();
            
        } catch (Exception e) {
            stateProperty.set(ShellConnState.FAILED);
            throw e;
        }
    }
    
    /**
     * RDP 主循环 - 接收和处理服务器数据
     */
    private void rdpMainLoop() {
        try {
            RDPPDUParser parser = new RDPPDUParser(rdpConnection.getInputStream());
            
            while (running && rdpConnection.isConnected()) {
                try {
                    // 读取一个 RDP PDU
                    RDPPacket packet = parser.readPacket();
                    
                    if (packet != null && packet.getPduData() != null) {
                        // 处理位图更新
                        if (isBitmapUpdate(packet.getPduData())) {
                            BitmapUpdate update = parser.parseBitmapUpdate(packet.getPduData());
                            
                            // 渲染每个位图
                            for (BitmapData bitmap : update.getBitmaps()) {
                                bitmapRenderer.renderBitmap(
                                    bitmap.getX(),
                                    bitmap.getY(),
                                    bitmap.getWidth(),
                                    bitmap.getHeight(),
                                    bitmap.getBitsPerPixel(),
                                    bitmap.getData()
                                );
                            }
                        }
                    }
                    
                } catch (EOFException e) {
                    // 连接已关闭
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    if (running) {
                        Thread.sleep(100); // 避免频繁异常
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * 判断数据包是否为位图更新
     */
    private boolean isBitmapUpdate(byte[] data) {
        if (data.length < 2) return false;
        // 简化判断：检查更新类型标志
        byte updateType = data[0];
        return updateType == 0x01; // Bitmap Update Type
    }
    
    /**
     * 配置输入事件绑定
     */
    private void setupInputBindings() {
        canvas.setOnMouseMoved(event -> {
            try {
                inputHandler.handleMouseMove(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        canvas.setOnMousePressed(event -> {
            try {
                inputHandler.handleMousePressed(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        canvas.setOnMouseReleased(event -> {
            try {
                inputHandler.handleMouseReleased(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        canvas.setOnKeyPressed(event -> {
            try {
                inputHandler.handleKeyPressed(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        canvas.setOnKeyReleased(event -> {
            try {
                inputHandler.handleKeyReleased(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        // 确保 Canvas 能获取焦点
        canvas.setFocusTraversable(true);
        canvas.requestFocus();
    }
    
    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }
    
    @Override
    public boolean isConnected() {
        return rdpConnection != null && rdpConnection.isConnected();
    }
    
    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return stateProperty;
    }
    
    @Override
    public void close() throws Exception {
        running = false;
        stateProperty.set(ShellConnState.CLOSED);
        
        if (rdpMainLoop != null && rdpMainLoop.isAlive()) {
            rdpMainLoop.join(5000);
        }
        
        if (rdpConnection != null) {
            rdpConnection.disconnect();
        }
    }
}