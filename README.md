# Carbon（碳）

Minecraft 1.20.1 / Forge 47.0.0 及以上（构建使用 47.4.22） 客户端 GPU 物品模型渲染优化模组，Java 17，MIT 许可证。

![Carbon](branding/mod-cover.svg)

缓存重复物品的模型几何，通过 GPU 实例化合并绘制。标准 `ItemRenderer.renderModelLists` 提交保留调用者的变换、光照和叠加参数，覆盖掉落物、手持、界面及其他调用标准接口的渲染。特殊投影光效、自定义材质和需排序的半透明模型保留原生处理。

默认启用掉落物专用管线，在 GPU 计算自旋、浮动和缩放，复用静止场景记录；普通附魔光效与地面阴影同样参与实例化。可选物理化沿模型局部轴翻滚，停止后平滑躺平，并加入每个实体固定的微小高度偏移。物理效果仅改变视觉，不修改速度、碰撞或拾取。

## 配置

配置文件为 `config/carbon-client.toml`。模组列表的“配置”按钮提供独立设置页，无需 NeoForge 配置界面。

```toml
enabled = true
optimizeDroppedItems = true
physicalDroppedItems = false
skipVisibilityChecks = true
cacheUpdateBudgetMs = 2.0
```

- `enabled`：优化总开关。
- `optimizeDroppedItems`：默认开启掉落物专用 GPU 管线；关闭后保留原有掉落物变换，通过标准模型提交继续优化。
- `physicalDroppedItems`：默认关闭，需要开启掉落物专用管线；落地姿态收稳后停止派发变换计算。
- `skipVisibilityChecks`：专用管线默认跳过遮挡与视锥检查，完整提交距离内的普通掉落物；距离限制始终有效。

- `cacheUpdateBudgetMs`：每帧缓存更新预算，默认 2 毫秒，可设为 0–20 毫秒，0 表示立即完成；队列完成前绘制旧实例与阴影。单个物品更新可能超预算。

`/carbon enabled false` 停用优化，`/carbon stats` 查看统计。新配置尚不存在时会迁移旧客户端配置；升级时替换旧 Jar。

## 光影

Iris / Oculus 访问集中在 `IrisCompatibility`，未向光影模组注入 Mixin；无法保持原生通道语义时保留原有提交。

## 构建

使用 Java 17，执行 `gradlew.bat build`，输出 `build/libs/carbon-1.20.1.9-B.jar`。

[更新记录](CHANGELOG.md)
