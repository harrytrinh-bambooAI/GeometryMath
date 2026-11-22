# Môi trường dùng chung cho script kiểm chứng (cụm riêng của dự án: cổng 54329, KHÔNG phải 5432)
export PGBIN=/opt/homebrew/opt/postgresql@17/bin
export PGHOST=127.0.0.1 PGPORT=54329 PGUSER=geo
# CSDL thử: lượt sửa sau phản biện RV dùng geo_arch2 (geo_arch giữ nguyên làm bằng chứng của lượt trước)
export PGDATABASE=${PGDATABASE:-geo_arch2}
export PATH=$PGBIN:$PATH
