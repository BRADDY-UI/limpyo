# Limpyo: Translations

English is the source (`app/src/main/res/values/strings.xml`). Filipino (`values-b+fil`) and Bisaya/Cebuano (`values-b+ceb`) must have every translatable string; Android lint fails the build when one is missing.

**Rule (DESIGN.md §10):** no machine-made or AI-drafted string ships in a release until Brad has reviewed it.

| Batch | Strings | Filipino | Bisaya | Notes |
|---|---|---|---|---|
| Phase 1B (Nov 14) | tabs, empty states, categories, statuses, theme, settings, about | draft (Claude), **needs review** | draft (Claude), **needs review** | Check category names (*Nabubulok / Madunot*, *Residwal*) against what GenSan's ENRO and barangays actually say. |

How to review: open the two `strings.xml` files next to the English one, fix anything that sounds unnatural, then mark the batch "reviewed" here. Short and plain beats literal.
