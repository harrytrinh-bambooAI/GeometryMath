# T-261128-57: sinh lại tệp mẫu exif-gps.jpg của TC-S5E4-40, 45 (ảnh trang vở 64×48 có EXIF: Make Apple, Model iPhone 15,
# GPS 21.0285 N, 105.8542 E — "nhà em"). Chạy: python3 make-exif-gps.py (cần Pillow). Tệp đã sinh được commit sẵn.
import os
from PIL import Image, ImageDraw

img = Image.new('RGB', (64, 48), 'white')
d = ImageDraw.Draw(img)
for y in range(8, 48, 8):
    d.line([(2, y), (62, y)], fill=(180, 200, 230))
d.text((4, 10), 'AH=a', fill='black')

exif = Image.Exif()
exif[0x010F] = 'Apple'          # Make
exif[0x0110] = 'iPhone 15'      # Model
exif[0x0132] = '2026:12:11 19:42:00'  # DateTime
gps = {
    1: 'N', 2: (21.0, 1.0, 42.6),   # 21°01'42.6" = 21.0285
    3: 'E', 4: (105.0, 51.0, 15.12),  # 105°51'15.12" = 105.8542
}
exif[0x8825] = gps
out = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'exif-gps.jpg')
img.save(out, 'JPEG', quality=80, exif=exif.tobytes())
print(out, os.path.getsize(out))
