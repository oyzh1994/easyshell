package cn.oyzh.easyshell.test.rdp;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * RDP 位图渲染器
 * 负责将 RDP 位图数据解码并渲染到 JavaFX Canvas
 */
public class BitmapRenderer {
    
    private Canvas canvas;
    private int screenWidth;
    private int screenHeight;
    
    public BitmapRenderer(Canvas canvas, int screenWidth, int screenHeight) {
        this.canvas = canvas;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
    
    /**
     * 渲染位图到画布
     */
    public void renderBitmap(int x, int y, int width, int height, 
                             int bitsPerPixel, byte[] bitmapData) {
        // 在 JavaFX 线程中执行渲染
        Platform.runLater(() -> {
            try {
                WritableImage image = decodeBitmap(width, height, bitsPerPixel, bitmapData);
                
                if (image != null) {
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.drawImage(image, x, y);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 解码 RDP 位图数据为 JavaFX Image
     * 支持 8-bit, 16-bit, 24-bit, 32-bit 色深
     */
    private WritableImage decodeBitmap(int width, int height, 
                                       int bitsPerPixel, byte[] bitmapData) {
        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();
        
        ByteBuffer buffer = ByteBuffer.wrap(bitmapData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        int bytesPerPixel = bitsPerPixel / 8;
        
        // 逐行处理位图数据 (RDP 位图数据可能是逆序的)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelIndex = (y * width + x) * bytesPerPixel;
                
                if (pixelIndex + bytesPerPixel <= bitmapData.length) {
                    int argb = extractPixel(buffer, pixelIndex, bitsPerPixel);
                    pixelWriter.setArgb(x, y, argb);
                }
            }
        }
        
        return image;
    }
    
    /**
     * 从位图数据中提取单个像素 ARGB 值
     */
    private int extractPixel(ByteBuffer buffer, int offset, int bitsPerPixel) {
        buffer.position(offset);
        
        switch (bitsPerPixel) {
            case 8:  // 8-bit 调色板模式 (需配置色表)
                byte paletteIdx = buffer.get();
                return 0xFF000000 | ((paletteIdx & 0xFF) << 16) 
                                  | ((paletteIdx & 0xFF) << 8) 
                                  | (paletteIdx & 0xFF);
                
            case 16: // 16-bit RGB565
                short pixel16 = buffer.getShort();
                int r = ((pixel16 >> 11) & 0x1F) << 3;
                int g = ((pixel16 >> 5) & 0x3F) << 2;
                int b = (pixel16 & 0x1F) << 3;
                return 0xFF000000 | (r << 16) | (g << 8) | b;
                
            case 24: // 24-bit RGB
                byte b24 = buffer.get();
                byte g24 = buffer.get();
                byte r24 = buffer.get();
                return 0xFF000000 | ((r24 & 0xFF) << 16) 
                                  | ((g24 & 0xFF) << 8) 
                                  | (b24 & 0xFF);
                
            case 32: // 32-bit BGRA
                byte b32 = buffer.get();
                byte g32 = buffer.get();
                byte r32 = buffer.get();
                byte a32 = buffer.get();
                return ((a32 & 0xFF) << 24) | ((r32 & 0xFF) << 16) 
                     | ((g32 & 0xFF) << 8) | (b32 & 0xFF);
                
            default:
                return 0xFF000000;
        }
    }
    
    /**
     * 清空画布
     */
    public void clearCanvas() {
        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, screenWidth, screenHeight);
        });
    }
    
    /**
     * 设置画布分辨率
     */
    public void setResolution(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        Platform.runLater(() -> {
            canvas.setWidth(width);
            canvas.setHeight(height);
        });
    }
}