import struct

BIN_FILE = "D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/assets/mgb/mgb08.bin"

with open(BIN_FILE, "rb") as f:
    header = f.read(4)
    num_records = struct.unpack(">i", header)[0]
    print(f"Num records: {num_records}")

    # Read first 5 records
    for i in range(5):
        lat = struct.unpack(">d", f.read(8))[0]
        lon = struct.unpack(">d", f.read(8))[0]
        n = struct.unpack(">d", f.read(8))[0]
        print(f"Record {i}: Lat={lat}, Lon={lon}, N={n}")

    # Read last 5 records
    f.seek(4 + (num_records - 5) * 24)
    for i in range(5):
        lat = struct.unpack(">d", f.read(8))[0]
        lon = struct.unpack(">d", f.read(8))[0]
        n = struct.unpack(">d", f.read(8))[0]
        print(f"Record {num_records-5+i}: Lat={lat}, Lon={lon}, N={n}")
