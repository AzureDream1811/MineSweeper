# MineSweeper — Hướng dẫn Deploy (File tải về)

Tài liệu này mô tả cách build, đóng gói và tự động publish artifact để người dùng tải về.

## **Yêu cầu**

- JDK 21 (có `jpackage` để đóng gói native)
- Maven
- (Chạy JAR) JavaFX SDK 21 nếu không dùng runtime đã đóng gói

### **1. Build jar**

```powershell
mvn clean package
```

### **2. Chạy trong môi trường dev**

```powershell
mvn javafx:run
```

### **3. Chạy JAR (nếu cần module-path cho JavaFX)**

```powershell
java --module-path "C:\path\to\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml -jar target\MineSweeper-1.0-SNAPSHOT.jar
```

### **4. Tạo native installer với `jpackage` (Windows ví dụ)**

- Ghi chú: `jpackage` đi kèm JDK 21. Cần cung cấp đường dẫn tới JavaFX libs nếu không đóng gói runtime.

```powershell
jpackage --input target --main-jar MineSweeper-1.0-SNAPSHOT.jar --main-class com.minesweeper.Main --type exe --name MineSweeper --module-path "C:\path\to\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml --dest out --icon path\to\icon.ico
```

### **5. Tự động publish bằng GitHub Actions (đã có workflow)**

- Workflow nằm ở `.github/workflows/release.yml`.
- Trigger: push tag `v*` (ví dụ `v1.0`) hoặc chạy thủ công (Actions → Run workflow).
- Workflow sẽ: build JAR, chạy `jpackage` trên `windows-latest`, và tạo GitHub Release upload JAR + installer.

Ví dụ tạo tag và push để kích hoạt:

```bash
git tag v1.0
git push origin v1.0
```

Hoặc chạy workflow thủ công từ trang Actions và cung cấp `tag` nếu muốn.

### **6. Nơi người dùng tải về**

- GitHub → Releases của repository (artifact JAR và installer sẽ có trong release).

### **7. Troubleshooting ngắn**

- Lỗi khi chạy JAR: kiểm tra `--module-path` và `--add-modules` chứa `javafx.controls,javafx.fxml`.
- Lỗi `jpackage` trên CI: đảm bảo JavaFX dependencies được copy vào `--module-path` hoặc dùng `jlink` để tạo runtime image trước.

Nếu bạn muốn tôi: thêm multi-OS packaging (macOS/Linux), hoặc tạo trang download `docs/` cho GitHub Pages, mình sẽ làm tiếp.
