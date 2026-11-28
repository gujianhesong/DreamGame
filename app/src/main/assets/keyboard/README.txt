本目录 gm.sf2 为完整 GM 音色库 microGM（microgm-opt，约 2.5MB），含 128 种 GM 音色。
许可：WTFPL 2.0（作者 michael / Musical Artifacts #2529）。
源码镜像：github.com/datamoc/mwg data/microgm-opt.sf2

重新编译安装后随 APK 打入 assets（build.gradle 已对 .sf2 设置 noCompress）。

调试用 adb 覆盖（大于 20KB 时优先于包内文件）：
  Android/data/com.game.dream/files/keyboard/gm.sf2

若需更大体积、更高音质，可自行替换为 GeneralUser GS 等，仍命名为 gm.sf2。

builtin_min.sf2 / BuiltinSoundFont.java 为 TinySoundFont 484 字节占位，仅在主 gm.sf2
加载失败时使用。