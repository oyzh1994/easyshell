package cn.oyzh.easyshell.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.net.bsd.RLoginClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class HandwritingApp extends Application {

    private Canvas canvas;
    private GraphicsContext gc;
    private double strokeWidth = 3.0;
    private Color strokeColor = Color.BLACK;
    
    // 绘图历史管理
    private Stack<List<PathPoint>> undoStack = new Stack<>();
    private Stack<List<PathPoint>> redoStack = new Stack<>();
    private List<PathPoint> currentPath = new ArrayList<>();
    
    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        
        // 初始化工具栏
        VBox toolbar = createToolbar();
        
        // 设置绘图事件
        setupDrawingEvents();
        
        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setBottom(toolbar);
        
        primaryStage.setScene(new Scene(root, 800, 650));
        primaryStage.setTitle("JavaFX 手写输入");
        primaryStage.show();
    }
    
    private VBox createToolbar() {
        // 颜色选择器
        ColorPicker colorPicker = new ColorPicker(strokeColor);
        colorPicker.setOnAction(e -> strokeColor = colorPicker.getValue());
        
        // 笔触粗细滑块
        Slider widthSlider = new Slider(1, 10, strokeWidth);
        widthSlider.setShowTickLabels(true);
        widthSlider.setShowTickMarks(true);
        widthSlider.setMajorTickUnit(1);
        widthSlider.valueProperty().addListener((obs, oldVal, newVal) ->
            strokeWidth = newVal.doubleValue()
        );
        
        // 撤销按钮
        Button undoBtn = new Button("撤销");
        undoBtn.setOnAction(e -> undo());
        
        // 重做按钮
        Button redoBtn = new Button("重做");
        redoBtn.setOnAction(e -> redo());
        
        // 清除按钮
        Button clearBtn = new Button("清除");
        clearBtn.setOnAction(e -> clearCanvas());
        
        // 组合工具栏
        HBox controls = new HBox(15);
        controls.getChildren().addAll(
            new VBox(5, new javafx.scene.control.Label("颜色"), colorPicker),
            new VBox(5, new javafx.scene.control.Label("粗细"), widthSlider),
            undoBtn, redoBtn, clearBtn
        );
        
        return new VBox(10, controls);
    }
    
    private void setupDrawingEvents() {
        // 鼠标按下 - 开始新路径
        canvas.setOnMousePressed(e -> {
            currentPath = new ArrayList<>();
            currentPath.add(new PathPoint(e.getX(), e.getY(), strokeWidth, strokeColor));
            redoStack.clear(); // 开始新操作后清除重做栈
        });
        
        // 鼠标拖动 - 记录路径点
        canvas.setOnMouseDragged(e -> {
            currentPath.add(new PathPoint(e.getX(), e.getY(), strokeWidth, strokeColor));
            redrawCanvas();
        });
        
        // 鼠标释放 - 完成路径并保存到撤销栈
        canvas.setOnMouseReleased(e -> {
            if (!currentPath.isEmpty()) {
                undoStack.push(new ArrayList<>(currentPath));
                currentPath = new ArrayList<>();
            }
        });
    }
    
    private void redrawCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // 绘制所有已保存的路径
        for (List<PathPoint> path : undoStack) {
            drawPath(path);
        }
        
        // 绘制当前正在绘制的路径
        if (!currentPath.isEmpty()) {
            drawPath(currentPath);
        }
    }
    
    private void drawPath(List<PathPoint> path) {
        if (path.isEmpty()) return;
        
        PathPoint firstPoint = path.get(0);
        gc.setLineWidth(firstPoint.width);
        gc.setStroke(firstPoint.color);
        
        gc.beginPath();
        gc.moveTo(firstPoint.x, firstPoint.y);
        
        for (int i = 1; i < path.size(); i++) {
            PathPoint p = path.get(i);
            gc.lineTo(p.x, p.y);
            gc.setLineWidth(p.width);
            gc.setStroke(p.color);
        }
        
        gc.stroke();
        gc.closePath();
    }
    
    private void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(undoStack.pop());
            redrawCanvas();
        }
    }
    
    private void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(redoStack.pop());
            redrawCanvas();
        }
    }
    
    private void clearCanvas() {
        redoStack.clear();
        if (!undoStack.isEmpty()) {
            undoStack.clear();
            redrawCanvas();
        }
    }
    
    // 路径点类 - 记录坐标、笔触宽度和颜色
    private static class PathPoint {
        double x, y;
        double width;
        Color color;
        
        public PathPoint(double x, double y, double width, Color color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.color = color;
        }
    }
    
    public static void main(String[] args) {
        launch(args);
        RLoginClient loginClient = new RLoginClient();
    }

    public static class HandwritingAppStarter{

        public static void main(String[] args) {
            HandwritingApp.main(args);
        }
    }
}