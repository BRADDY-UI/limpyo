#!/usr/bin/env node
// Turns design/tokens.json into:
//   1. app/.../core/designsystem/theme/LimpyoTokens.kt  (Android, Compose)
//   2. admin-web/src/tokens.css                          (admin web, Tailwind v4 @theme)
// Both outputs are committed. CI runs `node design/scripts/build-tokens.mjs --check`
// and fails if someone edited an output by hand or forgot to re-run this script.
import { readFileSync, writeFileSync, mkdirSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const root = join(dirname(fileURLToPath(import.meta.url)), "..", "..");
const tokens = JSON.parse(readFileSync(join(root, "design", "tokens.json"), "utf8"));
const check = process.argv.includes("--check");

const HEADER = "GENERATED from design/tokens.json by design/scripts/build-tokens.mjs. Do not edit by hand.";

/** "#RRGGBB" or "#AARRGGBB" -> 0xAARRGGBB */
function argb(hex) {
  const h = hex.replace("#", "").toUpperCase();
  if (h.length === 6) return `0xFF${h}`;
  if (h.length === 8) return `0x${h}`;
  throw new Error(`Bad color ${hex}`);
}
/** "#AARRGGBB" -> CSS rgba(), "#RRGGBB" stays. */
function css(hex) {
  const h = hex.replace("#", "");
  if (h.length === 6) return `#${h.toLowerCase()}`;
  const a = parseInt(h.slice(0, 2), 16) / 255;
  const [r, g, b] = [2, 4, 6].map((i) => parseInt(h.slice(i, i + 2), 16));
  return `rgb(${r} ${g} ${b} / ${Math.round(a * 100)}%)`;
}
const kebab = (s) => s.replace(/([A-Z])/g, "-$1").toLowerCase();
const num = (n) => (Number.isInteger(n) ? `${n}` : `${n}`);
const f = (n) => `${n}f`;

// ---------- Kotlin ----------
function kotlin() {
  const { color, type, space, radius, size, motion } = tokens;
  const palette = (name, c) =>
    `    val ${name} = LimpyoColors(\n` +
    Object.entries(c).map(([k, v]) => `        ${k} = Color(${argb(v)}),`).join("\n") +
    `\n        isDark = ${name === "Night"},\n    )`;
  const cats = (name, c) =>
    `    val ${name} = LimpyoCategoryColors(\n` +
    Object.entries(c).map(([k, v]) => `        ${k} = Color(${argb(v)}),`).join("\n") +
    `\n    )`;
  const colorFields = Object.keys(color.night).map((k) => `    val ${k}: Color,`).join("\n");
  const catFields = Object.keys(color.category.night).map((k) => `    val ${k}: Color,`).join("\n");
  const typeSpecs = Object.entries(type)
    .map(
      ([k, t]) =>
        `    val ${k} = TypeSpec(font = TokenFont.${t.font === "display" ? "Display" : "Ui"}, sizeSp = ${t.size}, lineSp = ${t.line}, weight = ${t.weight}, trackingEm = ${f(t.tracking ?? 0)})`,
    )
    .join("\n");
  const dp = (obj) => Object.entries(obj).map(([k, v]) => `    val ${k} = ${v}.dp`).join("\n");
  const ms = Object.entries(motion)
    .filter(([k]) => k.startsWith("duration"))
    .map(([k, v]) => `    const val ${k} = ${v}`)
    .join("\n");
  const eases = Object.entries(motion)
    .filter(([k]) => k.startsWith("ease"))
    .map(([k, v]) => `    val ${k} = CubicBezierEasing(${v.map(f).join(", ")})`)
    .join("\n");
  const springs = Object.entries(motion)
    .filter(([k]) => k.startsWith("spring"))
    .map(([k, v]) => `    val ${k} = SpringToken(dampingRatio = ${f(v.dampingRatio)}, stiffness = ${f(v.stiffness)})`)
    .join("\n");

  return `// ${HEADER}
package com.limpyo.app.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Every color role in one theme (Night Route or Day Route). */
@Immutable
data class LimpyoColors(
${colorFields}
    val isDark: Boolean,
)

/** Waste category colors. Always shown with an icon and a label, never color alone. */
@Immutable
data class LimpyoCategoryColors(
${catFields}
)

enum class TokenFont { Display, Ui }

/** One text style from the type scale (sizes in sp, tracking in em). */
data class TypeSpec(val font: TokenFont, val sizeSp: Int, val lineSp: Int, val weight: Int, val trackingEm: Float)

data class SpringToken(val dampingRatio: Float, val stiffness: Float)

object LimpyoPalettes {
${palette("Night", color.night)}

${palette("Day", color.day)}

${cats("NightCategories", color.category.night)}

${cats("DayCategories", color.category.day)}
}

object LimpyoTypeTokens {
${typeSpecs}
}

/** Spacing scale in dp. */
object LimpyoSpace {
${dp(space)}
}

object LimpyoRadius {
${dp(radius)}
}

object LimpyoSize {
${dp(size)}
}

object LimpyoMotion {
${ms}
${eases}
${springs}
}
`;
}

// ---------- CSS (Tailwind v4) ----------
function cssTheme() {
  const { color, type, space, radius, motion, font } = tokens;
  const lines = [];
  const vars = (c, prefix = "") => Object.entries(c).map(([k, v]) => `  --color-${prefix}${kebab(k)}: ${css(v)};`);
  lines.push(`/* ${HEADER} */`);
  lines.push(`/* Night Route is the default. Day Route applies with <html data-theme="day">. */`);
  lines.push(`@theme {`);
  lines.push(`  --font-display: "${font.display}", ui-sans-serif, system-ui, sans-serif;`);
  lines.push(`  --font-ui: "${font.ui}", ui-sans-serif, system-ui, sans-serif;`);
  lines.push(...vars(color.night));
  lines.push(...vars(color.category.night, "cat-"));
  for (const [k, v] of Object.entries(space)) lines.push(`  --spacing-${kebab(k)}: ${v}px;`);
  for (const [k, v] of Object.entries(radius)) lines.push(`  --radius-${kebab(k)}: ${v}px;`);
  for (const [k, t] of Object.entries(type)) {
    lines.push(`  --text-${kebab(k)}: ${t.size / 16}rem;`);
    lines.push(`  --text-${kebab(k)}--line-height: ${t.line / 16}rem;`);
    lines.push(`  --text-${kebab(k)}--font-weight: ${t.weight};`);
    if (t.tracking) lines.push(`  --text-${kebab(k)}--letter-spacing: ${t.tracking}em;`);
  }
  for (const [k, v] of Object.entries(motion)) {
    if (k.startsWith("duration")) lines.push(`  --${kebab(k)}: ${v}ms;`);
    if (k.startsWith("ease")) lines.push(`  --${kebab(k)}: cubic-bezier(${v.join(", ")});`);
  }
  lines.push(`}`);
  lines.push(``);
  lines.push(`[data-theme="day"] {`);
  lines.push(...vars(color.day));
  lines.push(...vars(color.category.day, "cat-"));
  lines.push(`}`);
  lines.push(``);
  return lines.join("\n");
}

const outputs = [
  [join(root, "app/src/main/java/com/limpyo/app/core/designsystem/theme/LimpyoTokens.kt"), kotlin()],
  [join(root, "admin-web/src/tokens.css"), cssTheme()],
];

let stale = false;
for (const [path, content] of outputs) {
  if (check) {
    let current = "";
    try {
      current = readFileSync(path, "utf8").replace(/\r\n/g, "\n");
    } catch {}
    if (current !== content) {
      console.error(`Out of date: ${path}`);
      stale = true;
    }
  } else {
    mkdirSync(dirname(path), { recursive: true });
    writeFileSync(path, content);
    console.log(`Wrote ${path}`);
  }
}
if (stale) {
  console.error("Run: node design/scripts/build-tokens.mjs");
  process.exit(1);
}
