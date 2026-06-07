# Hướng dẫn đóng gói MineSweeper thành file `.exe`

## Yêu cầu

| Tool         | Version | Link                                          |
|--------------|---------|-----------------------------------------------|
| JDK          | 21      | [Adoptium](https://adoptium.net)              |
| JavaFX SDK   | 21.x    | [Gluon](https://gluonhq.com/products/javafx/) |
| JavaFX jmods | 21.x    | [Gluon](https://gluonhq.com/products/javafx/) |
| Maven        | 3.x     | [Maven](https://maven.apache.org)             |

> ⚠️ JDK và JavaFX nên dùng cùng major version. Ví dụ: JDK 21 thì dùng JavaFX 21.x.

---

## Bước 1: Build JAR

Chạy lệnh:

```powershell
mvn clean package -DskipTests
```

Kiểm tra JAR đã có `Main-Class` trong manifest:

```powershell
jar xf target\MineSweeper-1.0-SNAPSHOT.jar META-INF/MANIFEST.MF
type META-INF\MANIFEST.MF
```

Kết quả cần có:

```text
Main-Class: com.minesweeper.Main
```

---

## Bước 2: Kiểm tra đường dẫn JavaFX jmods

Trước khi chạy `jlink`, cần chắc chắn đã tải đúng **JavaFX jmods**, không phải chỉ JavaFX SDK.

Thư mục JavaFX jmods phải chứa các file dạng:

```text
javafx.base.jmod
javafx.controls.jmod
javafx.fxml.jmod
javafx.graphics.jmod
```

Ví dụ nếu giải nén tại:

```text
D:\Tools\javafx-jmods-21.0.11
```

thì kiểm tra bằng lệnh:

```powershell
Get-ChildItem "D:\Tools\javafx-jmods-21.0.11"
```

Nếu không biết JavaFX jmods nằm ở đâu, có thể tìm nhanh:

```powershell
Get-ChildItem D:\ -Recurse -Filter javafx.base.jmod -ErrorAction SilentlyContinue
```

Sau khi tìm được file `javafx.base.jmod`, lấy thư mục chứa file đó để đưa vào `--module-path`.

---

## Bước 3: Tạo custom runtime với jlink

Xóa runtime cũ nếu có:

```powershell
Remove-Item -Recurse -Force custom-runtime
```

Tạo custom runtime:

```powershell
jlink `
  --module-path "$env:JAVA_HOME\jmods;D:\Tools\javafx-jmods-21.0.11" `
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,java.base,java.desktop,java.logging,java.naming,java.sql `
  --output custom-runtime `
  --strip-debug `
  --no-header-files `
  --no-man-pages
```

> Thay `D:\Tools\javafx-jmods-21.0.11` bằng đường dẫn thật đến thư mục JavaFX jmods trên máy.

Trong project này cần thêm `java.sql` vì ứng dụng có dùng các class như `java.sql.Timestamp` trong phần dữ liệu/bảng
điểm. Nếu thiếu `java.sql`, khi chạy `.exe` có thể báo lỗi:

```text
NoClassDefFoundError: java/sql/Timestamp
```

Kiểm tra runtime đã có JavaFX và `java.sql`:

```powershell
.\custom-runtime\bin\java.exe --list-modules | findstr javafx
.\custom-runtime\bin\java.exe --list-modules | findstr java.sql
```

Kết quả mong đợi có các module như:

```text
javafx.base
javafx.controls
javafx.fxml
javafx.graphics
java.sql
```

> Không cần kiểm tra `javafx_controls.dll` hoặc `javafx_graphics.dll`, vì JavaFX không nhất thiết tạo DLL theo tên từng
> module.

---

## Bước 4: Đóng gói với jpackage

Xóa installer cũ nếu có:

```powershell
Remove-Item -Recurse -Force installer
```

Tạo app-image:

```powershell
jpackage --type app-image --name MineSweeper `
  --input target `
  --main-jar MineSweeper-1.0-SNAPSHOT.jar `
  --main-class com.minesweeper.Main `
  --runtime-image custom-runtime `
  --dest installer `
  --verbose
```

Output sau khi đóng gói:

```text
installer\MineSweeper\
```

Bên trong thư mục này cần có:

```text
MineSweeper.exe
app\
runtime\
```

---

## Bước 5: Chạy thử ứng dụng

Chạy bằng PowerShell để xem lỗi nếu có:

```powershell
cd installer\MineSweeper
.\MineSweeper.exe
```

Nếu double-click không hiện gì, chạy lệnh sau để ghi log:

```powershell
.\MineSweeper.exe 2>&1 | Tee-Object log.txt
```

Sau đó mở `log.txt` để kiểm tra lỗi.

---

## Bước 6: Kiểm tra lỗi thường gặp

### Lỗi 1: Không tìm thấy module JavaFX

Lỗi:

```text
Error: Module javafx.base not found
```

Nguyên nhân: `--module-path` chưa trỏ đúng tới thư mục JavaFX jmods.

Cách xử lý:

* Kiểm tra thư mục jmods có file `javafx.base.jmod` hay không.
* Không dùng placeholder như `path\to\javafx-jmods-21.0.11`.
* Thay bằng đường dẫn thật, ví dụ:

```powershell
--module-path "$env:JAVA_HOME\jmods;D:\Tools\javafx-jmods-21.0.11"
```

### Lỗi 2: Thiếu `java.sql.Timestamp`

Lỗi:

```text
Caused by: java.lang.NoClassDefFoundError: java/sql/Timestamp
```

Nguyên nhân: custom runtime thiếu module `java.sql`.

Cách xử lý: thêm `java.sql` vào `--add-modules` khi chạy `jlink`.

### Lỗi 3: Thiếu file CSS

Nếu app lỗi ở đoạn load CSS:

```java
getClass().

getResource("/css/style.css").

toExternalForm()
```

Kiểm tra CSS có nằm trong JAR chưa:

```powershell
jar tf target\MineSweeper-1.0-SNAPSHOT.jar | findstr style.css
```

Kết quả cần thấy:

```text
css/style.css
```

Nếu không thấy, đặt file CSS vào:

```text
src/main/resources/css/style.css
```

Rồi build lại:

```powershell
mvn clean package -DskipTests
```

---

## Bước 7: Zip để phân phối

Không copy riêng file `.exe` ra ngoài. Cần giữ nguyên cả thư mục `MineSweeper` vì launcher cần `app\` và `runtime\`.

Tạo file zip:

```powershell
Compress-Archive -Path "installer\MineSweeper" -DestinationPath "MineSweeper-v1.0-windows.zip"
```

Người dùng chỉ cần giải nén và chạy:

```text
MineSweeper.exe
```

---

## Lưu ý

* Không di chuyển `MineSweeper.exe` ra khỏi thư mục app-image.
* Người dùng không cần cài Java vì runtime đã được bundle sẵn.
* Nếu Windows hiện SmartScreen warning, chọn **More info** → **Run anyway**.
* Nếu chạy không hiện gì, luôn chạy bằng PowerShell để xem lỗi.
