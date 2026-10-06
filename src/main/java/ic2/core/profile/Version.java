/*
 * Decompiled with CFR 0.152.
 */
package ic2.core.profile;

import ic2.core.profile.Both;
import ic2.core.profile.NotClassic;
import ic2.core.profile.NotExperimental;
import java.lang.reflect.AnnotatedElement;

public enum Version {
    NEW,
    BOTH,
    OLD;


    public boolean isExperimental() {
        return this == NEW;
    }

    public boolean isClassic() {
        return this == OLD;
    }

    public static boolean shouldEnable(AnnotatedElement e) {
        return Version.shouldEnable(e, true);
    }

    public static boolean shouldEnable(AnnotatedElement e, boolean defaultState) {
        return true;
    }
}
