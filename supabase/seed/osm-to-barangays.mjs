// Reads Overpass JSON (city outline ways + barangay center nodes) and prints SQL
// that loads them into temp tables, then builds approximate barangay areas:
// nearest-center (Voronoi) cells clipped to the city outline.
import { readFileSync } from "node:fs";
const [cityPath, centersPath] = process.argv.slice(2);
const city = JSON.parse(readFileSync(cityPath, "utf8"));
const centers = JSON.parse(readFileSync(centersPath, "utf8")).elements;

const num = (x) => {
  if (typeof x !== "number" || !Number.isFinite(x)) throw new Error("bad coordinate");
  return x.toFixed(7);
};
const lit = (s) => "'" + String(s).replace(/'/g, "''") + "'";

const ways = city.elements.filter((e) => e.type === "way");
const lines = ways.map(
  (w) => `ST_GeomFromText('LINESTRING(${w.geometry.map((p) => `${num(p.lon)} ${num(p.lat)}`).join(",")})',4326)`,
);

let sql = "create temp table w(g geometry);\n";
sql += lines.map((l) => `insert into w values (${l});`).join("\n") + "\n";
sql += "create temp table c(name text, osm_id bigint, p geometry);\n";
for (const n of centers) {
  sql += `insert into c values (${lit(n.tags.name)}, ${Number(n.id)}, ST_SetSRID(ST_MakePoint(${num(n.lon)}, ${num(n.lat)}),4326));\n`;
}
sql += `
create temp table city as
  select ST_MakeValid(ST_Union(geom)) g from (select (ST_Dump(ST_Polygonize(g))).geom from w) x;
select 'city_area_km2=' || round((ST_Area(g::geography)/1e6)::numeric, 1) from city;
select 'centers_outside_city=' || count(*) from c, city where not ST_Contains(city.g, c.p);
create temp table cells as
  select (ST_Dump(ST_VoronoiPolygons(ST_Collect(p), 0, (select ST_Envelope(ST_Buffer(g, 0.05)) from city)))).geom v from c;
\\pset format unaligned
\\pset tuples_only on
\\o /tmp/barangays.geojson
select json_build_object(
  'type','FeatureCollection',
  'license','ODbL 1.0, (c) OpenStreetMap contributors',
  'note','Approximate: OSM has no barangay boundaries for General Santos. Each area is the part of the city (OSM relation 14144757) nearest to the barangay center node. Officers can correct boundaries later.',
  'features', json_agg(json_build_object(
    'type','Feature',
    'properties', json_build_object('name', c.name, 'center_osm_node', c.osm_id, 'center', json_build_array(round(ST_X(c.p)::numeric,6), round(ST_Y(c.p)::numeric,6)), 'approximate', true),
    'geometry', ST_AsGeoJSON(ST_Multi(ST_SimplifyPreserveTopology(ST_Intersection(cells.v, city.g), 0.0003)), 6)::json
  ) order by c.name)
) from c join cells on ST_Contains(cells.v, c.p), city;
\\o
`;
process.stdout.write(sql);
