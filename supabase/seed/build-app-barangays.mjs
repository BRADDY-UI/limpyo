#!/usr/bin/env node
// Writes app/src/main/assets/barangays.json from supabase/seed/barangays.geojson,
// so the app can list barangays and find "my barangay" offline (docs/ARCHITECTURE.md: bundled + versioned).
// Ids match migration 0003 (alphabetical, 1..26). CI runs this with --check.
import { readFileSync, writeFileSync, mkdirSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const root = join(dirname(fileURLToPath(import.meta.url)), "..", "..");
const fc = JSON.parse(readFileSync(join(root, "supabase/seed/barangays.geojson"), "utf8"));
const features = [...fc.features].sort((a, b) => a.properties.name.localeCompare(b.properties.name));
const r5 = (n) => Math.round(n * 1e5) / 1e5; // ~1 m precision is plenty

const out = {
  version: 1,
  attribution: "Map data (c) OpenStreetMap contributors, ODbL 1.0. Boundaries are approximate.",
  barangays: features.map((f, i) => ({
    id: i + 1,
    name: f.properties.name,
    center: f.properties.center.map(r5),
    approximate: Boolean(f.properties.approximate),
    // MultiPolygon: polygons -> rings -> [lon, lat]
    polygons: f.geometry.coordinates.map((poly) => poly.map((ring) => ring.map(([x, y]) => [r5(x), r5(y)]))),
  })),
};
const path = join(root, "app/src/main/assets/barangays.json");
const text = JSON.stringify(out) + "\n";
if (process.argv.includes("--check")) {
  let cur = "";
  try {
    cur = readFileSync(path, "utf8").replace(/\r\n/g, "\n");
  } catch {}
  if (cur !== text) {
    console.error(`Out of date: ${path}\nRun: node supabase/seed/build-app-barangays.mjs`);
    process.exit(1);
  }
} else {
  mkdirSync(dirname(path), { recursive: true });
  writeFileSync(path, text);
  console.log(`Wrote ${path} (${text.length} bytes, ${out.barangays.length} barangays)`);
}
