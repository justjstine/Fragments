package com.example.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom View for animated pixel-art background.
 * Includes moving clouds and animated waves.
 */
public class SummerView extends View {

    private final Paint paint = new Paint();
    private float waveOffset = 0;
    private final List<Cloud> clouds = new ArrayList<>();
    
    // Pixel block size for the retro aesthetic
    private static final int PIXEL_SIZE = 12;

    public SummerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(false); // Keep pixel edges sharp
        initClouds();
    }

    private void initClouds() {
        // Initialize clouds with different positions and speeds for parallax effect
        clouds.add(new Cloud(-100, 120, 0.6f));
        clouds.add(new Cloud(300, 250, 0.4f));
        clouds.add(new Cloud(700, 180, 0.5f));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // We do NOT draw a background color here to keep it transparent,
        // so your bg_summer.png remains visible underneath.

        // 1. Animate and Draw Clouds
        paint.setColor(Color.WHITE);
        for (Cloud cloud : clouds) {
            drawPixelCloud(canvas, cloud.x, cloud.y);
            cloud.x += cloud.speed;
            
            // Loop clouds back to the left side
            if (cloud.x > getWidth()) {
                cloud.x = -200;
            }
        }

        // 2. Animate and Draw Waves
        drawPixelWaves(canvas);

        // Continuous animation loop
        postInvalidateOnAnimation();
    }

    private void drawPixelCloud(Canvas canvas, float x, float y) {
        // Blocky pixel-art cloud shape
        canvas.drawRect(x, y, x + 120, y + 40, paint);
        canvas.drawRect(x + 20, y - 20, x + 100, y, paint);
        canvas.drawRect(x + 40, y - 40, x + 80, y - 20, paint);
    }

    private void drawPixelWaves(Canvas canvas) {
        waveOffset += 0.04f;
        int width = getWidth();
        int height = getHeight();
        int horizonY = height / 2 + 100;

        // Tropical sea colors
        String[] colors = {"#4DD0E1", "#00BCD4", "#0097A7"};

        for (int i = 0; i < 3; i++) {
            paint.setColor(Color.parseColor(colors[i]));
            int layerY = horizonY + (i * 80);

            for (int x = 0; x < width; x += PIXEL_SIZE) {
                // Sine wave calculation for movement, snapped to pixel grid
                double wave = Math.sin((x * 0.01) + waveOffset + i);
                int snappedY = (int) (wave * 20 / PIXEL_SIZE) * PIXEL_SIZE;

                canvas.drawRect(
                        x, 
                        layerY + snappedY, 
                        x + PIXEL_SIZE, 
                        height, 
                        paint
                );
            }
        }
    }

    private static class Cloud {
        float x, y, speed;
        Cloud(float x, float y, float speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
        }
    }
}
