# Flatten all obfuscated classes into a single root package.
# Reduces dex string table size (package name prefixes).
-repackageclasses ''

# Strip Kotlin null-check intrinsics inserted at every non-null parameter boundary.
# Trade-off: NPE instead of "Parameter X is null" IllegalArgumentException.
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNull(...);
    public static void checkNotNullParameter(...);
    public static void checkParameterIsNotNull(...);
    public static void checkNotNullExpressionValue(...);
    public static void checkExpressionValueIsNotNull(...);
    public static void checkReturnedValueIsNotNull(...);
    public static void checkFieldIsNotNull(...);
}

# Disable coroutines debug facilities in release: assertions, debug mode, stack trace recovery.
-assumenosideeffects class kotlinx.coroutines.DebugKt {
    boolean getASSERTIONS_ENABLED();
    boolean getDEBUG();
    boolean getRECOVER_STACK_TRACES();
}

# Main dispatcher is always present on Android.
-assumenosideeffects class kotlinx.coroutines.internal.MainDispatchersKt {
    boolean SUPPORT_MISSING;
}
