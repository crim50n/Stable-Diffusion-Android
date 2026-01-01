#!/bin/bash
# Script to prepare QNN libraries from LocalDream APK
#
# Usage: ./scripts/prepare_qnn_libs.sh
#
# This script downloads the LocalDream APK and extracts:
# - libstable_diffusion_core.so (main native library)
# - QNN libraries (libQnnHtp*.so, libQnnSystem.so)
# - Base MNN models (clip, tokenizer, unet, vae)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
QNN_MODULE_DIR="$PROJECT_DIR/feature/qnn/src/main"

# GitHub repository info
REPO_OWNER="xororz"
REPO_NAME="local-dream"
APK_PATTERN="LocalDream_armv8a_.*\.apk"

APK_FILE="/tmp/LocalDream.apk"
EXTRACT_DIR="/tmp/localdream_extracted"

echo "============================================"
echo "  QNN Libraries Preparation Script"
echo "============================================"
echo ""

# Fetch latest release info from GitHub API
echo "[0/6] Fetching latest release info..."
LATEST_RELEASE_URL="https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"
RELEASE_INFO=$(curl -s "$LATEST_RELEASE_URL")

# Extract APK download URL
APK_URL=$(echo "$RELEASE_INFO" | grep -o "https://.*${APK_PATTERN}" | head -1)
RELEASE_TAG=$(echo "$RELEASE_INFO" | grep '"tag_name"' | sed -E 's/.*"tag_name": "([^"]+)".*/\1/')

if [ -z "$APK_URL" ]; then
    echo "ERROR: Could not find APK download URL in latest release"
    echo "Please check: https://github.com/$REPO_OWNER/$REPO_NAME/releases/latest"
    exit 1
fi

echo "  Latest release: $RELEASE_TAG"
echo "  APK URL: $APK_URL"
echo ""

# Check if libraries already exist
if [ -f "$QNN_MODULE_DIR/jniLibs/arm64-v8a/libstable_diffusion_core.so" ]; then
    echo "Libraries already exist. Delete them first to re-download."
    echo "  rm -rf $QNN_MODULE_DIR/jniLibs/arm64-v8a/*.so"
    echo "  rm -rf $QNN_MODULE_DIR/assets/qnnlibs/*.so"
    exit 0
fi

# Download APK
echo "[1/6] Downloading LocalDream APK..."
if [ -f "$APK_FILE" ]; then
    echo "  APK already downloaded, skipping..."
else
    curl -L -o "$APK_FILE" "$APK_URL"
    echo "  Downloaded $(du -h "$APK_FILE" | cut -f1)"
fi

# Extract APK
echo "[2/6] Extracting APK..."
rm -rf "$EXTRACT_DIR"
unzip -q -o "$APK_FILE" -d "$EXTRACT_DIR"
echo "  Extracted to $EXTRACT_DIR"

# Copy native library
echo "[3/6] Copying native library..."
mkdir -p "$QNN_MODULE_DIR/jniLibs/arm64-v8a"
cp "$EXTRACT_DIR/lib/arm64-v8a/libstable_diffusion_core.so" \
   "$QNN_MODULE_DIR/jniLibs/arm64-v8a/"
echo "  Copied libstable_diffusion_core.so ($(du -h "$QNN_MODULE_DIR/jniLibs/arm64-v8a/libstable_diffusion_core.so" | cut -f1))"

# Copy QNN libraries
echo "[4/6] Copying QNN libraries..."
mkdir -p "$QNN_MODULE_DIR/assets/qnnlibs"
cp "$EXTRACT_DIR/assets/qnnlibs/"*.so "$QNN_MODULE_DIR/assets/qnnlibs/"
QNN_COUNT=$(ls -1 "$QNN_MODULE_DIR/assets/qnnlibs/"*.so 2>/dev/null | wc -l)
QNN_SIZE=$(du -sh "$QNN_MODULE_DIR/assets/qnnlibs" | cut -f1)
echo "  Copied $QNN_COUNT QNN libraries ($QNN_SIZE)"

# Copy base models
echo "[5/6] Copying base models..."
mkdir -p "$QNN_MODULE_DIR/assets/cvtbase"
cp "$EXTRACT_DIR/assets/cvtbase/"* "$QNN_MODULE_DIR/assets/cvtbase/"
MODELS_SIZE=$(du -sh "$QNN_MODULE_DIR/assets/cvtbase" | cut -f1)
echo "  Copied base models ($MODELS_SIZE)"

# Cleanup
echo ""
echo "[6/6] Cleaning up..."
rm -rf "$EXTRACT_DIR"
# Keep APK for potential re-extraction
echo "  Kept APK at $APK_FILE for future use"

# Summary
echo ""
echo "============================================"
echo "  Done!"
echo "============================================"
echo ""
echo "Libraries prepared in:"
echo "  $QNN_MODULE_DIR/jniLibs/arm64-v8a/"
echo "  $QNN_MODULE_DIR/assets/qnnlibs/"
echo "  $QNN_MODULE_DIR/assets/cvtbase/"
echo ""
echo "You can now build the project with:"
echo "  ./gradlew :app:assembleFullDebug"
echo ""
echo "NOTE: These files are NOT committed to git."
echo "      Each developer must run this script."
