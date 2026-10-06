package app.prathxm.chess.extension.stockfish;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import app.prathxm.chess.extension.BuildConfig;

public class StockfishSettingsDialog {

    // ══ NNVC Cyber Luxury Frosted Palette ══════════════════════════════════════
    private static final int COLOR_BG_PANEL       = 0xF20C0F16; // Deep Frosted Midnight Glass
    private static final int COLOR_CARD_BG        = 0xCC121722; // Layered Glass Card
    private static final int COLOR_CARD_BORDER    = 0x330A84FF; // Cyber Blue Glow Border (20%)
    private static final int COLOR_ACCENT_BLUE    = 0xFF0A84FF; // NNVC Royal Blue
    private static final int COLOR_ACCENT_CYAN    = 0xFF64D2FF; // Cyber Cyan
    private static final int COLOR_ACCENT_GOLD    = 0xFFFFD760; // Amber Gold Accent
    private static final int COLOR_TEXT_PRIMARY   = 0xFFFFFFFF; // Pure White
    private static final int COLOR_TEXT_SECONDARY = 0xD0EBF0FF; // Muted White / Ice Blue
    private static final int COLOR_TEXT_MUTED     = 0x88A0B4D2; // Dim Subtitle
    private static final int COLOR_GREEN_READY    = 0xFF30D158; // Active Ready
    private static final int COLOR_DANGER_RED     = 0xFFFF453A; // Danger / Red

    public static void showSettingsMenu(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        float density = dm.density;
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        // Cố định chiều cao Dialog: 70% chiều cao màn hình để không bị co giật khi chuyển tab
        final int targetDialogHeight = (int) (screenHeight * 0.70f);
        final int targetDialogWidth = (int) (screenWidth * 0.92f);

        // Frosted Glass Window Frame với hiệu ứng viền thở động (Ambient Breathing Glow)
        final GradientDrawable dialogBg = new GradientDrawable();
        dialogBg.setColor(COLOR_BG_PANEL);
        dialogBg.setCornerRadius(22 * density);
        dialogBg.setStroke((int) (1.4f * density), COLOR_ACCENT_BLUE);
        dialog.getWindow().setBackgroundDrawable(dialogBg);

        // Theme động: Hiệu ứng viền thở Cyber Neon tuần hoàn
        ValueAnimator borderGlowAnim = ValueAnimator.ofFloat(0f, 1f);
        borderGlowAnim.setDuration(1600);
        borderGlowAnim.setRepeatMode(ValueAnimator.REVERSE);
        borderGlowAnim.setRepeatCount(ValueAnimator.INFINITE);
        borderGlowAnim.addUpdateListener(animation -> {
            float frac = (float) animation.getAnimatedValue();
            int glowColor = blendColor(0x330A84FF, 0xAA64D2FF, frac);
            dialogBg.setStroke((int) ((1.4f + 0.4f * frac) * density), glowColor);
        });
        borderGlowAnim.start();

        dialog.setOnDismissListener(d -> borderGlowAnim.cancel());

        // Root container (Cố định chiều cao và chiều rộng)
        LinearLayout windowRoot = new LinearLayout(activity);
        windowRoot.setOrientation(LinearLayout.VERTICAL);
        windowRoot.setLayoutParams(new ViewGroup.LayoutParams(
                targetDialogWidth,
                targetDialogHeight
        ));

        // ══════════════════════════════════════════════════════════════════════
        // 1. NNVC HEADER
        // ══════════════════════════════════════════════════════════════════════
        LinearLayout headerLayout = new LinearLayout(activity);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(Gravity.CENTER_VERTICAL);
        headerLayout.setPadding((int) (16 * density), (int) (14 * density), (int) (16 * density), (int) (12 * density));

        GradientDrawable headerBg = new GradientDrawable();
        headerBg.setColor(0x330A84FF); // 20% cyber blue glow
        headerBg.setCornerRadii(new float[]{
                22 * density, 22 * density,
                22 * density, 22 * density,
                0, 0, 0, 0
        });
        headerLayout.setBackground(headerBg);

        // Logo Icon
        ImageView logoView = NNVCLogoHelper.createLogoView(activity, (int) (34 * density));
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams((int) (34 * density), (int) (34 * density));
        logoParams.rightMargin = (int) (10 * density);
        logoView.setLayoutParams(logoParams);
        headerLayout.addView(logoView);

        // Title Column
        LinearLayout titleCol = new LinearLayout(activity);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleColParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        titleCol.setLayoutParams(titleColParams);

        // Brand Row (.ttl-name)
        LinearLayout brandRow = new LinearLayout(activity);
        brandRow.setOrientation(LinearLayout.HORIZONTAL);
        brandRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView brandText = new TextView(activity);
        brandText.setText("NNVC");
        brandText.setTextColor(COLOR_TEXT_PRIMARY);
        brandText.setTextSize(15f);
        brandText.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        brandText.setLetterSpacing(0.08f);
        brandRow.addView(brandText);

        // VIP Pill Badge (.nncv-vip-pill)
        TextView vipPill = new TextView(activity);
        vipPill.setText("VIP");
        vipPill.setTextColor(0xFFFFF7D6);
        vipPill.setTextSize(9.5f);
        vipPill.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        vipPill.setLetterSpacing(0.12f);
        int pillPadH = (int) (7 * density);
        int pillPadV = (int) (2 * density);
        vipPill.setPadding(pillPadH, pillPadV, pillPadH, pillPadV);

        GradientDrawable vipBg = new GradientDrawable();
        vipBg.setColor(0x40FFD760);
        vipBg.setStroke((int) (1.2f * density), 0x99FFD760);
        vipBg.setCornerRadius(999 * density);
        vipPill.setBackground(vipBg);

        LinearLayout.LayoutParams pillParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        pillParams.leftMargin = (int) (8 * density);
        vipPill.setLayoutParams(pillParams);
        brandRow.addView(vipPill);

        titleCol.addView(brandRow);

        // Subtitle (.ttl-sub)
        TextView subtitleTv = new TextView(activity);
        subtitleTv.setText(I18n.get(activity, "ttl_sub"));
        subtitleTv.setTextColor(COLOR_TEXT_MUTED);
        subtitleTv.setTextSize(8.5f);
        subtitleTv.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
        subtitleTv.setLetterSpacing(0.12f);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subParams.topMargin = (int) (2 * density);
        subtitleTv.setLayoutParams(subParams);
        titleCol.addView(subtitleTv);

        headerLayout.addView(titleCol);

        // Language Switch Button
        TextView langBtn = new TextView(activity);
        boolean isCurrentVi = "vi".equalsIgnoreCase(StockfishSettings.getLanguage(activity));
        langBtn.setText(isCurrentVi ? "VI" : "EN");
        langBtn.setTextColor(COLOR_TEXT_PRIMARY);
        langBtn.setTextSize(11);
        langBtn.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        langBtn.setGravity(Gravity.CENTER);
        int langPadH = (int) (9 * density);
        int langPadV = (int) (4 * density);
        langBtn.setPadding(langPadH, langPadV, langPadH, langPadV);

        GradientDrawable langBg = new GradientDrawable();
        langBg.setColor(0x22FFFFFF);
        langBg.setCornerRadius(8 * density);
        langBg.setStroke((int) (1 * density), COLOR_CARD_BORDER);
        langBtn.setBackground(langBg);

        langBtn.setOnClickListener(v -> {
            HapticHelper.pop(activity, v);
            String newLang = isCurrentVi ? "en" : "vi";
            StockfishSettings.setLanguage(activity, newLang);
            dialog.dismiss();
            showSettingsMenu(activity);
        });
        headerLayout.addView(langBtn);

        windowRoot.addView(headerLayout);

        // Thin Cyber Divider Line
        View headerDivider = new View(activity);
        headerDivider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (1.2f * density)
        ));
        GradientDivider(headerDivider, COLOR_ACCENT_BLUE);
        windowRoot.addView(headerDivider);

        // ══════════════════════════════════════════════════════════════════════
        // 2. SEGMENTED TABS (.tab-nav)
        // ══════════════════════════════════════════════════════════════════════
        LinearLayout tabNav = new LinearLayout(activity);
        tabNav.setOrientation(LinearLayout.HORIZONTAL);
        tabNav.setPadding((int) (4 * density), (int) (4 * density), (int) (4 * density), (int) (4 * density));
        LinearLayout.LayoutParams tabNavParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tabNavParams.setMargins((int) (14 * density), (int) (8 * density), (int) (14 * density), (int) (6 * density));
        tabNav.setLayoutParams(tabNavParams);

        GradientDrawable tabNavBg = new GradientDrawable();
        tabNavBg.setColor(0x66000000);
        tabNavBg.setCornerRadius(14 * density);
        tabNavBg.setStroke((int) (1 * density), 0x1AFFFFFF);
        tabNav.setBackground(tabNavBg);

        final TextView tabLive = createTabButton(activity, I18n.get(activity, "tab_live"), true, density);
        final TextView tabVisual = createTabButton(activity, I18n.get(activity, "tab_visual"), false, density);
        final TextView tabEngine = createTabButton(activity, I18n.get(activity, "tab_engine"), false, density);

        tabNav.addView(tabLive);
        tabNav.addView(tabVisual);
        tabNav.addView(tabEngine);

        windowRoot.addView(tabNav);

        // ══════════════════════════════════════════════════════════════════════
        // 3. TAB CONTENT CONTAINERS (SCROLLABLE VỚI CHIỀU CAO CỐ ĐỊNH)
        // ══════════════════════════════════════════════════════════════════════
        ScrollView scrollView = new ScrollView(activity);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setFillViewport(true); // Đảm bảo cuộn đầy đủ khung nhìn cố định
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0, 1.0f
        );
        scrollView.setLayoutParams(scrollParams);

        LinearLayout tabContentRoot = new LinearLayout(activity);
        tabContentRoot.setOrientation(LinearLayout.VERTICAL);
        int contentPadding = (int) (14 * density);
        tabContentRoot.setPadding(contentPadding, 0, contentPadding, (int) (8 * density));
        scrollView.addView(tabContentRoot);

        // ─── TAB 1: LIVE (Trực tiếp & Thế cờ) ───
        final LinearLayout panelLive = new LinearLayout(activity);
        panelLive.setOrientation(LinearLayout.VERTICAL);
        tabContentRoot.addView(panelLive);

        // Mini Dashboard Card with Pulsing LED (.dash)
        LinearLayout dashCard = createGlassCard(activity, density);
        panelLive.addView(dashCard);

        LinearLayout statusRow = new LinearLayout(activity);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setGravity(Gravity.CENTER_VERTICAL);
        dashCard.addView(statusRow);

        View statusDot = new View(activity);
        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setColor(StockfishSettings.isEngineEnabled(activity) ? COLOR_GREEN_READY : COLOR_DANGER_RED);
        dotBg.setCornerRadius(999 * density);
        statusDot.setBackground(dotBg);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams((int) (8 * density), (int) (8 * density));
        dotParams.rightMargin = (int) (8 * density);
        statusDot.setLayoutParams(dotParams);
        statusRow.addView(statusDot);

        // LED Breathing Pulse Animation
        AlphaAnimation pulseAnim = new AlphaAnimation(0.35f, 1.0f);
        pulseAnim.setDuration(700);
        pulseAnim.setRepeatMode(Animation.REVERSE);
        pulseAnim.setRepeatCount(Animation.INFINITE);
        statusDot.startAnimation(pulseAnim);

        TextView statusLabel = new TextView(activity);
        statusLabel.setText(StockfishSettings.isEngineEnabled(activity) ? "ENGINE READY (1 THREAD)" : "ENGINE OFF");
        statusLabel.setTextColor(COLOR_TEXT_SECONDARY);
        statusLabel.setTextSize(11);
        statusLabel.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        statusRow.addView(statusLabel);

        addDialogSpacer(panelLive, 8, density);

        // Live options in Glass Card
        LinearLayout liveCard = createGlassCard(activity, density);
        panelLive.addView(liveCard);

        final CyberSwitchView enabledSwitch = addCyberSwitchRow(liveCard,
                I18n.get(activity, "enable_stockfish"),
                I18n.get(activity, "panic_hint"),
                StockfishSettings.isEngineEnabled(activity),
                density, activity);

        addCardSeparator(liveCard, density);

        int currentDepth = StockfishSettings.getDepth(activity);
        final String depthPrefix = I18n.get(activity, "depth");
        final SeekBar depthSeekBar = addGlassSeekBarWithBadge(liveCard, depthPrefix,
                String.valueOf(currentDepth),
                currentDepth - 1, StockfishSettings.MAX_DEPTH - 1, 1,
                I18n.get(activity, "depth_hint"),
                density, activity);

        // ─── TAB 2: VISUAL (Mũi tên & Giao diện) ───
        final LinearLayout panelVisual = new LinearLayout(activity);
        panelVisual.setOrientation(LinearLayout.VERTICAL);
        panelVisual.setVisibility(View.GONE);
        tabContentRoot.addView(panelVisual);

        LinearLayout visualCard = createGlassCard(activity, density);
        panelVisual.addView(visualCard);

        final CyberSwitchView arrowsSwitch = addCyberSwitchRow(visualCard,
                I18n.get(activity, "best_move_arrows"),
                null,
                StockfishSettings.isArrowsVisible(activity),
                density, activity);

        addCardSeparator(visualCard, density);

        final CyberSwitchView sideSwitch = addCyberSwitchRow(visualCard,
                I18n.get(activity, "my_turn_only"),
                null,
                StockfishSettings.isMySideOnly(activity),
                density, activity);

        addCardSeparator(visualCard, density);

        int currentPV = StockfishSettings.getMultiPV(activity);
        final String pvPrefix = I18n.get(activity, "num_arrows");
        final SeekBar pvSeekBar = addGlassSeekBarWithBadge(visualCard, pvPrefix,
                currentPV + " PV",
                currentPV - 1, 4, 1,
                I18n.get(activity, "arrows_hint"),
                density, activity);

        addCardSeparator(visualCard, density);

        final CyberSwitchView threatSwitch = addCyberSwitchRow(visualCard,
                I18n.get(activity, "threat_arrow"),
                null,
                StockfishSettings.isThreatArrowsEnabled(activity),
                density, activity);

        addCardSeparator(visualCard, density);

        final CyberSwitchView evalBarSwitch = addCyberSwitchRow(visualCard,
                I18n.get(activity, "eval_bar"),
                null,
                StockfishSettings.isEvalBarEnabled(activity),
                density, activity);

        addCardSeparator(visualCard, density);

        final CyberSwitchView wdlSwitch = addCyberSwitchRow(visualCard,
                I18n.get(activity, "wdl_bar"),
                null,
                StockfishSettings.isWdlEnabled(activity),
                density, activity);

        addCardSeparator(visualCard, density);

        final CyberSwitchView infoSwitch = addCyberSwitchRow(visualCard,
                I18n.get(activity, "depth_score_above"),
                null,
                StockfishSettings.isEngineInfoEnabled(activity),
                density, activity);

        addCardSeparator(visualCard, density);

        final CyberSwitchView mateSwitch = addCyberSwitchRow(visualCard,
                I18n.get(activity, "forced_mates"),
                null,
                StockfishSettings.isMateAnnouncementEnabled(activity),
                density, activity);

        // ─── TAB 3: ENGINE (Sức mạnh & Phân loại) ───
        final LinearLayout panelEngine = new LinearLayout(activity);
        panelEngine.setOrientation(LinearLayout.VERTICAL);
        panelEngine.setVisibility(View.GONE);
        tabContentRoot.addView(panelEngine);

        LinearLayout engineCard = createGlassCard(activity, density);
        panelEngine.addView(engineCard);

        final CyberSwitchView classifSwitch = addCyberSwitchRow(engineCard,
                I18n.get(activity, "rate_moves"),
                null,
                StockfishSettings.isMoveClassificationEnabled(activity),
                density, activity);

        addCardSeparator(engineCard, density);

        final CyberSwitchView blunderSwitch = addCyberSwitchRow(engineCard,
                I18n.get(activity, "vibrate_blunder"),
                I18n.get(activity, "vibrate_hint"),
                StockfishSettings.isBlunderAlertsEnabled(activity),
                density, activity);

        addCardSeparator(engineCard, density);

        final CyberSwitchView eloSwitch = addCyberSwitchRow(engineCard,
                I18n.get(activity, "limit_strength"),
                null,
                StockfishSettings.isLimitStrength(activity),
                density, activity);

        addCardSeparator(engineCard, density);

        int currentElo = Math.max(1320, Math.min(3190, StockfishSettings.getElo(activity)));
        final String eloPrefix = I18n.get(activity, "engine_elo");
        final SeekBar eloSeekBar = addGlassSeekBarWithBadge(engineCard, eloPrefix,
                currentElo + " ELO",
                currentElo - 1320, 3190 - 1320, 1320,
                I18n.get(activity, "elo_hint"),
                density, activity);

        eloSeekBar.setEnabled(eloSwitch.isChecked());
        eloSeekBar.setAlpha(eloSwitch.isChecked() ? 1f : 0.4f);
        eloSwitch.setOnCheckedChangeListener((switchView, isChecked) -> {
            eloSeekBar.setEnabled(isChecked);
            eloSeekBar.setAlpha(isChecked ? 1f : 0.4f);
        });

        addCardSeparator(engineCard, density);

        // Reset Settings button
        TextView resetBtn = new TextView(activity);
        resetBtn.setText(I18n.get(activity, "reset_defaults"));
        resetBtn.setTextColor(COLOR_DANGER_RED);
        resetBtn.setTextSize(12.5f);
        resetBtn.setGravity(Gravity.CENTER);
        resetBtn.setPadding(0, (int) (8 * density), 0, (int) (8 * density));
        resetBtn.setOnClickListener(v -> {
            HapticHelper.pop(activity, v);
            new android.app.AlertDialog.Builder(activity)
                    .setTitle(I18n.get(activity, "reset_title"))
                    .setMessage(I18n.get(activity, "reset_message"))
                    .setNegativeButton(I18n.get(activity, "cancel"), null)
                    .setPositiveButton(I18n.get(activity, "reset"), (d, w) -> {
                        StockfishSettings.resetToDefaults(activity);
                        Object st = StockfishExtension.getStateImpl();
                        ArrowInjector.clearEngineArrows(st);
                        OverlayManager.hideEvalBar();
                        OverlayManager.hideWdlBar();
                        OverlayManager.hideMateAnnouncement();
                        OverlayManager.hideEngineInfo();
                        StockfishExtension.triggerAnalysisForCurrentState();
                        Toast.makeText(activity, I18n.get(activity, "reset_toast"), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .show();
        });
        engineCard.addView(resetBtn);

        // Tab Switching Click Listeners kèm Hoạt ảnh Fade & Slide mượt mà + Phản hồi rung
        View.OnClickListener tabListener = v -> {
            HapticHelper.tick(activity, v);
            boolean isL = (v == tabLive);
            boolean isV = (v == tabVisual);
            boolean isE = (v == tabEngine);

            updateTabStyle(tabLive, isL, density);
            updateTabStyle(tabVisual, isV, density);
            updateTabStyle(tabEngine, isE, density);

            if (isL) {
                switchTabWithAnim(panelLive, panelVisual, panelEngine);
            } else if (isV) {
                switchTabWithAnim(panelVisual, panelLive, panelEngine);
            } else {
                switchTabWithAnim(panelEngine, panelLive, panelVisual);
            }
        };

        tabLive.setOnClickListener(tabListener);
        tabVisual.setOnClickListener(tabListener);
        tabEngine.setOnClickListener(tabListener);

        windowRoot.addView(scrollView);

        // ══════════════════════════════════════════════════════════════════════
        // 4. FOOTER CREDITS & ACTIONS
        // ══════════════════════════════════════════════════════════════════════
        LinearLayout footerLayout = new LinearLayout(activity);
        footerLayout.setOrientation(LinearLayout.VERTICAL);
        footerLayout.setPadding((int) (14 * density), (int) (8 * density), (int) (14 * density), (int) (12 * density));

        // Telegram Button (.hdr-btn style)
        LinearLayout telegramBtn = new LinearLayout(activity);
        telegramBtn.setOrientation(LinearLayout.HORIZONTAL);
        telegramBtn.setGravity(Gravity.CENTER);
        int tgPadH = (int) (14 * density);
        int tgPadV = (int) (7 * density);
        telegramBtn.setPadding(tgPadH, tgPadV, tgPadH, tgPadV);

        GradientDrawable tgBg = new GradientDrawable();
        tgBg.setColor(0xCC0A84FF); // 80% Cyber Blue
        tgBg.setCornerRadius(14 * density);
        tgBg.setStroke((int) (1.2f * density), COLOR_ACCENT_CYAN);
        telegramBtn.setBackground(tgBg);

        ImageView tgIcon = TelegramIconHelper.createTelegramLogoView(activity, (int) (18 * density));
        LinearLayout.LayoutParams tgIconParams = new LinearLayout.LayoutParams((int) (18 * density), (int) (18 * density));
        tgIconParams.rightMargin = (int) (8 * density);
        tgIcon.setLayoutParams(tgIconParams);
        telegramBtn.addView(tgIcon);

        TextView tgText = new TextView(activity);
        tgText.setText(I18n.get(activity, "contact_telegram"));
        tgText.setTextColor(COLOR_TEXT_PRIMARY);
        tgText.setTextSize(12.5f);
        tgText.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        telegramBtn.addView(tgText);

        telegramBtn.setOnClickListener(v -> {
            HapticHelper.pop(activity, v);
            try {
                android.content.Intent intent = new android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://t.me/nncutett")
                );
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            } catch (Throwable t) {
                Toast.makeText(activity, "Telegram: @nncutett", Toast.LENGTH_LONG).show();
            }
        });
        footerLayout.addView(telegramBtn);

        addDialogSpacer(footerLayout, 6, density);

        // Version metadata
        TextView patchTv = new TextView(activity);
        patchTv.setText("NNVC v" + BuildConfig.PATCH_VERSION + " · by @nncutett");
        patchTv.setTextColor(COLOR_TEXT_MUTED);
        patchTv.setTextSize(10.5f);
        patchTv.setGravity(Gravity.CENTER);
        footerLayout.addView(patchTv);

        addDialogSpacer(footerLayout, 8, density);

        // Action Buttons (Cancel / Save)
        LinearLayout actionButtons = new LinearLayout(activity);
        actionButtons.setOrientation(LinearLayout.HORIZONTAL);
        actionButtons.setGravity(Gravity.END);

        TextView cancelBtn = new TextView(activity);
        cancelBtn.setText(I18n.get(activity, "cancel"));
        cancelBtn.setTextColor(COLOR_TEXT_MUTED);
        cancelBtn.setTextSize(14);
        cancelBtn.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        cancelBtn.setGravity(Gravity.CENTER);
        cancelBtn.setPadding((int) (16 * density), (int) (8 * density), (int) (16 * density), (int) (8 * density));
        cancelBtn.setOnClickListener(v -> {
            HapticHelper.pop(activity, v);
            dialog.dismiss();
        });
        actionButtons.addView(cancelBtn);

        View btnSpacer = new View(activity);
        btnSpacer.setLayoutParams(new LinearLayout.LayoutParams((int) (8 * density), 1));
        actionButtons.addView(btnSpacer);

        TextView saveBtn = new TextView(activity);
        saveBtn.setText(I18n.get(activity, "save"));
        saveBtn.setTextColor(COLOR_TEXT_PRIMARY);
        saveBtn.setTextSize(14);
        saveBtn.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        saveBtn.setGravity(Gravity.CENTER);
        saveBtn.setPadding((int) (22 * density), (int) (8 * density), (int) (22 * density), (int) (8 * density));

        GradientDrawable saveBg = new GradientDrawable();
        saveBg.setColor(COLOR_ACCENT_BLUE);
        saveBg.setCornerRadius(10 * density);
        saveBg.setStroke((int) (1.2f * density), COLOR_ACCENT_CYAN);
        saveBtn.setBackground(saveBg);

        saveBtn.setOnClickListener(v -> {
            HapticHelper.pop(activity, v);
            StockfishSettings.setEngineEnabled(activity, enabledSwitch.isChecked());
            StockfishSettings.setDepth(activity, Math.max(1, depthSeekBar.getProgress() + 1));
            StockfishSettings.setMultiPV(activity, Math.max(1, pvSeekBar.getProgress() + 1));
            StockfishSettings.setMySideOnly(activity, sideSwitch.isChecked());
            StockfishSettings.setLimitStrength(activity, eloSwitch.isChecked());
            StockfishSettings.setElo(activity, 1320 + eloSeekBar.getProgress());
            StockfishSettings.setPremiumEnabled(activity, true);
            StockfishSettings.setArrowsVisible(activity, arrowsSwitch.isChecked());
            StockfishSettings.setEvalBarEnabled(activity, evalBarSwitch.isChecked());
            StockfishSettings.setWdlEnabled(activity, wdlSwitch.isChecked());
            StockfishSettings.setEngineInfoEnabled(activity, infoSwitch.isChecked());
            StockfishSettings.setThreatArrowsEnabled(activity, threatSwitch.isChecked());
            StockfishSettings.setMoveClassificationEnabled(activity, classifSwitch.isChecked());
            StockfishSettings.setBlunderAlertsEnabled(activity, blunderSwitch.isChecked());
            StockfishSettings.setMateAnnouncementEnabled(activity, mateSwitch.isChecked());
            if (!mateSwitch.isChecked()) OverlayManager.hideMateAnnouncement();

            Toast.makeText(activity, I18n.get(activity, "settings_saved"), Toast.LENGTH_SHORT).show();

            Object state = StockfishExtension.getStateImpl();
            if (!enabledSwitch.isChecked() || !arrowsSwitch.isChecked()) {
                ArrowInjector.clearEngineArrows(state);
            }
            if (!enabledSwitch.isChecked() || !evalBarCbChecked(evalBarSwitch)) OverlayManager.hideEvalBar();
            if (!enabledSwitch.isChecked() || !wdlCbChecked(wdlSwitch)) OverlayManager.hideWdlBar();
            if (!enabledSwitch.isChecked() || !infoCbChecked(infoSwitch)) OverlayManager.hideEngineInfo();
            if (!enabledSwitch.isChecked()) OverlayManager.hideMateAnnouncement();
            if (enabledSwitch.isChecked()) {
                StockfishExtension.triggerAnalysisForCurrentState();
            }

            dialog.dismiss();
        });
        actionButtons.addView(saveBtn);

        footerLayout.addView(actionButtons);
        windowRoot.addView(footerLayout);

        dialog.setContentView(windowRoot);
        dialog.show();

        // Đảm bảo kích thước cửa sổ chuẩn cố định sau khi show
        dialog.getWindow().setLayout(targetDialogWidth, targetDialogHeight);
    }

    private static boolean evalBarCbChecked(CyberSwitchView s) { return s != null && s.isChecked(); }
    private static boolean wdlCbChecked(CyberSwitchView s) { return s != null && s.isChecked(); }
    private static boolean infoCbChecked(CyberSwitchView s) { return s != null && s.isChecked(); }

    // ══ UI Helpers matching NNVC Cyber Glass Architecture ══════════════════════

    private static void switchTabWithAnim(View showView, View... hideViews) {
        for (View v : hideViews) {
            v.setVisibility(View.GONE);
        }
        showView.setVisibility(View.VISIBLE);
        showView.setAlpha(0f);
        showView.setTranslationY(18f);
        showView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(190)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private static LinearLayout createGlassCard(Activity activity, float density) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        int padH = (int) (14 * density);
        int padV = (int) (12 * density);
        card.setPadding(padH, padV, padH, padV);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(COLOR_CARD_BG);
        bg.setCornerRadius(14 * density);
        bg.setStroke((int) (1.2f * density), COLOR_CARD_BORDER);
        card.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (8 * density);
        card.setLayoutParams(params);
        return card;
    }

    private static TextView createTabButton(Activity activity, String text, boolean active, float density) {
        TextView tab = new TextView(activity);
        tab.setText(text);
        tab.setTextSize(11f);
        tab.setGravity(Gravity.CENTER);
        tab.setPadding(0, (int) (7 * density), 0, (int) (7 * density));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tab.setLayoutParams(lp);
        updateTabStyle(tab, active, density);
        return tab;
    }

    private static void updateTabStyle(TextView tab, boolean active, float density) {
        tab.setTextColor(active ? COLOR_TEXT_PRIMARY : COLOR_TEXT_MUTED);
        tab.setTypeface(Typeface.create("sans-serif-medium", active ? Typeface.BOLD : Typeface.NORMAL));

        GradientDrawable bg = new GradientDrawable();
        if (active) {
            bg.setColor(0x550A84FF); // Active Cyber Glow
            bg.setStroke((int) (1.2f * density), COLOR_ACCENT_CYAN);
        } else {
            bg.setColor(Color.TRANSPARENT);
        }
        bg.setCornerRadius(10 * density);
        tab.setBackground(bg);
    }

    private static CyberSwitchView addCyberSwitchRow(LinearLayout container, String title, String subtitle, boolean initialChecked, float density, Activity activity) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding((int) (4 * density), (int) (8 * density), (int) (4 * density), (int) (8 * density));

        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textColParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        textColParams.rightMargin = (int) (10 * density);
        textCol.setLayoutParams(textColParams);

        TextView titleTv = new TextView(activity);
        titleTv.setText(title);
        titleTv.setTextColor(COLOR_TEXT_PRIMARY);
        titleTv.setTextSize(13.5f);
        titleTv.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        textCol.addView(titleTv);

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView subTv = new TextView(activity);
            subTv.setText(subtitle);
            subTv.setTextColor(COLOR_TEXT_MUTED);
            subTv.setTextSize(10.5f);
            subTv.setLineSpacing(1.2f, 1.1f);
            LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            );
            subParams.topMargin = (int) (2 * density);
            subTv.setLayoutParams(subParams);
            textCol.addView(subTv);
        }

        row.addView(textCol);

        CyberSwitchView cyberSwitch = new CyberSwitchView(activity);
        cyberSwitch.setChecked(initialChecked, false);
        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(
                (int) (48 * density), (int) (27 * density)
        );
        cyberSwitch.setLayoutParams(switchParams);
        row.addView(cyberSwitch);

        // Click row to toggle with feedback
        row.setClickable(true);
        row.setOnClickListener(v -> cyberSwitch.toggleWithFeedback());

        container.addView(row);
        return cyberSwitch;
    }

    private static SeekBar addGlassSeekBarWithBadge(LinearLayout layout, final String labelPrefix, String initialBadge, int progress, int max, final int minVal, String hint, float density, Activity activity) {
        // Label & Badge Header
        LinearLayout headerRow = new LinearLayout(activity);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setPadding(0, (int) (4 * density), 0, (int) (4 * density));

        TextView labelTv = new TextView(activity);
        labelTv.setText(labelPrefix);
        labelTv.setTextColor(COLOR_TEXT_SECONDARY);
        labelTv.setTextSize(13);
        labelTv.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        labelTv.setLayoutParams(labelParams);
        headerRow.addView(labelTv);

        // Amber Gold Value Badge
        final TextView valBadge = new TextView(activity);
        valBadge.setText(initialBadge);
        valBadge.setTextColor(COLOR_ACCENT_GOLD);
        valBadge.setTextSize(11);
        valBadge.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        valBadge.setPadding((int) (8 * density), (int) (2 * density), (int) (8 * density), (int) (2 * density));

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(0x33FFD760);
        badgeBg.setCornerRadius(6 * density);
        badgeBg.setStroke((int) (1 * density), COLOR_ACCENT_GOLD);
        valBadge.setBackground(badgeBg);
        headerRow.addView(valBadge);

        layout.addView(headerRow);

        // SeekBar với padding rộng và thumbOffset để núm tròn KHÔNG BAO GIỜ bị lòi dính ra ngoài viền
        final SeekBar seekBar = new SeekBar(activity);
        seekBar.setMax(max);
        seekBar.setProgress(progress);
        if (Build.VERSION.SDK_INT >= 21) {
            seekBar.setProgressTintList(ColorStateList.valueOf(COLOR_ACCENT_BLUE));
            seekBar.setThumbTintList(ColorStateList.valueOf(COLOR_ACCENT_CYAN));
        }

        // Padding ngang 16dp và thumbOffset 16dp triệt tiêu hoàn toàn hiện tượng dính mép viền
        int padH = (int) (16 * density);
        int padV = (int) (8 * density);
        seekBar.setPadding(padH, padV, padH, padV);
        seekBar.setThumbOffset(padH);

        LinearLayout.LayoutParams sbParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sbParams.setMargins(0, (int) (2 * density), 0, (int) (4 * density));
        seekBar.setLayoutParams(sbParams);

        final int[] lastVal = { Math.max(minVal, progress + minVal) };

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int prog, boolean fromUser) {
                int val = Math.max(minVal, prog + minVal);
                if (labelPrefix.toLowerCase().contains("elo")) {
                    valBadge.setText(val + " ELO");
                } else if (labelPrefix.toLowerCase().contains("mũi tên") || labelPrefix.toLowerCase().contains("arrow")) {
                    valBadge.setText(val + " PV");
                } else {
                    valBadge.setText(String.valueOf(val));
                }

                // Rung xúc giác nhẹ mỗi nấc giá trị thay đổi khi người dùng kéo
                if (fromUser && val != lastVal[0]) {
                    lastVal[0] = val;
                    HapticHelper.tick(activity, sb);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar sb) {
                HapticHelper.tick(activity, sb);
            }
            @Override
            public void onStopTrackingTouch(SeekBar sb) {
                HapticHelper.pop(activity, sb);
            }
        });

        layout.addView(seekBar);

        if (hint != null && !hint.isEmpty()) {
            TextView hintTv = new TextView(activity);
            hintTv.setText(hint);
            hintTv.setTextColor(COLOR_TEXT_MUTED);
            hintTv.setTextSize(10.5f);
            hintTv.setPadding((int) (4 * density), 0, (int) (4 * density), (int) (4 * density));
            layout.addView(hintTv);
        }

        return seekBar;
    }

    private static void addCardSeparator(LinearLayout layout, float density) {
        View sep = new View(layout.getContext());
        sep.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (1 * density)
        ));
        GradientDivider(sep, 0x1AFFFFFF);
        layout.addView(sep);
    }

    private static void GradientDivider(View view, int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        view.setBackground(d);
    }

    private static void addDialogSpacer(LinearLayout layout, int dpHeight, float density) {
        View spacer = new View(layout.getContext());
        spacer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (dpHeight * density)
        ));
        layout.addView(spacer);
    }

    private static int blendColor(int from, int to, float ratio) {
        float inverseRatio = 1f - ratio;
        float a = Color.alpha(from) * inverseRatio + Color.alpha(to) * ratio;
        float r = Color.red(from) * inverseRatio + Color.red(to) * ratio;
        float g = Color.green(from) * inverseRatio + Color.green(to) * ratio;
        float b = Color.blue(from) * inverseRatio + Color.blue(to) * ratio;
        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }
}
