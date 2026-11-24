package com.jeict.bridge;

import java.lang.reflect.Field;

/** Tells whether EMI's JEI compatibility layer (JEMI) currently has a JEI runtime. Read reflectively: no JEI types. */
public final class JemiRuntime {
    private static Field runtimeField;
    private static boolean lookedUp;

    private JemiRuntime() {}

    /** True while JEI is (re)loading and JEMI has no runtime to ask. */
    public static boolean isMissing() {
        try {
            if (!lookedUp) {
                lookedUp = true;
                runtimeField = Class.forName("dev.emi.emi.jemi.JemiPlugin").getField("runtime");
            }
            return runtimeField != null && runtimeField.get(null) == null;
        } catch (ReflectiveOperationException | LinkageError e) {
            return false;
        }
    }
}
