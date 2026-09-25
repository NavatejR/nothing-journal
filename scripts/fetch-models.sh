#!/usr/bin/env bash
#
# Fetches the bundled AI models into app/src/main/assets/ai/.
#
# The model weights are too large for git hosting, so they are attached to
# the GitHub Releases page (and exist at their upstream sources on
# HuggingFace). Run this once after cloning; the build fails fast if a model
# is missing.
#
#   Gemma 3 1B Instruct (int4)  ~530 MB  — LiteRT .task file for MediaPipe
#   whisper.cpp tiny.en         ~75 MB   — ggml weights for the JNI bridge
#
set -euo pipefail

DIR="$(cd "$(dirname "$0")/.." && pwd)/app/src/main/assets/ai"
mkdir -p "$DIR"

fetch () {
  local url="$1" out="$2" min_bytes="$3"
  if [ -f "$DIR/$out" ] && [ "$(stat -f%z "$DIR/$out" 2>/dev/null || stat -c%s "$DIR/$out")" -ge "$min_bytes" ]; then
    echo "✓ $out already present"
    return
  fi
  echo "↓ $out ..."
  curl -L --fail --progress-bar -o "$DIR/$out.part" "$url"
  mv "$DIR/$out.part" "$DIR/$out"
  echo "✓ $out ($(du -h "$DIR/$out" | cut -f1))"
}

# Gemma 3 1B Instruct, int4 LiteRT task file (public LiteRT Community repo).
fetch \
  "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/gemma3-1b-it-int4.task" \
  "gemma3-1b-it-int4.task" 500000000

# whisper.cpp tiny.en ggml weights (English-only, fastest dictation model).
fetch \
  "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin" \
  "ggml-tiny.en.bin" 70000000

echo
echo "All models in place. Build with:  ./gradlew assembleDebug"
