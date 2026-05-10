# Deployment Guide

## 1. Deploy file `.exe` lên GitHub Releases

### Bước 1: Đóng gói file exe

Làm theo hướng dẫn ở file `PACKAGE.md` để build file exe và đóng gói thành file zip:


### Bước 2: Tạo Release trên GitHub

1. Vào repository trên GitHub
2. Click **Releases** (cột bên phải) → **Create a new release**
3. Click **Choose a tag** → gõ `v1.0` → **Create new tag**
4. Điền **Release title**: `MineSweeper v1.0`
5. Kéo xuống phần **Attach binaries** → upload file `MineSweeper-v1.0-windows.zip`
6. Click **Publish release**

### Kết quả

Link download trực tiếp có dạng:

```
https://github.com/<username>/<repo>/releases/download/v1.0/MineSweeper-v1.0-windows.zip
```

---

## 2. Deploy frontend lên Vercel

### Yêu cầu

- Tài khoản [Vercel](https://vercel.com) (đăng nhập bằng GitHub)
- Thư mục frontend chứa `index.html` đã được push lên GitHub

### Bước 1: Push frontend lên GitHub

```powershell
git add index.html
git commit -m "Add frontend download page"
git push
```

### Bước 2: Deploy lên Vercel

1. Vào [vercel.com](https://vercel.com) → **Add New Project**
2. Import repository chứa `index.html`
3. Vercel tự detect static site, giữ nguyên settings mặc định
4. Click **Deploy**

Sau vài giây có link dạng `https://minesweeper-xxx.vercel.app`

### Bước 3: Gắn `domain` vào Vercel

1. Trong Vercel project → **Settings** → **Domains**
2. Nhập `domain` → **Add**
3. Vercel hiển thị các DNS record cần thêm, vào trang quản lý DNS của domain và thêm:

| Type  | Name | Value                  |
|-------|------|------------------------|
| A     | @    | `76.76.21.21`          |
| CNAME | www  | `cname.vercel-dns.com` |

## 4. Chờ 5–15 phút để DNS propagate
## 5. Vercel tự cấp **HTTPS** miễn phí

### Kiểm tra

```powershell
nslookup domain
# Phải trả về 76.76.21.21
```

Truy cập `https://domain.vn` là xong.

---

## Cập nhật sau này

### Cập nhật frontend

```powershell
git add .
git commit -m "Update frontend"
git push
```

Vercel tự động redeploy khi có commit mới.

### Release version mới

1. Build lại exe và zip
2. Lên GitHub → Releases → **Create a new release** với tag `v1.1`
3. Cập nhật link download trong `index.html` nếu đổi tên file