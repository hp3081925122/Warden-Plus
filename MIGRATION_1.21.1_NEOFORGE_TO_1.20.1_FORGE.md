# Warden Plus 1.21.1 NeoForge → 1.20.1 Forge 迁移文档

## 目标与基线

- 源项目：`WardenPlus-1211-neoforge`，Minecraft 1.21.1，NeoForge 21.1.x。
- 目标项目：`WardenPlus-1201-forge`，Minecraft 1.20.1，Forge 47.x。
- 工作分支：`mc-1.20.1-forge`。
- 迁移原则：保留现有转化、阶段、技能、伤害倍率、击退、Boss 血条、网络特效、客户端渲染和配置行为；只替换版本 API、构建插件、资源格式和注册入口。

## 迁移范围

| 范围 | 源实现 | 目标处理 |
| --- | --- | --- |
| 构建链 | NeoForge Gradle userdev、Java 21 | 切换 ForgeGradle 与 Forge 1.20.1，按模板使用目标 Java 版本 |
| 模组入口 | `net.neoforged.*` 事件总线、配置注册 | 改为 `net.minecraftforge.*` 入口、Mod 总线和配置注册 |
| 网络 | NeoForge payload registrar、StreamCodec | 改为 Forge 1.20.1 可用的 SimpleChannel 与消息编解码 |
| 配置 | NeoForge `ModConfigSpec` | 改为 Forge `ModConfigSpec`，保留所有键、默认值和范围 |
| 事件 | NeoForge 事件包与事件类型 | 映射到 Forge 1.20.1 事件及事件总线 |
| 注册表 | 1.21.1 注册与数据组件 API | 映射到 1.20.1 注册表、实体和物品 API |
| Mixin | Java 21、1.21.1 目标类 | 调整兼容级别和目标方法签名，保留服务端与客户端条目 |
| 资源 | `pack_format: 34` | 使用 1.20.1 的资源包格式，并核对标签、模型和语言资源 |
| 数据包 | 1.21.1 数据目录与 JSON | 按 1.20.1 数据格式逐项校验，不能用降级空实现替代 |

## 文件责任矩阵

| 文件或目录 | 迁移动作 | 验收点 |
| --- | --- | --- |
| `build.gradle`、`gradle.properties`、`settings.gradle` | 替换插件、版本、依赖和归档名 | `gradlew build` 成功 |
| `src/main/java/com/wardenplus/WardenPlus.java` | 替换 Forge 入口和事件总线 | 模组可加载并注册配置、事件 |
| `src/main/java/com/wardenplus/WpConfig.java` | 替换配置包名，保留全部配置项 | 生成配置键与默认值一致 |
| `src/main/java/com/wardenplus/network/*` | 重写为 Forge 网络通道 | 客户端特效消息可发送、可解码 |
| `src/main/java/com/wardenplus/warden/*` | 按 1.20.1 API 迁移实体、事件、技能和伤害逻辑 | 技能顺序、倍率、阶段阈值和击退行为一致 |
| `src/main/java/com/wardenplus/client/*` | 迁移客户端事件、渲染、光照和特效调用 | 客户端可启动，渲染无缺类 |
| `src/main/java/com/wardenplus/mixin/*` | 调整 Mixin 兼容级别及目标签名 | 服务端/客户端 Mixin 均能应用 |
| `src/main/resources/META-INF/*` | `neoforge.mods.toml` 改为 `mods.toml` | 依赖范围为最低 0.0.1，不声明可选依赖 |
| `src/main/resources/pack.mcmeta` | 设置 1.20.1 资源包格式 | 资源加载无警告或格式错误 |
| `src/main/resources/assets`、`data` | 核对 1.20.1 路径和 JSON 字段 | 语言、纹理、伤害类型和数据资源完整 |

## 风险与处理策略

1. 1.20.1 没有 1.21.1 的网络 payload API，网络层必须完整改写，不能删除客户端特效发送。
2. 1.20.1 的实体、伤害和目标选择 API 方法名可能不同；每个调用点以当前 Forge 依赖源码或模板示例为准。
3. Mixin 目标类签名变化可能导致编译通过但运行时不注入；构建后必须进行客户端和服务端启动检查。
4. 迁移过程中不得把技能伤害改成固定值、删除阶段逻辑或绕过击退抗性；属性配置仍只允许通过实体 NBT 标记应用一次。
5. 若目标版本没有对应的 1.21.1 行为，优先寻找 1.20.1 的等价 API；只有确认不存在时才记录兼容性差异并保留完整行为。

## 验收顺序

1. 运行编译与资源处理，修复全部编译错误和资源格式错误。
2. 启动 1.20.1 Forge 客户端，确认模组加载、Mixin、配置和渲染。
3. 启动 1.20.1 Forge 服务端，确认自然生成监守者、物品转化、阶段转换、技能伤害和网络消息。
4. 使用 `E:/模组/发cf的模组/Render Preview` 做渲染对照，检查监守者模型、共鸣层和所有 VFX。
5. 对照源版本逐项检查功能清单，确认没有简化实现，并保留服务端、客户端和渲染日志证据。

## 本轮迁移修复记录

- 网络层改为 Forge 1.20.1 `SimpleChannel`，保留全部 VFX 消息字段、广播范围和客户端处理。
- 将 1.21.1 的事件、客户端 Overlay、顶点构建、渲染模型颜色和光照采样 API 映射到 1.20.1 等价 API。
- 将属性修饰符改为 1.20.1 的稳定 UUID、`MULTIPLY_TOTAL` 操作，并继续使用实体持久化 NBT 标记避免重复应用。
- 保留源项目全部 73 个 Java 文件和 16 个资源文件；未删除技能、阶段、伤害、击退或特效实现。

## 校验记录

- 已完成本机缓存 Forge 1.20.1 官方映射下的 73 个 Java 文件编译检查，无编译错误。
- 已定位 TLS 问题原因为 `gradle.properties` 中指向 `127.0.0.1:7892` 的失效代理；移除失效代理后，Gradle 直接 HTTPS 下载恢复正常。MixinExtras 改用已验证的 Maven 坐标 `io.github.llamalad7:mixinextras-common:0.4.1` 与 `mixinextras-forge:0.4.1`。
- `gradlew.bat build` 成功，产物为 `build/libs/wardenplus-1.20.1-forge-0.0.1-beta.jar`。
- 服务端测试成功：Forge 47.4.23 启动世界并输出 `Done (32.266s)!`，WardenPlus、MixinExtras 和事件订阅均已加载。
- 客户端测试成功：OpenGL 4.6 初始化，出现 `Setting user: Dev`，WardenPlus 的客户端 Mixin、资源重载和渲染初始化完成，日志无 ERROR/FATAL/Exception。
- Render Preview 自动 smoke 验证成功：`passed=10, failed=0, server=false, levelRestored=true`。随后通过 Render Preview HTTP 接口选择 `entities/minecraft:warden` 并导出 `D:\AI临时文件\RenderPreview\20261028-131443-13112\wardenplus-1201.png`；捕获元数据显示 1280×720、前景像素 39212、近黑比例 0、洋红比例 0，画面可见完整监守者模型。
