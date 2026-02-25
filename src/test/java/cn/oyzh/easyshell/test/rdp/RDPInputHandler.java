package cn.oyzh.easyshell.test.rdp;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * RDP 输入事件处理器
 * 将 JavaFX 键盘和鼠标事件转换为 RDP 协议格式
 */
public class RDPInputHandler {
    
    // RDP Input Event Types
    private static final int INPUT_EVENT_MOUSE = 1;
    private static final int INPUT_EVENT_KEYBOARD = 4;
    
    // Mouse Event Flags
    private static final int MOUSE_MOVE = 0x0800;
    private static final int MOUSE_BUTTON1_DOWN = 0x1000;
    private static final int MOUSE_BUTTON1_UP = 0x2000;
    private static final int MOUSE_BUTTON2_DOWN = 0x0040;
    private static final int MOUSE_BUTTON2_UP = 0x0080;
    private static final int MOUSE_WHEEL_UP = 0x0078;
    private static final int MOUSE_WHEEL_DOWN = 0xFF88;
    
    // Keyboard Event Flags
    private static final int KEYBOARD_FLAG_DOWN = 0x0000;
    private static final int KEYBOARD_FLAG_UP = 0x8000;
    private static final int KEYBOARD_FLAG_EXTENDED = 0x0100;
    
    private DataOutputStream outputStream;
    
    public RDPInputHandler(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }
    
    /**
     * 处理鼠标移动事件
     */
    public void handleMouseMove(MouseEvent event) throws IOException {
        int x = (int) event.getX();
        int y = (int) event.getY();
        
        sendMouseEvent(x, y, MOUSE_MOVE, 0);
    }
    
    /**
     * 处理鼠标按下事件
     */
    public void handleMousePressed(MouseEvent event) throws IOException {
        int x = (int) event.getX();
        int y = (int) event.getY();
        
        switch (event.getButton()) {
            case PRIMARY:
                sendMouseEvent(x, y, MOUSE_BUTTON1_DOWN, 0);
                break;
            case SECONDARY:
                sendMouseEvent(x, y, MOUSE_BUTTON2_DOWN, 0);
                break;
            default:
                break;
        }
    }
    
    /**
     * 处理鼠标释放事件
     */
    public void handleMouseReleased(MouseEvent event) throws IOException {
        int x = (int) event.getX();
        int y = (int) event.getY();
        
        switch (event.getButton()) {
            case PRIMARY:
                sendMouseEvent(x, y, MOUSE_BUTTON1_UP, 0);
                break;
            case SECONDARY:
                sendMouseEvent(x, y, MOUSE_BUTTON2_UP, 0);
                break;
            default:
                break;
        }
    }
    
    /**
     * 发送鼠标事件到 RDP 服务器
     */
    private void sendMouseEvent(int x, int y, int flags, int wheelData) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.putShort((short) flags);
        buffer.putShort((short) x);
        buffer.putShort((short) y);
        
        sendInputEvent(INPUT_EVENT_MOUSE, buffer.array());
    }
    
    /**
     * 处理键盘按下事件
     */
    public void handleKeyPressed(KeyEvent event) throws IOException {
        int scanCode = mapKeyToScanCode(event.getCode());
        
        if (scanCode > 0) {
            int flags = KEYBOARD_FLAG_DOWN;
            if (isExtendedKey(event.getCode())) {
                flags |= KEYBOARD_FLAG_EXTENDED;
            }
            
            sendKeyboardEvent(scanCode, flags);
        }
    }
    
    /**
     * 处理键盘释放事件
     */
    public void handleKeyReleased(KeyEvent event) throws IOException {
        int scanCode = mapKeyToScanCode(event.getCode());
        
        if (scanCode > 0) {
            int flags = KEYBOARD_FLAG_UP;
            if (isExtendedKey(event.getCode())) {
                flags |= KEYBOARD_FLAG_EXTENDED;
            }
            
            sendKeyboardEvent(scanCode, flags);
        }
    }
    
    /**
     * 发送键盘事件到 RDP 服务器
     */
    private void sendKeyboardEvent(int scanCode, int flags) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.putShort((short) flags);
        buffer.put((byte) (scanCode & 0xFF));
        
        sendInputEvent(INPUT_EVENT_KEYBOARD, buffer.array());
    }
    
    /**
     * JavaFX KeyCode 映射到 RDP 扫描码
     */
    private int mapKeyToScanCode(KeyCode code) {
        switch (code) {
            // 字母键
            case A: return 0x1E;
            case B: return 0x30;
            case C: return 0x2E;
            case D: return 0x20;
            case E: return 0x12;
            case F: return 0x21;
            case G: return 0x22;
            case H: return 0x23;
            case I: return 0x17;
            case J: return 0x24;
            case K: return 0x25;
            case L: return 0x26;
            case M: return 0x32;
            case N: return 0x31;
            case O: return 0x18;
            case P: return 0x19;
            case Q: return 0x10;
            case R: return 0x13;
            case S: return 0x1F;
            case T: return 0x14;
            case U: return 0x16;
            case V: return 0x2F;
            case W: return 0x11;
            case X: return 0x2D;
            case Y: return 0x15;
            case Z: return 0x2C;
            
            // 数字键
            case DIGIT0: return 0x0B;
            case DIGIT1: return 0x02;
            case DIGIT2: return 0x03;
            case DIGIT3: return 0x04;
            case DIGIT4: return 0x05;
            case DIGIT5: return 0x06;
            case DIGIT6: return 0x07;
            case DIGIT7: return 0x08;
            case DIGIT8: return 0x09;
            case DIGIT9: return 0x0A;
            
            // 功能键
            case ESCAPE: return 0x01;
            case TAB: return 0x0F;
            case CAPS: return 0x3A;
            case SHIFT: return 0x2A;
            case CONTROL: return 0x1D;
            case ALT: return 0x38;
            case SPACE: return 0x39;
            case ENTER: return 0x1C;
            case BACK_SPACE: return 0x0E;
            
            // 方向键
            case UP: return 0x48;
            case DOWN: return 0x50;
            case LEFT: return 0x4B;
            case RIGHT: return 0x4D;
            
            // Fn 键
            case F1: return 0x3B;
            case F2: return 0x3C;
            case F3: return 0x3D;
            case F4: return 0x3E;
            case F5: return 0x3F;
            case F6: return 0x40;
            case F7: return 0x41;
            case F8: return 0x42;
            case F9: return 0x43;
            case F10: return 0x44;
            case F11: return 0x57;
            case F12: return 0x58;
            
            // 其他常用键
            case DELETE: return 0x53;
            case HOME: return 0x47;
            case END: return 0x4F;
            case PAGE_UP: return 0x49;
            case PAGE_DOWN: return 0x51;
            case PRINTSCREEN: return 0x37;
            case INSERT: return 0x52;
            
            default: return -1;
        }
    }
    
    /**
     * 检查是否为扩展键（需要设置 extended 标志）
     */
    private boolean isExtendedKey(KeyCode code) {
        switch (code) {
            case UP:
            case DOWN:
            case LEFT:
            case RIGHT:
            case HOME:
            case END:
            case PAGE_UP:
            case PAGE_DOWN:
            case DELETE:
            case INSERT:
            case PRINTSCREEN:
            case DIVIDE:
            case ALT:
            case CONTROL:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * 发送输入事件到 RDP 服务器
     */
    private void sendInputEvent(int eventType, byte[] eventData) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(6 + eventData.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.putShort((short) eventType);
        buffer.putShort((short) eventData.length);
        buffer.put(eventData);
        
        synchronized (outputStream) {
            outputStream.write(buffer.array());
            outputStream.flush();
        }
    }
}