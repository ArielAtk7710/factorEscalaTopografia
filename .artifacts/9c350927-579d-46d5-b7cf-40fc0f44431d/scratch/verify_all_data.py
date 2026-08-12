import math
import struct

# Constants
A = 6378137.0
F_INV = 298.257223563
F = 1.0 / F_INV
B = A * (1.0 - F)
ESQ = (A**2 - B**2) / (A**2)
E_PRIME_SQ = (A**2 - B**2) / (B**2)
K0 = 0.9996
P0_MMHG = 759.99
P0_HPA = 1013.25
PRESSURE_COEFF = 0.0000225577
PRESSURE_EXP = 5.2559

BIN_FILE = "D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/assets/mgb/mgb08.bin"

LAT_NORTH = -9.0
LAT_SOUTH = -23.0
LON_WEST = -70.0
LON_EAST = -56.0
STEP = 1.0 / 60.0
GRID_SIZE = 841

def dms_to_decimal(d, m, s, hemi):
    dec = d + m/60.0 + s/3600.0
    if hemi in ['S', 'W']:
        dec = -dec
    return dec

def calculate_pressure_mmhg(alt_orto):
    return P0_MMHG * (1.0 - PRESSURE_COEFF * alt_orto)**PRESSURE_EXP

def calculate_pressure_hpa(alt_orto):
    return P0_HPA * (1.0 - PRESSURE_COEFF * alt_orto)**PRESSURE_EXP

# Load MGB Data
with open(BIN_FILE, "rb") as f:
    header = f.read(4)
    num_records = struct.unpack(">i", header)[0]
    data_n = []
    for _ in range(num_records):
        f.seek(16, 1) # Skip lat, lon
        n_val = struct.unpack(">d", f.read(8))[0]
        data_n.append(n_val)

def get_geoid_undulation(lat, lon):
    if lat > LAT_NORTH or lat < LAT_SOUTH or lon < LON_WEST or lon > LON_EAST:
        return 0.0

    dRow = (LAT_NORTH - lat) / STEP
    dCol = (lon - LON_WEST) / STEP

    row = int(math.floor(dRow))
    col = int(math.floor(dCol))

    if row >= GRID_SIZE - 1: row = GRID_SIZE - 2
    if col >= GRID_SIZE - 1: col = GRID_SIZE - 2

    n00 = data_n[row * GRID_SIZE + col]
    n01 = data_n[row * GRID_SIZE + (col + 1)]
    n10 = data_n[(row + 1) * GRID_SIZE + col]
    n11 = data_n[(row + 1) * GRID_SIZE + (col + 1)]

    t = dRow - row
    u = dCol - col

    return (1 - t) * (1 - u) * n00 + (1 - t) * u * n01 + t * (1 - u) * n10 + t * u * n11

cases = [
    # [lat_dms, lon_dms, alt_orto, expected_p_mm, expected_p_hpa, expected_n]
    [[16, 46, 15.5243, 'S'], [68, 32, 50.0670, 'W'], 3245.895, 509.613, 679.437, 48.072],
    [[11, 17, 13.4758, 'S'], [68, 9, 20.2507, 'W'], 3698.214, 480.807, 641.032, 36.869],
    [[16, 10, 2.3783, 'S'], [69, 9, 47.5550, 'W'], 2845.358, 536.273, 714.981, 43.572],
    [[14, 20, 27.0149, 'S'], [68, 59, 11.1593, 'W'], 2478.523, 561.670, 748.842, 41.845],
    [[14, 58, 28.2408, 'S'], [62, 52, 1.6806, 'W'], 2000.842, 596.192, 794.868, 44.606],
    [[11, 48, 4.3494, 'S'], [62, 44, 3.0449, 'W'], 1875.822, 605.505, 807.285, 26.931],
    [[13, 14, 48.3194, 'S'], [62, 42, 30.1448, 'W'], 1648.985, 622.704, 830.214, 46.253],
    [[17, 42, 45.9414, 'S'], [62, 30, 43.1564, 'W'], 1254.178, 653.580, 871.380, 42.003],
    [[16, 2, 58.1169, 'S'], [56, 21, 33.8659, 'W'], 652.845, 702.979, 937.241, 45.849],
    [[17, 4, 54.7787, 'S'], [56, 18, 11.9709, 'W'], 412.965, 723.510, 964.613, 47.870],
    [[10, 59, 22.5419, 'S'], [64, 12, 53.8057, 'W'], 1337.424, 646.969, 862.565, 21.163],
    [[19, 49, 46.2563, 'S'], [59, 19, 28.3058, 'W'], 4885.384, 411.421, 548.523, 12.703],
    [[17, 4, 24.3303, 'S'], [66, 47, 11.5678, 'W'], 658.011, 702.543, 936.659, 13.836],
    [[14, 6, 57.0889, 'S'], [68, 22, 12.7839, 'W'], 2481.560, 561.456, 748.556, 14.356],
    [[15, 55, 58.9153, 'S'], [68, 30, 15.4743, 'W'], 2992.167, 526.374, 701.783, 10.757],
    [[16, 32, 31.5460, 'S'], [65, 29, 49.2319, 'W'], 4565.314, 429.272, 572.324, 14.124],
    [[12, 40, 30.7247, 'S'], [59, 7, 24.8977, 'W'], 2641.673, 550.257, 733.626, 25.506],
    [[18, 6, 2.5896, 'S'], [68, 21, 58.0604, 'W'], 2418.215, 565.937, 754.531, 7.209],
    [[10, 58, 42.8258, 'S'], [66, 1, 26.2032, 'W'], 1308.704, 649.243, 865.598, 22.758],
    [[11, 9, 21.8770, 'S'], [58, 7, 9.7090, 'W'], 3517.105, 492.179, 656.193, 24.563],
    [[21, 9, 54.7718, 'S'], [65, 1, 7.1199, 'W'], 1747.570, 615.181, 820.185, 9.005],
    [[21, 28, 41.4647, 'S'], [64, 45, 10.7563, 'W'], 1753.074, 614.764, 819.628, 4.731],
    [[21, 48, 1.8562, 'S'], [63, 15, 32.7393, 'W'], 4530.003, 431.280, 575.000, 3.588],
    [[13, 28, 28.4043, 'S'], [66, 58, 49.8738, 'W'], 3698.892, 480.765, 640.976, 20.297],
    [[15, 18, 9.0714, 'S'], [65, 39, 8.8251, 'W'], 2753.954, 542.513, 723.300, 20.428],
    [[19, 31, 22.1634, 'S'], [62, 44, 37.8178, 'W'], 4639.877, 425.059, 566.706, 15.295],
    [[13, 21, 52.5553, 'S'], [64, 58, 39.4780, 'W'], 3155.298, 515.547, 687.349, 17.394],
    [[18, 58, 27.2927, 'S'], [64, 31, 33.9405, 'W'], 1269.835, 652.332, 869.716, 9.418],
    [[21, 54, 48.1430, 'S'], [62, 49, 2.4472, 'W'], 1165.222, 660.705, 880.879, 2.222],
    [[18, 40, 12.1297, 'S'], [60, 19, 12.1192, 'W'], 2703.125, 546.008, 727.960, 10.021],
]

print("| Caso | Par\u00e1metro | Esperado | Calculado | Diferencia |")
print("| :--- | :--- | :--- | :--- | :--- |")

for idx, c in enumerate(cases):
    lat = dms_to_decimal(*c[0])
    lon = dms_to_decimal(*c[1])
    alt_orto = c[2]

    p_mm = calculate_pressure_mmhg(alt_orto)
    p_hpa = calculate_pressure_hpa(alt_orto)
    n_val = get_geoid_undulation(lat, lon)

    params = [
        ("Presi\u00f3n (mmHg)", c[3], p_mm),
        ("Presi\u00f3n (hPa)", c[4], p_hpa),
        ("Ondulaci\u00f3n (N)", c[5], n_val)
    ]

    for name, exp, calc in params:
        diff = abs(exp - calc)
        print(f"| {idx+1} | {name} | {exp:.3f} | {calc:.3f} | {diff:.6f} |")
