#!/usr/bin/env sh
# Gradle wrapper shell script (minimal)
BASEDIR=$(dirname "$0")
# Try to use local gradle if available
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
# Fallback to using wrapper jar (if present)
if [ -f "$BASEDIR/gradle/wrapper/gradle-wrapper.jar" ]; then
  java -jar "$BASEDIR/gradle/wrapper/gradle-wrapper.jar" "$@"
else
  echo "Gradle wrapper JAR not found. Install Gradle or add gradle/wrapper/gradle-wrapper.jar."
  exit 1
fi
