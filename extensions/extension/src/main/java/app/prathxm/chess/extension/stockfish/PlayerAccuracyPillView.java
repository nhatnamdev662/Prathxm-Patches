/*
 * Copyright 2026 NNVC
 * https://github.com/nhatnamdev662/Prathxm-Patches
 */

package app.prathxm.chess.extension.stockfish;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

/**
 * PlayerAccuracyPillView – Thẻ hiển thị Độ Chính Xác (%) và Estimated Elo thời gian thực
 * gắn trực tiếp sát mép trên/dưới của bàn cờ Chess.com theo phong cách Cyber Glass.
 */
public class PlayerAccuracyPillView extends View {

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pieceCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint roleBadgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF bounds = new RectF();
    private final RectF roleBounds = new RectF();

    private boolean isWhite = true;
    private boolean isYou = true;
    private float accuracy = -1f;
    private int estimatedElo = -1;

    public PlayerAccuracyPillView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);

        bgPaint.setStyle(Paint.Style.FILL);
        strokePaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStyle(Paint.Style.STROKE);
        pieceCirclePaint.setStyle(Paint.Style.FILL);
        roleBadgePaint.setStyle(Paint.Style.FILL);

        textPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        valPaint.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        labelPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
    }

    public void updateData(boolean isWhite, boolean isYou, float accuracy, int estimatedElo) {
        this.isWhite = isWhite;
        this.isYou = isYou;
        this.accuracy = accuracy;
        this.estimatedElo = estimatedElo;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float density = getResources().getDisplayMetrics().density;
        float radius = h / 2f;
        bounds.set(1.5f * density, 1.5f * density, w - 1.5f * density, h - 1.5f * density);

        // 1. Nền thẻ Cyber Glass
        if (isYou) {
            bgPaint.setShader(new LinearGradient(0, 0, w, 0,
                    0xF00D1624, 0xF0142338, Shader.TileMode.CLAMP));
            strokePaint.setColor(0xCC0A84FF); // Cyber Blue
            strokePaint.setStrokeWidth(1.5f * density);
        } else {
            bgPaint.setShader(new LinearGradient(0, 0, w, 0,
                    0xEB11151D, 0xEB1A202A, Shader.TileMode.CLAMP));
            strokePaint.setColor(0x4DFFFFFF);
            strokePaint.setStrokeWidth(1f * density);
        }
        canvas.drawRoundRect(bounds, radius, radius, bgPaint);
        canvas.drawRoundRect(bounds, radius, radius, strokePaint);

        float cx = radius + (2 * density);
        float cy = h / 2f;

        // 2. Biểu tượng quân cờ tròn (Trắng hoặc Đen)
        pieceCirclePaint.setColor(isWhite ? 0xFFF5F7FA : 0xFF2A2E39);
        canvas.drawCircle(cx, cy, 6.5f * density, pieceCirclePaint);
        if (isWhite) {
            strokePaint.setColor(0x33000000);
            strokePaint.setStrokeWidth(1f * density);
            canvas.drawCircle(cx, cy, 6.5f * density, strokePaint);
        } else {
            strokePaint.setColor(0x4DFFFFFF);
            strokePaint.setStrokeWidth(1f * density);
            canvas.drawCircle(cx, cy, 6.5f * density, strokePaint);
        }

        // 3. Huy hiệu [Bạn] / [Đối thủ]
        float startX = cx + (10 * density);
        String roleText = isYou ? "BẠN" : "ĐỐI THỦ";
        textPaint.setTextSize(9.5f * density);
        float roleWidth = textPaint.measureText(roleText);
        float roleH = 14 * density;
        roleBounds.set(startX, cy - (roleH / 2f), startX + roleWidth + (8 * density), cy + (roleH / 2f));
        roleBadgePaint.setColor(isYou ? 0xDD0A84FF : 0x33FFFFFF);
        canvas.drawRoundRect(roleBounds, 4 * density, 4 * density, roleBadgePaint);

        textPaint.setColor(0xFFFFFFFF);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = cy - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(roleText, startX + (4 * density), textY, textPaint);

        // 4. Nhãn Accuracy & Estimated Elo
        float curX = roleBounds.right + (8 * density);

        // Dải Accuracy
        labelPaint.setTextSize(8.5f * density);
        labelPaint.setColor(0x99FFFFFF);
        canvas.drawText("CHÍNH XÁC", curX, cy - (2 * density), labelPaint);

        valPaint.setTextSize(11f * density);
        valPaint.setColor(accuracy >= 85f ? 0xFF34D399 : (accuracy >= 70f ? 0xFFF0B84B : 0xFF64D2FF));
        String accStr = (accuracy >= 0f) ? String.format(java.util.Locale.US, "%.1f%%", accuracy) : "--";
        canvas.drawText(accStr, curX, cy + (9 * density), valPaint);

        curX += Math.max(valPaint.measureText(accStr), labelPaint.measureText("CHÍNH XÁC")) + (10 * density);

        // Đường phân cách mỏng
        strokePaint.setColor(0x26FFFFFF);
        strokePaint.setStrokeWidth(1f * density);
        canvas.drawLine(curX, cy - (8 * density), curX, cy + (8 * density), strokePaint);
        curX += (8 * density);

        // Dải Estimated Elo
        labelPaint.setColor(0x99FFFFFF);
        canvas.drawText("ELO ĐÁNH GIÁ", curX, cy - (2 * density), labelPaint);

        valPaint.setColor(0xFFFFD760); // Gold
        String eloStr = (estimatedElo > 0) ? String.valueOf(estimatedElo) : "--";
        canvas.drawText(eloStr, curX, cy + (9 * density), valPaint);
    }
}
