# Warden Plus 1.21.1 NeoForge → 26.1.2 NeoForge 迁移文档

## 迁移目标

- 源基线：`WardenPlus-1211-neoforge`，Minecraft 1.21.1，NeoForge 21.1.238。
- 目标工作树：`WardenPlus-2612-neoforge-migration`，Minecraft 26.1.2，NeoForge 26.1.2.95。
- Git 分支：`mc-26.1.2-neoforge`。
- Java：25；Gradle 插件：NeoForge ModDev 2.0.140。

## 迁移原则

保留末影监守者转化、普通攻击、阶段阈值、所有技能及伤害倍率、击退抗性、实体属性一次性 NBT 标记、网络 VFX、客户端动画、渲染层、光照和配置行为。只处理 26.1.2 的构建链、资源包格式、目录格式和 API 变化，不删除技能、特效或用固定值替代原有逻辑。

## 迁移范围

| 范围 | 处理内容 | 验收点 |
| --- | --- | --- |
| Gradle/Java | UserDev 7.1.37、Java 25 与 ModDev 2.0.140 | `gradlew build` 通过 |
| 资源包 | 1.21.1 `pack_format` 切换为 26.1.2 `min_format/max_format` | 资源加载无版本格式错误 |
| 数据目录 | 核对单数目录；当前仅有 `damage_type`，无需虚构新资源 | `rg --files src/main/resources/data` 无旧复数目录 |
| NeoForge API | 按 26.1.2 编译错误和依赖源码逐项映射 | 入口、事件、网络、渲染和 Mixin 均可加载 |
| 客户端代码 | 清理 `@OnlyIn`（如存在），保持客户端事件隔离 | 服务端不会加载客户端类 |
| 依赖 | 迁移到 26.1.2 对应的开发依赖版本 | 不在模组元数据中声明可选依赖 |

## 保留功能检查

- [x] 73 个 Java 源文件和所有 VFX 资源保留。
- [x] 0.5/0.25 百分比生命阶段阈值保留为配置值。
- [x] 技能伤害继续按攻击力乘技能倍率。
- [x] 击退继续使用实体击退抗性和配置倍率。
- [x] 属性只在 `wardenplus_attr_applied` NBT 标记不存在时应用。
- [x] 网络 payload、VFX 类型、编码字段和客户端分发不删减。
- [x] 26.1.2 构建通过（`gradlew.bat build`）。
- [x] 专用服务端启动并加载模组与 Mixin（移除仅用于开发环境、要求 NeoForge 26.1.2.99 的 JEI 依赖后通过）。
- [x] 客户端启动并加载渲染层与 Mixin（已进入单人世界）。
- [ ] 使用 `E:/模组/发cf的模组/Render Preview` 做模型、共鸣层和 VFX 对照。

## 26.1.2 特别检查

- `pack.mcmeta` 使用 `min_format: 84` 与 `max_format: 84`。
- 资源目录保持 1.21.1/26.1.2 的单数目录规则。
- 检索并删除所有 `@OnlyIn` 与 `net.neoforged.api.distmarker.OnlyIn` 引用；`EventBusSubscriber(value = Dist.CLIENT)` 如仍被 26.1.2 API 支持则保留。
- 若 26.1.2 的客户端渲染或网络 API 改名，必须迁移到等价 API，不能通过空实现绕过。

## 校验记录

本文件按阶段更新构建、启动和渲染验证结果；构建失败时记录第一处真实 API 错误及修复依据。`r`n`r`n2026-10-28 客户端验证：首次启动发现旧版 WardenModel Mixin 目标方法签名失效，已迁移到 `setupAnim(WardenRenderState)`；随后客户端成功进入单人世界并完成资源、Mixin、渲染初始化。

## 当前构建记录

2026-10-28：已修复服务端伤害事件、NBT Optional、注册表 lookup、BossBar UUID、缓慢效果 ID、粒子选项泛型等 26.1 API。客户端渲染状态系统已迁移到 `WardenRenderState`/`RenderLayer.submit`/`GuiGraphicsExtractor`/26.1 雾效事件，LightTexture Mixin 已迁移到 `LightmapRenderStateExtractor`。

2026-10-28 客户端验证：补齐 26.1.2 半透明顶点所需的 Overlay、Light、Normal 属性，修复 VFX 渲染阶段 `BufferBuilder.endLastVertex` 崩溃；重新构建通过并再次进入单人世界。

2026-10-28 服务端与渲染对比：独立服务端启动至 `Done (0.379s)` 并加载 Warden Plus；Render Preview 26.1.2 加载构建包后，`minecraft:warden` 完成 4 帧实际绘制验证。预览器随后在自带 `minecraft:player` 条目因空粒子提供器崩溃，与 Warden 条目无关。

2026-10-28 Render Preview 修复：玩家实体改用离线 `LocalPlayer`，空粒子场景不再抛异常；smoke 验证 10/10 通过，Warden 离屏截图已成功导出。


2026-10-28 特效与黑暗修正：VFX 渲染类型原先被替换为 `entityTranslucentEmissive`（普通透明混合、受雾影响、覆盖层坐标为 0 会染红），与 1.21.1 的加法发光不一致；现改为自建 `RenderPipeline`（`position_tex_color` 着色器、`BlendFunction.LIGHTNING` 加法混合、无剔除、不写深度、不受雾影响），通过 `RegisterRenderPipelinesEvent` 注册。光照 Mixin 原先直接返回黑暗系数作为 `darknessEffectScale`，导致整屏全黑；现改为修改 `getEffectBlendFactor` 的返回值，与 1.21.1 修改 `getDarknessGamma` 等价，保留原版亮度削减与脉动。雾同时收紧 `skyEnd`/`cloudEnd`。
