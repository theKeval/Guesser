"""Synthesizes Guesser's sound effects and background music (pure stdlib)."""
import math
import random
import struct
import sys
import wave
from array import array

OUT = sys.argv[1]
TAU = 2 * math.pi
rng = random.Random(20261011)


def write_wav(name, samples, rate, peak=0.85):
    m = max(1e-9, max(abs(s) for s in samples))
    gain = peak / m
    data = array("h", (int(max(-1, min(1, s * gain)) * 32767) for s in samples))
    with wave.open(f"{OUT}/{name}.wav", "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(rate)
        w.writeframes(data.tobytes())


def fade_edges(buf, rate, fade_in=0.002, fade_out=0.01):
    n_in = int(fade_in * rate)
    n_out = int(fade_out * rate)
    for i in range(min(n_in, len(buf))):
        buf[i] *= i / n_in
    for i in range(min(n_out, len(buf))):
        buf[-1 - i] *= i / n_out
    return buf


class Biquad:
    """RBJ band-pass / low-pass filter."""

    def __init__(self, kind, freq, q, rate):
        w0 = TAU * freq / rate
        alpha = math.sin(w0) / (2 * q)
        cw = math.cos(w0)
        if kind == "bp":
            b0, b1, b2 = alpha, 0, -alpha
        elif kind == "hp":
            b0, b1, b2 = (1 + cw) / 2, -(1 + cw), (1 + cw) / 2
        else:
            b0, b1, b2 = (1 - cw) / 2, 1 - cw, (1 - cw) / 2
        a0, a1, a2 = 1 + alpha, -2 * cw, 1 - alpha
        self.b = (b0 / a0, b1 / a0, b2 / a0)
        self.a = (a1 / a0, a2 / a0)
        self.x1 = self.x2 = self.y1 = self.y2 = 0.0

    def __call__(self, x):
        b0, b1, b2 = self.b
        a1, a2 = self.a
        y = b0 * x + b1 * self.x1 + b2 * self.x2 - a1 * self.y1 - a2 * self.y2
        self.x2, self.x1 = self.x1, x
        self.y2, self.y1 = self.y1, y
        return y


def pluck(buf, rate, start, freq, amp, decay, partials=((1, 1.0), (2.76, 0.25), (5.4, 0.06))):
    """Kalimba / music-box like pluck added into buf."""
    i0 = int(start * rate)
    n = int(decay * 6 * rate)
    for i in range(n):
        j = i0 + i
        if j >= len(buf):
            break
        t = i / rate
        attack = min(1.0, t / 0.003)
        s = 0.0
        for mult, a in partials:
            s += a * math.sin(TAU * freq * mult * t) * math.exp(-t / (decay / (1 + (mult - 1) * 0.9)))
        buf[j] += amp * attack * s


# ---------------------------------------------------------------- UI tap
def ui_tap():
    rate = 32000
    buf = [0.0] * int(0.09 * rate)
    lp = Biquad("bp", 2400, 1.2, rate)
    for i in range(len(buf)):
        t = i / rate
        f = 520 + 380 * math.exp(-t / 0.012)
        body = math.sin(TAU * f * t) * math.exp(-t / 0.022)
        click = lp(rng.uniform(-1, 1)) * math.exp(-t / 0.004)
        buf[i] = 0.8 * body + 0.6 * click
    write_wav("sfx_tap", fade_edges(buf, rate), rate, 0.7)


# ---------------------------------------------------------------- keypad pluck (C6)
def key_pluck():
    rate = 32000
    buf = [0.0] * int(0.32 * rate)
    pluck(buf, rate, 0, 1046.5, 1.0, 0.07)
    lp = Biquad("bp", 3000, 0.9, rate)
    for i in range(int(0.006 * rate)):
        buf[i] += 0.25 * lp(rng.uniform(-1, 1)) * (1 - i / (0.006 * rate))
    write_wav("sfx_key", fade_edges(buf, rate, 0.0005, 0.03), rate, 0.75)


# ---------------------------------------------------------------- delete
def delete_bloop():
    rate = 32000
    buf = [0.0] * int(0.16 * rate)
    phase = 0.0
    for i in range(len(buf)):
        t = i / rate
        f = 640 * math.exp(-t * 5.5)
        phase += TAU * f / rate
        env = min(1, t / 0.004) * math.exp(-t / 0.05)
        buf[i] = env * (math.sin(phase) + 0.2 * math.sin(2 * phase))
    write_wav("sfx_delete", fade_edges(buf, rate), rate, 0.65)


# ---------------------------------------------------------------- tile flip tick
def flip_tick():
    rate = 32000
    buf = [0.0] * int(0.07 * rate)
    bp = Biquad("bp", 1800, 2.5, rate)
    for i in range(len(buf)):
        t = i / rate
        n = bp(rng.uniform(-1, 1)) * math.exp(-t / 0.008)
        tone = math.sin(TAU * 1250 * t) * math.exp(-t / 0.012) * 0.35
        buf[i] = n + tone
    write_wav("sfx_flip", fade_edges(buf, rate), rate, 0.55)


# ---------------------------------------------------------------- clue chime (R/W remarks)
def clue_chime():
    rate = 32000
    buf = [0.0] * int(1.0 * rate)
    marimba = ((1, 1.0), (4.0, 0.18), (10.0, 0.03))
    pluck(buf, rate, 0.00, 659.25, 0.8, 0.16, marimba)  # E5
    pluck(buf, rate, 0.11, 987.77, 0.7, 0.20, marimba)  # B5
    write_wav("sfx_clue", fade_edges(buf, rate, 0.001, 0.08), rate, 0.7)


# ---------------------------------------------------------------- SMYLE jingle
def smyle_jingle():
    rate = 32000
    dur = 1.35
    buf = [0.0] * int(dur * rate)
    # Playful "boing" up-slide.
    phase = 0.0
    for i in range(int(0.22 * rate)):
        t = i / rate
        f = 330 * (1 + 1.4 * (t / 0.22) ** 0.6) * (1 + 0.04 * math.sin(TAU * 18 * t))
        phase += TAU * f / rate
        env = min(1, t / 0.01) * (1 - t / 0.22) ** 0.5
        buf[i] += 0.45 * env * (math.sin(phase) + 0.25 * math.sin(2 * phase))
    # Bright happy arpeggio C6 E6 G6 C7 with a sparkle on top.
    bell = ((1, 1.0), (2.0, 0.35), (3.0, 0.12), (4.2, 0.05))
    for k, f in enumerate((1046.5, 1318.5, 1568.0, 2093.0)):
        pluck(buf, rate, 0.2 + k * 0.085, f, 0.55 - k * 0.05, 0.22, bell)
    for k in range(7):
        pluck(buf, rate, 0.55 + k * 0.05 + rng.uniform(0, 0.02), rng.choice((2637, 3136, 3520, 4186)), 0.12, 0.06, ((1, 1.0),))
    write_wav("sfx_smyle", fade_edges(buf, rate, 0.002, 0.15), rate, 0.75)


# ---------------------------------------------------------------- applause
def applause():
    rate = 32000
    dur = 3.6
    n = int(dur * rate)
    buf = [0.0] * n
    clap_len = int(0.05 * rate)
    # Pre-render a handful of clap timbres (filtered noise bursts).
    claps = []
    for _ in range(24):
        f = rng.uniform(900, 2600)
        bp = Biquad("bp", f, rng.uniform(0.8, 2.0), rate)
        hp = Biquad("hp", 400, 0.7, rate)
        tau = rng.uniform(0.006, 0.014)
        c = []
        for i in range(clap_len):
            t = i / rate
            env = min(1, t / 0.0008) * math.exp(-t / tau)
            c.append(hp(bp(rng.uniform(-1, 1))) * env)
        claps.append(c)
    people = 34
    for _ in range(people):
        period = rng.uniform(0.17, 0.27)
        t = rng.uniform(0, 0.35)
        loud = rng.uniform(0.4, 1.0)
        clap = rng.choice(claps)
        while t < dur:
            # Crowd swells in quickly and tails off.
            env = min(1.0, t / 0.25) * (1.0 if t < 2.2 else max(0.0, 1 - (t - 2.2) / 1.3) ** 1.5)
            if env > 0:
                i0 = int(t * rate)
                a = loud * env * rng.uniform(0.6, 1.0)
                for i, s in enumerate(clap):
                    if i0 + i >= n:
                        break
                    buf[i0 + i] += a * s
            t += period * rng.uniform(0.85, 1.15)
    # Light room tail.
    out = buf[:]
    for delay, g in ((0.031, 0.35), (0.047, 0.25), (0.071, 0.18)):
        d = int(delay * rate)
        for i in range(d, n):
            out[i] += g * buf[i - d]
    write_wav("sfx_applause", fade_edges(out, rate, 0.01, 0.3), rate, 0.8)


# ---------------------------------------------------------------- background music loop
def note(name):
    names = {"C": 0, "D": 2, "E": 4, "F": 5, "G": 7, "A": 9, "B": 11}
    semis = names[name[0]] + int(name[-1]) * 12
    return 440.0 * 2 ** ((semis - 57) / 12)


def music():
    rate = 22050
    bar = 4.0
    chords = [
        ("C3", ["E4", "G4", "B4"], ["C5", "E5", "G5", "B5", "G5", "E5"]),
        ("A2", ["C4", "E4", "G4"], ["A4", "C5", "E5", "G5", "E5", "C5"]),
        ("F2", ["A3", "C4", "E4"], ["F4", "A4", "C5", "E5", "C5", "A4"]),
        ("G2", ["B3", "D4", "G4"], ["G4", "B4", "D5", "A5", "D5", "B4"]),
    ]
    cycles = 2
    total = bar * len(chords) * cycles
    n = int(total * rate)
    buf = [0.0] * n

    def add(i, v):
        buf[i % n] += v  # wrap tails so the loop is seamless

    # Precompute a soft pad wavetable (sine with gentle 2nd/3rd harmonics).
    table_size = 4096
    table = [math.sin(TAU * k / table_size) + 0.18 * math.sin(2 * TAU * k / table_size)
             + 0.05 * math.sin(3 * TAU * k / table_size) for k in range(table_size)]

    def pad_voice(start, length, freq, amp):
        attack, release = 1.4, 1.8
        total_len = length + release
        i0 = int(start * rate)
        for detune in (-0.0018, 0.0, 0.0021):
            step = freq * (1 + detune) * table_size / rate
            ph = rng.uniform(0, table_size)
            for i in range(int(total_len * rate)):
                t = i / rate
                if t < attack:
                    env = math.sin(0.5 * math.pi * t / attack) ** 2
                elif t < length:
                    env = 1.0
                else:
                    env = math.cos(0.5 * math.pi * (t - length) / release) ** 2
                add(i0 + i, amp / 3 * env * table[int(ph) % table_size])
                ph += step

    for c in range(cycles):
        for k, (bass, pad, arp) in enumerate(chords):
            start = (c * len(chords) + k) * bar
            pad_voice(start, bar, note(bass), 0.20)
            for p in pad:
                pad_voice(start, bar, note(p), 0.085)
            # Music-box arpeggio: sparse first cycle, a little fuller in the second.
            pattern = [0, 2, 3, 5] if c == 0 else [0, 1, 2, 3, 4, 5]
            step = bar / 6
            for idx in pattern:
                t0 = start + idx * step + rng.uniform(-0.01, 0.01)
                tmp_len = int(2.4 * rate)
                tmp = [0.0] * tmp_len
                pluck(tmp, rate, 0, note(arp[idx]), rng.uniform(0.07, 0.1), 0.5,
                      ((1, 1.0), (2.0, 0.12), (4.0, 0.05)))
                i0 = int(t0 * rate)
                for i, s in enumerate(tmp):
                    add(i0 + i, s)

    # Soft circular echo for space.
    out = buf[:]
    for delay, g in ((0.375, 0.28), (0.75, 0.14), (1.125, 0.07)):
        d = int(delay * rate)
        for i in range(n):
            out[i] += g * buf[(i - d) % n]
    # Gentle low-pass to keep it warm.
    lp = Biquad("lp", 3800, 0.7, rate)
    for _ in range(2):  # warm the filter state on a full pass so the loop point matches
        warmed = [lp(s) for s in out]
    write_wav("music_background", warmed, rate, 0.6)


for fn in (ui_tap, key_pluck, delete_bloop, flip_tick, clue_chime, smyle_jingle, applause, music):
    fn()
    print("done", fn.__name__, flush=True)
