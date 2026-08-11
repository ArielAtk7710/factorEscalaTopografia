import struct
import os

file_path = r"D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/assets/mgb/mgb08.bin"

def try_format(fmt, size, offset=0):
    try:
        with open(file_path, "rb") as f:
            f.seek(offset)
            data = f.read(size)
            if len(data) < size:
                return "File too small"
            values = struct.unpack(fmt, data)
            return values
    except Exception as e:
        return str(e)

print("File size:", os.path.getsize(file_path))
print("First record:", try_format(">ddd", 24, 4))
print("Second record:", try_format(">ddd", 24, 4 + 24))
print("Last record:", try_format(">ddd", 24, 4 + (707281-1)*24))
