package com.chess.chessboard.vm.movesinput;

import com.chess.chessboard.variants.PositionExtKt;
import com.chess.chessboard.variants.PromotionTargets;
import com.chess.chessboard.variants.d;
import com.chess.entities.Color;
import com.chess.entities.PremoveType;
import com.facebook.common.callercontext.ContextChain;
import com.google.android.c9a;
import com.google.android.fy3;
import com.google.android.oda;
import com.google.android.ph6;
import com.google.android.ps4;
import com.google.android.q22;
import com.google.android.qjd;
import com.google.android.rl2;
import com.google.android.ta2;
import com.google.android.ut0;
import java.util.List;
import kotlin.Lazy;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.MutablePropertyReference1Impl;
import kotlinx.coroutines.DelayKt;

/* JADX INFO: loaded from: e:\chess mobile\apk\unpacked_dex\classes6.dex */
@Metadata(d1 = {"\u0000Ð\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\b\u0016\u0018\u0000*\u000e\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u00028\u00000\u00012\b\u0012\u0004\u0012\u00028\u00000\u00032\u00020\u0004B1\u0012\u0006\u0010\u0005\u001a\u00028\u0000\u0012\b\b\u0001\u0010\u0007\u001a\u00020\u0006\u0012\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\b\u0012\b\b\u0002\u0010\f\u001a\u00020\u000b¢\u0006\u0004\b\r\u0010\u000eJ@\u0010\u0017\u001a\u00020\u00152\u0006\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0012\u001a\u00020\u00112\u001e\u0010\u0016\u001a\u001a\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u00150\u0013H\u0086@¢\u0006\u0004\b\u0017\u0010\u0018J\u000f\u0010\u0019\u001a\u00020\u0015H\u0000¢\u0006\u0004\b\u0019\u0010\u001aJ \u0010\u001e\u001a\u00020\u00152\u000e\u0010\u001d\u001a\n \u001c*\u0004\u0018\u00010\u001b0\u001bH\u0096\u0001¢\u0006\u0004\b\u001e\u0010\u001fJ \u0010 \u001a\u00020\u00152\u000e\u0010\u001d\u001a\n \u001c*\u0004\u0018\u00010\u001b0\u001bH\u0096\u0001¢\u0006\u0004\b \u0010\u001fJ>\u0010&\u001a\u0010\u0012\u0006\u0012\u0004\u0018\u00010\u0004\u0012\u0004\u0012\u00028\u00010%\"\n\b\u0001\u0010!*\u0004\u0018\u00010\u0004*\u00020\"2\u0006\u0010#\u001a\u00028\u00012\u0006\u0010$\u001a\u00020\u0011H\u0096\u0001¢\u0006\u0004\b&\u0010'JX\u0010*\u001a\u0010\u0012\u0006\u0012\u0004\u0018\u00010\u0004\u0012\u0004\u0012\u00028\u00010%\"\n\b\u0001\u0010!*\u0004\u0018\u00010\u0004*\u00020\"2\u0006\u0010#\u001a\u00028\u00012\u0006\u0010$\u001a\u00020\u00112\u0018\u0010)\u001a\u0014\u0012\u0004\u0012\u00028\u0001\u0012\u0004\u0012\u00028\u0001\u0012\u0004\u0012\u00020\u00150(H\u0096\u0001¢\u0006\u0004\b*\u0010+R \u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\b8\u0016X\u0096\u0004¢\u0006\f\n\u0004\b,\u0010-\u001a\u0004\b.\u0010/R\u0014\u0010\f\u001a\u00020\u000b8\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b0\u00101R+\u00109\u001a\u0002022\u0006\u00103\u001a\u0002028V@VX\u0096\u008e\u0002¢\u0006\u0012\n\u0004\b\u0019\u00104\u001a\u0004\b5\u00106\"\u0004\b7\u00108R+\u0010?\u001a\u00028\u00002\u0006\u00103\u001a\u00028\u00008V@VX\u0096\u008e\u0002¢\u0006\u0012\n\u0004\b:\u00104\u001a\u0004\b;\u0010<\"\u0004\b=\u0010>R+\u0010E\u001a\u00020@2\u0006\u00103\u001a\u00020@8V@VX\u0096\u008e\u0002¢\u0006\u0012\n\u0004\b\u0017\u00104\u001a\u0004\bA\u0010B\"\u0004\bC\u0010DR+\u0010K\u001a\u00020\u00062\u0006\u00103\u001a\u00020\u00068V@VX\u0096\u008e\u0002¢\u0006\u0012\n\u0004\bF\u00104\u001a\u0004\bG\u0010H\"\u0004\bI\u0010JR(\u0010T\u001a\b\u0012\u0004\u0012\u00020M0L8\u0016@\u0016X\u0096\u000e¢\u0006\u0012\n\u0004\bN\u0010O\u001a\u0004\bP\u0010Q\"\u0004\bR\u0010SR$\u0010Z\u001a\u0004\u0018\u00010M8\u0016@\u0016X\u0096\u000e¢\u0006\u0012\n\u0004\bP\u0010U\u001a\u0004\bV\u0010W\"\u0004\bX\u0010YR+\u0010`\u001a\u00020[2\u0006\u00103\u001a\u00020[8V@VX\u0096\u008e\u0002¢\u0006\u0012\n\u0004\b&\u00104\u001a\u0004\b\\\u0010]\"\u0004\b^\u0010_R/\u0010c\u001a\u0004\u0018\u00010M2\b\u00103\u001a\u0004\u0018\u00010M8V@VX\u0096\u008e\u0002¢\u0006\u0012\n\u0004\b*\u00104\u001a\u0004\ba\u0010W\"\u0004\bb\u0010YR/\u0010j\u001a\u0004\u0018\u00010d2\b\u00103\u001a\u0004\u0018\u00010d8V@VX\u0096\u008e\u0002¢\u0006\u0012\n\u0004\be\u00104\u001a\u0004\bf\u0010g\"\u0004\bh\u0010iR+\u0010p\u001a\u00020k2\u0006\u00103\u001a\u00020k8V@VX\u0096\u008e\u0002¢\u0006\u0012\n\u0004\b=\u00104\u001a\u0004\bl\u0010m\"\u0004\bn\u0010oR+\u0010w\u001a\u00020q2\u0006\u00103\u001a\u00020q8V@VX\u0096\u008e\u0002¢\u0006\u0012\n\u0004\br\u00104\u001a\u0004\bs\u0010t\"\u0004\bu\u0010vR+\u0010~\u001a\u00020x2\u0006\u00103\u001a\u00020x8V@VX\u0096\u008e\u0002¢\u0006\u0012\n\u0004\by\u00104\u001a\u0004\bz\u0010{\"\u0004\b|\u0010}R;\u0010\u0083\u0001\u001a\b\u0012\u0004\u0012\u00020\u007f0L2\f\u00103\u001a\b\u0012\u0004\u0012\u00020\u007f0L8V@VX\u0096\u008e\u0002¢\u0006\u0015\n\u0005\b\u0080\u0001\u00104\u001a\u0005\b\u0081\u0001\u0010Q\"\u0005\b\u0082\u0001\u0010SRA\u0010\u0088\u0001\u001a\u000b\u0012\u0005\u0012\u00030\u0084\u0001\u0018\u00010L2\u000f\u00103\u001a\u000b\u0012\u0005\u0012\u00030\u0084\u0001\u0018\u00010L8V@VX\u0096\u008e\u0002¢\u0006\u0015\n\u0005\b\u0085\u0001\u00104\u001a\u0005\b\u0086\u0001\u0010Q\"\u0005\b\u0087\u0001\u0010SR)\u0010\u008f\u0001\u001a\u00030\u0089\u00018\u0016@\u0016X\u0096.¢\u0006\u0017\n\u0006\b\u008a\u0001\u0010\u008b\u0001\u001a\u0006\b\u008c\u0001\u0010\u008d\u0001\"\u0005\br\u0010\u008e\u0001R%\u0010\u0095\u0001\u001a\b0\u0090\u0001j\u0003`\u0091\u00018VX\u0096\u0084\u0002¢\u0006\u000f\n\u0006\b\u0092\u0001\u0010\u0093\u0001\u001a\u0005\bN\u0010\u0094\u0001R\u001c\u0010\u0097\u0001\u001a\b\u0012\u0004\u0012\u00020M0L8VX\u0096\u0004¢\u0006\u0007\u001a\u0005\b\u0096\u0001\u0010Q¨\u0006\u0098\u0001"}, d2 = {"Lcom/chess/chessboard/vm/movesinput/CBViewModelStateImpl;", "Lcom/chess/chessboard/variants/d;", "POSITION", "Lcom/chess/chessboard/vm/movesinput/w;", "", "startingPosition", "", "startingFlipBoard", "Lkotlin/Function0;", "Lcom/chess/chessboard/vm/movesinput/Side;", "sideToPlaySelfEffects", "Landroidx/databinding/e;", "propertyChangeRegistry", "<init>", "(Lcom/chess/chessboard/variants/d;ZLkotlin/jvm/functions/Function0;Landroidx/databinding/e;)V", "Lcom/chess/entities/Color;", "color", "", "ply", "Lkotlin/Function3;", "Lcom/chess/chessboard/vm/movesinput/s0$a;", "", "applyUnverifiedMove", "f", "(Lcom/chess/entities/Color;ILcom/google/android/ps4;Lcom/google/android/q22;)Ljava/lang/Object;", "d", "()V", "Landroidx/databinding/d$a;", "kotlin.jvm.PlatformType", "callback", "a0", "(Landroidx/databinding/d$a;)V", "a3", "T", "Landroidx/databinding/d;", "initialValue", "propertyId", "Lcom/google/android/c9a;", "j", "(Landroidx/databinding/d;Ljava/lang/Object;I)Lcom/google/android/c9a;", "Lkotlin/Function2;", "afterChangeCallback", "k", "(Landroidx/databinding/d;Ljava/lang/Object;ILkotlin/jvm/functions/Function2;)Lcom/google/android/c9a;", "b", "Lkotlin/jvm/functions/Function0;", "getSideToPlaySelfEffects", "()Lkotlin/jvm/functions/Function0;", "c", "Landroidx/databinding/e;", "Lcom/chess/chessboard/vm/movesinput/c;", "<set-?>", "Lcom/google/android/c9a;", "o4", "()Lcom/chess/chessboard/vm/movesinput/c;", "N1", "(Lcom/chess/chessboard/vm/movesinput/c;)V", "availableMoves", "e", "getPosition", "()Lcom/chess/chessboard/variants/d;", "m", "(Lcom/chess/chessboard/variants/d;)V", "position", "Lcom/chess/chessboard/vm/movesinput/i;", "u6", "()Lcom/chess/chessboard/vm/movesinput/i;", "f1", "(Lcom/chess/chessboard/vm/movesinput/i;)V", "dragData", "g", "getFlipBoard", "()Z", "setFlipBoard", "(Z)V", "flipBoard", "", "Lcom/chess/chessboard/t;", "h", "Ljava/util/List;", ContextChain.TAG_INFRA, "()Ljava/util/List;", "c0", "(Ljava/util/List;)V", "highlightedMoveSquares", "Lcom/chess/chessboard/t;", "S0", "()Lcom/chess/chessboard/t;", "m5", "(Lcom/chess/chessboard/t;)V", "selectedSquare", "Lcom/chess/chessboard/vm/movesinput/i0;", "T0", "()Lcom/chess/chessboard/vm/movesinput/i0;", "i1", "(Lcom/chess/chessboard/vm/movesinput/i0;)V", "moveFeedback", "A4", "h6", "selectedSquareToMoveFrom", "Lcom/chess/chessboard/vm/movesinput/c0;", "l", "p2", "()Lcom/chess/chessboard/vm/movesinput/c0;", "U2", "(Lcom/chess/chessboard/vm/movesinput/c0;)V", "animationToPerformOnBoard", "Lcom/chess/chessboard/variants/PromotionTargets;", "g4", "()Lcom/chess/chessboard/variants/PromotionTargets;", "J1", "(Lcom/chess/chessboard/variants/PromotionTargets;)V", "promotionTargets", "Lcom/chess/entities/PremoveType;", "n", "F", "()Lcom/chess/entities/PremoveType;", "H2", "(Lcom/chess/entities/PremoveType;)V", "premoveType", "Lcom/chess/chessboard/vm/movesinput/s0;", "o", "Y2", "()Lcom/chess/chessboard/vm/movesinput/s0;", "t4", "(Lcom/chess/chessboard/vm/movesinput/s0;)V", "premoves", "Lcom/chess/chessboard/vm/movesinput/x0;", ContextChain.TAG_PRODUCT, "r1", "l5", "movesToHighlight", "Lcom/chess/chessboard/vm/movesinput/g0;", "q", "j5", "G2", "moveArrows", "Lcom/google/android/ta2;", "r", "Lcom/google/android/ta2;", "V1", "()Lcom/google/android/ta2;", "(Lcom/google/android/ta2;)V", "scope", "Lcom/google/android/fy3;", "Lkotlinx/coroutines/CloseableCoroutineDispatcher;", "s", "Lkotlin/Lazy;", "()Lcom/google/android/fy3;", "computeContext", "b5", "highlightedSquares", "cbviewmodel_release"}, k = 1, mv = {Color.BLACK_INT, Color.BLACK_INT, Color.NONE_INT}, xi = 48)
public class CBViewModelStateImpl<POSITION extends com.chess.chessboard.variants.d<POSITION>> implements w<POSITION>, androidx.databinding.d {
    static final /* synthetic */ ph6<Object>[] t = {oda.g(new MutablePropertyReference1Impl(CBViewModelStateImpl.class, "availableMoves", "getAvailableMoves()Lcom/chess/chessboard/vm/movesinput/AvailableMoves;", 0)), oda.g(new MutablePropertyReference1Impl(CBViewModelStateImpl.class, "position", "getPosition()Lcom/chess/chessboard/variants/Position;", 0)), oda.g(new MutablePropertyReference1Impl(CBViewModelStateImpl.class, "dragData", "getDragData()Lcom/chess/chessboard/vm/movesinput/CBPieceDragData;", 0)), oda.g(new MutablePropertyReference1Impl(CBViewModelStateImpl.class, "flipBoard", "getFlipBoard()Z", 0)), oda.g(new MutablePropertyReference1Impl(CBViewModelStateImpl.class, "moveFeedback", "getMoveFeedback()Lcom/chess/chessboard/vm/movesinput/MoveFeedback;", 0)), oda.g(new MutablePropertyReference1Impl(CBViewModelStateImpl.class, "selectedSquareToMoveFrom", "getSelectedSquareToMoveFrom()Lcom/chess/chessboard/Square;", 0)), oda.g(new MutablePropertyReference1Impl(CBViewModelStateImpl.class, "animationToPerformOnBoard", "getAnimationToPerformOnBoard()Lcom/chess/chessboard/vm/movesinput/ChessBoardAnimation;", 0)), oda.g(new MutablePropertyReference1Impl(CBViewModelStateImpl.class, "promotionTargets", "getPromotionTargets()Lcom/chess/chessboard/variants/PromotionTargets;", 0)), oda.g(new MutablePropertyReference1Impl(CBViewModelStateImpl.class, "premoveType", "getPremoveType()Lcom/chess/entities/PremoveType;", 0)), oda.g(new MutablePropertyReference1Impl(CBViewModelStateImpl.class, "premoves", "getPremoves()Lcom/chess/chessboard/vm/movesinput/Premoves;", 0)), oda.g(new MutablePropertyReference1Impl(CBViewModelStateImpl.class, "movesToHighlight", "getMovesToHighlight()Ljava/util/List;", 0)), oda.g(new MutablePropertyReference1Impl(CBViewModelStateImpl.class, "moveArrows", "getMoveArrows()Ljava/util/List;", 0))};
    private final /* synthetic */ rl2 a;

    /* JADX INFO: renamed from: b, reason: from kotlin metadata */
    private final Function0<Side> sideToPlaySelfEffects;

    /* JADX INFO: renamed from: c, reason: from kotlin metadata */
    private final androidx.databinding.e propertyChangeRegistry;

    /* JADX INFO: renamed from: d, reason: from kotlin metadata */
    private final c9a availableMoves;

    /* JADX INFO: renamed from: e, reason: from kotlin metadata */
    private final c9a position;

    /* JADX INFO: renamed from: f, reason: from kotlin metadata */
    private final c9a dragData;

    /* JADX INFO: renamed from: g, reason: from kotlin metadata */
    private final c9a flipBoard;

    /* JADX INFO: renamed from: h, reason: from kotlin metadata */
    private List<? extends com.chess.chessboard.t> highlightedMoveSquares;

    /* JADX INFO: renamed from: i, reason: from kotlin metadata */
    private com.chess.chessboard.t selectedSquare;

    /* JADX INFO: renamed from: j, reason: from kotlin metadata */
    private final c9a moveFeedback;

    /* JADX INFO: renamed from: k, reason: from kotlin metadata */
    private final c9a selectedSquareToMoveFrom;

    /* JADX INFO: renamed from: l, reason: from kotlin metadata */
    private final c9a animationToPerformOnBoard;

    /* JADX INFO: renamed from: m, reason: from kotlin metadata */
    private final c9a promotionTargets;

    /* JADX INFO: renamed from: n, reason: from kotlin metadata */
    private final c9a premoveType;

    /* JADX INFO: renamed from: o, reason: from kotlin metadata */
    private final c9a premoves;

    /* JADX INFO: renamed from: p, reason: from kotlin metadata */
    private final c9a movesToHighlight;

    /* JADX INFO: renamed from: q, reason: from kotlin metadata */
    private final c9a moveArrows;

    /* JADX INFO: renamed from: r, reason: from kotlin metadata */
    public ta2 scope;

    /* JADX INFO: renamed from: s, reason: from kotlin metadata */
    private final Lazy computeContext;

    /* JADX WARN: Multi-variable type inference failed */
    public CBViewModelStateImpl(POSITION position, boolean z, Function0<? extends Side> function0, androidx.databinding.e eVar) {
        Intrinsics.checkNotNullParameter(position, "startingPosition");
        Intrinsics.checkNotNullParameter(function0, "sideToPlaySelfEffects");
        Intrinsics.checkNotNullParameter(eVar, "propertyChangeRegistry");
        this.a = new rl2(eVar);
        this.sideToPlaySelfEffects = function0;
        this.propertyChangeRegistry = eVar;
        this.availableMoves = j(this, AvailableMoves.INSTANCE.a(), com.chess.chessboard.vm.a.b);
        this.position = j(this, position, com.chess.chessboard.vm.a.k);
        this.dragData = j(this, l.a, com.chess.chessboard.vm.a.c);
        this.flipBoard = k(this, Boolean.valueOf(z), com.chess.chessboard.vm.a.d, new Function2() { // from class: com.chess.chessboard.vm.movesinput.x
            public final Object invoke(Object obj, Object obj2) {
                return CBViewModelStateImpl.g(this.a, ((Boolean) obj).booleanValue(), ((Boolean) obj2).booleanValue());
            }
        });
        this.highlightedMoveSquares = kotlin.collections.m.p();
        this.moveFeedback = j(this, MoveFeedback.INSTANCE.a(), com.chess.chessboard.vm.a.g);
        this.selectedSquareToMoveFrom = j(this, null, com.chess.chessboard.vm.a.o);
        this.animationToPerformOnBoard = j(this, null, com.chess.chessboard.vm.a.p);
        this.promotionTargets = j(this, PromotionTargets.a, com.chess.chessboard.vm.a.m);
        this.premoveType = k(this, PremoveType.DISABLED, com.chess.chessboard.vm.a.f, new Function2() { // from class: com.chess.chessboard.vm.movesinput.y
            public final Object invoke(Object obj, Object obj2) {
                return CBViewModelStateImpl.l(this.a, (PremoveType) obj, (PremoveType) obj2);
            }
        });
        this.premoves = j(this, new Premoves(null, null, false, 7, null), com.chess.chessboard.vm.a.l);
        this.movesToHighlight = j(this, kotlin.collections.m.p(), com.chess.chessboard.vm.a.j);
        this.moveArrows = j(this, null, com.chess.chessboard.vm.a.e);
        this.computeContext = kotlin.c.b(new Function0() { // from class: com.chess.chessboard.vm.movesinput.z
            public final Object invoke() {
                return CBViewModelStateImpl.e(this.a);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final fy3 e(CBViewModelStateImpl cBViewModelStateImpl) {
        return com.chess.utils.android.coroutines.b.a.a().b(cBViewModelStateImpl.V1());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit g(CBViewModelStateImpl cBViewModelStateImpl, boolean z, boolean z2) {
        cBViewModelStateImpl.N1(AvailableMoves.INSTANCE.a());
        return Unit.a;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit l(CBViewModelStateImpl cBViewModelStateImpl, PremoveType premoveType, PremoveType premoveType2) {
        Intrinsics.checkNotNullParameter(premoveType, "<unused var>");
        Intrinsics.checkNotNullParameter(premoveType2, "newValue");
        if (!premoveType2.getEnabled()) {
            cBViewModelStateImpl.N1(AvailableMoves.INSTANCE.a());
            cBViewModelStateImpl.d();
        }
        return Unit.a;
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public com.chess.chessboard.t A4() {
        return (com.chess.chessboard.t) this.selectedSquareToMoveFrom.getValue(this, t[5]);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public PremoveType F() {
        return (PremoveType) this.premoveType.getValue(this, t[8]);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public void G2(List<HintArrow> list) {
        this.moveArrows.setValue(this, t[11], list);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public void H2(PremoveType premoveType) {
        Intrinsics.checkNotNullParameter(premoveType, "<set-?>");
        this.premoveType.setValue(this, t[8], premoveType);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public void J1(PromotionTargets promotionTargets) {
        Intrinsics.checkNotNullParameter(promotionTargets, "<set-?>");
        this.promotionTargets.setValue(this, t[7], promotionTargets);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public void N1(AvailableMoves availableMoves) {
        Intrinsics.checkNotNullParameter(availableMoves, "<set-?>");
        this.availableMoves.setValue(this, t[0], availableMoves);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    /* JADX INFO: renamed from: S0, reason: from getter */
    public com.chess.chessboard.t getSelectedSquare() {
        return this.selectedSquare;
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public MoveFeedback T0() {
        return (MoveFeedback) this.moveFeedback.getValue(this, t[4]);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public void U2(c0 c0Var) {
        this.animationToPerformOnBoard.setValue(this, t[6], c0Var);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public ta2 V1() {
        ta2 ta2Var = this.scope;
        if (ta2Var != null) {
            return ta2Var;
        }
        Intrinsics.x("scope");
        return null;
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public Premoves Y2() {
        return (Premoves) this.premoves.getValue(this, t[9]);
    }

    public void a0(androidx.databinding.d.a callback) {
        this.a.a0(callback);
    }

    public void a3(androidx.databinding.d.a callback) {
        this.a.a3(callback);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public List<com.chess.chessboard.t> b5() {
        com.chess.chessboard.l lVarE = T0().e();
        Pair pairA = (lVarE == null || (lVarE instanceof com.chess.chessboard.q)) ? qjd.a((Object) null, (Object) null) : qjd.a(com.chess.chessboard.n.a(lVarE), com.chess.chessboard.n.b(lVarE));
        return kotlin.collections.m.w0(kotlin.collections.m.X0(kotlin.collections.m.X0(kotlin.collections.m.b1(i(), getSelectedSquare()), (com.chess.chessboard.t) pairA.a()), (com.chess.chessboard.t) pairA.b()));
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public void c0(List<? extends com.chess.chessboard.t> list) {
        Intrinsics.checkNotNullParameter(list, "<set-?>");
        this.highlightedMoveSquares = list;
    }

    public final void d() {
        t4(new Premoves(null, null, false, 7, null));
    }

    /* JADX WARN: Code duplicated, block: B:7:0x0013  */
    public final Object f(Color color, int i, ps4<? super Premoves.Premove, ? super Integer, ? super Color, Unit> ps4Var, q22<? super Unit> q22Var) {
        CBViewModelStateImpl$executePremove$1 cBViewModelStateImpl$executePremove$1;
        if (q22Var instanceof CBViewModelStateImpl$executePremove$1) {
            cBViewModelStateImpl$executePremove$1 = (CBViewModelStateImpl$executePremove$1) q22Var;
            int i2 = cBViewModelStateImpl$executePremove$1.label;
            if ((i2 & Integer.MIN_VALUE) != 0) {
                cBViewModelStateImpl$executePremove$1.label = i2 - Integer.MIN_VALUE;
            } else {
                cBViewModelStateImpl$executePremove$1 = new CBViewModelStateImpl$executePremove$1(this, q22Var);
            }
        } else {
            cBViewModelStateImpl$executePremove$1 = new CBViewModelStateImpl$executePremove$1(this, q22Var);
        }
        Object obj = cBViewModelStateImpl$executePremove$1.result;
        Object objG = kotlin.coroutines.intrinsics.a.g();
        int i3 = cBViewModelStateImpl$executePremove$1.label;
        if (i3 == 0) {
            kotlin.f.b(obj);
            cBViewModelStateImpl$executePremove$1.L$0 = color;
            cBViewModelStateImpl$executePremove$1.L$1 = ps4Var;
            cBViewModelStateImpl$executePremove$1.I$0 = i;
            cBViewModelStateImpl$executePremove$1.label = 1;
            if (DelayKt.b(100L, cBViewModelStateImpl$executePremove$1) == objG) {
                return objG;
            }
        } else {
            if (i3 != 1) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            i = cBViewModelStateImpl$executePremove$1.I$0;
            ps4Var = (ps4) cBViewModelStateImpl$executePremove$1.L$1;
            color = (Color) cBViewModelStateImpl$executePremove$1.L$0;
            kotlin.f.b(obj);
        }
        if (PositionExtKt.e(getPosition()) != i) {
            return Unit.a;
        }
        Pair<Premoves.Premove, Premoves> pairO = Y2().o(color);
        Premoves.Premove premove = (Premoves.Premove) pairO.a();
        Premoves premoves = (Premoves) pairO.b();
        if (premove == null) {
            return Unit.a;
        }
        t4(premoves);
        ps4Var.invoke(premove, ut0.e(i), color);
        t4(Premoves.e(Y2(), null, null, false, 3, null));
        return Unit.a;
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public void f1(i iVar) {
        Intrinsics.checkNotNullParameter(iVar, "<set-?>");
        this.dragData.setValue(this, t[2], iVar);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public PromotionTargets g4() {
        return (PromotionTargets) this.promotionTargets.getValue(this, t[7]);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public boolean getFlipBoard() {
        return ((Boolean) this.flipBoard.getValue(this, t[3])).booleanValue();
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public POSITION getPosition() {
        return (POSITION) this.position.getValue(this, t[1]);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    /* JADX INFO: renamed from: h, reason: merged with bridge method [inline-methods] */
    public fy3 y2() {
        return (fy3) this.computeContext.getValue();
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public void h6(com.chess.chessboard.t tVar) {
        this.selectedSquareToMoveFrom.setValue(this, t[5], tVar);
    }

    public List<com.chess.chessboard.t> i() {
        return this.highlightedMoveSquares;
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public void i1(MoveFeedback moveFeedback) {
        Intrinsics.checkNotNullParameter(moveFeedback, "<set-?>");
        this.moveFeedback.setValue(this, t[4], moveFeedback);
    }

    public <T> c9a<Object, T> j(androidx.databinding.d dVar, T t2, int i) {
        Intrinsics.checkNotNullParameter(dVar, "<this>");
        return this.a.b(dVar, t2, i);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public List<HintArrow> j5() {
        return (List) this.moveArrows.getValue(this, t[11]);
    }

    public <T> c9a<Object, T> k(androidx.databinding.d dVar, T t2, int i, Function2<? super T, ? super T, Unit> function2) {
        Intrinsics.checkNotNullParameter(dVar, "<this>");
        Intrinsics.checkNotNullParameter(function2, "afterChangeCallback");
        return this.a.c(dVar, t2, i, function2);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public void l5(List<SquareToHighlightWithColor> list) {
        Intrinsics.checkNotNullParameter(list, "<set-?>");
        this.movesToHighlight.setValue(this, t[10], list);
    }

    public void m(POSITION position) {
        Intrinsics.checkNotNullParameter(position, "<set-?>");
        this.position.setValue(this, t[1], position);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public void m5(com.chess.chessboard.t tVar) {
        this.selectedSquare = tVar;
    }

    public void n(ta2 ta2Var) {
        Intrinsics.checkNotNullParameter(ta2Var, "<set-?>");
        this.scope = ta2Var;
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public AvailableMoves o4() {
        return (AvailableMoves) this.availableMoves.getValue(this, t[0]);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public c0 p2() {
        return (c0) this.animationToPerformOnBoard.getValue(this, t[6]);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public List<SquareToHighlightWithColor> r1() {
        return (List) this.movesToHighlight.getValue(this, t[10]);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public void t4(Premoves premoves) {
        Intrinsics.checkNotNullParameter(premoves, "<set-?>");
        this.premoves.setValue(this, t[9], premoves);
    }

    @Override // com.chess.chessboard.vm.movesinput.w
    public i u6() {
        return (i) this.dragData.getValue(this, t[2]);
    }

    public /* synthetic */ CBViewModelStateImpl(com.chess.chessboard.variants.d dVar, boolean z, Function0 function0, androidx.databinding.e eVar, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(dVar, z, function0, (i & 8) != 0 ? new androidx.databinding.e() : eVar);
    }
}
