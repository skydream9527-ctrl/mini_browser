# GeckoView
-keep class org.mozilla.geckoview.** { *; }
-dontwarn org.mozilla.geckoview.**

# SnakeYAML (GeckoView transitive dependency)
-dontwarn java.beans.**
-dontwarn org.yaml.snakeyaml.**

# OkHttp
-dontwarn okhttp3.internal.platform.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
