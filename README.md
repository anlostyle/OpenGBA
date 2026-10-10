# OpenGBA

OpenGBA 是一款开源的 **GBA 掌机启动器**：单 APK、只面向 Game Boy Advance，可设为 Android 默认主屏幕（HOME），开机即进入游戏库或续玩上次的游戏。最初为 960×640（3:2）的安卓游戏掌机设计，同时支持实体按键与触屏。

本项目基于 [Lemuroid](https://github.com/Swordfish90/Lemuroid)（GPLv3）和 [LibretroDroid](https://github.com/Swordfish90/LibretroDroid)，模拟核心为 libretro 的 [mGBA](https://docs.libretro.com/library/mgba/)。

## 功能

- 开机自动续玩 / 首页游戏库（最近游玩、收藏、全部游戏），支持实体按键与触摸
- 游戏库翻页：L/R、十字键越过首行/末行、触屏左右滑动
- 应用抽屉：作为默认主屏幕时，也能打开系统里的其他应用
- 主题配色：GBA 绿、珊瑚橙、靛蓝、复古 DMG、蜜桃、黑冰，对应 KONKR Pocket Advance 的官方机身配色（设置 → 常规 → 主题配色）
- 长按快进、快速回溯
- 即时存档 / 读档，多存档槽
- 金手指（`.cht`）：内置 mGBA 金手指库，按 ROM 的 CRC 精确匹配；也可在 ROM 旁放置同名 `.cht`
- GBA 专用的统一热键与机内按键说明
- 应用内在线更新（读取 Release 中的 `update.json`）

## 下载

最新 APK 见 [Releases](https://github.com/anlostyle/OpenGBA/releases)。

> 说明：触摸、方向键等交互在 960×640 的 KPA（AYANEO KONKR Pocket Advance）上验证；其他设备未经系统测试。

## 构建

```bash
git submodule update --init
./gradlew :lemuroid-app:assembleFreeBundleDebug
```

需要 JDK 17 与 Android SDK。发布签名密钥未包含在仓库中，仓库内的 `debug.keystore` 仅用于调试构建。

## ROM 与金手指

- 本项目**不包含也不提供任何游戏 ROM**，请使用你自己合法持有的备份。
- `lemuroid-app/src/main/assets/gba-cheats/` 中的金手指数据来自第三方整理的 mGBA 金手指集合，其原始许可未明确；如权利人认为不妥，请提 Issue，我们会及时移除。

## 许可证

GPLv3，见 [COPYING](COPYING)。原 Lemuroid 的说明保留在 [README.lemuroid.md](README.lemuroid.md)。
