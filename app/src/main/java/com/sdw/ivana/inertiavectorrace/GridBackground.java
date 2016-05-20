package com.sdw.ivana.inertiavectorrace;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;


/**
 * View
 */
public class GridBackground extends View {
    private int paperColor = ContextCompat.getColor(getContext(), R.color.divider);
    private int gridRows;
    private int gridSize;
    private Paint gridPaint;
    private Paint textPaint;
    private int gapVer;
    private int gapHor;

    public GridBackground(Context context) {
        super(context);
        init(null, 0);
    }

    public GridBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GridBackground(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.PaperGridView, defStyle, 0);
        int gridColor = a.getColor(R.styleable.PaperGridView_gridColor, ContextCompat.getColor(getContext(),
                R.color.primary));
        paperColor = a.getColor(R.styleable.PaperGridView_paperColor, ContextCompat.getColor(getContext(),
                R.color.primary_light));
        gridRows = a.getInt(R.styleable.PaperGridView_rows, 9);
        a.recycle();

        // Set Paints
        gridPaint = new Paint();
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{5,5,5,5}, 0));
        gridPaint.setAntiAlias(true);
        gridPaint.setColor(gridColor);
        gridPaint.setAlpha(40);
        textPaint = new Paint();
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.secondary_text));
        textPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(paperColor);
        gridSize = getMeasuredHeight() / gridRows;
        gapVer = (getMeasuredHeight() % gridSize) / 2;
        gapHor = (getMeasuredWidth() % gridSize) / 2;
        drawGrid(canvas);
    }

    private void drawGrid(Canvas canvas) {
        for (int y = gapVer; y < getHeight(); y += gridSize) {
            canvas.drawLine(0, y, getWidth(), y, gridPaint);
        }
        for (int x = gapHor; x < getWidth(); x += gridSize) {
            canvas.drawLine(x, 0, x, getHeight(), gridPaint);
        }
    }
}
