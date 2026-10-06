<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Engine-Stockfish_19-4A90D9?style=for-the-badge&logo=chess.com&logoColor=white" />
  <img src="https://img.shields.io/badge/Telegram-@nncutett-229ED9?style=for-the-badge&logo=telegram&logoColor=white" />
</p>

<h1 align="center">♟️ NNVC Patches</h1>

<p align="center">
  Bản mod tùy chỉnh dành cho <b>Chess.com</b> trên Android của <b>NNVC</b>. Hỗ trợ phân tích ngoại tuyến với Stockfish 19 NNUE, gỡ quảng cáo, mở khóa bot, song ngữ Anh - Việt và bài tập Lichess.
</p>

<p align="center">
  <a href="https://t.me/nncutett"><img src="https://img.shields.io/badge/Telegram-@nncutett-229ED9?style=flat-square&logo=telegram&logoColor=white" /></a>
</p>

---

## 📖 About & Features



### 🌟 Key Features
- **Local Stockfish 18 & Offline Reviews**: Centipawn evaluation, Win/Draw/Loss tracking, move classification (Brilliant, Great, etc.), ELO estimation, and configurable bot strengths.
- **Offline Lichess Puzzles**: Complete puzzle journey map featuring millions of offline puzzles, streaking, and thematic practice.
- **Ad-Free UI & Unlocked Bots**: Banners, interstitial ads, and video promotions are removed. All Versus Bots are fully unlocked.
- **Panic Mode**: Toggle all overlays instantly.

---

## 🎮 Gestures & Panic Mode

Access features by interacting with the Chess.com logo on the main screen:
- **Tap & Hold** → Opens the settings menu.
- **Double-Tap** → Toggles **Panic Mode** (instantly hides/shows all overlays).

---


## 🩹 Patches

<!-- PATCHES_START -->
> **[v1.15.0](https://github.com/PrathxmOp/Prathxm-Patches/releases/tag/v1.15.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;8 patches total
<details>
<summary>📦 Chess.com&nbsp;&nbsp;•&nbsp;&nbsp;8 patches</summary>
<br>

**🎯 Supported versions:**

| 4.10.0 | 4.10.0-googleplay | 4.10.20 | 4.10.20-googleplay |
| :---: | :---: | :---: | :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Ad-Free](#ad-free) | Removes advertisements |  |
| [Clone Chess.com](#clone-chess-com) | Changes the package name to com.chess.prathxm, allowing the patched app to be installed side-by-side with the original Chess.com app. |  |
| [Custom Titles](#custom-titles) | Fetches and applies custom titles for users from a remote database. DM PrathxmOp to get yours for fun lol! |  |
| [Global Crash Handler](#global-crash-handler) | Catches uncaught exceptions and displays a custom crash screen with details to report issues. |  |
| [Lichess Puzzles](#lichess-puzzles) | Loads daily puzzles from Lichess and bypasses Chess.com puzzle premium limits. |  |
| [Local Stockfish Analysis](#local-stockfish-analysis) | Enables local Stockfish engine for post-game review & analysis. |  |
| [Unlimited Play Coach](#unlimited-play-coach) | Removes the one-free-game-per-day limit on Play Coach. |  |
| [Unlock All Bots](#unlock-all-bots) | Unlocks all premium and restricted bots in the Versus Bots feature. |  |

</details>

<!-- PATCHES_END -->

---

## 🛠️ Cài Đặt (Installation)

### Cách 1 · Morphe Manager <sup>Khuyên dùng</sup>

1. Cài đặt [**Morphe Manager**](https://morphe.software) trên điện thoại Android của bạn.
2. Thêm kho nguồn patch này vào Morphe:
   <p align="center">
     <a href="https://morphe.software/add-source?github=nhatnamdev662/Prathxm-Patches"><b>➕ Thêm Nguồn Vào Morphe Manager</b></a>
   </p>
   Hoặc thêm thủ công `https://github.com/nhatnamdev662/Prathxm-Patches` tại mục **Patch Sources** trong Morphe Manager.
3. Chọn **Chess.com**, chọn các patch cần dùng và nhấn **Patch**.

---

## 💬 Liên Hệ & Hỗ Trợ (Community & Support)

Mọi thắc mắc, phản hồi hoặc cần hỗ trợ:
- [**Telegram: @nncutett (NNVC)**](https://t.me/nncutett)
- [**GitHub Discussions**](https://github.com/nhatnamdev662/Prathxm-Patches/discussions)

## 📜 Attribution & Credits

This project includes implementations, fixes, and features derived from and ported from:
- [**VenusIsJaded/Prathxm-Patches**](https://github.com/VenusIsJaded/Prathxm-Patches) (GPL-3.0) — Special thanks for opening book data integration, Lichess puzzle sound fixes, win-probability review math improvements, and coroutine flow bridge enhancements.

---

## ⚠️ Disclaimer & License

- **Disclaimer**: For educational and personal use only. Usage may violate the terms of service. The author is not responsible for any account bans.
- **License**: Licensed under the [GNU General Public License v3.0](LICENSE).
