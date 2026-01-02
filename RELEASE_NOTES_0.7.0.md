# PDAI v0.7.0 — Release Notes

## New Features (compared to [SDAI](https://github.com/ShiftHackZ/Stable-Diffusion-Android) v0.6.8)

### Fal.AI — Cloud Generation with FLUX Models

Full integration with the Fal.AI platform for cloud-based image generation.

**Default models:**
- FLUX Schnell, FLUX Schnell Redux
- FLUX Dev, FLUX Dev Redux, FLUX Dev Image-to-Image
- FLUX 2, FLUX 2 Flash, FLUX 2 Edit, FLUX 2 Flash Edit
- FLUX Lora, FLUX Lora Image-to-Image, FLUX Lora Inpainting
- FLUX Kontext Dev
- (and other models available via custom endpoints)

**Modes:**
- Text-to-Image, Image-to-Image, Inpainting

**Highlights:**
- Dynamic parameter form generation based on OpenAPI specifications

---

### Qualcomm QNN — Local Generation on NPU

Image generation on Snapdragon devices using NPU and GPU.

**Supported chipsets:** Snapdragon 8 Gen 1 and newer

**Runtimes:**
- HTP (NPU) — optimized for neural networks
- GPU (OpenCL/Adreno) — general-purpose graphics processor
- CPU — central processor (fallback option)

---

### ADetailer and Hires.Fix for A1111/Forge

- **ADetailer** — automatic face and hand enhancement
- **Hires.Fix** — high-resolution generation with upscaling
- **Forge Modules** — basic support for Forge modules (API endpoint)

---

## UI Improvements

### ZoomableImage
- Improved scaling and gestures
- Smoother transition animations

### InPaint
- Enhanced scaling and canvas panning controls
- Pinch-to-zoom support

### Gallery
- Model name displayed in image details
- Optimized swipe navigation between images

### Log Export
- Log file export function for diagnostics

### Light Theme
- Support for light status bar in app theme

---

## Optimization

### Image File Storage
- Migration from Base64 to file-based storage
- Significantly improved performance and reduced DB size
- Added `mediaPath`, `inputMediaPath` fields in DB (migration v8)

### Network Requests
- Requests are sent only to the active generation source

---

## Technical Changes

### Rebranding SDAI → PDAI
- Log tag: `[PDAI]` instead of `[SDAI]`
- Log file: `pdaiv1.log` instead of `sdaiv1.log`
- Model path: `/Download/PDAI/` instead of `/Download/SDAI/`
- `SdaiWorkerFactory` → `PdaiWorkerFactory`
- Documentation and website updates

### Dependency Updates
- Compose BOM → 2025.12.01
- Lifecycle → 2.10.0
- Navigation → 2.9.6
- Work → 2.11.0
- Koin → 4.1.1
- Retrofit → 3.0.0
- ONNX Runtime → 1.23.2
- Core KTX → 1.17.0
- Material → 1.13.0
- RxJava → 3.1.12
- Apache String Utils → 3.20.0
- MediaPipe → 0.10.26.1
- Serialization → 1.9.0
- Turbine → 1.2.1
- Appcompat → 1.7.1
- Compose Activity → 1.12.2
- Crypto → 1.1.0
- EXIF → 1.4.2
- Gson → 2.13.2
- compileSdk: 35 → 36

### Database Migration
- **Schema version: 7 → 9** — two sequential migrations
- **New fields:** `modelName` (v9), `mediaPath`, `inputMediaPath` (v8)
- **Automatic migration** of existing data on update

---

## Fixes

- Requests to inactive servers are no longer executed
- Light status bar works correctly
- Localization files updated (RU, TR, UK, ZH)
- Added module tests for Fal.AI and Forge, updated existing tests

