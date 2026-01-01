# Qualcomm QNN Feature Module

This module provides local Stable Diffusion inference using Qualcomm's Neural Processing Unit (NPU) via the QNN SDK.

## Overview

The QNN module enables on-device image generation without internet connection, leveraging the dedicated AI hardware in Qualcomm Snapdragon processors.

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Android App                          │
├─────────────────────────────────────────────────────────┤
│  LocalQnn (Kotlin)                                      │
│    ├── QnnGenerationService (HTTP Client)              │
│    └── QnnBackendService (Foreground Service)          │
├─────────────────────────────────────────────────────────┤
│  libstable_diffusion_core.so (C++ HTTP Server)         │
│    ├── MNN Framework (CPU/GPU fallback)                │
│    └── QNN SDK (NPU acceleration)                      │
├─────────────────────────────────────────────────────────┤
│  Qualcomm HTP (Hexagon Tensor Processor)               │
└─────────────────────────────────────────────────────────┘
```

## Supported Devices

| Chipset | SoC | QNN Version | Status |
|---------|-----|-------------|--------|
| Snapdragon 8 Gen 1 | SM8450 | V68 | ✅ Supported |
| Snapdragon 8+ Gen 1 | SM8475 | V69 | ✅ Supported |
| Snapdragon 8 Gen 2 | SM8550 | V73 | ✅ Supported |
| Snapdragon 8 Gen 3 | SM8650 | V75 | ✅ Supported |
| Snapdragon 8s Gen 3 | SM8635 | V79 | ✅ Supported |
| Snapdragon 8 Elite | SM8750 | V81 | ✅ Supported |

Other Snapdragon devices may work with MNN CPU/GPU fallback.

## Setup

### Option 1: From LocalDream APK (Recommended)

Run the preparation script:

```bash
./scripts/prepare_qnn_libs.sh
```

This will:
1. Download LocalDream APK (~54 MB)
2. Extract native libraries to `jniLibs/arm64-v8a/`
3. Extract QNN libraries to `assets/qnnlibs/`
4. Extract base models to `assets/cvtbase/`

### Option 2: Manual Setup

1. Download [LocalDream APK](https://github.com/xororz/local-dream/releases)
2. Extract the APK:
   ```bash
   unzip LocalDream_armv8a_*.apk -d extracted/
   ```
3. Copy files:
   ```bash
   cp extracted/lib/arm64-v8a/libstable_diffusion_core.so \
      feature/qnn/src/main/jniLibs/arm64-v8a/

   cp extracted/assets/qnnlibs/*.so \
      feature/qnn/src/main/assets/qnnlibs/

   cp extracted/assets/cvtbase/* \
      feature/qnn/src/main/assets/cvtbase/
   ```

## Building

The QNN module is only included in the `full` flavor:

```bash
./gradlew :app:assembleFullDebug
```

## Usage

### Starting the Backend

```kotlin
// Inject LocalQnn
val localQnn: LocalQnn by inject()

// Start the background service
localQnn.startService()
    .subscribeOn(Schedulers.io())
    .subscribe()

// Wait for service to be ready
localQnn.isAvailable()
    .filter { it }
    .firstOrError()
    .subscribe { ready ->
        // Backend is ready
    }
```

### Generating Images

```kotlin
val payload = TextToImagePayload(
    prompt = "a beautiful sunset over mountains",
    negativePrompt = "ugly, blurry",
    width = 512,
    height = 512,
    samplingSteps = 20,
    cfgScale = 7.0f,
    seed = "-1",
    // ... other parameters
)

localQnn.processTextToImage(payload)
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe { bitmap ->
        imageView.setImageBitmap(bitmap)
    }
```

### Observing Progress

```kotlin
localQnn.observeStatus()
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe { status ->
        progressBar.progress = (status.step * 100 / status.maxStep)
    }
```

## File Structure

```
feature/qnn/
├── build.gradle.kts
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/.../feature/qnn/
│   │   ├── LocalQnnImpl.kt          # Main implementation
│   │   ├── api/
│   │   │   ├── QnnLocalApi.kt       # Retrofit API interface
│   │   │   └── model/ApiModels.kt   # Request/Response models
│   │   ├── di/QnnModule.kt          # Koin DI module
│   │   ├── jni/QnnBridge.kt         # JNI wrapper
│   │   ├── model/QnnModelManager.kt # Model management
│   │   └── service/
│   │       ├── QnnBackendService.kt # Foreground service
│   │       └── QnnGenerationService.kt
│   ├── jniLibs/arm64-v8a/
│   │   └── libstable_diffusion_core.so (NOT in git)
│   └── assets/
│       ├── qnnlibs/*.so             (NOT in git)
│       └── cvtbase/*.mnn            (NOT in git)
```

## API Endpoints

The native backend runs an HTTP server on `localhost:8081`:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Health check |
| `/txt2img` | POST | Text-to-image generation |
| `/img2img` | POST | Image-to-image generation |
| `/progress` | GET | Generation progress |
| `/interrupt` | POST | Cancel generation |
| `/models` | GET | List available models |
| `/load_model` | POST | Load a model |

## Licensing

- **Module code**: MIT (same as main project)
- **QNN SDK libraries**: Qualcomm proprietary license
- **libstable_diffusion_core.so**: MIT (from local-dream)
- **MNN Framework**: Apache 2.0

The QNN SDK libraries are **not** included in the source repository. They must be downloaded separately as described above.

## Troubleshooting

### Service doesn't start
- Check that all `.so` files are in place
- Verify device has ARM64 architecture
- Check logcat for native library errors

### Generation is slow
- Ensure device supports QNN HTP
- Check that QNN libraries match your SoC version
- MNN CPU fallback is 10-50x slower than NPU

### Out of memory
- Reduce image resolution
- Close other apps
- Some models require 8+ GB RAM

## References

- [local-dream](https://github.com/xororz/local-dream) - Original implementation
- [Qualcomm QNN SDK](https://www.qualcomm.com/developer/software/neural-processing-sdk)
- [MNN Framework](https://github.com/alibaba/MNN)
