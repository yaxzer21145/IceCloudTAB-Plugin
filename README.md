# IceCloudTAB-Plugin
About Minecraft plugin: icecloudtab plugin's Repository
# IceCloudTAB

**Advanced Tab & Scoreboard Plugin — 全版本原生兼容 (1.7.10 ~ 26.1)**

一个高性能、全版本兼容的 Minecraft Java 版玩家列表（Tab）与计分板（Scoreboard）插件。单个 JAR 即可在 1.7.10 至 26.1 的 Spigot/Paper/Folia 服务端原生运行，无需 ViaVersion 等跨版本协议插件。

---

## 特性

- **全版本原生兼容** — 1.7.10 ~ 26.1 服务端开箱即用，自适应版本差异
- **Tab 列表** — 可自定义的表头/表尾，渐变色、Hex 颜色、变量系统全支持
- **计分板** — 自定义标题与行内容，动态刷新，支持完整颜色渲染
- **渐变色** — `<gradient:#color1:#color2>text</gradient>` 语法，1.16+ 显示 Hex 渐变，低版本自动降级
- **Hex 颜色** — `#RRGGBB` 格式，1.16+ 原生渲染，低版本自动剥离
- **标准颜色** — `&0`~`&f`、`&l`、`&o`、`&r` 等全格式支持
- **变量系统** — 20+ 内置变量（玩家名、在线数、Ping、TPS、坐标等）
- **PlaceholderAPI** — 自动检测并支持 PAPI 变量扩展
- **Folia 兼容** — 原生支持 Folia 服务端线程调度
- **轻量高性能** — Java 8 编译，反射缓存优化，无额外依赖

---
## 命令与权限

| 命令 | 权限 | 说明 |
|------|------|------|
| `/ictab reload` | `icecloudtab.admin` | 热重载配置文件 |
| `/ictab toggle` | `icecloudtab.admin` | 切换计分板显示 |
| `/ictab` | `icecloudtab.see` | 查看帮助 |

---

## Tab 列表配置

```yaml
tab:
  enabled: true
  update-interval: 20
  header:
    - "&b&lIceCloud &f&lNetwork"
    - "&7<gradient:#55ff55:#00aa00>Welcome %player%</gradient>"
    - ""
  footer:
    - ""
    - "&7Online: &a%online%&7/&a%max_players%  &8|  &7Ping: &e%ping%ms"
    - "&7%date% &8- &7%time%"
```

## 计分板配置

```yaml
scoreboard:
  enabled: true
  update-interval: 20
  title: "&b&lIceCloud &f&lNetwork"
  lines:
    - "&7&m------------------"
    - " &fPlayer: &a%player%"
    - " &fPing: &e%ping%ms"
    - ""
    - " &fOnline: &a%online%&7/&a%max_players%"
    - " &fTPS: &6%tps%"
    - "&7&m------------------"
```

---

## 变量列表

| 变量 | 说明 |
|------|------|
| `%player%` | 玩家名 |
| `%displayname%` | 玩家显示名 |
| `%uuid%` | 玩家 UUID |
| `%world%` | 当前世界 |
| `%online%` | 在线玩家数 |
| `%max_players%` | 最大玩家数 |
| `%ping%` | 延迟 (ms) |
| `%tps%` | 服务器 TPS |
| `%x%` `%y%` `%z%` | 坐标 |
| `%health%` | 生命值 |
| `%food%` | 饱食度 |
| `%level%` | 经验等级 |
| `%gamemode%` | 游戏模式 |
| `%ip%` | IP 地址 |
| `%time%` | 当前时间 |
| `%date%` | 当前日期 |
| `%onlineplayers%` | 在线玩家列表 |

已安装 PlaceholderAPI 时，还可使用 `%vault_group%`、`%player_exp%` 等 PAPI 变量。

---

## 颜色系统

| 语法 | 说明 | 支持版本 |
|------|------|---------|
| `&0`~`&9` `&a`~`&f` | 标准颜色代码 | 全版本 |
| `&l` `&o` `&n` `&m` `&k` `&r` | 样式格式 | 全版本 |
| `#FF5500` | Hex 颜色 | 1.16+ |
| `<gradient:#color1:#color2>text</gradient>` | 渐变色 | 1.16+ Hex / 低版本降级 |

---

## 版本兼容

| 版本区间 | Tab 实现 | 颜色支持 |
|---------|---------|---------|
| 1.7.10 ~ 1.12.2 | NMS Packet 反射 | 标准颜色 + 渐变色降级 |
| 1.13 ~ 1.15.x | Bukkit API | 标准颜色 + 渐变色降级 |
| 1.16 ~ 26.1 | Bukkit API | Hex 颜色 + 渐变色 RGB |

---

## 技术架构

```
IceCloudTAB/
├── core/                # 版本无关核心逻辑
│   ├── IceCloudTAB      # 主类
│   ├── ConfigManager    # 配置管理
│   ├── TabManager       # Tab 调度
│   ├── ScoreboardManager# 计分板管理
│   └── UpdateTask       # 定时更新
├── adapter/             # 版本隔离适配层
│   ├── TabAdapter       # 接口
│   ├── TabAdapterPre13  # 1.7~1.12 NMS 实现
│   └── TabAdapterPost13 # 1.13+ Bukkit API 实现
├── utils/               # 全版本兼容工具
│   ├── VersionUtils     # 版本检测
│   ├── ReflectionUtil   # 安全反射（缓存）
│   ├── ColorUtils       # 颜色/渐变处理
│   └── VariableUtils    # 变量替换
```

全版本兼容三大原则：**基线兼容**（1.16.5 API 编译）+ **版本隔离**（适配器模式）+ **动态适配**（自动识别版本加载对应实现）。

---

## 依赖

- **硬依赖**: 无
- **软依赖**: PlaceholderAPI（自动检测，无则降级运行）
- **服务端**: Spigot / Paper / Folia 1.7.10 ~ 26.1

---

## 构建

```bash
mvn clean package
```

构建产物位于 `target/IceCloudTAB-1.0.0.jar`。

---

## 协议

MIT License © 2026 IceCloud & Plux Dev
Made by ya_xzer21145
