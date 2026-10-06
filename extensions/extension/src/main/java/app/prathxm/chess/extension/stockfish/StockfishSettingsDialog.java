package app.prathxm.chess.extension.stockfish;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import app.prathxm.chess.extension.BuildConfig;

public class StockfishSettingsDialog {

    // ══ NNVC Cyber Luxury Frosted Palette ══════════════════════════════════════
    private static final int COLOR_BG_PANEL       = 0xF00C0F16; // Deep Frosted Midnight Glass
    private static final int COLOR_CARD_BG        = 0xCC121722; // Layered Glass Card
    private static final int COLOR_CARD_BORDER    = 0x330A84FF; // Cyber Blue Glow Border (20%)
    private static final int COLOR_CARD_SUB_BG    = 0x881A2130; // Inner Cell/Dash BG
    private static final int COLOR_ACCENT_BLUE    = 0xFF0A84FF; // NNVC Royal Blue
    private static final int COLOR_ACCENT_CYAN    = 0xFF64D2FF; // Cyber Cyan
    private static final int COLOR_ACCENT_GOLD    = 0xFFF0B84B; // Amber Gold Accent
    private static final int COLOR_TEXT_PRIMARY   = 0xFFF8FAFC; // White
    private static final int COLOR_TEXT_SECONDARY = 0xD0EBF0FF; // Muted White / Ice Blue
    private static final int COLOR_TEXT_MUTED     = 0x88A0B4D2; // Dim Subtitle
    private static final int COLOR_GREEN_READY    = 0xFF30D158; // Ready / Active
    private static final int COLOR_DANGER_RED     = 0xFFFF453A; // Danger / Urgent

    public static void showSettingsMenu(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        float density = activity.getResources().getDisplayMetrics().density;

        // Frosted Glass Window Frame
        GradientDrawable dialogBg = new GradientDrawable();
        dialogBg.setColor(COLOR_BG_PANEL);
        dialogBg.setCornerRadius(22 * density);
        dialogBg.setStroke((int) (1.2f * density), COLOR_CARD_BORDER);
        dialog.getWindow().setBackgroundDrawable(dialogBg);

        // Root container
        LinearLayout windowRoot = new LinearLayout(activity);
        windowRoot.setOrientation(LinearLayout.VERTICAL);
        windowRoot.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // ══════════════════════════════════════════════════════════════════════
        // 1. NNVC HEADER (.x3)
        // ══════════════════════════════════════════════════════════════════════
        LinearLayout headerLayout = new LinearLayout(activity);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(Gravity.CENTER_VERTICAL);
        headerLayout.setPadding((int) (16 * density), (int) (14 * density), (int) (16 * density), (int) (12 * density));

        GradientDrawable headerBg = new GradientDrawable();
        headerBg.setColor(0x330A84FF); // 20% cyber blue glow under header
        headerBg.setCornerRadii(new float[]{
                22 * density, 22 * density,
                22 * density, 22 * density,
                0, 0, 0, 0
        });
        headerLayout.setBackground(headerBg);

        // Logo Icon (.ttl-icon .brand)
        ImageView logoView = NNVCLogoHelper.createLogoView(activity, (int) (34 * density));
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams((int) (34 * density), (int) (34 * density));
        logoParams.rightMargin = (int) (10 * density);
        logoView.setLayoutParams(logoParams);
        headerLayout.addView(logoView);

        // Title Column (.ttl-text)
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
        brandText.setTextSize(14.5f);
        brandText.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        brandText.setLetterSpacing(0.08f);
        brandRow.addView(brandText);

        // VIP Pill Badge (.nncv-vip-pill)
        TextView vipPill = new TextView(activity);
        vipPill.setText("VIP");
        vipPill.setTextColor(0xFFFFF7D6);
        vipPill.setTextSize(9);
        vipPill.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        vipPill.setLetterSpacing(0.12f);
        int pillPadH = (int) (7 * density);
        int pillPadV = (int) (2 * density);
        vipPill.setPadding(pillPadH, pillPadV, pillPadH, pillPadV);

        GradientDrawable vipBg = new GradientDrawable();
        vipBg.setColor(0x40FFD760);
        vipBg.setStroke((int) (1 * density), 0x88FFD760);
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

        // Language Switch Button (.hdr-btn)
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
            String newLang = isCurrentVi ? "en" : "vi";
            StockfishSettings.setLanguage(activity, newLang);
            dialog.dismiss();
            showSettingsMenu(activity);
        });
        headerLayout.addView(langBtn);

        windowRoot.addView(headerLayout);

        // Thin Cyber Divider Line (.think-bar / sep)
        View headerDivider = new View(activity);
        headerDivider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (1 * density)
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
        // 3. TAB CONTENT CONTAINERS (SCROLLABLE)
        // ══════════════════════════════════════════════════════════════════════
        ScrollView scrollView = new ScrollView(activity);
        scrollView.setVerticalScrollBarEnabled(false);
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

        // --- TAB 1: LIVE (Trực tiếp & Thế cờ) ---
        final LinearLayout panelLive = new LinearLayout(activity);
        panelLive.setOrientation(LinearLayout.VERTICAL);
        tabContentRoot.addView(panelLive);

        // Mini Dashboard Card (.dash)
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

        final CheckBox enabledCb = addGlassCheckbox(liveCard, I18n.get(activity, "enable_stockfish"), StockfishSettings.isEngineEnabled(activity), density, activity);
        addGlassHint(liveCard, I18n.get(activity, "panic_hint"), density, activity);
        addCardSeparator(liveCard, density);

        int currentDepth = StockfishSettings.getDepth(activity);
        final String depthPrefix = I18n.get(activity, "depth");
        TextView depthLabel = addGlassLabel(liveCard, depthPrefix + ": " + currentDepth, density, activity);
        final SeekBar depthSeekBar = addGlassSeekBar(liveCard, depthLabel, depthPrefix, currentDepth - 1, StockfishSettings.MAX_DEPTH - 1, 1, density, activity);
        addGlassHint(liveCard, I18n.get(activity, "depth_hint"), density, activity);

        // --- TAB 2: VISUAL (Mũi tên & Giao diện) ---
        final LinearLayout panelVisual = new LinearLayout(activity);
        panelVisual.setOrientation(LinearLayout.VERTICAL);
        panelVisual.setVisibility(View.GONE);
        tabContentRoot.addView(panelVisual);

        LinearLayout visualCard = createGlassCard(activity, density);
        panelVisual.addView(visualCard);

        final CheckBox arrowsCb = addGlassCheckbox(visualCard, I18n.get(activity, "best_move_arrows"), StockfishSettings.isArrowsVisible(activity), density, activity);
        final CheckBox sideCb = addGlassCheckbox(visualCard, I18n.get(activity, "my_turn_only"), StockfishSettings.isMySideOnly(activity), density, activity);
        addCardSeparator(visualCard, density);

        int currentPV = StockfishSettings.getMultiPV(activity);
        final String pvPrefix = I18n.get(activity, "num_arrows");
        TextView pvLabel = addGlassLabel(visualCard, pvPrefix + ": " + currentPV, density, activity);
        final SeekBar pvSeekBar = addGlassSeekBar(visualCard, pvLabel, pvPrefix, currentPV - 1, 4, 1, density, activity);
        addGlassHint(visualCard, I18n.get(activity, "arrows_hint"), density, activity);
        addCardSeparator(visualCard, density);

        final CheckBox threatCb = addGlassCheckbox(visualCard, I18n.get(activity, "threat_arrow"), StockfishSettings.isThreatArrowsEnabled(activity), density, activity);
        final CheckBox evalBarCb = addGlassCheckbox(visualCard, I18n.get(activity, "eval_bar"), StockfishSettings.isEvalBarEnabled(activity), density, activity);
        final CheckBox wdlCb = addGlassCheckbox(visualCard, I18n.get(activity, "wdl_bar"), StockfishSettings.isWdlEnabled(activity), density, activity);
        final CheckBox infoCb = addGlassCheckbox(visualCard, I18n.get(activity, "depth_score_above"), StockfishSettings.isEngineInfoEnabled(activity), density, activity);
        final CheckBox mateCb = addGlassCheckbox(visualCard, I18n.get(activity, "forced_mates"), StockfishSettings.isMateAnnouncementEnabled(activity), density, activity);

        // --- TAB 3: ENGINE (Sức mạnh & Phân loại) ---
        final LinearLayout panelEngine = new LinearLayout(activity);
        panelEngine.setOrientation(LinearLayout.VERTICAL);
        panelEngine.setVisibility(View.GONE);
        tabContentRoot.addView(panelEngine);

        LinearLayout engineCard = createGlassCard(activity, density);
        panelEngine.addView(engineCard);

        final CheckBox classifCb = addGlassCheckbox(engineCard, I18n.get(activity, "rate_moves"), StockfishSettings.isMoveClassificationEnabled(activity), density, activity);
        final CheckBox blunderCb = addGlassCheckbox(engineCard, I18n.get(activity, "vibrate_blunder"), StockfishSettings.isBlunderAlertsEnabled(activity), density, activity);
        addGlassHint(engineCard, I18n.get(activity, "vibrate_hint"), density, activity);
        addCardSeparator(engineCard, density);

        final CheckBox eloCb = addGlassCheckbox(engineCard, I18n.get(activity, "limit_strength"), StockfishSettings.isLimitStrength(activity), density, activity);
        int currentElo = Math.max(1320, Math.min(3190, StockfishSettings.getElo(activity)));
        final String eloPrefix = I18n.get(activity, "engine_elo");
        final TextView eloLabel = addGlassLabel(engineCard, eloPrefix + ": " + currentElo, density, activity);
        final SeekBar eloSeekBar = addGlassSeekBar(engineCard, eloLabel, eloPrefix, currentElo - 1320, 3190 - 1320, 1320, density, activity);
        addGlassHint(engineCard, I18n.get(activity, "elo_hint"), density, activity);

        eloLabel.setEnabled(eloCb.isChecked());
        eloSeekBar.setEnabled(eloCb.isChecked());
        eloLabel.setAlpha(eloCb.isChecked() ? 1f : 0.4f);
        eloSeekBar.setAlpha(eloCb.isChecked() ? 1f : 0.4f);
        eloCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            eloLabel.setEnabled(isChecked);
            eloSeekBar.setEnabled(isChecked);
            eloLabel.setAlpha(isChecked ? 1f : 0.4f);
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
        resetBtn.setOnClickListener(v -> new android.app.AlertDialog.Builder(activity)
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
                .show());
        engineCard.addView(resetBtn);

        // Tab Switching Click Listeners
        View.OnClickListener tabListener = v -> {
            boolean isL = (v == tabLive);
            boolean isV = (v == tabVisual);
            boolean isE = (v == tabEngine);

            updateTabStyle(tabLive, isL, density);
            updateTabStyle(tabVisual, isV, density);
            updateTabStyle(tabEngine, isE, density);

            panelLive.setVisibility(isL ? View.VISIBLE : View.GONE);
            panelVisual.setVisibility(isV ? View.VISIBLE : View.GONE);
            panelEngine.setVisibility(isE ? View.VISIBLE : View.GONE);
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
        tgBg.setStroke((int) (1 * density), COLOR_ACCENT_CYAN);
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
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
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
        saveBtn.setPadding((int) (20 * density), (int) (8 * density), (int) (20 * density), (int) (8 * density));

        GradientDrawable saveBg = new GradientDrawable();
        saveBg.setColor(COLOR_ACCENT_BLUE);
        saveBg.setCornerRadius(10 * density);
        saveBg.setStroke((int) (1 * density), COLOR_ACCENT_CYAN);
        saveBtn.setBackground(saveBg);

        saveBtn.setOnClickListener(v -> {
            StockfishSettings.setEngineEnabled(activity, enabledCb.isChecked());
            StockfishSettings.setDepth(activity, Math.max(1, depthSeekBar.getProgress() + 1));
            StockfishSettings.setMultiPV(activity, Math.max(1, pvSeekBar.getProgress() + 1));
            StockfishSettings.setMySideOnly(activity, sideCb.isChecked());
            StockfishSettings.setLimitStrength(activity, eloCb.isChecked());
            StockfishSettings.setElo(activity, 1320 + eloSeekBar.getProgress());
            StockfishSettings.setPremiumEnabled(activity, true);
            StockfishSettings.setArrowsVisible(activity, arrowsCb.isChecked());
            StockfishSettings.setEvalBarEnabled(activity, evalBarCb.isChecked());
            StockfishSettings.setWdlEnabled(activity, wdlCb.isChecked());
            StockfishSettings.setEngineInfoEnabled(activity, infoCb.isChecked());
            StockfishSettings.setThreatArrowsEnabled(activity, threatCb.isChecked());
            StockfishSettings.setMoveClassificationEnabled(activity, classifCb.isChecked());
            StockfishSettings.setBlunderAlertsEnabled(activity, blunderCb.isChecked());
            StockfishSettings.setMateAnnouncementEnabled(activity, mateCb.isChecked());
            if (!mateCb.isChecked()) OverlayManager.hideMateAnnouncement();

            Toast.makeText(activity, I18n.get(activity, "settings_saved"), Toast.LENGTH_SHORT).show();

            Object state = StockfishExtension.getStateImpl();
            if (!enabledCb.isChecked() || !arrowsCb.isChecked()) {
                ArrowInjector.clearEngineArrows(state);
            }
            if (!enabledCb.isChecked() || !evalBarCb.isChecked()) OverlayManager.hideEvalBar();
            if (!enabledCb.isChecked() || !wdlCb.isChecked()) OverlayManager.hideWdlBar();
            if (!enabledCb.isChecked() || !infoCb.isChecked()) OverlayManager.hideEngineInfo();
            if (!enabledCb.isChecked()) OverlayManager.hideMateAnnouncement();
            if (enabledCb.isChecked()) {
                StockfishExtension.triggerAnalysisForCurrentState();
            }

            dialog.dismiss();
        });
        actionButtons.addView(saveBtn);

        footerLayout.addView(actionButtons);
        windowRoot.addView(footerLayout);

        dialog.setContentView(windowRoot);
        dialog.show();

        int width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.92f);
        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    // ══ UI Helpers matching NNVC Cyber Glass Architecture ══════════════════════

    private static LinearLayout createGlassCard(Activity activity, float density) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (12 * density);
        card.setPadding(pad, pad, pad, pad);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(COLOR_CARD_BG);
        bg.setCornerRadius(14 * density);
        bg.setStroke((int) (1 * density), COLOR_CARD_BORDER);
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
            bg.setStroke((int) (1 * density), COLOR_ACCENT_CYAN);
        } else {
            bg.setColor(Color.TRANSPARENT);
        }
        bg.setCornerRadius(10 * density);
        tab.setBackground(bg);
    }

    private static CheckBox addGlassCheckbox(LinearLayout layout, String labelText, boolean checked, float density, Activity activity) {
        CheckBox cb = new CheckBox(activity);
        cb.setText(labelText);
        cb.setTextColor(COLOR_TEXT_PRIMARY);
        cb.setTextSize(14);
        cb.setChecked(checked);
        cb.setPadding((int) (8 * density), (int) (6 * density), 0, (int) (6 * density));
        if (Build.VERSION.SDK_INT >= 21) {
            cb.setButtonTintList(ColorStateList.valueOf(COLOR_ACCENT_BLUE));
        }
        layout.addView(cb);
        return cb;
    }

    private static TextView addGlassLabel(LinearLayout layout, String text, float density, Activity activity) {
        TextView label = new TextView(activity);
        label.setText(text);
        label.setTextColor(COLOR_TEXT_SECONDARY);
        label.setTextSize(13);
        label.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        label.setPadding(0, (int) (6 * density), 0, 0);
        layout.addView(label);
        return label;
    }

    private static SeekBar addGlassSeekBar(LinearLayout layout, final TextView labelTv, final String labelPrefix, int progress, int max, final int minVal, float density, Activity activity) {
        SeekBar seekBar = new SeekBar(activity);
        seekBar.setMax(max);
        seekBar.setProgress(progress);
        if (Build.VERSION.SDK_INT >= 21) {
            seekBar.setProgressTintList(ColorStateList.valueOf(COLOR_ACCENT_BLUE));
            seekBar.setThumbTintList(ColorStateList.valueOf(COLOR_ACCENT_CYAN));
        }
        seekBar.setPadding((int) (6 * density), (int) (6 * density), (int) (6 * density), (int) (10 * density));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int prog, boolean fromUser) {
                int val = Math.max(minVal, prog + minVal);
                labelTv.setText(labelPrefix + ": " + val);
            }
            @Override
            public void onStartTrackingTouch(SeekBar sb) {}
            @Override
            public void onStopTrackingTouch(SeekBar sb) {}
        });

        layout.addView(seekBar);
        return seekBar;
    }

    private static TextView addGlassHint(LinearLayout layout, String text, float density, Activity activity) {
        TextView hint = new TextView(activity);
        hint.setText(text);
        hint.setTextColor(COLOR_TEXT_MUTED);
        hint.setTextSize(11);
        hint.setPadding((int) (4 * density), 0, (int) (4 * density), (int) (4 * density));
        layout.addView(hint);
        return hint;
    }

    private static void addCardSeparator(LinearLayout layout, float density) {
        View sep = new View(layout.getContext());
        sep.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (1 * density)
        ));
        GradientDivider(sep, 0x22FFFFFF);
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
}
