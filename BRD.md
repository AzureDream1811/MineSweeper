# Business Requirements Document (BRD)
## Ứng dụng Game Dò Mìn — Minesweeper Desktop (Java)

**Phiên bản:** 1.0 &nbsp;|&nbsp; **Ngày:** 22/04/2026 &nbsp;|&nbsp; **Trạng thái:** Draft

---

## Thông tin nhóm

**Nhóm 5 — Minesweeper**

| MSSV | Họ và tên |
|:---:|:---|
| 23130068 | Phạm Tấn Đức |
| 23130108 | Trần Lê Công Hiếu |
| 23130120 | Nguyễn Bá Học |
| 23130135 | Nguyễn Hoàng Huy |
| 23130347 | Trần Đức Trí |

---

## Mục lục

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Đối tượng người dùng & User Stories](#2-đối-tượng-người-dùng--user-stories)
3. [Yêu cầu người dùng](#3-yêu-cầu-người-dùng-user-requirements)
4. [Yêu cầu hệ thống](#4-yêu-cầu-hệ-thống-system-requirements)
5. [Môi trường triển khai](#5-môi-trường-triển-khai)
6. [Ma trận truy xuất nguồn gốc](#6-ma-trận-truy-xuất-nguồn-gốc)
7. [Ràng buộc & Giả định](#7-các-ràng-buộc--giả-định)

---

## 1. Tổng quan dự án

### 1.1. Mục tiêu kinh doanh

Phát triển một ứng dụng Desktop game Dò Mìn (Minesweeper) bằng ngôn ngữ Java, đáp ứng nhu cầu giải trí của cả người chơi bình thường lẫn người chơi chuyên nghiệp. Ứng dụng phải đảm bảo trải nghiệm người dùng mượt mà, trực quan và có tính cạnh tranh thông qua hệ thống lưu kỷ lục.

### 1.2. Phạm vi dự án

Dự án tập trung vào việc xây dựng ứng dụng Desktop chạy độc lập (Portable), hỗ trợ đa nền tảng (Windows, macOS, Linux) thông qua nền tảng Java (JVM), bao gồm đầy đủ logic trò chơi, giao diện người dùng và hệ thống quản lý kỷ lục.

---

## 2. Đối tượng người dùng & User Stories

### Người chơi giải trí (Players)

| Mã | User Story |
|:---:|:---|
| **US-01** | Là người chơi giải trí, tôi muốn hệ thống hiển thị số mìn xung quanh ô và cho phép đánh dấu ô nghi ngờ bằng cờ, để xác định vị trí nguy hiểm và suy luận mà không bị nhầm lẫn. |
| **US-02** | Là người không có nhiều thời gian, tôi muốn hệ thống tự động mở các ô trống xung quanh khi click vào ô không có mìn lân cận, để tiết kiệm thời gian và tập trung vào phần cần suy luận. |
| **US-03** | Là người chơi mới, tôi muốn hệ thống hỗ trợ luật chơi và các chức năng cơ bản (tạo game mới, chơi lại, dừng, chơi tiếp) để dễ dàng hiểu và thao tác với game. |
| **US-04** | Là người chơi chuyên nghiệp, tôi muốn hệ thống có bảng xếp hạng (High Score) ghi lại thời gian hoàn thành chính xác đến từng mili giây cho từng mức độ khó, để theo dõi tiến bộ và so sánh kỷ lục với người khác. |

---

## 3. Yêu cầu người dùng (User Requirements)

### 3.1. Người chơi giải trí

| Mã | Mô tả |
|:---:|:---|
| **UR-01** | Người chơi có thể thực hiện thao tác mở ô. |
| **UR-02** | Người chơi có thể thực hiện thao tác đánh dấu cờ. |
| **UR-03** | Người chơi có thể hủy cờ cho ô đã được đánh dấu. |
| **UR-04** | Người chơi không thể mở ô đã cắm cờ (ngăn thua do sơ suất). |
| **UR-05** | Người chơi có thể thấy số mìn xung quanh mỗi ô sau khi mở. |
| **UR-06** | Hệ thống hiển thị số lượng cờ còn lại để người chơi cân đối phương án loại trừ. |
| **UR-07** | Người chơi có thể bắt đầu một game mới. |
| **UR-08** | Người chơi có thể tạm dừng game đang chơi. |
| **UR-09** | Người chơi có thể tiếp tục game đang dừng. |
| **UR-10** | Người chơi có thể chơi lại với cùng độ khó và định dạng của ván hiện tại. |
| **UR-11** | Người chơi được thông báo kết quả khi thắng hoặc thua. |

### 3.2. Người chơi chuyên nghiệp

| Mã | Mô tả |
|:---:|:---|
| **UR-12** | Người chơi thấy đồng hồ đếm thời gian khởi động ngay khi mở ô đầu tiên. |
| **UR-13** | Người chơi có thể xem kỷ lục riêng cho từng cấp độ: Easy, Medium, Hard. |
| **UR-14** | Người chơi có thể dùng phím tắt (ví dụ: `F2`) để reset màn chơi ngay lập tức mà không cần dùng chuột. |

---

## 4. Yêu cầu hệ thống (System Requirements)

### 4.1. Yêu cầu chức năng (Functional Requirements)

#### Function 1 — Quản lý Trò chơi (Game Management)

> Nhóm chức năng điều khiển luồng hoạt động chính của ứng dụng.

| Mã | Mô tả |
|:---:|:---|
| **FR-01** | Hệ thống cung cấp chức năng **New Game** để bắt đầu ván mới. |
| **FR-02** | Hệ thống cung cấp chức năng **Reset** để bắt đầu lại với cùng cấu hình hiện tại. |
| **FR-03** | Hệ thống cung cấp chức năng **Pause** để tạm dừng ván chơi. |
| **FR-04** | Hệ thống cung cấp chức năng **Resume** để tiếp tục ván đã dừng. |
| **FR-05** | Hệ thống cho phép chọn mức độ khó: Dễ (9×9, 10 mìn) / Trung bình (16×16, 40 mìn) / Khó (30×16, 99 mìn). |
| **FR-06** | Hệ thống theo dõi và hiển thị thời gian chơi (Timer) theo giây, bắt đầu từ khi mở ô đầu tiên. |
| **FR-07** | Hệ thống lưu giữ và hiển thị kỷ lục tốt nhất (Best Time) cho từng cấp độ khó. |

#### Function 2 — Quản lý Tương tác Bàn cờ (Board Interaction)

> Nhóm chức năng xử lý các thao tác chuột/cảm ứng của người chơi.

| Mã | Mô tả |
|:---:|:---|
| **FR-08** | Hệ thống xử lý sự kiện **click chuột trái** để mở ô. |
| **FR-09** | Hệ thống xử lý sự kiện **click chuột phải** để đặt/hủy cờ (Flag). |
| **FR-10** | Hệ thống ngăn không cho click chuột trái vào ô đã đặt cờ. |
| **FR-11** | Hệ thống hỗ trợ **Chording** (double-click vào ô số đã đủ cờ xung quanh) để tự động mở nhanh các ô còn lại. |
| **FR-12** | Hệ thống đảm bảo ô đầu tiên người dùng click **không bao giờ là mìn** (thuật toán tạo mìn sau click đầu tiên). |

#### Function 3 — Logic Xử lý Ô (Cell Logic)

> Nhóm chức năng tính toán giá trị hiển thị trên từng ô.

| Mã | Mô tả |
|:---:|:---|
| **FR-13** | Khi mở ô không có mìn, hiển thị số lượng mìn xung quanh ô đó (từ 1 đến 8). |
| **FR-14** | Khi mở ô trống (0 mìn xung quanh), hệ thống **đệ quy** tự động mở tất cả ô trống liền kề đến khi gặp ô có số. |
| **FR-15** | Hệ thống hiển thị tổng số mìn của màn chơi. |
| **FR-16** | Hệ thống cập nhật và hiển thị số lượng cờ còn lại theo thời gian thực. |

#### Function 4 — Kiểm soát Trạng thái Kết thúc (Game Status Control)

> Nhóm chức năng xác định điều kiện thắng/thua.

| Mã | Mô tả |
|:---:|:---|
| **FR-17** | **Điều kiện Thua:** Khi click vào ô có mìn, hệ thống dừng trò chơi, hiển thị toàn bộ vị trí mìn và đánh dấu ô mìn đã nổ. |
| **FR-18** | **Điều kiện Thắng:** Khi mở được tất cả ô không có mìn, hệ thống dừng đồng hồ và thông báo chiến thắng. |
| **FR-19** | Khi trò chơi kết thúc (thắng hoặc thua), hệ thống **vô hiệu hóa** mọi tương tác trên bàn cờ cho đến khi bắt đầu ván mới. |

---

### 4.2. Yêu cầu phi chức năng (Non-Functional Requirements)

#### NFR-1: Hiệu năng (Performance)

| Mã | Mô tả | Ngưỡng chấp nhận |
|:---:|:---|:---:|
| **NFR-01** | Tốc độ phản hồi khi click vào ô (mở ô hoặc nổ mìn) | `< 100ms` |
| **NFR-02** | Xử lý đệ quy mở ô trống diện rộng không gây lag hoặc treo máy | Không lag |
| **NFR-03** | Thời gian khởi động ứng dụng | `< 5 giây` |

#### NFR-2: Độ tin cậy & Tính chính xác (Reliability & Accuracy)

| Mã | Mô tả | Ngưỡng chấp nhận |
|:---:|:---|:---:|
| **NFR-04** | Vị trí mìn được phân bổ hoàn toàn ngẫu nhiên, không lặp quy luật giữa các ván | Ngẫu nhiên thực sự |
| **NFR-05** | Đồng hồ tính giờ chính xác theo thời gian thực | Sai số `≤ 0.1s` |

#### NFR-3: Khả năng sử dụng (Usability)

| Mã | Mô tả |
|:---:|:---|
| **NFR-06** | Các con số (1–8) phải có màu sắc khác biệt rõ rệt: số 1 xanh dương, số 2 xanh lá, số 3 đỏ... |
| **NFR-07** | Hệ thống hỗ trợ phím tắt cơ bản (ví dụ: `F2` để reset nhanh). |
| **NFR-08** | Giao diện có thể chuyển đổi linh hoạt giữa tiếng Việt và tiếng Anh (nếu dự án mở rộng). |

#### NFR-4: Tính tương thích & Khả năng di động (Compatibility & Portability)

| Mã | Mô tả |
|:---:|:---|
| **NFR-09** | Giao diện hiển thị tốt (không bị vỡ hình) trên các độ phân giải từ HD đến 4K. |
| **NFR-10** | Ứng dụng có thể chạy Portable mà không cần cài đặt thư viện phức tạp bên ngoài. |

#### NFR-5: Bảo mật (Security)

| Mã | Mô tả |
|:---:|:---|
| **NFR-11** | File lưu kỷ lục (Best Time) phải được mã hóa nhẹ hoặc lưu an toàn để ngăn người dùng sửa đổi thủ công. |

---

## 5. Môi trường triển khai

### 5.1. Môi trường phát triển & Thực thi

| Thành phần | Yêu cầu |
|:---|:---|
| Ngôn ngữ lập trình | Java (đề xuất: **Java 11** hoặc **Java 17 LTS**) |
| Runtime | JRE hoặc JDK phải được cài đặt trên máy người dùng |

### 5.2. Hệ điều hành hỗ trợ

> Nhờ tính chất _"Write Once, Run Anywhere"_ của Java, ứng dụng hỗ trợ đa nền tảng.

| Hệ điều hành | Phiên bản hỗ trợ |
|:---|:---|
| **Windows** | Windows 10, Windows 11 |
| **macOS** | macOS 10.15 (Catalina) trở lên |
| **Linux** | Ubuntu, Fedora, Debian và các bản phân phối phổ biến |

### 5.3. Yêu cầu phần cứng

| Thành phần | Tối thiểu | Đề xuất |
|:---|:---|:---|
| **CPU** | Intel Core i3 / AMD Ryzen 3 | Intel Core i5 / AMD Ryzen 5 trở lên |
| **RAM** | 2 GB | 4 GB trở lên |
| **Ổ cứng** | 200 MB trống | 500 MB trống |
| **Màn hình** | 1024×768 | 1920×1080 trở lên |
| **Thiết bị ngoại vi** | Chuột có đầy đủ phím trái & phải | — |

---

## 6. Ma trận truy xuất nguồn gốc

> Bảng liên kết: **User Story → User Requirement → Functional Requirement**

| User Story | User Requirements | Functional Requirements |
|:---:|:---|:---|
| **US-01** | UR-02, UR-03, UR-04, UR-05 | FR-09, FR-10, FR-13 |
| **US-02** | UR-01 | FR-08, FR-14 |
| **US-03** | UR-06, UR-07, UR-08, UR-09, UR-10, UR-11 | FR-01, FR-02, FR-03, FR-04, FR-05, FR-15, FR-16, FR-17, FR-18, FR-19 |
| **US-04** | UR-12, UR-13, UR-14 | FR-06, FR-07, FR-11, FR-12 |

---

## 7. Các ràng buộc & Giả định

### Ràng buộc

- Ứng dụng phải được phát triển hoàn toàn bằng ngôn ngữ **Java**.
- Không sử dụng các thư viện bên ngoài phức tạp đòi hỏi cài đặt riêng biệt.
- File kỷ lục phải chống giả mạo theo yêu cầu **NFR-11**.

### Giả định

- Người dùng đã cài đặt **JRE/JDK** tương thích trên máy tính cá nhân.
- Thiết bị đầu vào chính là **chuột có đầy đủ nút trái và nút phải**.
- Ứng dụng hoạt động **offline**, không cần kết nối mạng.

---

*Tài liệu này được lập dựa trên phân tích yêu cầu người dùng và yêu cầu hệ thống của dự án Minesweeper Desktop — Java.*
