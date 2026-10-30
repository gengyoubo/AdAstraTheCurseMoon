# Ad Astra: The Curse Moon

Minecraft 1.20.1 的 Architectury API 项目，同时构建 Forge 和 Fabric 版本。

## 诅咒之月

在 Ad Astra 火箭的星球选择界面中，进入独立的“诅咒之月”目录，再选择目标并登陆。
星图以诅咒之月为中心，三颗可登陆的卫星沿紫色轨道环绕；目的地按最低火箭等级由外向内排列：

| 目的地（由外向内） | 最低火箭等级 | 地表维度 | 轨道维度 |
| --- | --- | --- | --- |
| 诅咒卫星外侧（紫色卫星） | 1级 | `ad_ssc:curse_satellite_outer` | `ad_ssc:curse_satellite_outer_orbit` |
| 诅咒卫星中部（浅黄色卫星） | 2级 | `ad_ssc:curse_satellite_middle` | `ad_ssc:curse_satellite_middle_orbit` |
| 诅咒卫星内侧（棕色卫星） | 3级 | `ad_ssc:curse_satellite_inner` | `ad_ssc:curse_satellite_inner_orbit` |
| 诅咒之月（中心） | 4级 | `ad_ssc:curse_moon` | `ad_ssc:curse_moon_orbit` |

高等级火箭也可访问低等级目的地；等级不足的目的地不会出现在可选列表中。
三颗卫星拥有各自独立的地表与轨道维度，首版均沿用 Ad Astra 的月球地形、生物群系、1.622 重力和无氧低温环境。
地表与轨道使用独立维度类型，将时间固定在午夜；天空显示卫星与星空。
只有已变形的 SSC 玩家在诅咒之月及三颗卫星的地表和轨道中免疫 Ad Astra 的缺氧与温度伤害。
普通人形（包括启用 SSC 但尚未变形的玩家）仍需航天服和氧气；离开该星系后恢复正常环境规则。
星系归属、火箭等级和环境参数位于 `common/src/main/resources/data/ad_ssc/planets/`；
地形入口位于同目录下的 `../dimension/`，天空定义位于 `assets/ad_ssc/planet_renderers/`。
已有世界需重启后加载新增维度。

## 诅咒之月辐射

三个卫星及诅咒之月的地表、轨道均有辐射，按外侧、中部、内侧、月球分别施加 I、II、III、IV 级状态效果。
图标复用 SSC 的诅咒之月图标。效果在辐射区内持续存在，离开后移除并恢复进入前的形态。

- 已变形的玩家沿 SSC 进化路径临时前进“辐射等级”步，到主节点终点停止，不转入其终点子变体，也不循环回起点。
- 每 20 个游戏刻（正常速度下每秒）受到 `max(0, 辐射等级 − 原始形态阶段)` 点辐射伤害，1点等于半颗心；不使用临时形态的阶段来减免伤害。
- 例如原始1阶段进入2级辐射区，临时变为3阶段，每秒受到1点伤害。没有后续形态时保持原形，仍按阶段差计算伤害。
- 未启用 SSC 的人形视为0阶段，只受伤，不启用 SSC。已启用但仍为人形的玩家随机选择一个初始形态，消耗第一步，再前进剩余步数，伤害仍以原始0阶段计算；离开后恢复人形。
- 切换辐射区域时始终从原始形态重算；随机选择的形态路线在本次暴露期间保持不变。原始形态随玩家存档保存，退出重进、死亡重生后仍可恢复。
- 辐射伤害不受护甲、保护附魔或抗性效果减免；创造模式保留原版伤害免疫。玩家主动或因其他 SSC 机制永久变身时，以新形态作为基准，避免退出辐射区时覆盖该变身。

Forge 使用 SSC 的1起始阶段编号，Fabric 将其0起始编号换算为1起始编号。辐射效果为 `ad_ssc:curse_moon_radiation`。
`./gradlew :common:testRadiation` 可单独执行暴露、阶段、每秒伤害及恢复状态的行为测试；完整 `build` 也会运行。

## 月尘风暴与诅咒之月耀斑

两种天象只出现在三颗卫星和诅咒之月的地表，轨道和其他维度不受影响。
星球等级与火箭等级相同：外侧、中部、内侧卫星和诅咒之月分别为1至4级。
每个地表维度独立计时，每分钟掷骰一次，状态随存档保存；开始、结束和倒计时会显示在动作栏。
抗性按原始形态阶段计算（与辐射相同，不使用辐射带来的临时阶段），每阶段减免20%，最多80%；未启用 SSC 的人形为0阶段。

**月尘风暴**

- 每分钟触发概率为 `4% × 星球等级`，持续2至5分钟。
- 客户端雾气变为月尘紫色并明显变浓，地表上方有随风吹动的紫色尘粒，数量随“粒子”视频设置减少。
- 头顶没有遮挡，或视线高度的8个水平方向中有至少4个在8格内没有方块时，视为暴露。
- 暴露的玩家每2秒受到 `(1 + 0.5 × (星球等级 − 1)) × (1 − 抗性)` 点伤害，即1至2.5点；护甲照常减免。

**诅咒之月耀斑爆发**

- 每分钟触发概率为 `1% + 1% × 星球等级`，先预警10秒，再爆发10秒；结束后5分钟内不会再次发生。
- 卫星：爆发期间，头顶没有遮挡的玩家每秒受到 `3 × 星球等级 × (1 − 抗性)` 点伤害，无视护甲。雾气随耀斑闪烁。
- 诅咒之月：爆发期间，每位玩家附近24格内每0.5秒从高空落下一块月球圆石。落地处3格内的生物受到最多10点伤害，距离越远越低；石块会留在地面。

伤害类型为 `ad_ssc:moon_dust_storm`、`ad_ssc:curse_moon_flare` 和 `ad_ssc:curse_moon_meteor`。
`./gradlew :common:testCurseSky` 检查触发概率、阶段计时、存档读取及伤害公式；完整 `build` 也会运行。

## JEI 兼容

Forge 开发环境加载 JEI 15.71.0.224 及 MezzConfig，Fabric 开发环境加载 JEI 15.20.0.106，兼容现有 Fabric Loader 与 Fabric API。
JEI 是可选客户端集成，发布包不内嵌 JEI；未安装 JEI 时，本模组的配方和服务端照常运行。
安装 JEI 后，常规工作台和锻造台配方由 JEI 自动展示；塑形航天服另有用途说明。

月尘矿使用独立的“月尘矿熔炼”分类，列出普通矿石及四个产地变体，并显示对应输入 NBT、输出数量、耗时和经验。
配方直接读取当前世界同步的配方数据，数据包修改产量后也会正确展示。原版熔炼分类中忽略产地的重复条目会被隐藏。
在物品列表中可以分别查看四个产地的矿石；熔炉界面支持将对应产地的矿石填入输入槽，燃料仍需自行放入。
矿石的产地由 `BlockStateTag.origin` 判定，不受自定义名称等无关 NBT 影响。

接入方式参照 [JEI 官方文档](https://github.com/mezz/JustEnoughItems/wiki/Getting-Started-%5BMinecraft-1.18.2-to-1.20.1%5D)，共享代码仅编译依赖 JEI API。
`./gradlew :common:testMoonDustRecipes` 检查五种产地的熔炼读取、产量、输入匹配及网络同步；完整构建会自动执行。

## 管道

物流管道、液体管道和电能管道外观沿用 SSC 的白色外壳、金色镶边和月尘玻璃，分别以紫、青、粉色区分。
同种管道相连组成网络，网络每刻统一搬运一次；不同种类的管道互不相连。

每个面的出入模式参照通用机械，用管道配置器右键该面切换（潜行时反向）：

| 模式 | 作用 | 模型 |
| --- | --- | --- |
| 普通 | 接受相邻方块推入，也向其输出 | 普通接口 |
| 推出 | 只向相邻方块输出，不接受推入 | 红色法兰 |
| 拉入 | 主动从相邻方块抽取，不向其输出 | 蓝色法兰 |
| 断开 | 不连接 | 无接口 |

管道之间的面只在“普通”和“断开”之间切换，任一侧断开即拆分网络。
点击接口或法兰时配置对应的面，点击管道中心时配置点到的那一面。

- 物流管道：拉入面每 10 刻抽取最多 16 个物品，按轮询依次送往各输出面的容器。漏斗等可从普通/拉入面推入。
- 液体管道：拉入面每刻抽取最多 1000 mB，平均分给各输出面的储罐。
- 电能管道：同时输送加载器能量（Forge 的 FE、Fabric 的 Team Reborn Energy，与 Ad Astra 机器相同）和月尘通量，两者互不转换。拉入面每刻最多抽取 4000 能量和 2000 MF。

每根管道带有少量缓冲，供其他方块推入；推入的内容不会送回来源的那一面。
液体和能量通过 Botarium（Ad Astra 自带的前置）接入两端的存储接口；物品在 Forge 使用物品处理器能力，在 Fabric 使用 Transfer API。
`./gradlew :common:testPipes` 检查液体与能量的平均分配；完整构建会自动执行。

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
| 幻形者诅咒 | 1.9.3.18，CurseForge 文件 9224248 | 1.9.2，Modrinth 版本 52YrQBKA |

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
