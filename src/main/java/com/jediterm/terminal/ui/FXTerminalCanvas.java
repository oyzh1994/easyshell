package com.jediterm.terminal.ui;

import cn.oyzh.fx.plus.adapter.DestroyAdapter;
import cn.oyzh.fx.plus.controls.pane.FXPane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;


/**
 * 终端渲染组件
 *
 * @author oyzh
 * @since 2025/04/24
 */
public class FXTerminalCanvas extends FXPane implements DestroyAdapter {

    public FXTerminalCanvas() {
        Canvas canvas = new Canvas();
        // canvas.setCache(true);
        // canvas.setCacheHint(CacheHint.QUALITY);
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());
        this.addChild(canvas);
    }

    public Canvas getCanvas() {
        return (Canvas) this.getFirstChild();
    }

    public GraphicsContext getGraphicsContext2D() {
        return this.getCanvas().getGraphicsContext2D();
    }

    @Override
    public void destroy() {
        Canvas canvas = this.getCanvas();
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
//        NodeDestroyUtil.destroyObject(canvas);
        DestroyAdapter.super.destroy();
    }
}