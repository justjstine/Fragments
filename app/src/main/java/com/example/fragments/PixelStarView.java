package com.example.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A custom view that renders animated pixelated stars.
 */
public class PixelStarView extends View {

    private final Paint starPaint = new Paint();
    private final List<Star> stars = new ArrayList<>();
    private final Random random = new Random();
    
    // Pixel size for the "blocky" retro look
    private static final int PIXEL_SIZE = 8;
    private static final int STAR_COUNT = 40;

    public PixelStarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Disable anti-aliasing for sharp pixel edges
        starPaint.setAntiAlias(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            initStars(w, h);
        }
    }

    private void initStars(int width, int height) {
        stars.clear();
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star(
                    random.nextInt(width),
                    random.nextInt(height),
                    random.nextInt(2) + 1, // Star size: 1 or 2 pixels
                    random.nextFloat()     // Initial alpha
            ));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Star star : stars) {
            star.update();
            starPaint.setColor(Color.WHITE);
            starPaint.setAlpha((int) (star.alpha * 255));
            
            // Snap coordinates to pixel grid
            float snappedX = (int)(star.x / PIXEL_SIZE) * PIXEL_SIZE;
            float snappedY = (int)(star.y / PIXEL_SIZE) * PIXEL_SIZE;
            float size = star.size * PIXEL_SIZE;

            canvas.drawRect(snappedX, snappedY, snappedX + size, snappedY + size, starPaint);
        }

        // Keep animating
        postInvalidateOnAnimation();
    }

    /**
     * Internal class to represent a single star and its animation state.
     */
    private class Star {
        float x, y;
        int size;
        float alpha;
        float alphaSpeed;

        Star(float x, float y, int size, float alpha) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.alpha = alpha;
            // Randomize twinkle speed
            this.alphaSpeed = 0.005f + random.nextFloat() * 0.015f;
        }

        void update() {
            alpha += alphaSpeed;
            if (alpha > 1.0f) {
                alpha = 1.0f;
                alphaSpeed = -Math.abs(alphaSpeed);
            } else if (alpha < 0.1f) {
                alpha = 0.1f;
                alphaSpeed = Math.abs(alphaSpeed);
            }
        }
    }
}
