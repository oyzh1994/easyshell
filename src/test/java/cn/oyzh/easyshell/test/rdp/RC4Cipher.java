package cn.oyzh.easyshell.test.rdp;

/**
 * RC4 密码实现 - 用于 RDP 加密/解密
 */
public class RC4Cipher {
    
    private byte[] state;
    private int x;
    private int y;
    
    public RC4Cipher(byte[] key) {
        init(key);
    }
    
    /**
     * 初始�� RC4 状态向量
     */
    private void init(byte[] key) {
        state = new byte[256];
        
        // 初始化排列
        for (int i = 0; i < 256; i++) {
            state[i] = (byte) i;
        }
        
        // KSA (Key Scheduling Algorithm)
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + (state[i] & 0xFF) + (key[i % key.length] & 0xFF)) & 0xFF;
            
            // 交换
            byte temp = state[i];
            state[i] = state[j];
            state[j] = temp;
        }
        
        x = 0;
        y = 0;
    }
    
    /**
     * 加密或解密数据（RC4 的加密和解密相同）
     */
    public byte[] crypt(byte[] data) {
        byte[] output = new byte[data.length];
        
        for (int i = 0; i < data.length; i++) {
            // PRGA (Pseudo-Random Generation Algorithm)
            x = (x + 1) & 0xFF;
            y = (y + (state[x] & 0xFF)) & 0xFF;
            
            // 交换
            byte temp = state[x];
            state[x] = state[y];
            state[y] = temp;
            
            // 生成密钥流字节
            int index = ((state[x] & 0xFF) + (state[y] & 0xFF)) & 0xFF;
            byte keyStream = state[index];
            
            // XOR 与输入
            output[i] = (byte) (data[i] ^ keyStream);
        }
        
        return output;
    }
}