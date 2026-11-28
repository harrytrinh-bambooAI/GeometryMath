"""Ảnh mẫu nộp bài S5-E4 (T-261128-59). Chạy: python3 make-fixtures.py (cần Pillow; HEIC cần heif-enc của libheif).

- bn01-p1.jpg, bn01-p2.jpg: trang viết rõ (nét đen trên nền trắng), không EXIF (băm trên máy chủ = băm tệp tải lên).
- bn03-p2-mo.jpg: trang 2 của Đức Huy bị nhoè (điểm độ rõ < 0,5, F-S5-055).
- bn01-p1.heic: trang 1 dạng HEIC (US-S5E4-03-SC2); bản nhỏ để giữ repo gọn (đặc tả ghi 1,4 MB).
Kích thước thật của ảnh mẫu (2,1 MB / 1,8 MB / 1,4 MB) không ảnh hưởng hành vi; ca 8 MB dựng trong test.
"""
import os
import shutil
import subprocess

from PIL import Image, ImageDraw, ImageFilter

HERE = os.path.dirname(os.path.abspath(__file__))


def page(lines, seed):
    im = Image.new('RGB', (1200, 1600), 'white')
    d = ImageDraw.Draw(im)
    y = 80
    for i, t in enumerate(lines):
        d.text((80, y), t, fill='black')
        # nét viết tay giả: đường gấp khúc dày 3px
        pts = [(80 + k * 40, y + 30 + ((k * 7 + seed + i) % 5) * 4) for k in range(26)]
        d.line(pts, fill='black', width=3)
        y += 110
    d.polygon([(700, 900), (1050, 1250), (650, 1350)], outline='black', width=4)
    d.line([(700, 900), (650, 1350)], fill='black', width=4)
    return im


def save_jpeg(im, name):
    im.save(os.path.join(HERE, name), 'JPEG', quality=85)  # Pillow không ghi EXIF khi không truyền exif=


p1 = page(['Ke AH vuong goc SB tai H', 'BC vuong goc (SAB) nen AH vuong goc (SBC)', '1/AH^2 = 1/SA^2 + 1/AB^2'], 1)
p2 = page(['SB = a can 3', 'AH = a can 6 / 3', 'Vay d(A,(SBC)) = a can 6 / 3'], 3)
save_jpeg(p1, 'bn01-p1.jpg')
save_jpeg(p2, 'bn01-p2.jpg')
save_jpeg(p2.filter(ImageFilter.GaussianBlur(14)), 'bn03-p2-mo.jpg')

png = os.path.join(HERE, '_tmp.png')
p1.resize((600, 800)).save(png)
if shutil.which('heif-enc'):
    subprocess.run(['heif-enc', '-q', '60', '-o', os.path.join(HERE, 'bn01-p1.heic'), png], check=True)
os.remove(png)
