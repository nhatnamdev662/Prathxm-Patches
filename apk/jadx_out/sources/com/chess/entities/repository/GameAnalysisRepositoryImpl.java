package com.chess.entities.repository;

import com.chess.coach.Coach;
import com.chess.compengine.ceac.b;
import com.chess.compengine.ceac.h;
import com.chess.compengine.entities.AnalyzedGameData;
import com.chess.entities.AnalysisDepth;
import com.chess.entities.AnalysisEngine;
import com.chess.entities.CompatGameIdAndType;
import com.chess.entities.ComputerAnalysisConfiguration;
import com.chess.entities.GameAnalysisPermissions;
import com.chess.entities.UserSide;
import com.chess.net.internal.i;
import com.chess.net.v1.analysis.c;
import com.chess.utils.android.coroutines.CoroutineContextProvider;
import com.google.android.ai4;
import com.google.android.q22;
import com.google.android.ui4;
import com.squareup.moshi.f;
import java.util.Locale;
import java.util.Set;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.intrinsics.a;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.flow.d;

/* JADX INFO: loaded from: e:\chess mobile\apk\unpacked_dex\classes8.dex */
@Metadata(d1 = {"\u0000\u0086\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u0000 52\u00020\u0001:\u0001'B)\b\u0007\u0012\u0006\u0010\u0003\u001a\u00020\u0002\u0012\u0006\u0010\u0005\u001a\u00020\u0004\u0012\u0006\u0010\u0007\u001a\u00020\u0006\u0012\u0006\u0010\t\u001a\u00020\b¢\u0006\u0004\b\n\u0010\u000bJ2\u0010\u0015\u001a\u00020\u0014*\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0013\u001a\u00020\u0012H\u0082@¢\u0006\u0004\b\u0015\u0010\u0016JU\u0010'\u001a\b\u0012\u0004\u0012\u00020\r0&2\u0006\u0010\u0018\u001a\u00020\u00172\u0006\u0010\u001a\u001a\u00020\u00192\u0006\u0010\u001c\u001a\u00020\u001b2\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u001e0\u001d2\u0006\u0010!\u001a\u00020 2\b\u0010#\u001a\u0004\u0018\u00010\"2\u0006\u0010%\u001a\u00020$H\u0016¢\u0006\u0004\b'\u0010(R\u0014\u0010\u0003\u001a\u00020\u00028\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b'\u0010)R\u0014\u0010\u0005\u001a\u00020\u00048\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b*\u0010+R\u0014\u0010\u0007\u001a\u00020\u00068\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b,\u0010-R\u0014\u0010\t\u001a\u00020\b8\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b.\u0010/R\u001a\u00104\u001a\b\u0012\u0004\u0012\u000201008\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b2\u00103¨\u00066"}, d2 = {"Lcom/chess/gamereview/repository/GameAnalysisRepositoryImpl;", "Lcom/chess/gamereview/repository/h;", "Lcom/chess/utils/android/coroutines/CoroutineContextProvider;", "coroutineContextProvider", "Lcom/chess/net/v1/analysis/c;", "gameAnalysisService", "Lcom/chess/gamereview/repository/k;", "gameReviewLocalCache", "Lcom/chess/compengine/ceac/h;", "ceacGameAnalyzer", "<init>", "(Lcom/chess/utils/android/coroutines/CoroutineContextProvider;Lcom/chess/net/v1/analysis/c;Lcom/chess/gamereview/repository/k;Lcom/chess/compengine/ceac/h;)V", "Lcom/google/android/ui4;", "Lcom/chess/gamereview/repository/g;", "Lcom/chess/compengine/ceac/c;", "game", "Lcom/chess/compengine/ceac/b;", "meta", "Lcom/chess/entities/GameAnalysisPermissions;", "permissions", "", "h", "(Lcom/google/android/ui4;Lcom/chess/compengine/ceac/c;Lcom/chess/compengine/ceac/b;Lcom/chess/entities/GameAnalysisPermissions;Lcom/google/android/q22;)Ljava/lang/Object;", "Lcom/chess/entities/ComputerAnalysisConfiguration;", "config", "Lcom/chess/entities/UserSide;", "userSide", "Lcom/chess/coach/Coach;", "coach", "", "Lcom/chess/gamereview/repository/i;", "allowedSources", "Lcom/chess/entities/AnalysisDepth;", "analysisDepth", "Lcom/chess/entities/AnalysisEngine;", "analysisEngine", "", "skillsEnabled", "Lcom/google/android/ai4;", "a", "(Lcom/chess/entities/ComputerAnalysisConfiguration;Lcom/chess/entities/UserSide;Lcom/chess/coach/Coach;Ljava/util/Set;Lcom/chess/entities/AnalysisDepth;Lcom/chess/entities/AnalysisEngine;Z)Lcom/google/android/ai4;", "Lcom/chess/utils/android/coroutines/CoroutineContextProvider;", "b", "Lcom/chess/net/v1/analysis/c;", "c", "Lcom/chess/gamereview/repository/k;", "d", "Lcom/chess/compengine/ceac/h;", "Lcom/squareup/moshi/f;", "Lcom/chess/compengine/entities/AnalyzedGameData;", "e", "Lcom/squareup/moshi/f;", "analyzedGameDataAdapter", "f", "impl_release"}, k = 1, mv = {2, 2, 0}, xi = 48)
public final class GameAnalysisRepositoryImpl implements h {
    public static final int g = 8;

    /* JADX INFO: renamed from: a, reason: from kotlin metadata */
    private final CoroutineContextProvider coroutineContextProvider;

    /* JADX INFO: renamed from: b, reason: from kotlin metadata */
    private final c gameAnalysisService;

    /* JADX INFO: renamed from: c, reason: from kotlin metadata */
    private final k gameReviewLocalCache;

    /* JADX INFO: renamed from: d, reason: from kotlin metadata */
    private final h ceacGameAnalyzer;

    /* JADX INFO: renamed from: e, reason: from kotlin metadata */
    private final f<AnalyzedGameData> analyzedGameDataAdapter;

    public GameAnalysisRepositoryImpl(CoroutineContextProvider coroutineContextProvider, c cVar, k kVar, h hVar) {
        Intrinsics.checkNotNullParameter(coroutineContextProvider, "coroutineContextProvider");
        Intrinsics.checkNotNullParameter(cVar, "gameAnalysisService");
        Intrinsics.checkNotNullParameter(kVar, "gameReviewLocalCache");
        Intrinsics.checkNotNullParameter(hVar, "ceacGameAnalyzer");
        this.coroutineContextProvider = coroutineContextProvider;
        this.gameAnalysisService = cVar;
        this.gameReviewLocalCache = kVar;
        this.ceacGameAnalyzer = hVar;
        f<AnalyzedGameData> fVarC = i.b().c(AnalyzedGameData.class);
        Intrinsics.checkNotNullExpressionValue(fVarC, "adapter(...)");
        this.analyzedGameDataAdapter = fVarC;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Object h(ui4<? super g> ui4Var, com.chess.compengine.ceac.c cVar, b bVar, GameAnalysisPermissions gameAnalysisPermissions, q22<? super Unit> q22Var) {
        CompatGameIdAndType compatGameIdAndTypeB = cVar.b();
        Locale locale = Locale.getDefault();
        Intrinsics.checkNotNullExpressionValue(locale, "getDefault(...)");
        k.CacheKey cacheKey = new k.CacheKey(compatGameIdAndTypeB, locale, bVar.b(), bVar.a(), cVar.f(), bVar.c(), bVar.d(), bVar.f());
        com.chess.p010logging.f.b.m("GameReviewRepo", "Using CEAC for " + cacheKey);
        Object objCollect = this.ceacGameAnalyzer.a(cVar, bVar).collect(new GameAnalysisRepositoryImpl$remoteGameAnalysisFlow$2(ui4Var, gameAnalysisPermissions, bVar, compatGameIdAndTypeB, this, cacheKey), q22Var);
        return objCollect == a.g() ? objCollect : Unit.a;
    }

    @Override // com.chess.entities.repository.h
    public ai4<g> a(ComputerAnalysisConfiguration config, UserSide userSide, Coach coach, Set<? extends i> allowedSources, AnalysisDepth analysisDepth, AnalysisEngine analysisEngine, boolean skillsEnabled) {
        Intrinsics.checkNotNullParameter(config, "config");
        Intrinsics.checkNotNullParameter(userSide, "userSide");
        Intrinsics.checkNotNullParameter(coach, "coach");
        Intrinsics.checkNotNullParameter(allowedSources, "allowedSources");
        Intrinsics.checkNotNullParameter(analysisDepth, "analysisDepth");
        return d.O(new GameAnalysisRepositoryImpl$getGameAnalysis$1(config, coach, userSide, analysisDepth, analysisEngine, skillsEnabled, allowedSources, this, null));
    }
}
