package ru.codfi.Utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import ru.codfi.R;

/**
 * Created by ilyas on 17-Nov-18.
 */

public class MapItemView extends android.support.v7.widget.AppCompatImageView {

    public static final int NO_COLOR = 0;
    public static final int INVADED = 1;
    public static final int NOT_INVADED = 0;

    private static final int RED = Color.RED;
    private static final int GREEN = Color.GREEN;

    private boolean isCapital;
    private int zoneColor;
    private int whoseZone;
    private Context context;
    private MapEventListener listener;
    private boolean isEnabled = true;

    private ValueAnimator colorAnim;


    public interface MapEventListener {
        public void onZoneTouch(MapItemView v);
    }


    public void setMapListener(MapEventListener l) {
        this.listener = l;
    }

    public MapItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MapItemView,
                0, 0);

        try {
            isCapital = a.getBoolean(R.styleable.MapItemView_isCapital, false);
            zoneColor = a.getInteger(R.styleable.MapItemView_zoneColor, 0);
            whoseZone = a.getInteger(R.styleable.MapItemView_whoseZone, 0);
        } finally {
            a.recycle();
        }
        setDrawingCacheEnabled(true);
        setEnabled(true);
    }

    public boolean isCapital() {
        return isCapital;
    }

    public void setCapital(boolean capital) {
        isCapital = capital;
        invalidate();
        requestLayout();
    }

    public int getZoneColor() {
        return zoneColor;
    }

    public void setZoneColor(int zoneColor) {
        this.zoneColor = zoneColor;
        invalidate();
        requestLayout();
    }


    public int getWhoseZone() {
        return whoseZone;
    }

    public void setWhoseZone(int whoseZone) {
        this.whoseZone = whoseZone;
        invalidate();
        requestLayout();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled) {
            Bitmap bmp = Bitmap.createBitmap(getDrawingCache());
            int color = 0;
            try {
                color = bmp.getPixel((int) event.getX(), (int) event.getY());
            } catch (Exception e) {
                Log.e("COLOR ERROR", e.toString());
            }

            if (color != Color.TRANSPARENT) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (listener != null) listener.onZoneTouch(this);
                        break;
                    default:
                        break;
                }
            }
        }
        return super.onTouchEvent(event);
    }


    public void paintZone(final int res, final int color) {

        Bitmap zone = BitmapFactory.decodeResource(getResources(), res).copy(Bitmap.Config.ARGB_8888, true);
        Paint p = new Paint(Paint.FILTER_BITMAP_FLAG);
        final Bitmap bmOverlay = Bitmap.createBitmap(zone.getWidth(), zone.getHeight(), zone.getConfig());
        final Canvas canv = new Canvas(bmOverlay);
        ColorFilter filter = new LightingColorFilter(color, 0);
        zone.setHasAlpha(true);
        p.setColorFilter(filter);

        canv.drawBitmap(zone, new Matrix(), p);

        if (color == RED) {
            setZoneColor(RED);
            setWhoseZone(NOT_INVADED);
        } else if (color == GREEN) {
            setZoneColor(GREEN);
            setWhoseZone(INVADED);
        }
        setImageBitmap(bmOverlay);

    }

    public void paintZoneCapital(final int res, final int color) {

        Bitmap zone = BitmapFactory.decodeResource(getResources(), res).copy(Bitmap.Config.ARGB_8888, true);
        Paint p = new Paint(Paint.FILTER_BITMAP_FLAG);
        final Bitmap bmOverlay = Bitmap.createBitmap(zone.getWidth(), zone.getHeight(), zone.getConfig());
        final Canvas canv = new Canvas(bmOverlay);
        ColorFilter filter = new LightingColorFilter(color, 0);
        zone.setHasAlpha(true);
        p.setColorFilter(filter);

        int sumY = 0, sumX = 0, num = 0;

        for (int y = 0; y < zone.getHeight(); y += 3) {
            for (int x = 0; x < zone.getWidth(); x += 3) {
                if (zone.getPixel(x, y) != Color.TRANSPARENT) {
                    sumX += x;
                    sumY += y;
                    num++;
                }
            }
        }

        sumX = sumX / num;
        sumY = sumY / num;

        canv.drawBitmap(zone, new Matrix(), p);

        final int finalSumX = sumX;
        final int finalSumY = sumY;

        Bitmap castle = BitmapFactory.decodeResource(getResources(), R.drawable.castle1).copy(Bitmap.Config.ARGB_8888, true);
        canv.drawBitmap(castle, finalSumX - (castle.getWidth() / 2), finalSumY - (castle.getHeight() / 2), null);
        setCapital(true);

        if (color == RED) {
            setZoneColor(RED);
            setWhoseZone(NOT_INVADED);
        } else if (color == GREEN) {
            setZoneColor(GREEN);
            setWhoseZone(INVADED);
        }
        setImageBitmap(bmOverlay);

    }

    public void startAnimation() {
        float factor = 0.6f;
        final int orange = getResources().getColor(android.R.color.holo_green_dark);

        colorAnim = ObjectAnimator.ofFloat(0.2f, 0.8f);
        colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float mul = (Float) animation.getAnimatedValue();
                int alphaOrange = adjustAlpha(orange, mul);
                setColorFilter(alphaOrange, PorterDuff.Mode.SRC_ATOP);
                if (mul == 0.0) {
                    setColorFilter(null);
                }
            }
        });

        colorAnim.setDuration(1500);
        colorAnim.setRepeatMode(ValueAnimator.REVERSE);
        colorAnim.setRepeatCount(-1);
        colorAnim.start();

        int alphaOrange = adjustAlpha(orange, factor);
        setColorFilter(alphaOrange, PorterDuff.Mode.SRC_ATOP);
    }

    public void stopAnimation() {
        if (colorAnim != null) {
            colorAnim.end();
            clearColorFilter();
        }
    }

    public void disableZone() {
        setEnabled(false);
        this.isEnabled = false;
//        setColorFilter(adjustAlpha(Color.GRAY, 0.5f), PorterDuff.Mode.SRC_ATOP);
    }

    public void enableZone() {
        setEnabled(true);
        this.isEnabled = true;
//        clearColorFilter();
    }

    public void limitClick() {
        setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setEnabled(true);

            }
        }, 1000);
    }

    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }


}