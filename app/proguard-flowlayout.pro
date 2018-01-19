# This rule will properly ProGuard all the model classes in
# the package org.apmem.tools.layouts.
-keepclassmembers class org.apmem.tools.layouts.** {
  *;
}