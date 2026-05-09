# Hướng dẫn đóng gói MineSweeper thành file `.exe`

## Yêu cầu

| Tool         | Version | Link                                          |
| ------------ | ------- | --------------------------------------------- |
| JDK          | 21      | [Adoptium](https://adoptium.net)              |
| JavaFX SDK   | 21.0.11 | [Gluon](https://gluonhq.com/products/javafx/) |
| JavaFX jmods | 21.0.11 | [Gluon](https://gluonhq.com/products/javafx/) |
| Maven        | 3.x     | [Maven](https://maven.apache.org)             |

> ⚠️ JavaFX SDK và jmods phải **cùng version với JDK**. JDK 21 → JavaFX 21.

---

## Bước 1: Build JAR

```powershell
mvn clean package -DskipTests
```

Kiểm tra JAR đã có `Main-Class` trong manifest:

```powershell
jar xf target\MineSweeper-1.0-SNAPSHOT.jar META-INF/MANIFEST.MF
cat META-INF\MANIFEST.MF
# Phải thấy: Main-Class: com.minesweeper.Main
```

---

## Bước 2: Tạo custom runtime với jlink

```powershell
jlink `
  --module-path "$env:JAVA_HOME\jmods;path\to\javafx-jmods-21.0.11" `
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,java.base,java.desktop,java.logging,java.naming `
  --output custom-runtime `
  --strip-debug `
  --no-header-files `
  --no-man-pages
```

Thay `path\to\javafx-jmods-21.0.11` bằng đường dẫn thực tế đến folder jmods.

Kiểm tra JavaFX đã được bundle:

```powershell
ls custom-runtime\bin | findstr javafx
# Phải thấy javafx_graphics.dll, javafx_controls.dll, ...
```

---

## Bước 3: Đóng gói với jpackage

```powershell
jpackage --type app-image --name MineSweeper `
  --input target `
  --main-jar MineSweeper-1.0-SNAPSHOT.jar `
  --main-class com.minesweeper.Main `
  --runtime-image custom-runtime `
  --dest installer
```

Output: thư mục `installer\MineSweeper\` chứa `MineSweeper.exe`.

---

## Bước 4: Kiểm tra

```powershell
.\installer\MineSweeper\MineSweeper.exe
```

---

## Bước 5: Zip để phân phối

```powershell
Compress-Archive -Path "installer\MineSweeper" -DestinationPath "MineSweeper-v1.0-windows.zip"
```

Upload file zip lên GitHub Releases để người dùng tải về.

---

## Lưu ý

- Không di chuyển `MineSweeper.exe` ra khỏi folder — launcher cần `app\` và `runtime\` nằm cùng cấp.
- Người dùng **không cần cài Java** vì runtime đã được bundle sẵn.
- Nếu Windows hiện SmartScreen warning → click **More info** → **Run anyway**.
