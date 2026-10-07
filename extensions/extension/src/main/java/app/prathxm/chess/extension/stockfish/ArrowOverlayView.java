/*
 * Copyright 2026 NNVC
 * https://github.com/nhatnamdev662/Prathxm-Patches
 */

package app.prathxm.chess.extension.stockfish;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Canvas Overlay for rendering glowing cyberpunk arrows matching 100% of the
 * NNVC Chess extension style: multi-tier palette, linear gradients, highlight edge strokes,
 * and neon drop shadows / blur glows.
 */
public class ArrowOverlayView extends View {

    public static class ArrowData {
        public final String move;
        public final String from;
        public final String to;
        public final int tier;
        public final boolean isThreat;
        public final String evalText;

        public ArrowData(String move, int tier, boolean isThreat) {
            this(move, tier, isThreat, null);
        }

        public ArrowData(String move, int tier, boolean isThreat, String evalText) {
            this.move = move;
            this.from = move.substring(0, 2);
            this.to = move.substring(2, 4);
            this.tier = tier;
            this.isThreat = isThreat;
            this.evalText = evalText;
        }
    }

    private static class HeadPoints {
        final float tipX, tipY;
        final float leftOuterX, leftOuterY;
        final float rightOuterX, rightOuterY;
        final float leftNotchX, leftNotchY;
        final float rightNotchX, rightNotchY;

        HeadPoints(float tipX, float tipY,
                   float leftOuterX, float leftOuterY,
                   float rightOuterX, float rightOuterY,
                   float leftNotchX, float leftNotchY,
                   float rightNotchX, float rightNotchY) {
            this.tipX = tipX;
            this.tipY = tipY;
            this.leftOuterX = leftOuterX;
            this.leftOuterY = leftOuterY;
            this.rightOuterX = rightOuterX;
            this.rightOuterY = rightOuterY;
            this.leftNotchX = leftNotchX;
            this.leftNotchY = leftNotchY;
            this.rightNotchX = rightNotchX;
            this.rightNotchY = rightNotchY;
        }
    }

    private static class BadgeLayout {
        float x;
        float y;
        float width;
        float height;
        String text;
        int tier;
        boolean isThreat;
    }

    private final List<ArrowData> arrows = new ArrayList<>();
    private boolean flipped = false;

    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint badgeBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint badgeStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint badgeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint badgeGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ArrowOverlayView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        // Software layer ensures BlurMaskFilter renders smoothly on all devices
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        fillPaint.setStyle(Paint.Style.FILL);

        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeJoin(Paint.Join.ROUND);
        edgePaint.setStrokeCap(Paint.Cap.ROUND);

        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeJoin(Paint.Join.ROUND);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);

        badgeBgPaint.setStyle(Paint.Style.FILL);

        badgeStrokePaint.setStyle(Paint.Style.STROKE);
        badgeStrokePaint.setStrokeJoin(Paint.Join.ROUND);

        badgeGlowPaint.setStyle(Paint.Style.STROKE);
        badgeGlowPaint.setStrokeJoin(Paint.Join.ROUND);

        badgeTextPaint.setTypeface(android.graphics.Typeface.create("monospace", android.graphics.Typeface.BOLD));
        badgeTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never consume touches so user can move pieces underneath
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return false;
    }

    public void update(int boardX, int boardY, int width, int height, List<ArrowData> newArrows, boolean flipped) {
        this.flipped = flipped;
        this.arrows.clear();
        if (newArrows != null) {
            this.arrows.addAll(newArrows);
        }

        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(width, height);
        } else {
            lp.width = width;
            lp.height = height;
        }
        setLayoutParams(lp);
        setTranslationX(boardX);
        setTranslationY(boardY);

        invalidate();
    }

    public void clear() {
        this.arrows.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (arrows.isEmpty()) return;

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float sqSize = Math.min(w, h) / 8.0f;

        // 1. Draw arrows in reverse order (Tier 5 first, Tier 1 last so Tier 1 is on top)
        for (int i = arrows.size() - 1; i >= 0; i--) {
            ArrowData arrow = arrows.get(i);
            float perpOffset = computePerpOffset(i, sqSize);
            drawSingleArrow(canvas, arrow, sqSize, perpOffset);
        }

        // 2. Draw Eval Badges with Anti-collision avoidance synchronized with arrows
        drawEvalBadges(canvas, sqSize);
    }

    /**
     * Compute orthogonal lane separation (perpOffset) matching NNVC extension
     * so concurrent arrows sharing paths, opposite directions, or common squares don't overlap.
     */
    private float computePerpOffset(int currentIndex, float sqSize) {
        if (currentIndex <= 0 || currentIndex >= arrows.size()) return 0f;
        ArrowData current = arrows.get(currentIndex);
        float offset = 0f;

        for (int j = 0; j < currentIndex; j++) {
            ArrowData prev = arrows.get(j);
            boolean sameDirect = current.from.equals(prev.from) && current.to.equals(prev.to);
            boolean opposite   = current.from.equals(prev.to)   && current.to.equals(prev.from);
            boolean sameTarget = current.to.equals(prev.to);
            boolean sameSource = current.from.equals(prev.from);

            if (sameDirect) {
                offset += sqSize * 0.12f;
            } else if (opposite) {
                offset += sqSize * 0.10f;
            } else if (sameTarget) {
                offset += (currentIndex % 2 == 1 ? 1 : -1) * sqSize * 0.08f;
            } else if (sameSource) {
                offset += (currentIndex % 2 == 1 ? 1 : -1) * sqSize * 0.06f;
            }
        }
        return offset;
    }

    private void drawSingleArrow(Canvas canvas, ArrowData arrow, float sqSize, float perpOffset) {
        float[] fromCenter = getSquareCenter(arrow.from, flipped, sqSize);
        float[] toCenter   = getSquareCenter(arrow.to,   flipped, sqSize);
        if (fromCenter == null || toCenter == null) return;

        float x1 = fromCenter[0], y1 = fromCenter[1];
        float x2 = toCenter[0],   y2 = toCenter[1];

        int fileDelta = Math.abs(arrow.to.charAt(0) - arrow.from.charAt(0));
        int rankDelta = Math.abs(arrow.to.charAt(1) - arrow.from.charAt(1));
        boolean isKnight = (fileDelta == 1 && rankDelta == 2) || (fileDelta == 2 && rankDelta == 1);

        float thicknessScale = Math.max(0.70f, 1.0f - 0.06f * (arrow.tier - 1));
        float baseShaftHalf = 0.042f;
        float baseHeadHalf = 0.17f;
        float baseHeadLen = 0.24f;

        float shaftHalf = sqSize * baseShaftHalf * thicknessScale;
        float neckHalf = shaftHalf;
        float headHalf = sqSize * baseHeadHalf * thicknessScale;
        float headLen = sqSize * baseHeadLen * thicknessScale;

        float dx = x2 - x1, dy = y2 - y1;
        float len = (float) Math.hypot(dx, dy);
        float startOffset = Math.min(len * 0.25f, sqSize * 0.16f);
        float edgeStroke = Math.max(1.8f, sqSize * 0.032f);

        Path arrowPath;
        if (isKnight) {
            arrowPath = buildKnightPath(x1, y1, x2, y2, fileDelta, rankDelta,
                    startOffset, shaftHalf, neckHalf, headHalf, headLen, perpOffset);
        } else {
            arrowPath = buildStraightPath(x1, y1, x2, y2, len,
                    startOffset, shaftHalf, neckHalf, headHalf, headLen, perpOffset);
        }
        if (arrowPath == null) return;

        // Color computation matching NNVC extension with custom tier palette
        int[] rawRgb = getBaseRgb(getContext(), arrow.tier, arrow.isThreat);
        int[] rgb = (arrow.tier == 2) ? tint(rawRgb, 0.12f)
                : (arrow.tier >= 3) ? tint(rawRgb, 0.30f)
                : rawRgb;

        int[] colTail = shade(rgb, 0.14f);
        int[] colMid = rgb;
        int[] colHeadLight = tint(rgb, 0.22f);
        int[] colEdge = shade(rgb, 0.42f);

        float opacity = arrow.isThreat ? 0.92f
                : (arrow.tier == 1) ? 0.94f
                : (arrow.tier == 2) ? 0.82f
                : 0.65f;

        // 1. Neon Drop Shadow / Glow
        glowPaint.setColor(Color.argb((int)(0.40f * opacity * 255), colMid[0], colMid[1], colMid[2]));
        glowPaint.setStrokeWidth(edgeStroke + sqSize * 0.045f);
        glowPaint.setMaskFilter(new BlurMaskFilter(Math.max(1.0f, sqSize * 0.06f), BlurMaskFilter.Blur.NORMAL));
        canvas.drawPath(arrowPath, glowPaint);

        // 2. Linear Gradient Fill
        int cTail = Color.argb((int)(0.88f * opacity * 255), colTail[0], colTail[1], colTail[2]);
        int cMid  = Color.argb((int)(0.96f * opacity * 255), colMid[0], colMid[1], colMid[2]);
        int cHead = Color.argb((int)(1.0f  * opacity * 255), colHeadLight[0], colHeadLight[1], colHeadLight[2]);
        fillPaint.setShader(new LinearGradient(x1, y1, x2, y2,
                new int[]{cTail, cMid, cHead},
                new float[]{0.0f, 0.45f, 1.0f},
                Shader.TileMode.CLAMP));
        canvas.drawPath(arrowPath, fillPaint);

        // 3. Highlight Edge Stroke
        int cEdgeTail = Color.argb((int)(0.75f * opacity * 255), colEdge[0], colEdge[1], colEdge[2]);
        int cEdgeHead = Color.argb((int)(0.65f * opacity * 255), 255, 255, 255);
        edgePaint.setStrokeWidth(edgeStroke);
        edgePaint.setShader(new LinearGradient(x1, y1, x2, y2,
                new int[]{cEdgeTail, cEdgeHead},
                new float[]{0.0f, 1.0f},
                Shader.TileMode.CLAMP));
        canvas.drawPath(arrowPath, edgePaint);
    }

    private Path buildStraightPath(float x1, float y1, float x2, float y2, float len,
                                   float startOffset, float shaftHalf, float neckHalf,
                                   float headHalf, float headLen, float perpOffset) {
        if (len < 2.0f) return null;

        float ux = (x2 - x1) / len;
        float uy = (y2 - y1) / len;
        float px = -uy;
        float py = ux;

        float startX = x1 + ux * startOffset + px * perpOffset;
        float startY = y1 + uy * startOffset + py * perpOffset;
        float endX = x2 + px * perpOffset;
        float endY = y2 + py * perpOffset;

        HeadPoints h = buildHeadPoints(endX, endY, ux, uy, px, py, headLen, headHalf, neckHalf);

        float leftStartX  = startX + px * shaftHalf;
        float leftStartY  = startY + py * shaftHalf;
        float rightStartX = startX - px * shaftHalf;
        float rightStartY = startY - py * shaftHalf;

        Path p = new Path();
        p.moveTo(leftStartX, leftStartY);
        p.lineTo(h.leftNotchX, h.leftNotchY);
        p.lineTo(h.leftOuterX, h.leftOuterY);
        p.lineTo(h.tipX, h.tipY);
        p.lineTo(h.rightOuterX, h.rightOuterY);
        p.lineTo(h.rightNotchX, h.rightNotchY);
        p.lineTo(rightStartX, rightStartY);
        p.close();
        return p;
    }

    private Path buildKnightPath(float x1, float y1, float x2, float y2,
                                 int fileDelta, int rankDelta, float startOffset,
                                 float shaftHalf, float neckHalf, float headHalf, float headLen,
                                 float perpOffset) {
        float elbowX = x1;
        float elbowY = y2;
        if (fileDelta == 2 && rankDelta == 1) {
            elbowX = x2;
            elbowY = y1;
        }

        float s1dx = elbowX - x1, s1dy = elbowY - y1;
        float s1len = (float) Math.hypot(s1dx, s1dy);
        if (s1len < 1e-3f) s1len = 1.0f;
        float u1x = s1dx / s1len, u1y = s1dy / s1len;
        float p1x = -u1y, p1y = u1x;

        float startX = x1 + u1x * startOffset + p1x * perpOffset;
        float startY = y1 + u1y * startOffset + p1y * perpOffset;

        float s2dx = x2 - elbowX, s2dy = y2 - elbowY;
        float s2len = (float) Math.hypot(s2dx, s2dy);
        if (s2len < 1e-3f) s2len = 1.0f;
        float u2x = s2dx / s2len, u2y = s2dy / s2len;
        float p2x = -u2y, p2y = u2x;

        float endX = x2 + p2x * perpOffset;
        float endY = y2 + p2y * perpOffset;

        HeadPoints h = buildHeadPoints(endX, endY, u2x, u2y, p2x, p2y, headLen, headHalf, neckHalf);

        float leftStartX  = startX + p1x * shaftHalf;
        float leftStartY  = startY + p1y * shaftHalf;
        float rightStartX = startX - p1x * shaftHalf;
        float rightStartY = startY - p1y * shaftHalf;

        float shiftedElbowX = elbowX + p1x * perpOffset;
        float shiftedElbowY = elbowY + p1y * perpOffset;

        float[] leftElbow = lineIntersect(leftStartX, leftStartY, u1x, u1y,
                shiftedElbowX + p2x * shaftHalf, shiftedElbowY + p2y * shaftHalf, u2x, u2y);
        float[] rightElbow = lineIntersect(rightStartX, rightStartY, u1x, u1y,
                shiftedElbowX - p2x * shaftHalf, shiftedElbowY - p2y * shaftHalf, u2x, u2y);

        Path p = new Path();
        p.moveTo(leftStartX, leftStartY);
        p.lineTo(leftElbow[0], leftElbow[1]);
        p.lineTo(h.leftNotchX, h.leftNotchY);
        p.lineTo(h.leftOuterX, h.leftOuterY);
        p.lineTo(h.tipX, h.tipY);
        p.lineTo(h.rightOuterX, h.rightOuterY);
        p.lineTo(h.rightNotchX, h.rightNotchY);
        p.lineTo(rightElbow[0], rightElbow[1]);
        p.lineTo(rightStartX, rightStartY);
        p.close();
        return p;
    }

    private HeadPoints buildHeadPoints(float tipX, float tipY, float ux, float uy,
                                       float px, float py, float headLen,
                                       float headHalf, float neckHalf) {
        float headBaseX = tipX - ux * headLen;
        float headBaseY = tipY - uy * headLen;
        return new HeadPoints(
                tipX, tipY,
                headBaseX + px * headHalf, headBaseY + py * headHalf,
                headBaseX - px * headHalf, headBaseY - py * headHalf,
                headBaseX + px * neckHalf, headBaseY + py * neckHalf,
                headBaseX - px * neckHalf, headBaseY - py * neckHalf
        );
    }

    private float[] lineIntersect(float ax, float ay, float adx, float ady,
                                  float bx, float by, float bdx, float bdy) {
        float den = adx * bdy - ady * bdx;
        if (Math.abs(den) < 1e-6f) return new float[]{ax, ay};
        float t = ((bx - ax) * bdy - (by - ay) * bdx) / den;
        return new float[]{ax + adx * t, ay + ady * t};
    }

    private float[] getSquareCenter(String sq, boolean flipped, float sqSize) {
        if (sq == null || sq.length() < 2) return null;
        int file = sq.charAt(0) - 'a';
        int rank = sq.charAt(1) - '1';
        if (file < 0 || file > 7 || rank < 0 || rank > 7) return null;

        int col = flipped ? (7 - file) : file;
        int row = flipped ? rank : (7 - rank);

        float cx = col * sqSize + sqSize / 2.0f;
        float cy = row * sqSize + sqSize / 2.0f;
        return new float[]{cx, cy};
    }

    private static int[] getBaseRgb(Context context, int tier, boolean isThreat) {
        if (isThreat) {
            return new int[]{239, 68, 68}; // #EF4444 (Danger Red)
        }
        int color = (context != null)
                ? StockfishSettings.getArrowTierColor(context, tier)
                : StockfishSettings.DEFAULT_TIER_COLORS[Math.max(1, Math.min(5, tier)) - 1];
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return new int[]{r, g, b};
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static int[] tint(int[] c, float amt) {
        return new int[]{
                clamp((int)(c[0] + (255 - c[0]) * amt)),
                clamp((int)(c[1] + (255 - c[1]) * amt)),
                clamp((int)(c[2] + (255 - c[2]) * amt))
        };
    }

    private static int[] shade(int[] c, float amt) {
        return new int[]{
                clamp((int)(c[0] * (1.0f - amt))),
                clamp((int)(c[1] * (1.0f - amt))),
                clamp((int)(c[2] * (1.0f - amt)))
        };
    }

    /**
     * Renders cyberpunk frosted eval score badges on top of arrows
     * with anti-collision lane separation matching NNVC browser extension logic.
     */
    private void drawEvalBadges(Canvas canvas, float sqSize) {
        List<BadgeLayout> layouts = new ArrayList<>();

        // 1. Calculate ideal initial badge position along arrow shaft
        for (int i = 0; i < arrows.size(); i++) {
            ArrowData arrow = arrows.get(i);
            if (arrow.evalText == null || arrow.evalText.isEmpty()) continue;

            float[] fromCenter = getSquareCenter(arrow.from, flipped, sqSize);
            float[] toCenter   = getSquareCenter(arrow.to,   flipped, sqSize);
            if (fromCenter == null || toCenter == null) continue;

            float x1 = fromCenter[0], y1 = fromCenter[1];
            float x2 = toCenter[0],   y2 = toCenter[1];

            int fileDelta = Math.abs(arrow.to.charAt(0) - arrow.from.charAt(0));
            int rankDelta = Math.abs(arrow.to.charAt(1) - arrow.from.charAt(1));
            boolean isKnight = (fileDelta == 1 && rankDelta == 2) || (fileDelta == 2 && rankDelta == 1);

            float badgeCenterX;
            float badgeCenterY;

            if (isKnight) {
                // For knight moves, anchor along the second leg near the target
                float elbowX = x1;
                float elbowY = y2;
                if (fileDelta == 2 && rankDelta == 1) {
                    elbowX = x2;
                    elbowY = y1;
                }
                // Place badge at 60% of second leg
                badgeCenterX = elbowX + (x2 - elbowX) * 0.60f;
                badgeCenterY = elbowY + (y2 - elbowY) * 0.60f;
            } else {
                // Straight move: Anchor at 62% along the line towards target (near head but clear of tip)
                badgeCenterX = x1 + (x2 - x1) * 0.62f;
                badgeCenterY = y1 + (y2 - y1) * 0.62f;
            }

            // Text measurement
            float textSize = Math.max(16f, sqSize * 0.22f);
            badgeTextPaint.setTextSize(textSize);
            float textWidth = badgeTextPaint.measureText(arrow.evalText);
            Paint.FontMetrics fm = badgeTextPaint.getFontMetrics();
            float textHeight = fm.descent - fm.ascent;

            float padH = sqSize * 0.10f;
            float padV = sqSize * 0.05f;
            float badgeW = textWidth + padH * 2f;
            float badgeH = textHeight + padV * 2f;

            BadgeLayout bl = new BadgeLayout();
            bl.x = badgeCenterX;
            bl.y = badgeCenterY;
            bl.width = badgeW;
            bl.height = badgeH;
            bl.text = arrow.evalText;
            bl.tier = arrow.tier;
            bl.isThreat = arrow.isThreat;

            layouts.add(bl);
        }

        // 2. Anti-collision relaxation: Resolve overlaps between badges
        float minSeparation = sqSize * 0.35f;
        int maxPasses = 5;
        for (int pass = 0; pass < maxPasses; pass++) {
            boolean shifted = false;
            for (int i = 0; i < layouts.size(); i++) {
                BadgeLayout b1 = layouts.get(i);
                for (int j = i + 1; j < layouts.size(); j++) {
                    BadgeLayout b2 = layouts.get(j);
                    float dx = b2.x - b1.x;
                    float dy = b2.y - b1.y;
                    float dist = (float) Math.hypot(dx, dy);
                    float minDist = Math.max(minSeparation, (b1.width + b2.width) * 0.45f);

                    if (dist < minDist) {
                        float push = (minDist - dist) * 0.5f;
                        if (dist < 1e-3f) {
                            dx = 0f;
                            dy = 1f;
                            dist = 1f;
                        }
                        float nx = dx / dist;
                        float ny = dy / dist;

                        // Lower tier (b2) yields more than higher tier (b1)
                        b1.x -= nx * push * 0.3f;
                        b1.y -= ny * push * 0.3f;
                        b2.x += nx * push * 0.7f;
                        b2.y += ny * push * 0.7f;
                        shifted = true;
                    }
                }
            }
            if (!shifted) break;
        }

        // 3. Draw each badge: Cyber Frosted Pill with Neon Glow & Crisp Monospace Text
        for (BadgeLayout b : layouts) {
            int[] rawRgb = getBaseRgb(getContext(), b.tier, b.isThreat);
            int primaryColor = Color.rgb(rawRgb[0], rawRgb[1], rawRgb[2]);

            float left = b.x - b.width / 2f;
            float top = b.y - b.height / 2f;
            float right = left + b.width;
            float bottom = top + b.height;
            float cornerRadius = b.height / 2f;

            // Ambient Glow
            badgeGlowPaint.setColor(Color.argb(90, rawRgb[0], rawRgb[1], rawRgb[2]));
            badgeGlowPaint.setStrokeWidth(sqSize * 0.04f);
            badgeGlowPaint.setMaskFilter(new BlurMaskFilter(Math.max(1f, sqSize * 0.05f), BlurMaskFilter.Blur.NORMAL));
            android.graphics.RectF rect = new android.graphics.RectF(left, top, right, bottom);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, badgeGlowPaint);

            // Frosted Dark Glass Pill Background
            badgeBgPaint.setColor(0xE60C0F16);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, badgeBgPaint);

            // Cyber Neon Outline
            badgeStrokePaint.setColor(primaryColor);
            badgeStrokePaint.setStrokeWidth(Math.max(1.5f, sqSize * 0.024f));
            badgeStrokePaint.setMaskFilter(null);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, badgeStrokePaint);

            // Monospace Eval Text with High Contrast
            badgeTextPaint.setColor(0xFFFFFFFF);
            badgeTextPaint.setTextSize(Math.max(16f, sqSize * 0.22f));
            Paint.FontMetrics fm = badgeTextPaint.getFontMetrics();
            float textBaseline = b.y - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(b.text, b.x, textBaseline, badgeTextPaint);
        }
    }
}
