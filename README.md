# Ad Astra: The Curse Moon

Minecraft 1.20.1 的 Architectury API 项目，同时构建 Forge 和 Fabric 版本。

## 诅咒之月

在 Ad Astra 火箭的星球选择界面中，进入独立的“诅咒之月”目录，再选择“诅咒之月”并登陆。
一级火箭即可往返；星图以诅咒之月为中心，三颗卫星沿紫色轨道环绕。
卫星目前用于星图与夜空展示，登陆目标为诅咒之月。
地表维度为 `ad_ssc:curse_moon`，轨道维度为 `ad_ssc:curse_moon_orbit`。
首版沿用 Ad Astra 的月球地形、生物群系、1.622 重力和无氧低温环境。
地表与轨道使用独立维度类型，将时间固定在午夜；天空显示卫星与星空。
只有已变形的 SSC 玩家在诅咒之月地表和轨道中免疫 Ad Astra 的缺氧与温度伤害。
普通人形（包括启用 SSC 但尚未变形的玩家）仍需航天服和氧气；离开该星系后恢复正常环境规则。
星系归属、火箭等级和环境参数位于 `common/src/main/resources/data/ad_ssc/planets/`；
地形入口位于同目录下的 `../dimension/`，天空定义位于 `assets/ad_ssc/planet_renderers/`。
当前只接入星球旅行；诅咒机制尚未添加。已有世界需重启后加载新增维度。

## 代码结构

- `common/`：共享逻辑和资源。`AdAstraTheCurseMoon.init()` 由两个平台入口调用。
- `forge/`：Forge 入口、`mods.toml`、Forge 依赖和平台适配。
- `fabric/`：Fabric 入口、`fabric.mod.json`、Fabric 依赖和平台适配。

共享模块使用 Mojang 官方映射和 Java 17。加载器特有代码应放在各自平台模块。
Ad Astra 的共享 API 通过 Fabric 工件提供编译依赖；成品 JAR 只合并本项目的共享代码，
不会把 Ad Astra、Architectury API 或幻形者诅咒打包进去。

## 构建和运行

Gradle 使用 JDK 21 运行，Java toolchain 使用 JDK 17 编译和启动 Minecraft。
在 IDEA 中将 Gradle JVM 设为 JDK 21，然后重新加载根项目的 Gradle 配置。
命令行运行时也应让 `JAVA_HOME` 指向 JDK 21。

```powershell
.\gradlew.bat build
.\gradlew.bat :forge:runClient
.\gradlew.bat :fabric:runClient
```

服务端分别使用 `:forge:runServer` 和 `:fabric:runServer`。
Forge 开发环境继续使用根目录 `run/`，保留原有配置和存档；Fabric 使用 `fabric/run/`。

发布用 JAR：

- `forge/build/libs/ad_ssc-forge-1.20.1-1.0.0.jar`
- `fabric/build/libs/ad_ssc-fabric-1.20.1-1.0.0.jar`

不要使用带 `dev`、`dev-shadow` 或 `sources` 后缀的 JAR 安装游戏。

## 依赖

| 依赖 | Forge | Fabric |
| --- | --- | --- |
| Architectury API | 9.2.14 | 9.2.14 |
| Ad Astra | 1.15.21，CurseForge 文件 8833730 | 1.15.21，CurseForge 文件 8833668 |
| 幻形者诅咒 | 1.9.3.17，CurseForge 文件 9104450 | 1.9.2，Modrinth 版本 52YrQBKA |

两端还配置了 Ad Astra 的 Resourceful Lib、Resourceful Config 和 Botarium 前置。
Forge 配置 GeckoLib；Fabric 配置 Fabric API、Pehkui 和 Satin。
Fabric 开发环境显式加载 Kanos Config 0.4.1，补齐 Pehkui 的内嵌前置；使用发布版 Pehkui 时该前置已包含在其 JAR 中。
Pehkui 固定为 3.7.8+1.14.4-1.20.1，避免 1.21 工件在 Loom 的 1.20.1 开发映射下调用不存在的方法。
Fabric 还显式加载幻形者诅咒内嵌的 Apoli 2.9.2（含其传递依赖）、AzureLib 3.0.19、Player Animator 1.0.2-rc1 和 Reach Entity Attributes 2.4.0，供 Loom 开发启动使用。
这些幻形者诅咒前置使用 `modImplementation`，让 Loom 在重映射继承方法时能解析 Apoli/Calio 等父类，避免创建世界时出现 `AbstractMethodError`。
Forge 显式加载并内嵌 MixinExtras 0.5.5，供 Ad Astra 的 `@WrapOperation` 等 Mixin 使用。
Forge 的 Loom 开发环境还显式加载 ByteCodecs 1.0.2、YABN 1.0.3 和 JLayer 1.0.1，
与 Ad Astra 上游的 `forgeRuntimeLibrary` 配置一致。
开发运行任务自动加载这些依赖；安装发布版时，需要另外安装对应加载器的依赖模组。

幻形者诅咒的两端实现不是同一个 Java 包：

- Forge：模组 ID `shape_shifter_curse`，包名 `net.onixary.shapeShifterCurseForge`。
- Fabric：模组 ID `shape-shifter-curse`，包名 `net.onixary.shapeShifterCurseFabric`。

访问它们的专有 API 时，应通过平台适配隔离，避免共享代码直接引用任一平台包。
