# NumbCrunch icon

Three concepts were considered:

1. **Bitten digit tiles:** three warm keypad tiles connect the three-digit puzzle to the playful “crunch” name.
2. **NC tile monogram:** a compact gold-framed initial mark would emphasize the name, but explain less about the game.
3. **Combination dial:** a cracked numeric dial would suggest code-breaking, but need more detail at launcher size.

The bitten tiles are the chosen direction: the digits remain readable at 48 px, the two-lobed bite gives the mark a distinct silhouette, and the gold/orange/chocolate palette matches the game. The fixed digits are an illustration, not an in-game secret or clue. Crumb digits were omitted to keep small sizes clear.

`icon_generated_source.png` is the original built-in imagegen output. `export_icons.cjs` contains its manual vector refinement: 18 foreground paths and three monochrome silhouette paths with digit cutouts. The foreground fits both the central 66 × 66 dp square and the 72 dp diameter circle in a 108 dp viewport. The background fills the canvas. The adaptive XML includes the Android 13 monochrome layer.

Exports:

- `icon_master.svg` / `icon_master.png`: 1024 × 1024 adaptive-canvas master.
- `icon_foreground.svg` / `icon_monochrome.svg`: editable, transparent vector layers.
- `play_store_icon.png`: opaque 512 × 512, 32-bit RGBA, square corners; foreground framed at the visible 72 dp viewport.
- `numbcrunch/src/main/res/drawable/ic_launcher_{foreground,background,monochrome}.xml`: Android vectors.
- `numbcrunch/src/main/res/mipmap-anydpi-v26/ic_launcher{,_round}.xml`: adaptive references.
- `numbcrunch/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher{,_round}.png`: 48, 72, 96, 144 and 192 px legacy fallbacks; squircle and circle masks with transparent corners.
- `icon-preview.png`: circle, squircle, themed monochrome, true 48 px samples, and the title on the existing wood art.

Regenerate with Node and the `sharp` package available to the export process:

```sh
node design_assets/brand/export_icons.cjs
```

This is design tooling only; it adds no Android dependency. The script checks the rendered safe-area bounds, store opacity/RGBA dimensions, and every legacy PNG's dimensions and transparent corners. It also reads the adjacent `wordmark.png` and existing game wood art when composing the preview.

## Built-in imagegen prompt

Use case: logo-brand. Create one finished app-icon source illustration for NumbCrunch, a cozy 3-digit logic puzzle game. Square 1024x1024 opaque chocolate-brown background (#3E2A1E), very subtle warm wood tonal depth only. Center three chunky rounded orange/gold digit tiles in one tight horizontal row with a slight friendly fan/overlap, center tile foremost and a little higher. Each tile bears a single large very bold dark chocolate digit: 3, 7, 2 from left to right. The center tile has a clearly playful curved two-lobed bite missing from its upper-right corner. Simple orange bevels, cream-gold highlights, chocolate outline and shallow dimensional lower edge. Colors #FFD36B, #F29A1C, #D8740C, #3E2A1E, sparing #FFF3DC. Friendly game-art style, clean vector-like shapes, strong coherent silhouette readable at 48px, no minute decoration, no tiny crumb digits, no text except the three large digits. Composition: all tile artwork and shadows fits inside central 62 percent of canvas width and 50 percent height; ample plain background all around for adaptive launcher masks. No border around the entire icon, no rounded overall app icon corners, no watermark, no letters, no safe or dial. This image will be manually vectorized into concise Android paths.
