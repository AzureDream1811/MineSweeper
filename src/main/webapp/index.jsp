<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>MineSweeper</title>
  <link href="https://fonts.googleapis.com/css2?family=Share+Tech+Mono&family=Rajdhani:wght@400;600;700&display=swap" rel="stylesheet">
  <style>
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

    :root {
      --bg: #0a0c0f;
      --surface: #111418;
      --border: #1e2530;
      --accent: #00ff88;
      --accent2: #ff3c3c;
      --text: #c8d8e8;
      --muted: #4a5a6a;
      --cell: #1a2030;
    }

    body {
      background: var(--bg);
      color: var(--text);
      font-family: 'Rajdhani', sans-serif;
      min-height: 100vh;
      overflow-x: hidden;
    }

    /* Grid background */
    body::before {
      content: '';
      position: fixed;
      inset: 0;
      background-image:
        linear-gradient(rgba(0,255,136,0.03) 1px, transparent 1px),
        linear-gradient(90deg, rgba(0,255,136,0.03) 1px, transparent 1px);
      background-size: 40px 40px;
      pointer-events: none;
      z-index: 0;
    }

    .container {
      position: relative;
      z-index: 1;
      max-width: 800px;
      margin: 0 auto;
      padding: 60px 24px;
    }

    /* Header */
    .header {
      text-align: center;
      margin-bottom: 64px;
    }

    .game-grid {
      display: inline-grid;
      grid-template-columns: repeat(5, 28px);
      gap: 3px;
      margin-bottom: 32px;
    }

    .cell {
      width: 28px;
      height: 28px;
      background: var(--cell);
      border: 1px solid var(--border);
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: 'Share Tech Mono', monospace;
      font-size: 11px;
      animation: cellReveal 0.4s ease forwards;
      opacity: 0;
    }

    @keyframes cellReveal {
      from { opacity: 0; transform: scale(0.5); }
      to { opacity: 1; transform: scale(1); }
    }

    .title {
      font-size: clamp(42px, 8vw, 72px);
      font-weight: 700;
      letter-spacing: 8px;
      text-transform: uppercase;
      color: #fff;
      text-shadow: 0 0 40px rgba(0,255,136,0.3);
      line-height: 1;
    }

    .title span { color: var(--accent); }

    .subtitle {
      margin-top: 12px;
      font-family: 'Share Tech Mono', monospace;
      font-size: 13px;
      color: var(--muted);
      letter-spacing: 3px;
    }

    /* Download card */
    .download-card {
      background: var(--surface);
      border: 1px solid var(--border);
      padding: 40px;
      margin-bottom: 40px;
      position: relative;
      overflow: hidden;
    }

    .download-card::before {
      content: '';
      position: absolute;
      top: 0; left: 0; right: 0;
      height: 2px;
      background: linear-gradient(90deg, transparent, var(--accent), transparent);
    }

    .release-tag {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: rgba(0,255,136,0.08);
      border: 1px solid rgba(0,255,136,0.2);
      color: var(--accent);
      font-family: 'Share Tech Mono', monospace;
      font-size: 12px;
      padding: 4px 12px;
      margin-bottom: 20px;
      letter-spacing: 1px;
    }

    .release-tag::before {
      content: '●';
      animation: blink 1.5s infinite;
    }

    @keyframes blink {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.2; }
    }

    .card-title {
      font-size: 22px;
      font-weight: 700;
      color: #fff;
      margin-bottom: 8px;
      letter-spacing: 1px;
    }

    .card-desc {
      color: var(--muted);
      font-size: 15px;
      margin-bottom: 28px;
      line-height: 1.6;
    }

    .download-btn {
      display: inline-flex;
      align-items: center;
      gap: 12px;
      background: var(--accent);
      color: #000;
      text-decoration: none;
      font-family: 'Rajdhani', sans-serif;
      font-weight: 700;
      font-size: 16px;
      letter-spacing: 2px;
      text-transform: uppercase;
      padding: 14px 32px;
      transition: all 0.2s;
      position: relative;
      overflow: hidden;
    }

    .download-btn::before {
      content: '';
      position: absolute;
      inset: 0;
      background: rgba(255,255,255,0.15);
      transform: translateX(-100%);
      transition: transform 0.3s;
    }

    .download-btn:hover::before { transform: translateX(0); }
    .download-btn:hover { box-shadow: 0 0 30px rgba(0,255,136,0.4); }

    .download-btn svg { width: 18px; height: 18px; flex-shrink: 0; }

    .file-info {
      margin-top: 16px;
      font-family: 'Share Tech Mono', monospace;
      font-size: 12px;
      color: var(--muted);
    }

    /* Steps */
    .section-label {
      font-family: 'Share Tech Mono', monospace;
      font-size: 11px;
      color: var(--accent);
      letter-spacing: 4px;
      text-transform: uppercase;
      margin-bottom: 20px;
    }

    .steps {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 40px;
    }

    .step {
      display: flex;
      gap: 16px;
      align-items: flex-start;
      background: var(--surface);
      border: 1px solid var(--border);
      padding: 16px 20px;
      transition: border-color 0.2s;
    }

    .step:hover { border-color: var(--accent); }

    .step-num {
      font-family: 'Share Tech Mono', monospace;
      font-size: 13px;
      color: var(--accent);
      min-width: 24px;
      margin-top: 2px;
    }

    .step-text {
      font-size: 15px;
      color: var(--text);
      line-height: 1.5;
    }

    .step-text strong { color: #fff; }

    code {
      font-family: 'Share Tech Mono', monospace;
      font-size: 12px;
      background: rgba(0,255,136,0.08);
      border: 1px solid rgba(0,255,136,0.15);
      color: var(--accent);
      padding: 1px 6px;
    }

    /* Warning */
    .warning {
      background: rgba(255,60,60,0.06);
      border: 1px solid rgba(255,60,60,0.2);
      border-left: 3px solid var(--accent2);
      padding: 14px 18px;
      font-size: 14px;
      color: #e8a0a0;
      line-height: 1.6;
      margin-bottom: 40px;
    }

    .warning strong { color: var(--accent2); }

    /* Requirements */
    .req-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 12px;
      margin-bottom: 48px;
    }

    .req-item {
      background: var(--surface);
      border: 1px solid var(--border);
      padding: 16px;
    }

    .req-label {
      font-family: 'Share Tech Mono', monospace;
      font-size: 10px;
      color: var(--muted);
      letter-spacing: 2px;
      margin-bottom: 6px;
    }

    .req-value {
      font-size: 15px;
      font-weight: 600;
      color: #fff;
    }

    /* Footer */
    .footer {
      text-align: center;
      font-family: 'Share Tech Mono', monospace;
      font-size: 11px;
      color: var(--muted);
      padding-top: 32px;
      border-top: 1px solid var(--border);
      letter-spacing: 2px;
    }

    .footer a { color: var(--accent); text-decoration: none; }
    .footer a:hover { text-decoration: underline; }
  </style>
</head>
<body>
<div class="container">

  <!-- Header -->
  <div class="header">
    <div class="game-grid">
      <%
        String[][] grid = {
          {"1","·","·","F","·"},
          {"·","2","1","·","1"},
          {"·","·","·","·","·"},
          {"1","1","·","2","·"},
          {"·","·","·","·","💣"}
        };
        String[][] cls = {
          {"num1 open","open","open","flag open","open"},
          {"open","num2 open","num1 open","open","num1 open"},
          {"open","open","open","open","open"},
          {"num1 open","num1 open","open","num2 open","open"},
          {"open","open","open","open","mine open"}
        };
        int delay = 0;
        for (int r = 0; r < 5; r++) {
          for (int c = 0; c < 5; c++) {
      %>
      <div class="cell <%= cls[r][c] %>" style="animation-delay: <%= delay * 60 %>ms">
        <%= grid[r][c] %>
      </div>
      <% delay++; } } %>
    </div>

    <div class="title">MINE<span>SWEEPER</span></div>
    <div class="subtitle">// JAVAFX EDITION — v1.0</div>
  </div>

  <!-- Download -->
  <div class="download-card">
    <div class="release-tag">LATEST RELEASE &nbsp;v1.0</div>
    <div class="card-title">Windows 10 / 11 — 64-bit</div>
    <div class="card-desc">
      Không cần cài Java. Runtime đã được bundle sẵn trong file zip.
    </div>
    <a class="download-btn"
       href="https://github.com/HocNguyen0605/MineSweeper/releases/download/v1.0/MineSweeper-v1.0-windows.zip">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
        <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"></path>
        <polyline points="7 10 12 15 17 10"></polyline>
        <line x1="12" y1="15" x2="12" y2="3"></line>
      </svg>
      TẢI VỀ
    </a>
    <div class="file-info">MineSweeper-v1.0-windows.zip</div>
  </div>

  <!-- Steps -->
  <div class="section-label">// Hướng dẫn cài đặt</div>
  <div class="steps">
    <div class="step">
      <div class="step-num">01</div>
      <div class="step-text">Nhấn nút <strong>TẢI VỀ</strong> để tải file <code>MineSweeper-v1.0-windows.zip</code></div>
    </div>
    <div class="step">
      <div class="step-num">02</div>
      <div class="step-text">Giải nén file zip — chuột phải → <strong>Extract All</strong></div>
    </div>
    <div class="step">
      <div class="step-num">03</div>
      <div class="step-text">Mở thư mục <code>MineSweeper\</code> vừa giải nén</div>
    </div>
    <div class="step">
      <div class="step-num">04</div>
      <div class="step-text">Double-click <strong>MineSweeper.exe</strong> để chạy game</div>
    </div>
  </div>

  <!-- Warning -->
  <div class="warning">
    <strong>⚠ Windows SmartScreen</strong><br>
    Nếu Windows hiện thông báo "Windows protected your PC" →
    click <strong>More info</strong> → <strong>Run anyway</strong>.
    Đây là cảnh báo bình thường với app chưa có code signing, hoàn toàn an toàn.
  </div>

  <!-- Requirements -->
  <div class="section-label">// Yêu cầu hệ thống</div>
  <div class="req-grid">
    <div class="req-item">
      <div class="req-label">HỆ ĐIỀU HÀNH</div>
      <div class="req-value">Windows 10 / 11</div>
    </div>
    <div class="req-item">
      <div class="req-label">KIẾN TRÚC</div>
      <div class="req-value">64-bit (x64)</div>
    </div>
    <div class="req-item">
      <div class="req-label">JAVA</div>
      <div class="req-value">Không cần cài</div>
    </div>
  </div>

  <!-- Footer -->
  <div class="footer">
    <a href="https://github.com/HocNguyen0605/MineSweeper">GitHub Repository</a>
    &nbsp;·&nbsp;
    <a href="https://github.com/HocNguyen0605/MineSweeper/releases">All Releases</a>
  </div>

</div>
</body>
</html>
