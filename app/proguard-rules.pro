-keep class com.pcmiguel.easysign.services.** { *; }

-keep class com.blongho.** { *; }
-keep interface com.blongho.**

# -renamesourcefileattribute SourceFile
-keeppackagenames com.blongho.country_data
-keepclassmembers class com.blongho.country_data.* { public *; }
-keep class com.blongho.country_data.R$* { *; }