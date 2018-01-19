# Add this global rule
-keepattributes Signature

# This rule will properly ProGuard all the model classes in
# the package com.artemchep.timetable.database.models.
-keepclassmembers class com.artemchep.horario.models.** {
  *;
}