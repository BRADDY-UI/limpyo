# Limpyo — On-device AI

Two on-device pipelines. Neither sends raw images to any cloud service.
1. **Waste scanner**: classifies one item into a disposal category.
2. **Photo redaction**: blurs faces and text in report photos before upload.

## 1. Waste scanner

### What went wrong in the study (baseline to beat)
- 77.65% validation accuracy, but Non-Biodegradable/Residual recall 0.29, and 44% of that class was predicted as Recyclable.
- Validation came from the same distribution as training (web photos), so real-world accuracy was lower.
- One label per whole photo: small items lost to background.
- E-Waste and Residual had about half the data of other classes.

### Classes (aligned with RA 9003 practice)
`biodegradable`, `recyclable`, `residual`, `special` (hazardous household: batteries, paint, chemicals, medical), `ewaste`, plus a gate class `not_waste`.
Textile folds into `recyclable` or `residual` by condition in the guide; it's not a model class (it caused confusion before).

### Data plan
| Source | Use | Check before use |
|---|---|---|
| TrashNet | recyclables, residual | license in repo |
| TACO (CC BY 4.0) | real-world litter photos with boxes (crop objects into classification samples) | attribution |
| RealWaste and other open sets on Kaggle/Hugging Face | balance classes | license per dataset (only commercial-safe or CC BY) |
| **Local GenSan set (most important)** | 600+ photos Brad takes around GenSan: sachets, styro, banana leaves, pet bottles, used batteries, broken phones, ukay clothes, cartons | own photos, no people |
| In-app feedback (opt-in) | ongoing improvement | redacted, consented, reviewed in W-SCANQ |

Rules: max 3:1 imbalance between classes (oversample + augment the small ones); dedup near-duplicates with perceptual hashes; a `ml/DATA_CARD.md` lists every source, license, count and known gaps.

**Test set** = 300+ local photos never used in training (60 per waste class + not_waste), taken in real conditions (cluttered floors, sacks, low light). That's the number we report, not validation accuracy.

### Model
- Backbone: MobileNetV3-Large or EfficientNet-Lite0, ImageNet-pretrained, fine-tuned (pick the better of the two on the local test set).
- Input 224×224. Augment: random crop, flip, color jitter, blur, JPEG artifacts, background pasting (item cropped from TACO onto floor/sack backgrounds).
- Loss: class-weighted cross-entropy or focal loss; label smoothing 0.1.
- Training: free Google Colab or Kaggle GPU; notebook `ml/train_classifier.ipynb`; fixed seeds; experiment log in `ml/EXPERIMENTS.md`.
- Export: LiteRT (`.tflite`), full-integer int8 quantization with a calibration set; target < 6 MB; check accuracy drop < 1 point vs float.
- Ships inside the APK (`core/ml/src/main/assets/scanner_v{n}.tflite`) with a `labels.json` and `model_card.json` (version, date, metrics).

### Targets (on the local test set)
| Metric | Target |
|---|---|
| Macro F1 (5 waste classes) | ≥ 0.80 |
| Residual recall | ≥ 0.70 (study: 0.29) |
| Special + E-Waste recall | ≥ 0.75 (safety-relevant) |
| "Not sure" rate | ≤ 20% |
| Accuracy when the app does answer | ≥ 88% |
| Inference on Realme 6 Pro | < 150 ms (GPU delegate, CPU fallback) |

### Decision logic in the app
1. CameraFrame crops the center square, so the item fills the frame. This fixes the "small object loses to background" problem without a detector.
2. Run the model → softmax.
3. If `not_waste` ≥ 0.6 → "This doesn't look like waste."
4. Else if top waste prob ≥ 0.55 AND margin over second ≥ 0.15 → answer "Likely <category>".
5. Else → "Not sure": show the top 3 categories as buttons + guide search.
6. Thresholds are tuned on the local test set and stored in `model_card.json`, not hard-coded.
7. Safety bias: if `special` or `ewaste` is in the top 2 with ≥ 0.25, always show the hazardous handling note.

### v2 options (after launch)
- Object detector (YOLO-style, small) trained on TACO boxes + local boxes, for multi-item photos.
- Opt-in cloud second opinion (Gemini free tier) on already-redacted crops, only for "Not sure" results. Off by default because free-tier content may be used by the provider.

## 2. Photo redaction (report photos)
Runs entirely on device in `core/ml/Redactor`:
1. Decode the image, downscale so the long edge is 1600 px.
2. ML Kit Face Detection (fast mode, min face size 0.05). Each face box is expanded by 25% and blurred with a strong box blur (radius scales with box size). If any face confidence is uncertain, it's still blurred.
3. ML Kit Text Recognition v2: each text block box is expanded by 15% and pixelated (downscale ×16, nearest-neighbor up). This covers plates, house numbers and signs.
4. Re-encode as WebP quality 80, long edge 1280 → about 150 KB. Re-encoding drops all EXIF/GPS metadata. A unit test verifies no metadata remains.
5. Show the result in R-REP-NEW-2 ("Faces and text blurred"). The user can tap-drag to add extra blur boxes, but can't remove automatic ones.
6. Latency target < 1.2 s on Realme 6 Pro; a progress indicator shows "Securing image…".
7. If either detector fails to load, the photo can't be submitted (fail closed) and the user is asked to retry.

## 3. Evaluation and honesty
- `ml/README.md` publishes: confusion matrix on the local test set, per-class precision/recall, "Not sure" rate, model size, latency, and known failure cases with example images (no people).
- The app's About screen links to the model card. The scanner always says it can be wrong and points to the guide.
