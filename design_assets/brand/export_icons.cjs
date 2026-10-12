// Run with Node and the sharp package available; this is asset tooling, not an app dependency.
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const sharp = require('sharp');

const output = __dirname;
const res = path.resolve(output, '../../numbcrunch/src/main/res');
const chocolate = '#3E2A1E';
const outline = '#2A1A12';
const gold = '#FFD36B';
const orange = '#F29A1C';
const bronze = '#D8740C';
const cream = '#FFF3DC';

// A concise manual vectorization of icon_generated_source.png, with large open numerals.
const tiles = [
  {
    rotation: -9, pivotX: 33, pivotY: 56, scaleX: 1, translateX: -2,
    silhouette: 'M26 39 H40 Q47 39 47 46 V68 Q47 76 40 76 H26 Q19 76 19 68 V46 Q19 39 26 39 Z',
    paths: [
      [bronze, 'M26 40 H40 Q46 40 46 47 V67 Q46 74 40 74 H26 Q20 74 20 67 V47 Q20 40 26 40 Z'],
      [orange, 'M26 40 H40 Q46 40 46 47 V64 Q46 71 40 71 H26 Q20 71 20 64 V47 Q20 40 26 40 Z'],
      [gold, 'M26 40 H40 Q46 40 46 47 V62 Q46 68 40 68 H26 Q20 68 20 62 V47 Q20 40 26 40 Z'],
      [cream, 'M26 41.5 H40 Q43.5 41.5 44.5 44 H26 Q22 44 22 49 V62 Q22 65 24 66 C21 65 21 63 21 60 V47 Q21 41.5 26 41.5 Z'],
    ],
    digit: 'M25 48 C28 43.5 39 44 40 49.5 C40.5 52.4 38.7 54 36.8 54.7 C40 55.1 42 58.3 40.5 62 C38.5 67 28 68 24.5 62.2 Q23.5 60.3 25.5 59.2 L27.3 58.4 Q28.4 57.8 29.3 59.2 C31.3 62.7 35.8 61.8 35.8 59.2 C35.8 57.2 33.4 57.2 31.7 57.4 Q29 57.8 29 55.4 V54.5 Q29 52.4 31.5 52.4 C34.2 52.4 35.1 51.4 34.6 50.1 C34.1 48.8 31.4 48.4 29.8 50.6 Q28.9 51.8 27.4 51.1 L25.6 50.4 Q24 49.8 25 48 Z',
  },
  {
    rotation: 9, pivotX: 76, pivotY: 56, scaleX: 1, translateX: 2,
    silhouette: 'M69 39 H83 Q90 39 90 46 V68 Q90 76 83 76 H69 Q62 76 62 68 V46 Q62 39 69 39 Z',
    paths: [
      [bronze, 'M69 40 H83 Q89 40 89 47 V67 Q89 74 83 74 H69 Q63 74 63 67 V47 Q63 40 69 40 Z'],
      [orange, 'M69 40 H83 Q89 40 89 47 V64 Q89 71 83 71 H69 Q63 71 63 64 V47 Q63 40 69 40 Z'],
      [gold, 'M69 40 H83 Q89 40 89 47 V62 Q89 68 83 68 H69 Q63 68 63 62 V47 Q63 40 69 40 Z'],
      [cream, 'M69 41.5 H83 Q86.5 41.5 87.5 44 H69 Q65 44 65 49 V62 Q65 65 67 66 C64 65 64 63 64 60 V47 Q64 41.5 69 41.5 Z'],
    ],
    digit: 'M68.5 48.5 C72 43 82 43.5 84 49 C86.5 56 78 58.4 74.3 61 H82.5 Q84.5 61 84.5 63 V64 Q84.5 66 82.5 66 H69 Q67 66 67 64 V62.5 C67 59 71 57 76.6 53.3 C79.3 51.5 79.2 49.5 77.2 49 C75.6 48.6 74.5 49.4 73.5 51 Q72.5 52.7 70.8 51.8 L69.5 51.2 Q67.6 50.5 68.5 48.5 Z',
  },
  {
    rotation: 0, pivotX: 54, pivotY: 54, scaleX: 0.88, translateX: 6.48,
    silhouette: 'M45 30 H61 Q61 35.5 66 35.5 Q66 41.5 72 41.5 V67 Q72 76 63 76 H45 Q36 76 36 67 V40 Q36 30 45 30 Z',
    paths: [
      [bronze, 'M45 31.5 H60 Q60.7 36.6 65 36.8 Q65.5 42 70.5 42.8 V66 Q70.5 74 63 74 H45 Q37.5 74 37.5 66 V40 Q37.5 31.5 45 31.5 Z'],
      [orange, 'M45 31.5 H60 Q60.7 36.6 65 36.8 Q65.5 42 70.5 42.8 V63.5 Q70.5 71 63 71 H45 Q37.5 71 37.5 63.5 V40 Q37.5 31.5 45 31.5 Z'],
      [gold, 'M45 31.5 H60 Q60.7 36.6 65 36.8 Q65.5 42 70.5 42.8 V61.5 Q70.5 68.5 63 68.5 H45 Q37.5 68.5 37.5 61.5 V40 Q37.5 31.5 45 31.5 Z'],
      [cream, 'M45 33 H58.8 Q59.4 37.6 63.7 38.2 Q64.4 43.2 69.2 44 V45.2 Q62.8 44.5 62.3 39.4 Q58.4 38.7 57.7 34.5 H45 Q40 34.5 40 41 V61 Q40 65 42 66 C38.8 64.4 38.8 61.7 38.8 58 V40 Q38.8 33 45 33 Z'],
    ],
    digit: 'M44.5 41 H61.5 Q64 41 64 43.5 V46 Q64 47 63.5 48 L53.8 63.5 Q53 65 51.4 65 H47.5 Q44.7 65 46.1 62.5 L55.8 46.8 H44.5 Q42 46.8 42 44.5 V43.5 Q42 41 44.5 41 Z',
  },
];

function svgPaths(monochrome = false) {
  return tiles.map(tile => {
    const body = monochrome
      ? `<path fill="${cream}" fill-rule="evenodd" d="${tile.silhouette} ${tile.digit}"/>`
      : [[outline, tile.silhouette], ...tile.paths, [chocolate, tile.digit]]
          .map(([fill, d]) => `<path fill="${fill}" d="${d}"/>`).join('');
    return `<g transform="translate(${tile.translateX} 0) scale(${tile.scaleX} 1)"><g transform="rotate(${tile.rotation} ${tile.pivotX} ${tile.pivotY})">${body}</g></g>`;
  }).join('');
}

function svg({size = 1024, monochrome = false, transparent = false, viewBox = '0 0 108 108'} = {}) {
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="${viewBox}">${transparent ? '' : `<path fill="${chocolate}" d="M0 0 H108 V108 H0 Z"/>`}<g transform="translate(9.72 9.72) scale(.82)">${svgPaths(monochrome)}</g></svg>`;
}

function vector(monochrome = false) {
  const groups = tiles.map(tile => {
    const paths = monochrome
      ? `<path android:fillColor="#FFFFFFFF" android:fillType="evenOdd" android:pathData="${tile.silhouette} ${tile.digit}" />`
      : [[outline, tile.silhouette], ...tile.paths, [chocolate, tile.digit]]
          .map(([fill, d]) => `<path android:fillColor="${fill}" android:pathData="${d}" />`).join('\n        ');
    return `<group android:translateX="${tile.translateX}" android:scaleX="${tile.scaleX}"><group android:rotation="${tile.rotation}" android:pivotX="${tile.pivotX}" android:pivotY="${tile.pivotY}">\n        ${paths}\n    </group></group>`;
  }).join('\n    ');
  return `<?xml version="1.0" encoding="utf-8"?>\n<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">\n    <group android:scaleX="0.82" android:scaleY="0.82" android:translateX="9.72" android:translateY="9.72">\n    ${groups}\n    </group>\n</vector>\n`;
}

function write(file, content) {
  fs.mkdirSync(path.dirname(file), {recursive: true});
  fs.writeFileSync(file, content);
}

function mask(shape, size) {
  const half = size / 2;
  const points = Array.from({length: 128}, (_, i) => {
    const angle = i * Math.PI / 64;
    const x = Math.cos(angle), y = Math.sin(angle);
    return `${half + half * Math.sign(x) * Math.sqrt(Math.abs(x))},${half + half * Math.sign(y) * Math.sqrt(Math.abs(y))}`;
  }).join(' ');
  return Buffer.from(`<svg width="${size}" height="${size}">${shape === 'circle' ? `<circle cx="${half}" cy="${half}" r="${half}" fill="white"/>` : `<polygon points="${points}" fill="white"/>`}</svg>`);
}

async function main() {
  write(path.join(output, 'icon_master.svg'), svg());
  write(path.join(output, 'icon_foreground.svg'), svg({transparent: true}));
  write(path.join(output, 'icon_monochrome.svg'), svg({monochrome: true, transparent: true}));
  write(path.join(res, 'drawable/ic_launcher_foreground.xml'), vector());
  write(path.join(res, 'drawable/ic_launcher_monochrome.xml'), vector(true));
  write(path.join(res, 'drawable/ic_launcher_background.xml'), `<?xml version="1.0" encoding="utf-8"?>\n<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">\n    <path android:fillColor="${chocolate}" android:pathData="M0,0 H108 V108 H0 Z" />\n</vector>\n`);
  const adaptive = `<?xml version="1.0" encoding="utf-8"?>\n<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n    <background android:drawable="@drawable/ic_launcher_background" />\n    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />\n</adaptive-icon>\n`;
  for (const name of ['ic_launcher', 'ic_launcher_round']) {
    write(path.join(res, `mipmap-anydpi-v26/${name}.xml`), adaptive);
  }
  await sharp(Buffer.from(svg())).ensureAlpha().png().toFile(path.join(output, 'icon_master.png'));
  await sharp(Buffer.from(svg({size: 512, viewBox: '18 18 72 72'}))).ensureAlpha().png().toFile(path.join(output, 'play_store_icon.png'));
  for (const [density, size] of Object.entries({mdpi: 48, hdpi: 72, xhdpi: 96, xxhdpi: 144, xxxhdpi: 192})) {
    const folder = path.join(res, `mipmap-${density}`);
    fs.mkdirSync(folder, {recursive: true});
    const square = await sharp(Buffer.from(svg({size: size * 4, viewBox: '18 18 72 72'}))).resize(size, size).ensureAlpha().png().toBuffer();
    await sharp(square).composite([{input: mask('squircle', size), blend: 'dest-in'}]).png().toFile(path.join(folder, 'ic_launcher.png'));
    await sharp(square).composite([{input: mask('circle', size), blend: 'dest-in'}]).png().toFile(path.join(folder, 'ic_launcher_round.png'));
  }

  // Verify the rendered foreground respects both adaptive-icon safe-zone constraints.
  const {data, info} = await sharp(Buffer.from(svg({size: 1080, transparent: true}))).ensureAlpha().raw().toBuffer({resolveWithObject: true});
  let opaquePixels = 0;
  for (let y = 0; y < info.height; y++) {
    for (let x = 0; x < info.width; x++) {
      if (data[(y * info.width + x) * 4 + 3] < 128) continue;
      opaquePixels++;
      const px = (x + 0.5) / 10, py = (y + 0.5) / 10;
      assert(px >= 21 && px <= 87 && py >= 21 && py <= 87, 'Foreground left the 66dp safe square');
      assert(Math.hypot(px - 54, py - 54) <= 36, 'Foreground left the 72dp safe circle');
    }
  }
  assert(opaquePixels > 100000, 'Empty or unexpectedly small foreground');
  const store = await sharp(path.join(output, 'play_store_icon.png')).metadata();
  assert.equal(store.width, 512);
  assert.equal(store.height, 512);
  assert.equal(store.channels, 4);
  assert.equal((await sharp(path.join(output, 'play_store_icon.png')).stats()).isOpaque, true);
  for (const [density, size] of Object.entries({mdpi: 48, hdpi: 72, xhdpi: 96, xxhdpi: 144, xxxhdpi: 192})) {
    for (const name of ['ic_launcher', 'ic_launcher_round']) {
      const meta = await sharp(path.join(res, `mipmap-${density}/${name}.png`)).metadata();
      assert.equal(meta.width, size);
      assert.equal(meta.height, size);
      assert.equal(meta.channels, 4);
      const pixels = await sharp(path.join(res, `mipmap-${density}/${name}.png`)).raw().toBuffer();
      assert.equal(pixels[3], 0, 'Legacy icon needs transparent corners');
    }
  }

  const previewSize = 220;
  const color = await sharp(Buffer.from(svg({size: previewSize, viewBox: '18 18 72 72'}))).ensureAlpha().png().toBuffer();
  const mono = await sharp(Buffer.from(svg({size: previewSize, viewBox: '18 18 72 72', monochrome: true}).replaceAll(cream, chocolate).replace(`<path fill="${chocolate}" d="M0 0 H108 V108 H0 Z"/>`, '<path fill="#E8D7BA" d="M0 0 H108 V108 H0 Z"/>'))).ensureAlpha().png().toBuffer();
  const shapes = await Promise.all([
    sharp(color).composite([{input: mask('circle', previewSize), blend: 'dest-in'}]).png().toBuffer(),
    sharp(color).composite([{input: mask('squircle', previewSize), blend: 'dest-in'}]).png().toBuffer(),
    sharp(mono).composite([{input: mask('circle', previewSize), blend: 'dest-in'}]).png().toBuffer(),
  ]);
  const wood = await sharp(path.resolve(output, '../../gameplay/src/main/res/drawable-nodpi/home_background.png')).extract({left: 0, top: 420, width: 896, height: 400}).resize(1024, 340).png().toBuffer();
  const wordmark = await sharp(path.join(output, 'wordmark.png')).resize({width: 850}).png().toBuffer();
  const labels = Buffer.from(`<svg width="1024" height="820"><g fill="${cream}" font-family="sans-serif" font-size="22" text-anchor="middle"><text x="512" y="46" font-size="26" font-weight="bold">NumbCrunch · launcher and title artwork</text><text x="198" y="336">Circle</text><text x="512" y="336">Squircle</text><text x="826" y="336">Themed monochrome</text><text x="512" y="432" font-size="18">48 px legacy fallbacks</text><text x="512" y="791" font-size="18">Wordmark on the existing game wood</text></g></svg>`);
  await sharp({create: {width: 1024, height: 820, channels: 4, background: '#241910'}}).composite([
    {input: shapes[0], left: 88, top: 82}, {input: shapes[1], left: 402, top: 82}, {input: shapes[2], left: 716, top: 82},
    {input: path.join(res, 'mipmap-mdpi/ic_launcher.png'), left: 446, top: 353},
    {input: path.join(res, 'mipmap-mdpi/ic_launcher_round.png'), left: 530, top: 353},
    {input: wood, left: 0, top: 480}, {input: wordmark, left: 87, top: 549}, {input: labels, left: 0, top: 0},
  ]).png().toFile(path.join(output, 'icon-preview.png'));
  console.log('Icon exports verified: adaptive safe zones, opaque 512px RGBA store icon, ten legacy PNG sizes.');
}

main().catch(error => {console.error(error); process.exitCode = 1;});
