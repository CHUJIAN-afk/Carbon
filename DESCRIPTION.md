# Carbon

**GPU-powered item rendering optimization for Minecraft.**

Carbon is a client-side performance mod that caches item model geometry on the GPU and batches matching items through instanced rendering. It reduces repeated rendering work, especially in scenes with large numbers of dropped items.

## Features

- **Optimized dropped items:** Handles rotation, bobbing, and scaling on the GPU, and reuses scene data when it has not changed.
- **Support across display contexts:** Optimizes compatible standard item models in your hand, in GUIs, and in other contexts that use Minecraft's standard item rendering pipeline.
- **Enchantment glint and shadows:** Batches ordinary enchantment glint and vanilla circular item shadows alongside item rendering.
- **Optional visual physics:** Adds tumbling while items move and lets them settle naturally when they land. This only affects appearance; movement, collisions, and pickup behavior remain unchanged.
- **Rendering fallback:** Uses the original rendering path for models or effects that cannot be safely batched.

## Shader Compatibility

Tested with **Iris 1.8.14-beta.1+mc1.21.1** and **Photon v1.4**, including shader shadows. Compatibility with other shader packs may vary.

## Configuration

Configure Carbon through its in-game configuration screen or `config/carbon-client.toml`. Optional visual physics is disabled by default. Use `/carbon stats` to inspect rendering and cache statistics.
