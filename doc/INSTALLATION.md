# Hướng dẫn cài đặt môi trường phát triển

Tài liệu này hướng dẫn cách cài đặt môi trường để phát triển và chạy ứng dụng MineSweeper.

## 1. Yêu cầu phần mềm

Bạn cần cài đặt các phần mềm sau trên máy tính của mình:

*   **Java Development Kit (JDK)**: Phiên bản 21 hoặc mới hơn. Bạn có thể tải JDK từ [trang web của Oracle](https://www.oracle.com/java/technologies/downloads/) hoặc sử dụng một bản phân phối OpenJDK như [Amazon Corretto](https://aws.amazon.com/corretto/).
*   **Apache Maven**: Phiên bản 3.6.3 hoặc mới hơn. Maven là công cụ để quản lý và xây dựng dự án. Bạn có thể tải Maven từ [trang web chính thức của Maven](https://maven.apache.org/download.cgi).
*   **Git**: Để tải mã nguồn của dự án. Bạn có thể tải Git từ [trang web chính thức của Git](https://git-scm.com/downloads).
*   **IDE (Môi trường phát triển tích hợp)**: Một IDE như [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) hoặc [Visual Studio Code](https://code.visualstudio.com/download) sẽ giúp bạn phát triển dễ dàng hơn.

### Kiểm tra cài đặt

Sau khi cài đặt, hãy mở Terminal (hoặc Command Prompt) và kiểm tra phiên bản của các công cụ:

```shell
java -version
mvn -version
git --version
```

## 2. Tải mã nguồn dự án

Sử dụng Git để sao chép (clone) mã nguồn của dự án từ repository về máy tính của bạn:

```shell
git clone <https://github.com/HocNguyen0605/MineSweeper>
```

```shell
cd MineSweeper
```

## 3. Xây dựng và chạy dự án

Dự án sử dụng Maven để quản lý các thư viện và xây dựng ứng dụng.

### Cài đặt các thư viện cần thiết

Maven sẽ tự động tải về các thư viện (dependencies) đã được định nghĩa trong tệp `pom.xml` khi bạn xây dựng dự án.

### Chạy ứng dụng

Để biên dịch và chạy ứng dụng, hãy thực hiện lệnh sau trong thư mục gốc của dự án:

```shell
mvn clean javafx:run
```

Lệnh này sẽ:
- `clean`: Xóa các tệp đã được biên dịch trước đó.
- `javafx:run`: Biên dịch mã nguồn và khởi chạy ứng dụng JavaFX.

Sau khi lệnh thực thi thành công, cửa sổ ứng dụng MineSweeper sẽ xuất hiện.

