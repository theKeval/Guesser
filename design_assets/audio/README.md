# Guesser Audio

All revamp sounds and the background music are synthesized by
`make_audio.py` (Python standard library only), so they carry no third-party
licensing.

```bash
python3 design_assets/audio/make_audio.py /tmp/guesser_audio
cp /tmp/guesser_audio/sfx_*.wav gameplay/src/main/res/raw/
ffmpeg -i /tmp/guesser_audio/music_background.wav -ac 2 -c:a vorbis -strict -2 -b:a 96k \
  gameplay/src/main/res/raw/music_background.ogg
```

| File | Used for |
| --- | --- |
| `sfx_tap` | Buttons and toggles |
| `sfx_key` | Keypad digits, pitched per digit |
| `sfx_delete` | DELETE |
| `sfx_flip` | Each guess tile flipping |
| `sfx_clue` | R/W remark |
| `sfx_smyle` | SMYLE remark |
| `sfx_applause` | Win |
| `music_background` | 32-second background loop |
