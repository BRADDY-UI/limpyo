# Limpyo — Design System: "Night Route"

Chosen style: **B · Night Route**. It's dark-first and map-first: the truck and its route are the hero. A light variant, **Day Route**, keeps the app readable in direct sunlight.
Theme default: Dark. Options: Dark / Light / System (Settings).

Single source of truth: `design/tokens.json` generates both the Compose theme (`core-designsystem`) and the Tailwind config (`admin-web`), so app and web never drift.

## 1. Principles
1. **The map is the home.** Everything else slides over it in a sheet.
2. **One bright thing per screen.** Lime marks the single most important element (the live truck, the primary action). Never use it for decoration.
3. **Quiet surfaces.** Depth comes from 1 dp borders and surface steps, not shadows, gradients or blur.
4. **Words over numbers** where numbers mislead ("Likely", "10–15 min").
5. **Every element earns its place.** No illustrations for empty space, no stock icons as filler.

## 2. Color

### Night Route (dark)
| Token | Hex | Use |
|---|---|---|
| ground | #0B1210 | app background, nav bar |
| surface | #141E1A | sheets, cards |
| raised | #1E2B26 | pressed rows, inputs, chips |
| border | #2C3C36 | 1 dp outlines |
| divider | #26352F | separators, progress track |
| ink | #EAF5EF | primary text, icons |
| muted | #9DB3A8 | secondary text (7.6:1 on surface) |
| accent | #B6F23A | live truck, route, primary button fill |
| onAccent | #0B1210 | text/icons on accent |
| accentContainer | #25331B | success chip background |
| warning / container | #FFC15E / #3A2E12 | delayed |
| danger / container | #FF9A73 / #3B2018 | problem, destructive |
| info / container | #7CC4FF / #15283A | info, links in text |
| scrim | #000000 at 60% | behind dialogs |

### Day Route (light)
| Token | Hex | Note |
|---|---|---|
| ground | #F4F7F5 | |
| surface | #FFFFFF | |
| raised | #EAF0EC | |
| border | #D3DDD8 | |
| divider | #E2E9E5 | |
| ink | #0B1210 | |
| muted | #4E6159 | 6.4:1 on white |
| accent | #B6F23A | fills only (buttons, truck dot) with ink text; never lime text on light |
| accentStrong | #3D6B00 | lime-family text/icons/route line on light (6.2:1) |
| warning / danger / info | #8A5300 / #B3261E / #0B5CAD | text-safe on white |
| accentContainer | #E6F4D0 | added Phase 0 (success chip bg) |
| warning / danger / info container | #FBEBD2 / #FBE3E0 / #DDEBF9 | added Phase 0; text color on its container ≥ 4.5:1 |

Night Route also gets `accentStrong = accent` so components can use one token for "lime-family text" in both themes. Values live in `design/tokens.json`; the contrast test (`TokenContrastTest`) checks every text/icon pair.

### Waste categories (always shown with icon + label, never color alone)
| Category | Dark | Light | Icon |
|---|---|---|---|
| Biodegradable | #4DD6C1 | #00695C | leaf |
| Recyclable | #7CC4FF | #0B5CAD | recycle arrows |
| Residual | #C9D1CD | #4E5A55 | bin |
| Special/Hazardous | #FF9A73 | #B3261E | warning triangle |
| E-Waste | #C8A6FF | #6A3FB5 | battery-plug |
| Not waste / Unknown | muted | muted | question circle |

Contrast rule: body text ≥ 4.5:1, large text and icons ≥ 3:1. Checked in CI with a token contrast test.

## 3. Typography
Fonts (OFL, bundled in the APK, no network): **Space Grotesk** (display/titles), **Inter** (UI/body). Fallback is the system sans.
| Style | Font | Size/Line (sp) | Weight |
|---|---|---|---|
| display | Space Grotesk | 34/40 | 700 |
| headline | Space Grotesk | 28/34 | 700 |
| title | Space Grotesk | 22/28 | 700 |
| titleSmall | Inter | 17/24 | 600 |
| body | Inter | 16/24 | 400 |
| bodyStrong | Inter | 16/24 | 600 |
| label | Inter | 14/20 | 500 |
| caption | Inter | 13/18 | 500 |
| overline | Inter | 12/16 | 600, +0.08em, caps |
| numeric | Inter, tabular figures | as context | 600 |
Rules: sentence case everywhere (no ALL CAPS except overline), max 2 weights per screen, layouts tested at 200% font scale.

## 4. Spacing, size, shape
- Spacing scale (dp): 4, 8, 12, 16, 20, 24, 32, 40, 48, 64. Screen margin 20. Card padding 16–20. Section gap 24.
- Touch targets: 48 dp minimum; driver-mode primary actions 56 dp.
- Radius: 8 (small chips/inputs inner), 12 (buttons, inputs), 20 (cards), 28 (sheet top), full (pills, avatars, FAB).
- Borders: 1 dp `border`. Focus ring: 2 dp `accent` (dark) / `accentStrong` (light) with 2 dp offset.
- Grid (admin web): 12 columns, 24 px gutter, max width 1440, sidebar 248.

## 5. Elevation and layering
No drop shadows in the app. Layers, bottom to top: map → floating pills (surface + border) → sheet (surface, 28 radius top, border top) → dialogs (raised + scrim) → snackbar.

## 6. Iconography
Material Symbols Rounded (Apache 2.0) as vector drawables, outline style, 24 dp, weight 400 (≈1.75 stroke look). The active nav icon is filled. Custom icons (truck, route, categories) are drawn on the same 24 dp grid. No emoji anywhere in the UI.

## 7. Motion
Feel: quick, physical, calm. Things move like they have weight; nothing bounces for fun.

### Tokens
| Token | Value |
|---|---|
| durationFast | 100 ms (press, toggle) |
| durationShort | 160 ms (fades, chip changes) |
| durationMedium | 240 ms (enter, sheet content) |
| durationLong | 360 ms (screen transitions, shared element) |
| easeStandard | cubic-bezier(0.2, 0, 0, 1) |
| easeEnter | cubic-bezier(0.16, 1, 0.3, 1) (expo out) |
| easeExit | cubic-bezier(0.3, 0, 1, 1) |
| springDefault | dampingRatio 0.9, stiffness 400 |
| springSheet | dampingRatio 0.85, stiffness 300 |
| springSnappy | dampingRatio 0.7, stiffness 700 (status chip "stamp") |

### Interactions
| Moment | Motion |
|---|---|
| Screen enter | content fades in + rises 24 dp, staggered 30 ms per block, max 4 blocks |
| Tab switch | crossfade 160 ms, no slide |
| Push to detail | shared-element card → detail (360 ms), others fade |
| Back | Android predictive back (14+), scale to 0.92 with the gesture |
| Bottom sheet | user drags with springSheet; snaps peek (148 dp) / half (50%) / full |
| Press | scale 0.98 + content alpha 0.85, 100 ms; light haptic on primary actions |
| Status change | chip cross-fades and scales 1.15→1 with springSnappy |
| Truck marker | glides linearly from previous to new ping over the ping interval (one ping behind = never jumps); heading rotates by the shortest arc; lime pulse ring every 2 s |
| Route progress | completed segment dims to ink 30% with 240 ms fade |
| Loading | skeleton blocks in `raised`, opacity pulse 0.6↔1 over 1.2 s (no shimmer sweep) |
| Pull to refresh | Material pull indicator in accent |
| Success (report sent) | check draws in 300 ms, sheet collapses, toast |
- Respect system "Remove animations" (animator scale 0): replace motion with instant state changes plus a 100 ms fade.
- Performance: target 90 fps on the Realme 6 Pro's 90 Hz panel and never below 60. No RenderEffect blur. No animated gradients. Lists use stable keys. Map annotations use MapLibre symbol layers, not Compose overlays.

### Haptics
Light tick on primary button press and snap of the sheet; success haptic on report sent and stop done. Off when system haptics are off.

## 8. Components (core-designsystem)
| Component | Spec |
|---|---|
| TopPill | 48 dp, surface + border, full radius, icon + label; barangay switcher and status |
| IconPill | 48×48, surface + border, full radius; notifications, locate-me |
| BottomNav | 72 dp + system inset, ground bg, top divider; 5 items, active = accent icon/label (dark) or accentStrong (light) |
| MapSheet | surface, 28 top radius, 40×4 handle, 3 snap points, scroll inside when full |
| Card | surface, 20 radius, 16–20 padding, 1 dp border |
| PrimaryButton | 52 dp, 12 radius, accent fill, onAccent text 16/600; disabled = raised + muted |
| SecondaryButton | 52 dp, 12 radius, 1 dp border, ink text |
| TextButton | 48 dp min, accent (dark) / accentStrong (light) text |
| DangerButton | danger container + danger text; confirm dialog for irreversible actions |
| StatusChip | 32 dp, full radius, container color + strong color text, optional leading dot |
| CategoryChip | 32 dp, category color dot + label; selectable variant with check |
| TextField | 56 dp, raised bg, 12 radius, label above, helper/error below, counter for limited fields |
| SegmentedControl | raised track, surface thumb sliding with springDefault |
| DayStrip | 7 cells, 44 dp circles; today = accent fill; collection day = 2 dp category ring |
| MonthCalendar | 6×7 grid, dots for waste types, exceptions marked with a small slash |
| Timeline | vertical, 12 dp nodes, line in divider, current node accent |
| SegmentProgress | N segments 4 dp tall, done = accent, remaining = divider |
| ListRow | 56–72 dp, leading icon 24, title + subtitle, trailing chevron/value |
| Snackbar | raised, 12 radius, ink text, optional action, above nav |
| Banner (offline) | warning container, slides from top under pills, "Offline · showing saved schedule" |
| EmptyState | one icon (48), one line title, one line body, one action. No illustrations |
| Dialog | raised, 20 radius, max 2 actions |
| CameraFrame | dimmed outside (scrim 50%), 4 corner marks in ink, instruction text below |
| RankRow | rank number (tabular), name, score, trend arrow |
| Medal | 56 dp circle, border, icon; locked = muted 40% |

## 9. Map style ("night-route.json", MapLibre, OpenFreeMap vector tiles)
| Layer | Dark | Light |
|---|---|---|
| land | #0F1A16 | #F1F4F2 |
| water | #0E2430 | #CFE3EE |
| parks | #11211A | #E2EFE5 |
| buildings | #121D19 | #E6EBE8 |
| minor roads | #18241F | #FFFFFF |
| major roads | #1E2B26 | #FFFFFF, casing #D3DDD8 |
| highways | #26352F | #FDFDFD, casing #C4D0CA |
| labels | #9DB3A8, halo ground | #4E6159, halo white |
| own barangay boundary | accent at 50%, 1.5 dp dashed | accentStrong 50% |
| other boundaries | border, 1 dp | border |
| route remaining | accent 5 dp, casing ground 2 dp | accentStrong 5 dp, white casing |
| route done | ink 30%, 4 dp | ink 25% |
| stop | 10 dp ring (ink), done = filled muted | same |
| truck | 16 dp accent dot, ground ring 3 dp, heading wedge, pulse | accent dot, ink ring |
| report pins (staff only) | danger dot | danger dot |
POI icons are hidden except schools, markets and barangay halls. OSM attribution is always visible (bottom-left, caption).

## 10. Voice and copy
- Plain, short, friendly, second person. "Truck 2 is about 12 min away", not "ETA: 12 minutes".
- Never blame: "We couldn't send your report. We'll try again when you're online."
- Translations: English (source), Filipino, Bisaya (Cebuano). Brad reviews Bisaya and Filipino. No machine-translated strings ship unreviewed.
- Numbers: 12-hour time with AM/PM, Philippine format dates ("Fri, Nov 14").

## 11. Accessibility
TalkBack labels on all icon-only controls; the map has a list alternative (trucks and stops as a list); 200% font scale doesn't clip; color is never the only signal; focus order follows the visual order; the driver mode is usable one-handed.

## 12. Brand
- Wordmark: "limpyo", lowercase Space Grotesk 700, ink. The dot of the "i" is the lime truck dot.
- App icon: ground tile, a lime route line forming an "L" ending in a dot. Adaptive icon + monochrome (Android 13 themed icons).
- Splash: Android 12 SplashScreen API, ground background, icon only, no delay beyond app start.
