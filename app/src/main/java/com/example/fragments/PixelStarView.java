package com.example.fragments;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class PixelStarView extends View {

    private static final int STAR_COUNT = 140;
    private static final int STAR_SIZE = 5;
    private static final int GLOW_RADIUS = 5;
    private static final int BLUR_RADIUS = 12;
    private static final int MAX_SHOOTING_STARS = 15;


    private float[] x = new float[STAR_COUNT];
    private float[] y = new float[STAR_COUNT];
    private float[] baseAlpha = new float[STAR_COUNT];
    private float[] twinkleProgress = new float[STAR_COUNT];
    private float[] twinkleSpeed = new float[STAR_COUNT];
    private float[] speed = new float[STAR_COUNT];

    private Paint starPaint = new Paint();
    private Paint glowPaint = new Paint();

    private Random random = new Random();

    private ShootingStar[] shootingStars = new ShootingStar[MAX_SHOOTING_STARS];

    public PixelStarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        starPaint.setColor(0xFFFFFFFF);
        starPaint.setAntiAlias(false);

        glowPaint.setColor(0x66FFFFFF);
        glowPaint.setMaskFilter(new BlurMaskFilter(BLUR_RADIUS, BlurMaskFilter.Blur.NORMAL));
    }

    private class ShootingStar {
        float x, y, speedX, speedY;
        int alpha = 255;

        ShootingStar() {
            x = random.nextInt(Math.max(1, getWidth()));
            y = 0;
            speedX = (random.nextFloat() - 0.5f) * 10;
            speedY = 10 + random.nextFloat() * 10;
        }

        void update() {
            x += speedX;
            y += speedY;
            alpha -= 5;
        }

        boolean isVisible() {
            return alpha > 0 && y < getHeight() && x > 0 && x < getWidth();
        }

        void draw(Canvas canvas, Paint paint) {
            paint.setAlpha(alpha);
            canvas.drawRect(x, y, x + STAR_SIZE, y + STAR_SIZE, paint);
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;

        for (int i = 0; i < STAR_COUNT; i++) {
            x[i] = random.nextInt(w);
            y[i] = random.nextInt(h);
            baseAlpha[i] = 0.2f + random.nextFloat() * 0.5f;
            twinkleProgress[i] = random.nextFloat() * (float) (2 * Math.PI);
            twinkleSpeed[i] = 0.01f + random.nextFloat() * 0.04f;
            speed[i] = 0.2f + random.nextFloat() * 0.5f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Don't animate in preview to save resources and avoid flicker
        boolean isPreview = isInEditMode();

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        for (int i = 0; i < STAR_COUNT; i++) {
            if (!isPreview) {
                y[i] += speed[i];
                if (y[i] > height) {
                    y[i] = 0;
                    x[i] = random.nextInt(width);
                }
                twinkleProgress[i] += twinkleSpeed[i];
            }

            float pulse = (float) Math.abs(Math.sin(twinkleProgress[i]));
            float alpha = baseAlpha[i] * (0.3f + pulse * 0.7f);
            int a = (int) (alpha * 255);

            starPaint.setAlpha(a);
            glowPaint.setAlpha(a / 2);

            canvas.drawCircle(x[i] + STAR_SIZE / 2f, y[i] + STAR_SIZE / 2f, GLOW_RADIUS, glowPaint);
            canvas.drawRect(x[i], y[i], x[i] + STAR_SIZE, y[i] + STAR_SIZE, starPaint);
        }

        if (!isPreview) {
            if (random.nextInt(200) == 0) {
                for (int i = 0; i < MAX_SHOOTING_STARS; i++) {
                    if (shootingStars[i] == null || !shootingStars[i].isVisible()) {
                        shootingStars[i] = new ShootingStar();
                        break;
                    }
                }
            }

            for (int i = 0; i < MAX_SHOOTING_STARS; i++) {
                if (shootingStars[i] != null) {
                    shootingStars[i].update();
                    if (shootingStars[i].isVisible()) {
                        shootingStars[i].draw(canvas, starPaint);
                    } else {
                        shootingStars[i] = null;
                    }
                }
            }
            postInvalidateOnAnimation();
        }
    }
}