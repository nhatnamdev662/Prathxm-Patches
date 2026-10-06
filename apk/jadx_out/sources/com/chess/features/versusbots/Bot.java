package com.chess.features.versusbots;

import android.os.Parcel;
import android.os.Parcelable;
import com.chess.coach.Coach;
import com.chess.compengine.Personality;
import com.chess.entities.Country;
import com.chess.entities.CountryParceler;
import com.google.android.hc6;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: loaded from: e:\chess mobile\apk\unpacked_dex\classes8.dex */
@Metadata(d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b7\u0018\u00002\u00020\u0001:\u0003\u0014\u0015\u0016B\t\b\u0004¢\u0006\u0004\b\u0002\u0010\u0003R\u0014\u0010\u0007\u001a\u00020\u00048&X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006R\u0016\u0010\u000b\u001a\u0004\u0018\u00010\b8&X¦\u0004¢\u0006\u0006\u001a\u0004\b\t\u0010\nR\u0014\u0010\r\u001a\u00020\b8&X¦\u0004¢\u0006\u0006\u001a\u0004\b\f\u0010\nR\u0014\u0010\u0011\u001a\u00020\u000e8&X¦\u0004¢\u0006\u0006\u001a\u0004\b\u000f\u0010\u0010R\u0016\u0010\u0013\u001a\u0004\u0018\u00010\b8&X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0012\u0010\n\u0082\u0001\u0003\u0017\u0018\u0019¨\u0006\u001a"}, d2 = {"Lcom/chess/features/versusbots/Bot;", "Landroid/os/Parcelable;", "<init>", "()V", "Lcom/chess/features/versusbots/ChessEngineSettings;", "b", "()Lcom/chess/features/versusbots/ChessEngineSettings;", "engineSettings", "", "a", "()Ljava/lang/String;", "avatarUrl", "e", "username", "", "f", "()Z", "isV2", "d", "themeOverride", "CoachBot", "EngineBot", "PersonalityBot", "Lcom/chess/features/versusbots/Bot$CoachBot;", "Lcom/chess/features/versusbots/Bot$EngineBot;", "Lcom/chess/features/versusbots/Bot$PersonalityBot;", "entities_release"}, k = 1, mv = {2, 2, 0}, xi = 48)
@hc6(generateAdapter = true)
public abstract class Bot implements Parcelable {
    public static final int a = 0;

    @Metadata(d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\u0014\b\u0087\b\u0018\u00002\u00020\u0001B\u001d\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u0002\u0012\u0006\u0010\u0006\u001a\u00020\u0005¢\u0006\u0004\b\u0007\u0010\bJ\r\u0010\t\u001a\u00020\u0005¢\u0006\u0004\b\t\u0010\nJ\u001d\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\f\u001a\u00020\u000b2\u0006\u0010\r\u001a\u00020\u0005¢\u0006\u0004\b\u000f\u0010\u0010J*\u0010\u0011\u001a\u00020\u00002\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u00022\b\b\u0002\u0010\u0006\u001a\u00020\u0005HÆ\u0001¢\u0006\u0004\b\u0011\u0010\u0012J\u0010\u0010\u0014\u001a\u00020\u0013HÖ\u0001¢\u0006\u0004\b\u0014\u0010\u0015J\u0010\u0010\u0016\u001a\u00020\u0005HÖ\u0001¢\u0006\u0004\b\u0016\u0010\nJ\u001a\u0010\u001a\u001a\u00020\u00192\b\u0010\u0018\u001a\u0004\u0018\u00010\u0017HÖ\u0003¢\u0006\u0004\b\u001a\u0010\u001bR\u001d\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u00028\u0006¢\u0006\f\n\u0004\b\u001c\u0010\u001d\u001a\u0004\b\u001e\u0010\u001fR\u0017\u0010\u0006\u001a\u00020\u00058\u0006¢\u0006\f\n\u0004\b \u0010!\u001a\u0004\b\"\u0010\nR\u001d\u0010)\u001a\u00020\u00038\u0006¢\u0006\u0012\n\u0004\b#\u0010$\u0012\u0004\b'\u0010(\u001a\u0004\b%\u0010&R \u0010/\u001a\u00020*8\u0016X\u0096\u0004¢\u0006\u0012\n\u0004\b+\u0010,\u0012\u0004\b.\u0010(\u001a\u0004\b\u001c\u0010-R\"\u00104\u001a\u0004\u0018\u00010\u00138\u0016X\u0096\u0004¢\u0006\u0012\n\u0004\b0\u00101\u0012\u0004\b3\u0010(\u001a\u0004\b2\u0010\u0015R \u00106\u001a\u00020\u00138\u0016X\u0096\u0004¢\u0006\u0012\n\u0004\b\u0011\u00101\u0012\u0004\b5\u0010(\u001a\u0004\b+\u0010\u0015R \u0010;\u001a\u00020\u00198\u0016X\u0096D¢\u0006\u0012\n\u0004\b7\u00108\u0012\u0004\b:\u0010(\u001a\u0004\b0\u00109R\"\u0010=\u001a\u0004\u0018\u00010\u00138\u0016X\u0096\u0004¢\u0006\u0012\n\u0004\b\u001e\u00101\u0012\u0004\b<\u0010(\u001a\u0004\b#\u0010\u0015¨\u0006>"}, d2 = {"Lcom/chess/features/versusbots/Bot$EngineBot;", "Lcom/chess/features/versusbots/Bot;", "", "Lcom/chess/features/versusbots/EngineBotLevel;", "levels", "", "selectedLevelIndex", "<init>", "(Ljava/util/List;I)V", "describeContents", "()I", "Landroid/os/Parcel;", "dest", "flags", "", "writeToParcel", "(Landroid/os/Parcel;I)V", "g", "(Ljava/util/List;I)Lcom/chess/features/versusbots/Bot$EngineBot;", "", "toString", "()Ljava/lang/String;", "hashCode", "", "other", "", "equals", "(Ljava/lang/Object;)Z", "b", "Ljava/util/List;", "i", "()Ljava/util/List;", "c", "I", "k", "d", "Lcom/chess/features/versusbots/EngineBotLevel;", "j", "()Lcom/chess/features/versusbots/EngineBotLevel;", "getSelectedLevel$annotations", "()V", "selectedLevel", "Lcom/chess/features/versusbots/ChessEngineSettings;", "e", "Lcom/chess/features/versusbots/ChessEngineSettings;", "()Lcom/chess/features/versusbots/ChessEngineSettings;", "getEngineSettings$annotations", "engineSettings", "f", "Ljava/lang/String;", "a", "getAvatarUrl$annotations", "avatarUrl", "getUsername$annotations", "username", "h", "Z", "()Z", "isV2$annotations", "isV2", "getThemeOverride$annotations", "themeOverride", "entities_release"}, k = 1, mv = {2, 2, 0}, xi = 48)
    @hc6(generateAdapter = true)
    public static final /* data */ class EngineBot extends Bot {
        public static final Parcelable.Creator<EngineBot> CREATOR = new a();
        public static final int j = 0;

        /* JADX INFO: renamed from: b, reason: from kotlin metadata and from toString */
        private final List<EngineBotLevel> levels;

        /* JADX INFO: renamed from: c, reason: from kotlin metadata and from toString */
        private final int selectedLevelIndex;

        /* JADX INFO: renamed from: d, reason: from kotlin metadata */
        private final EngineBotLevel selectedLevel;

        /* JADX INFO: renamed from: e, reason: from kotlin metadata */
        private final ChessEngineSettings engineSettings;

        /* JADX INFO: renamed from: f, reason: from kotlin metadata */
        private final String avatarUrl;

        /* JADX INFO: renamed from: g, reason: from kotlin metadata */
        private final String username;

        /* JADX INFO: renamed from: h, reason: from kotlin metadata */
        private final boolean isV2;

        /* JADX INFO: renamed from: i, reason: from kotlin metadata */
        private final String themeOverride;

        @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
        public static final class a implements Parcelable.Creator<EngineBot> {
            @Override // android.os.Parcelable.Creator
            /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
            public final EngineBot createFromParcel(Parcel parcel) {
                Intrinsics.checkNotNullParameter(parcel, "parcel");
                int i = parcel.readInt();
                ArrayList arrayList = new ArrayList(i);
                for (int i2 = 0; i2 != i; i2++) {
                    arrayList.add(EngineBotLevel.CREATOR.createFromParcel(parcel));
                }
                return new EngineBot(arrayList, parcel.readInt());
            }

            @Override // android.os.Parcelable.Creator
            /* JADX INFO: renamed from: b, reason: merged with bridge method [inline-methods] */
            public final EngineBot[] newArray(int i) {
                return new EngineBot[i];
            }
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public EngineBot(List<EngineBotLevel> list, int i) {
            super(null);
            Intrinsics.checkNotNullParameter(list, "levels");
            this.levels = list;
            this.selectedLevelIndex = i;
            EngineBotLevel engineBotLevel = list.get(i);
            this.selectedLevel = engineBotLevel;
            this.engineSettings = new ChessEngineSettings(engineBotLevel.getLegacyKomodoLevel(), engineBotLevel.getKomodoLevel(), Personality.DEFAULT, "balanced", null, false, engineBotLevel.getRating(), 16, null);
            this.avatarUrl = engineBotLevel.getAvatarUrl();
            this.username = engineBotLevel.getUsername();
            this.themeOverride = engineBotLevel.getThemeOverride();
        }

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ EngineBot h(EngineBot engineBot, List list, int i, int i2, Object obj) {
            if ((i2 & 1) != 0) {
                list = engineBot.levels;
            }
            if ((i2 & 2) != 0) {
                i = engineBot.selectedLevelIndex;
            }
            return engineBot.g(list, i);
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: a, reason: from getter */
        public String getAvatarUrl() {
            return this.avatarUrl;
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: b, reason: from getter */
        public ChessEngineSettings getEngineSettings() {
            return this.engineSettings;
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: d, reason: from getter */
        public String getThemeOverride() {
            return this.themeOverride;
        }

        @Override // android.os.Parcelable
        public final int describeContents() {
            return 0;
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: e, reason: from getter */
        public String getUsername() {
            return this.username;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof EngineBot)) {
                return false;
            }
            EngineBot engineBot = (EngineBot) other;
            return Intrinsics.e(this.levels, engineBot.levels) && this.selectedLevelIndex == engineBot.selectedLevelIndex;
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: f, reason: from getter */
        public boolean getIsV2() {
            return this.isV2;
        }

        public final EngineBot g(List<EngineBotLevel> levels, int selectedLevelIndex) {
            Intrinsics.checkNotNullParameter(levels, "levels");
            return new EngineBot(levels, selectedLevelIndex);
        }

        public int hashCode() {
            return (this.levels.hashCode() * 31) + Integer.hashCode(this.selectedLevelIndex);
        }

        public final List<EngineBotLevel> i() {
            return this.levels;
        }

        /* JADX INFO: renamed from: j, reason: from getter */
        public final EngineBotLevel getSelectedLevel() {
            return this.selectedLevel;
        }

        /* JADX INFO: renamed from: k, reason: from getter */
        public final int getSelectedLevelIndex() {
            return this.selectedLevelIndex;
        }

        public String toString() {
            return "EngineBot(levels=" + this.levels + ", selectedLevelIndex=" + this.selectedLevelIndex + ")";
        }

        @Override // android.os.Parcelable
        public final void writeToParcel(Parcel dest, int flags) {
            Intrinsics.checkNotNullParameter(dest, "dest");
            List<EngineBotLevel> list = this.levels;
            dest.writeInt(list.size());
            Iterator<EngineBotLevel> it = list.iterator();
            while (it.hasNext()) {
                it.next().writeToParcel(dest, flags);
            }
            dest.writeInt(this.selectedLevelIndex);
        }
    }

    public /* synthetic */ Bot(DefaultConstructorMarker defaultConstructorMarker) {
        this();
    }

    /* JADX INFO: renamed from: a */
    public abstract String getAvatarUrl();

    /* JADX INFO: renamed from: b */
    public abstract ChessEngineSettings getEngineSettings();

    /* JADX INFO: renamed from: d */
    public abstract String getThemeOverride();

    /* JADX INFO: renamed from: e */
    public abstract String getUsername();

    /* JADX INFO: renamed from: f */
    public abstract boolean getIsV2();

    private Bot() {
    }

    @Metadata(d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0000\n\u0002\b\u0011\n\u0002\u0018\u0002\n\u0002\b\u0012\b\u0087\b\u0018\u00002\u00020\u0001B+\b\u0000\u0012\u0006\u0010\u0003\u001a\u00020\u0002\u0012\u0006\u0010\u0005\u001a\u00020\u0004\u0012\u0006\u0010\u0007\u001a\u00020\u0006\u0012\b\b\u0002\u0010\b\u001a\u00020\u0006¢\u0006\u0004\b\t\u0010\nB%\b\u0016\u0012\b\u0010\u0003\u001a\u0004\u0018\u00010\u0002\u0012\u0006\u0010\u0005\u001a\u00020\u0004\u0012\b\b\u0002\u0010\b\u001a\u00020\u0006¢\u0006\u0004\b\t\u0010\u000bJ\r\u0010\r\u001a\u00020\f¢\u0006\u0004\b\r\u0010\u000eJ\u001d\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\f¢\u0006\u0004\b\u0013\u0010\u0014J8\u0010\u0015\u001a\u00020\u00002\b\b\u0002\u0010\u0003\u001a\u00020\u00022\b\b\u0002\u0010\u0005\u001a\u00020\u00042\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\u0006HÆ\u0001¢\u0006\u0004\b\u0015\u0010\u0016J\u0010\u0010\u0018\u001a\u00020\u0017HÖ\u0001¢\u0006\u0004\b\u0018\u0010\u0019J\u0010\u0010\u001a\u001a\u00020\fHÖ\u0001¢\u0006\u0004\b\u001a\u0010\u000eJ\u001a\u0010\u001d\u001a\u00020\u00062\b\u0010\u001c\u001a\u0004\u0018\u00010\u001bHÖ\u0003¢\u0006\u0004\b\u001d\u0010\u001eR\u0017\u0010\u0003\u001a\u00020\u00028\u0006¢\u0006\f\n\u0004\b\u001f\u0010 \u001a\u0004\b!\u0010\"R\u0017\u0010\u0005\u001a\u00020\u00048\u0006¢\u0006\f\n\u0004\b#\u0010$\u001a\u0004\b%\u0010&R\u0017\u0010\u0007\u001a\u00020\u00068\u0006¢\u0006\f\n\u0004\b'\u0010(\u001a\u0004\b)\u0010*R\u0017\u0010\b\u001a\u00020\u00068\u0006¢\u0006\f\n\u0004\b+\u0010(\u001a\u0004\b,\u0010*R \u00103\u001a\u00020-8\u0016X\u0096\u0004¢\u0006\u0012\n\u0004\b.\u0010/\u0012\u0004\b1\u00102\u001a\u0004\b\u001f\u00100R \u00107\u001a\u00020\u00178\u0016X\u0096\u0004¢\u0006\u0012\n\u0004\b\u0015\u00104\u0012\u0004\b6\u00102\u001a\u0004\b5\u0010\u0019R \u0010:\u001a\u00020\u00178\u0016X\u0096\u0004¢\u0006\u0012\n\u0004\b8\u00104\u0012\u0004\b9\u00102\u001a\u0004\b+\u0010\u0019R \u0010<\u001a\u00020\u00068\u0016X\u0096D¢\u0006\u0012\n\u0004\b!\u0010(\u0012\u0004\b;\u00102\u001a\u0004\b.\u0010*R\"\u0010>\u001a\u0004\u0018\u00010\u00178\u0016X\u0096\u0004¢\u0006\u0012\n\u0004\b%\u00104\u0012\u0004\b=\u00102\u001a\u0004\b'\u0010\u0019¨\u0006?"}, d2 = {"Lcom/chess/features/versusbots/Bot$CoachBot;", "Lcom/chess/features/versusbots/Bot;", "Lcom/chess/coach/Coach;", "coach", "Lcom/chess/features/versusbots/CoachStrength;", "strength", "", "usesFallbackCoach", "isUltraEasy", "<init>", "(Lcom/chess/coach/Coach;Lcom/chess/features/versusbots/CoachStrength;ZZ)V", "(Lcom/chess/coach/Coach;Lcom/chess/features/versusbots/CoachStrength;Z)V", "", "describeContents", "()I", "Landroid/os/Parcel;", "dest", "flags", "", "writeToParcel", "(Landroid/os/Parcel;I)V", "g", "(Lcom/chess/coach/Coach;Lcom/chess/features/versusbots/CoachStrength;ZZ)Lcom/chess/features/versusbots/Bot$CoachBot;", "", "toString", "()Ljava/lang/String;", "hashCode", "", "other", "equals", "(Ljava/lang/Object;)Z", "b", "Lcom/chess/coach/Coach;", "i", "()Lcom/chess/coach/Coach;", "c", "Lcom/chess/features/versusbots/CoachStrength;", "j", "()Lcom/chess/features/versusbots/CoachStrength;", "d", "Z", "k", "()Z", "e", "l", "Lcom/chess/features/versusbots/ChessEngineSettings;", "f", "Lcom/chess/features/versusbots/ChessEngineSettings;", "()Lcom/chess/features/versusbots/ChessEngineSettings;", "getEngineSettings$annotations", "()V", "engineSettings", "Ljava/lang/String;", "a", "getAvatarUrl$annotations", "avatarUrl", "h", "getUsername$annotations", "username", "isV2$annotations", "isV2", "getThemeOverride$annotations", "themeOverride", "entities_release"}, k = 1, mv = {2, 2, 0}, xi = 48)
    @hc6(generateAdapter = true)
    public static final /* data */ class CoachBot extends Bot {
        public static final Parcelable.Creator<CoachBot> CREATOR = new a();
        public static final int k = Coach.$stable;

        /* JADX INFO: renamed from: b, reason: from kotlin metadata and from toString */
        private final Coach coach;

        /* JADX INFO: renamed from: c, reason: from kotlin metadata and from toString */
        private final CoachStrength strength;

        /* JADX INFO: renamed from: d, reason: from kotlin metadata and from toString */
        private final boolean usesFallbackCoach;

        /* JADX INFO: renamed from: e, reason: from kotlin metadata and from toString */
        private final boolean isUltraEasy;

        /* JADX INFO: renamed from: f, reason: from kotlin metadata */
        private final ChessEngineSettings engineSettings;

        /* JADX INFO: renamed from: g, reason: from kotlin metadata */
        private final String avatarUrl;

        /* JADX INFO: renamed from: h, reason: from kotlin metadata */
        private final String username;

        /* JADX INFO: renamed from: i, reason: from kotlin metadata */
        private final boolean isV2;

        /* JADX INFO: renamed from: j, reason: from kotlin metadata */
        private final String themeOverride;

        @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
        public static final class a implements Parcelable.Creator<CoachBot> {
            @Override // android.os.Parcelable.Creator
            /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
            public final CoachBot createFromParcel(Parcel parcel) {
                Intrinsics.checkNotNullParameter(parcel, "parcel");
                return new CoachBot(parcel.readParcelable(CoachBot.class.getClassLoader()), CoachStrength.valueOf(parcel.readString()), parcel.readInt() != 0, parcel.readInt() != 0);
            }

            @Override // android.os.Parcelable.Creator
            /* JADX INFO: renamed from: b, reason: merged with bridge method [inline-methods] */
            public final CoachBot[] newArray(int i) {
                return new CoachBot[i];
            }
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public CoachBot(Coach coach, CoachStrength coachStrength, boolean z, boolean z2) {
            super(null);
            Intrinsics.checkNotNullParameter(coach, "coach");
            Intrinsics.checkNotNullParameter(coachStrength, "strength");
            this.coach = coach;
            this.strength = coachStrength;
            this.usesFallbackCoach = z;
            this.isUltraEasy = z2;
            this.engineSettings = new ChessEngineSettings(coachStrength.getKomodoLevel(), Integer.valueOf(coachStrength.getKomodoLevel()), z2 ? Personality.NEW_TO_CHESS : Personality.DEFAULT, null, null, coachStrength.getIsAdaptive(), z2 ? 100 : coachStrength.getRating(), 16, null);
            this.avatarUrl = coach.getImageUrl();
            this.username = coach.getName();
            this.isV2 = true;
        }

        public static /* synthetic */ CoachBot h(CoachBot coachBot, Coach coach, CoachStrength coachStrength, boolean z, boolean z2, int i, Object obj) {
            if ((i & 1) != 0) {
                coach = coachBot.coach;
            }
            if ((i & 2) != 0) {
                coachStrength = coachBot.strength;
            }
            if ((i & 4) != 0) {
                z = coachBot.usesFallbackCoach;
            }
            if ((i & 8) != 0) {
                z2 = coachBot.isUltraEasy;
            }
            return coachBot.g(coach, coachStrength, z, z2);
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: a, reason: from getter */
        public String getAvatarUrl() {
            return this.avatarUrl;
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: b, reason: from getter */
        public ChessEngineSettings getEngineSettings() {
            return this.engineSettings;
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: d, reason: from getter */
        public String getThemeOverride() {
            return this.themeOverride;
        }

        @Override // android.os.Parcelable
        public final int describeContents() {
            return 0;
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: e, reason: from getter */
        public String getUsername() {
            return this.username;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CoachBot)) {
                return false;
            }
            CoachBot coachBot = (CoachBot) other;
            return Intrinsics.e(this.coach, coachBot.coach) && this.strength == coachBot.strength && this.usesFallbackCoach == coachBot.usesFallbackCoach && this.isUltraEasy == coachBot.isUltraEasy;
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: f, reason: from getter */
        public boolean getIsV2() {
            return this.isV2;
        }

        public final CoachBot g(Coach coach, CoachStrength strength, boolean usesFallbackCoach, boolean isUltraEasy) {
            Intrinsics.checkNotNullParameter(coach, "coach");
            Intrinsics.checkNotNullParameter(strength, "strength");
            return new CoachBot(coach, strength, usesFallbackCoach, isUltraEasy);
        }

        public int hashCode() {
            return (((((this.coach.hashCode() * 31) + this.strength.hashCode()) * 31) + Boolean.hashCode(this.usesFallbackCoach)) * 31) + Boolean.hashCode(this.isUltraEasy);
        }

        /* JADX INFO: renamed from: i, reason: from getter */
        public final Coach getCoach() {
            return this.coach;
        }

        /* JADX INFO: renamed from: j, reason: from getter */
        public final CoachStrength getStrength() {
            return this.strength;
        }

        /* JADX INFO: renamed from: k, reason: from getter */
        public final boolean getUsesFallbackCoach() {
            return this.usesFallbackCoach;
        }

        /* JADX INFO: renamed from: l, reason: from getter */
        public final boolean getIsUltraEasy() {
            return this.isUltraEasy;
        }

        public String toString() {
            return "CoachBot(coach=" + this.coach + ", strength=" + this.strength + ", usesFallbackCoach=" + this.usesFallbackCoach + ", isUltraEasy=" + this.isUltraEasy + ")";
        }

        @Override // android.os.Parcelable
        public final void writeToParcel(Parcel dest, int flags) {
            Intrinsics.checkNotNullParameter(dest, "dest");
            dest.writeParcelable(this.coach, flags);
            dest.writeString(this.strength.name());
            dest.writeInt(this.usesFallbackCoach ? 1 : 0);
            dest.writeInt(this.isUltraEasy ? 1 : 0);
        }

        public /* synthetic */ CoachBot(Coach coach, CoachStrength coachStrength, boolean z, boolean z2, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this(coach, coachStrength, z, (i & 8) != 0 ? false : z2);
        }

        /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
        public CoachBot(Coach coach, CoachStrength coachStrength, boolean z) {
            this(coach == null ? Coach.Companion.a() : coach, coachStrength, coach == null, z);
            Intrinsics.checkNotNullParameter(coachStrength, "strength");
        }
    }

    @Metadata(d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010$\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\u0000\n\u0002\b#\b\u0087\b\u0018\u00002\u00020\u0001Bµ\u0001\u0012\u0006\u0010\u0003\u001a\u00020\u0002\u0012\u0006\u0010\u0004\u001a\u00020\u0002\u0012\u0006\u0010\u0005\u001a\u00020\u0002\u0012\u0006\u0010\u0007\u001a\u00020\u0006\u0012\b\b\u0002\u0010\b\u001a\u00020\u0002\u0012\u0006\u0010\n\u001a\u00020\t\u0012\u0006\u0010\f\u001a\u00020\u000b\u0012\b\u0010\r\u001a\u0004\u0018\u00010\u0002\u0012\u0006\u0010\u000e\u001a\u00020\u0002\u0012\u0006\u0010\u0010\u001a\u00020\u000f\u0012\u0014\b\u0002\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00020\u0011\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u000f\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0002\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u000f\u0012\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0016\u0012\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u0002\u0012\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u0002¢\u0006\u0004\b\u001a\u0010\u001bJ\r\u0010\u001c\u001a\u00020\u0006¢\u0006\u0004\b\u001c\u0010\u001dJ\u001d\u0010\"\u001a\u00020!2\u0006\u0010\u001f\u001a\u00020\u001e2\u0006\u0010 \u001a\u00020\u0006¢\u0006\u0004\b\"\u0010#J\u0010\u0010$\u001a\u00020\u0002HÖ\u0001¢\u0006\u0004\b$\u0010%J\u0010\u0010&\u001a\u00020\u0006HÖ\u0001¢\u0006\u0004\b&\u0010\u001dJ\u001a\u0010)\u001a\u00020\u000f2\b\u0010(\u001a\u0004\u0018\u00010'HÖ\u0003¢\u0006\u0004\b)\u0010*R\u0017\u0010\u0003\u001a\u00020\u00028\u0006¢\u0006\f\n\u0004\b+\u0010,\u001a\u0004\b-\u0010%R\u0017\u0010\u0004\u001a\u00020\u00028\u0006¢\u0006\f\n\u0004\b.\u0010,\u001a\u0004\b/\u0010%R\u0017\u0010\u0005\u001a\u00020\u00028\u0006¢\u0006\f\n\u0004\b0\u0010,\u001a\u0004\b1\u0010%R\u0017\u0010\u0007\u001a\u00020\u00068\u0006¢\u0006\f\n\u0004\b2\u00103\u001a\u0004\b4\u0010\u001dR\u0017\u0010\b\u001a\u00020\u00028\u0006¢\u0006\f\n\u0004\b5\u0010,\u001a\u0004\b6\u0010%R\u0017\u0010\n\u001a\u00020\t8\u0006¢\u0006\f\n\u0004\b7\u00108\u001a\u0004\b9\u0010:R\u001a\u0010\f\u001a\u00020\u000b8\u0016X\u0096\u0004¢\u0006\f\n\u0004\b;\u0010<\u001a\u0004\b+\u0010=R\u001c\u0010\r\u001a\u0004\u0018\u00010\u00028\u0016X\u0096\u0004¢\u0006\f\n\u0004\b9\u0010,\u001a\u0004\b>\u0010%R\u001a\u0010\u000e\u001a\u00020\u00028\u0016X\u0096\u0004¢\u0006\f\n\u0004\b1\u0010,\u001a\u0004\b2\u0010%R\u0017\u0010\u0010\u001a\u00020\u000f8\u0006¢\u0006\f\n\u0004\b?\u0010@\u001a\u0004\b;\u0010AR#\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00020\u00118\u0006¢\u0006\f\n\u0004\bB\u0010C\u001a\u0004\bD\u0010ER\u001a\u0010\u0013\u001a\u00020\u000f8\u0016X\u0096\u0004¢\u0006\f\n\u0004\b-\u0010@\u001a\u0004\b5\u0010AR\u001c\u0010\u0014\u001a\u0004\u0018\u00010\u00028\u0016X\u0096\u0004¢\u0006\f\n\u0004\b/\u0010,\u001a\u0004\b0\u0010%R\u0017\u0010\u0015\u001a\u00020\u000f8\u0006¢\u0006\f\n\u0004\bD\u0010@\u001a\u0004\bF\u0010AR\u0019\u0010\u0017\u001a\u0004\u0018\u00010\u00168\u0006¢\u0006\f\n\u0004\b4\u0010G\u001a\u0004\b7\u0010HR\u0019\u0010\u0018\u001a\u0004\u0018\u00010\u00028\u0006¢\u0006\f\n\u0004\bI\u0010,\u001a\u0004\bB\u0010%R\u0019\u0010\u0019\u001a\u0004\u0018\u00010\u00028\u0006¢\u0006\f\n\u0004\b6\u0010,\u001a\u0004\b?\u0010%¨\u0006J"}, d2 = {"Lcom/chess/features/versusbots/Bot$PersonalityBot;", "Lcom/chess/features/versusbots/Bot;", "", "id", "name", "description", "", "rating", "ratingText", "Lcom/chess/entities/Country;", "country", "Lcom/chess/features/versusbots/ChessEngineSettings;", "engineSettings", "avatarUrl", "username", "", "canPlay", "", "phrases", "isV2", "themeOverride", "isAssociatedWithCampaignThatRequiresAccountActivation", "Lcom/chess/features/versusbots/BotEventProperties;", "botEventProperties", "fenAsWhite", "fenAsBlack", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Lcom/chess/entities/Country;Lcom/chess/features/versusbots/ChessEngineSettings;Ljava/lang/String;Ljava/lang/String;ZLjava/util/Map;ZLjava/lang/String;ZLcom/chess/features/versusbots/BotEventProperties;Ljava/lang/String;Ljava/lang/String;)V", "describeContents", "()I", "Landroid/os/Parcel;", "dest", "flags", "", "writeToParcel", "(Landroid/os/Parcel;I)V", "toString", "()Ljava/lang/String;", "hashCode", "", "other", "equals", "(Ljava/lang/Object;)Z", "b", "Ljava/lang/String;", "m", "c", "n", "d", "j", "e", "I", "p", "f", "r", "g", "Lcom/chess/entities/Country;", "i", "()Lcom/chess/entities/Country;", "h", "Lcom/chess/features/versusbots/ChessEngineSettings;", "()Lcom/chess/features/versusbots/ChessEngineSettings;", "a", "k", "Z", "()Z", "l", "Ljava/util/Map;", "o", "()Ljava/util/Map;", "s", "Lcom/chess/features/versusbots/BotEventProperties;", "()Lcom/chess/features/versusbots/BotEventProperties;", "q", "entities_release"}, k = 1, mv = {2, 2, 0}, xi = 48)
    @hc6(generateAdapter = true)
    public static final /* data */ class PersonalityBot extends Bot {
        public static final Parcelable.Creator<PersonalityBot> CREATOR = new a();
        public static final int s = 0;

        /* JADX INFO: renamed from: b, reason: from kotlin metadata and from toString */
        private final String id;

        /* JADX INFO: renamed from: c, reason: from kotlin metadata and from toString */
        private final String name;

        /* JADX INFO: renamed from: d, reason: from kotlin metadata and from toString */
        private final String description;

        /* JADX INFO: renamed from: e, reason: from kotlin metadata and from toString */
        private final int rating;

        /* JADX INFO: renamed from: f, reason: from kotlin metadata and from toString */
        private final String ratingText;

        /* JADX INFO: renamed from: g, reason: from kotlin metadata and from toString */
        private final Country country;

        /* JADX INFO: renamed from: h, reason: from kotlin metadata and from toString */
        private final ChessEngineSettings engineSettings;

        /* JADX INFO: renamed from: i, reason: from kotlin metadata and from toString */
        private final String avatarUrl;

        /* JADX INFO: renamed from: j, reason: from kotlin metadata and from toString */
        private final String username;

        /* JADX INFO: renamed from: k, reason: from kotlin metadata and from toString */
        private final boolean canPlay;

        /* JADX INFO: renamed from: l, reason: from kotlin metadata and from toString */
        private final Map<String, String> phrases;

        /* JADX INFO: renamed from: m, reason: from kotlin metadata and from toString */
        private final boolean isV2;

        /* JADX INFO: renamed from: n, reason: from kotlin metadata and from toString */
        private final String themeOverride;

        /* JADX INFO: renamed from: o, reason: from kotlin metadata and from toString */
        private final boolean isAssociatedWithCampaignThatRequiresAccountActivation;

        /* JADX INFO: renamed from: p, reason: from kotlin metadata and from toString */
        private final BotEventProperties botEventProperties;

        /* JADX INFO: renamed from: q, reason: from kotlin metadata and from toString */
        private final String fenAsWhite;

        /* JADX INFO: renamed from: r, reason: from kotlin metadata and from toString */
        private final String fenAsBlack;

        @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
        public static final class a implements Parcelable.Creator<PersonalityBot> {
            @Override // android.os.Parcelable.Creator
            /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
            public final PersonalityBot createFromParcel(Parcel parcel) {
                Intrinsics.checkNotNullParameter(parcel, "parcel");
                String string = parcel.readString();
                String string2 = parcel.readString();
                String string3 = parcel.readString();
                int i = parcel.readInt();
                String string4 = parcel.readString();
                Country countryCreate = CountryParceler.INSTANCE.create(parcel);
                ChessEngineSettings chessEngineSettingsCreateFromParcel = ChessEngineSettings.CREATOR.createFromParcel(parcel);
                String string5 = parcel.readString();
                String string6 = parcel.readString();
                boolean z = true;
                if (parcel.readInt() == 0) {
                    z = false;
                }
                int i2 = parcel.readInt();
                LinkedHashMap linkedHashMap = new LinkedHashMap(i2);
                for (int i3 = 0; i3 != i2; i3++) {
                    linkedHashMap.put(parcel.readString(), parcel.readString());
                }
                return new PersonalityBot(string, string2, string3, i, string4, countryCreate, chessEngineSettingsCreateFromParcel, string5, string6, z, linkedHashMap, parcel.readInt() != 0, parcel.readString(), parcel.readInt() != 0, parcel.readInt() == 0 ? null : BotEventProperties.CREATOR.createFromParcel(parcel), parcel.readString(), parcel.readString());
            }

            @Override // android.os.Parcelable.Creator
            /* JADX INFO: renamed from: b, reason: merged with bridge method [inline-methods] */
            public final PersonalityBot[] newArray(int i) {
                return new PersonalityBot[i];
            }
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public PersonalityBot(String str, String str2, String str3, int i, String str4, Country country, ChessEngineSettings chessEngineSettings, String str5, String str6, boolean z, Map<String, String> map, boolean z2, String str7, boolean z3, BotEventProperties botEventProperties, String str8, String str9) {
            super(null);
            Intrinsics.checkNotNullParameter(str, "id");
            Intrinsics.checkNotNullParameter(str2, "name");
            Intrinsics.checkNotNullParameter(str3, "description");
            Intrinsics.checkNotNullParameter(str4, "ratingText");
            Intrinsics.checkNotNullParameter(country, "country");
            Intrinsics.checkNotNullParameter(chessEngineSettings, "engineSettings");
            Intrinsics.checkNotNullParameter(str6, "username");
            Intrinsics.checkNotNullParameter(map, "phrases");
            this.id = str;
            this.name = str2;
            this.description = str3;
            this.rating = i;
            this.ratingText = str4;
            this.country = country;
            this.engineSettings = chessEngineSettings;
            this.avatarUrl = str5;
            this.username = str6;
            this.canPlay = z;
            this.phrases = map;
            this.isV2 = z2;
            this.themeOverride = str7;
            this.isAssociatedWithCampaignThatRequiresAccountActivation = z3;
            this.botEventProperties = botEventProperties;
            this.fenAsWhite = str8;
            this.fenAsBlack = str9;
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: a, reason: from getter */
        public String getAvatarUrl() {
            return this.avatarUrl;
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: b, reason: from getter */
        public ChessEngineSettings getEngineSettings() {
            return this.engineSettings;
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: d, reason: from getter */
        public String getThemeOverride() {
            return this.themeOverride;
        }

        @Override // android.os.Parcelable
        public final int describeContents() {
            return 0;
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: e, reason: from getter */
        public String getUsername() {
            return this.username;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof PersonalityBot)) {
                return false;
            }
            PersonalityBot personalityBot = (PersonalityBot) other;
            return Intrinsics.e(this.id, personalityBot.id) && Intrinsics.e(this.name, personalityBot.name) && Intrinsics.e(this.description, personalityBot.description) && this.rating == personalityBot.rating && Intrinsics.e(this.ratingText, personalityBot.ratingText) && Intrinsics.e(this.country, personalityBot.country) && Intrinsics.e(this.engineSettings, personalityBot.engineSettings) && Intrinsics.e(this.avatarUrl, personalityBot.avatarUrl) && Intrinsics.e(this.username, personalityBot.username) && this.canPlay == personalityBot.canPlay && Intrinsics.e(this.phrases, personalityBot.phrases) && this.isV2 == personalityBot.isV2 && Intrinsics.e(this.themeOverride, personalityBot.themeOverride) && this.isAssociatedWithCampaignThatRequiresAccountActivation == personalityBot.isAssociatedWithCampaignThatRequiresAccountActivation && Intrinsics.e(this.botEventProperties, personalityBot.botEventProperties) && Intrinsics.e(this.fenAsWhite, personalityBot.fenAsWhite) && Intrinsics.e(this.fenAsBlack, personalityBot.fenAsBlack);
        }

        @Override // com.chess.features.versusbots.Bot
        /* JADX INFO: renamed from: f, reason: from getter */
        public boolean getIsV2() {
            return this.isV2;
        }

        /* JADX INFO: renamed from: g, reason: from getter */
        public final BotEventProperties getBotEventProperties() {
            return this.botEventProperties;
        }

        /* JADX INFO: renamed from: h, reason: from getter */
        public final boolean getCanPlay() {
            return this.canPlay;
        }

        public int hashCode() {
            int iHashCode = ((((((((((((this.id.hashCode() * 31) + this.name.hashCode()) * 31) + this.description.hashCode()) * 31) + Integer.hashCode(this.rating)) * 31) + this.ratingText.hashCode()) * 31) + this.country.hashCode()) * 31) + this.engineSettings.hashCode()) * 31;
            String str = this.avatarUrl;
            int iHashCode2 = (((((((((iHashCode + (str == null ? 0 : str.hashCode())) * 31) + this.username.hashCode()) * 31) + Boolean.hashCode(this.canPlay)) * 31) + this.phrases.hashCode()) * 31) + Boolean.hashCode(this.isV2)) * 31;
            String str2 = this.themeOverride;
            int iHashCode3 = (((iHashCode2 + (str2 == null ? 0 : str2.hashCode())) * 31) + Boolean.hashCode(this.isAssociatedWithCampaignThatRequiresAccountActivation)) * 31;
            BotEventProperties botEventProperties = this.botEventProperties;
            int iHashCode4 = (iHashCode3 + (botEventProperties == null ? 0 : botEventProperties.hashCode())) * 31;
            String str3 = this.fenAsWhite;
            int iHashCode5 = (iHashCode4 + (str3 == null ? 0 : str3.hashCode())) * 31;
            String str4 = this.fenAsBlack;
            return iHashCode5 + (str4 != null ? str4.hashCode() : 0);
        }

        /* JADX INFO: renamed from: i, reason: from getter */
        public final Country getCountry() {
            return this.country;
        }

        /* JADX INFO: renamed from: j, reason: from getter */
        public final String getDescription() {
            return this.description;
        }

        /* JADX INFO: renamed from: k, reason: from getter */
        public final String getFenAsBlack() {
            return this.fenAsBlack;
        }

        /* JADX INFO: renamed from: l, reason: from getter */
        public final String getFenAsWhite() {
            return this.fenAsWhite;
        }

        /* JADX INFO: renamed from: m, reason: from getter */
        public final String getId() {
            return this.id;
        }

        /* JADX INFO: renamed from: n, reason: from getter */
        public final String getName() {
            return this.name;
        }

        public final Map<String, String> o() {
            return this.phrases;
        }

        /* JADX INFO: renamed from: p, reason: from getter */
        public final int getRating() {
            return this.rating;
        }

        /* JADX INFO: renamed from: r, reason: from getter */
        public final String getRatingText() {
            return this.ratingText;
        }

        /* JADX INFO: renamed from: s, reason: from getter */
        public final boolean getIsAssociatedWithCampaignThatRequiresAccountActivation() {
            return this.isAssociatedWithCampaignThatRequiresAccountActivation;
        }

        public String toString() {
            return "PersonalityBot(id=" + this.id + ", name=" + this.name + ", description=" + this.description + ", rating=" + this.rating + ", ratingText=" + this.ratingText + ", country=" + this.country + ", engineSettings=" + this.engineSettings + ", avatarUrl=" + this.avatarUrl + ", username=" + this.username + ", canPlay=" + this.canPlay + ", phrases=" + this.phrases + ", isV2=" + this.isV2 + ", themeOverride=" + this.themeOverride + ", isAssociatedWithCampaignThatRequiresAccountActivation=" + this.isAssociatedWithCampaignThatRequiresAccountActivation + ", botEventProperties=" + this.botEventProperties + ", fenAsWhite=" + this.fenAsWhite + ", fenAsBlack=" + this.fenAsBlack + ")";
        }

        @Override // android.os.Parcelable
        public final void writeToParcel(Parcel dest, int flags) {
            Intrinsics.checkNotNullParameter(dest, "dest");
            dest.writeString(this.id);
            dest.writeString(this.name);
            dest.writeString(this.description);
            dest.writeInt(this.rating);
            dest.writeString(this.ratingText);
            CountryParceler.INSTANCE.write(this.country, dest, flags);
            this.engineSettings.writeToParcel(dest, flags);
            dest.writeString(this.avatarUrl);
            dest.writeString(this.username);
            dest.writeInt(this.canPlay ? 1 : 0);
            Map<String, String> map = this.phrases;
            dest.writeInt(map.size());
            for (Map.Entry<String, String> entry : map.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
            dest.writeInt(this.isV2 ? 1 : 0);
            dest.writeString(this.themeOverride);
            dest.writeInt(this.isAssociatedWithCampaignThatRequiresAccountActivation ? 1 : 0);
            BotEventProperties botEventProperties = this.botEventProperties;
            if (botEventProperties == null) {
                dest.writeInt(0);
            } else {
                dest.writeInt(1);
                botEventProperties.writeToParcel(dest, flags);
            }
            dest.writeString(this.fenAsWhite);
            dest.writeString(this.fenAsBlack);
        }

        public /* synthetic */ PersonalityBot(String str, String str2, String str3, int i, String str4, Country country, ChessEngineSettings chessEngineSettings, String str5, String str6, boolean z, Map map, boolean z2, String str7, boolean z3, BotEventProperties botEventProperties, String str8, String str9, int i2, DefaultConstructorMarker defaultConstructorMarker) {
            this(str, str2, str3, i, (i2 & 16) != 0 ? String.valueOf(i) : str4, country, chessEngineSettings, str5, str6, z, (i2 & 1024) != 0 ? kotlin.collections.b0.j() : map, (i2 & 2048) != 0 ? false : z2, (i2 & 4096) != 0 ? null : str7, (i2 & 8192) != 0 ? false : z3, (i2 & 16384) != 0 ? null : botEventProperties, (32768 & i2) != 0 ? null : str8, (i2 & 65536) != 0 ? null : str9);
        }
    }
}
