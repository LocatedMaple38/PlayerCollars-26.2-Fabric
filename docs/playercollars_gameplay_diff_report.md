# PlayerCollars 玩法逻辑差异审计报告

审计时间：2026-06-10  
本地源码目录：`PlayerCollars-fabric-1.21.4`  
原版仓库：https://github.com/jlortiz0/PlayerCollars  
fork 线索：`项目位置.txt` 指向 `https://github.com/ElsaCounteragent/PlayerCollars/tree/fabric-1.21.4`

## 1. 仓库状态

- `git status --short`：未能执行。当前 shell 中 `git` 不在 PATH，且本地目录及子目录没有 `.git`。
- 当前分支：无法从本地读取。本地不是 Git 工作树。
- 工作区是否干净：无法用 Git 判定。审计开始前源码目录没有 `.git`；本轮只新增了临时分析目录 `.analysis_tmp/` 和报告文件 `docs/playercollars_gameplay_diff_report.md`，没有修改玩法代码。
- remotes：无法检查或添加，因为本地不是 Git 工作树。
- 原版 fetch：无法通过 Git fetch。替代方式是下载 GitHub 原版 `fabric-1.21.4` 源码 zip 到 `.analysis_tmp/original/`。
- 原版对比基准 commit：`jlortiz0/PlayerCollars fabric-1.21.4`，`92450a287f03a5d47acbf96706cf70aa943f56bf`，GitHub API 返回的提交信息为 `Merge pull request #110 from ScoreUnder/fix/owner-attack-gamerule`。
- fork 参考基准：`ElsaCounteragent/PlayerCollars fabric-1.21.4`，GitHub API 返回 `419e2ecffefbe9433d0c82c40519e41fea621085`，提交信息 `Added a bunch of new stuff yay`。外层本地 zip `PlayerCollars-fabric-1.21.4.zip` 与该 fork 内容语义一致，用作 fork 改版参考。
- 当前 HEAD commit：无法确定。当前本地源码是 fork 基础上的 Minecraft 26.1.2 / Fabric / Trinkets 迁移快照，不可假设等于任一远程分支。
- 对比准确性限制：没有 Git 三点 diff；报告使用“原版源码 zip vs fork zip vs 当前本地源码”的文件哈希、关键词搜索和逐文件阅读。因为当前本地已经做过 26.1.2 API 迁移，部分差异属于 API 适配而非玩法设计。

## 2. 结论摘要

- 原版行为：Foot Paws 穿戴后在 `PlayerEntityMixin#forceCrawl` 中强制将站立/潜行姿态改为 `SWIMMING`，即 always crawl。
- fork 改版行为：Foot Paws 的 always crawl 被注释/移除，改成读取 collar 上的 `forced_crawl_component`。Paw Configurator 新增 shift 使用切换 forced crawl；如果 owner 副手拿食物则切换 diet control。
- 当前本地行为：当前代码仍保留 fork 的 forced crawl 设计，并已把该路径迁到 `EquippedTrinkets` / Trinkets。单独穿 Foot Paws 不会触发爬行，这是 fork 设计，不是本地迁移独有 bug。
- 当前疑似移植失效：仍直接调用 `AccessoriesCapability` 的逻辑现在大多无效，因为当前 `io.wispforest.accessories.api.AccessoriesCapability#get()` 是本地占位 shim，固定返回 `null`。受影响包括 Dog Bowl 的“宠物不能填碗”、Invisible Fence 的 collar 检测、Collar Locker、Spatula 的饰品卸除等。
- 当前明确适配断点：手爪挖掘速度逻辑在当前 `PlayerEntityMixin#getBlockBreakingSpeed` 中被 TODO 替换为固定 `1.0f`，没有保留原版或 fork 的真实挖掘速度。
- 需要设计选择：Foot Paws 要恢复原版 always crawl，还是保留 fork 的 Paw Control forceCrawl；手爪的 mining speed/cumbersome 要恢复、保留 fork 删除，还是做成配置；Diet Control 是否应继续绑定在 collar DataComponent 上。

## 3. 玩法差异总表

| 玩法模块 | 原版行为 | 当前本地行为 | 差异类型 | 涉及文件与方法 | 风险 | 建议决策 |
|---|---|---|---|---|---|---|
| Foot Paws always crawl | 穿 Foot Paws 后 `updatePose` redirect 将 `STANDING/CROUCHING` 改为 `SWIMMING` | 不检查 Foot Paws，改查 collar 的 `FORCED_CRAWL_COMPONENT_TYPE` | 删除 + 迁移 | 原版 `mixin/PlayerEntityMixin.java:84-91`；当前 `mixin/PlayerEntityMixin.java:71-85` | 高 | 需要确认：恢复原版或保留 fork |
| Paw Control forceCrawl | 原版无该开关，只打开手爪配置 GUI | shift 使用 Paw Configurator 切换 collar DataComponent；mixin 读取该组件 | 新增 | 当前 `item/PawSetupItem.java:49-61`；`PlayerCollarsMod.java:130-133`；`PlayerEntityMixin.java:75-80` | 中 | 保留 fork 或做配置 |
| Diet Control | 原版无 | Paw Configurator 在 owner 副手持食物时切换；UseItemCallback 阻止佩戴者直接吃食物 | 新增 | 当前 `PawSetupItem.java:51-55`；`PlayerCollarsMod.java:134-137,398-409` | 中 | 需要玩法确认 |
| 手爪持物限制 | 按 paws stack 的 `held_items_component` 白名单决定是否掉落主手/副手物品 | 保留，已迁移到 Trinkets `EquippedTrinkets` | API 适配变化 | 原版 `PlayerEntityMixin.java:68-81`；当前 `PlayerEntityMixin.java:57-68`；`PawsItem.java:40-48` | 中 | 测试迁移后是否生效 |
| 手爪交互限制 | 客户端拦截 block use / break，允许 `paws_allow_interact` tag 和配置白名单 | 保留，已迁移到 `MultiPlayerGameMode` + `EquippedTrinkets` | API 适配变化 | 当前 `ClientPlayerInteractionManagerMixin.java:30-50`；`PawsItem.java:29-37`；`data/playercollars/tags/block/paws_allow_interact.json` | 中 | 测试客户端拦截 |
| 手爪挖掘速度 | 原版穿 paws 后 shovel mineable 用 iron speed，其它速度 `(ret - 1) * 0.125 + 1` | 当前 TODO，固定返回 `1.0f` | 疑似移植断点 | 原版 `PlayerEntityMixin.java:42-51`；当前 `PlayerEntityMixin.java:39-42` | 高 | 需要确认是否恢复 |
| 手爪攻击伤害 | 穿 paws 后攻击属性缩放 `(ret - 1) * 0.75 + 1` | 保留，已迁移到 `EquippedTrinkets` | API 适配变化 | 当前 `PlayerEntityMixin.java:45-49` | 中 | 测试 |
| Paw Config block/item GUI | 原版配置 paws 的 block interact 白名单与 held item 白名单 | 保留，已迁移到 26.1.2 menu API 和 Trinkets | API 适配变化 | 当前 `PacketOpenPawsConfig.java:38-83`；`PawsConfigScreenHandler.java:139-192` | 中 | 测试 GUI 和保存 |
| Owner / leash | owner collar 控制 lead 拉拽、owner 攻击规则、knot break 限制 | 大体保留，部分从 Accessories 改为 Trinkets | API 适配变化 | 当前 `PlayerCollarsMod.java:261-293,372-395`；`leash/mixin/MixinServerPlayerEntity.java:200` | 中 | 测试 leash |
| Clicker | owner 使用 clicker，使附近 owned players 看向 owner | 保留，检测改为 `EquippedTrinkets.findOwned` | API 适配变化 | 当前 `item/ClickerItem.java:53-61`；`network/PacketLookAtLerped.java` | 中 | 测试 packet |
| Laser Pointer | 原版无 | 新物品，使 owned players 看向准星命中的方块位置，带 `laser_reach` 附魔 | 新增 | 当前 `item/LaserPointerItem.java:40-70`；`data/playercollars/enchantment/laser_reach.json` | 中 | 保留 fork，测试距离公式 |
| Grooming Brush | 原版无 | 右键玩家发送心形粒子和声音，无 owner 校验 | 新增 | 当前 `item/GroomingBrushItem.java:20-33` | 低 | 需要确认是否应校验 owner |
| Hand Feeding | 原版无 | owner 手持食物右键 pet 可喂食、扣食物、播放粒子和声音 | 新增 | 当前 `PlayerCollarsMod.java:337-369` | 中 | 保留 fork，测试饱食度和 cooldown |
| Dog Bowl | 原版碗可存食物/牛奶，玩家可空手吃/喝 | fork/当前增加“佩戴 collar 的 pet 不能填碗”，但当前仍走 `AccessoriesCapability`，实际失效 | 新增 + 疑似移植断点 | 当前 `block/DogBowlBlock.java:103-109`；shim `AccessoriesCapability.java:14-20` | 高 | 修迁移或确认设计 |
| Invisible Fence | 原版 powered 时阻挡 collared living，collared player 可切换，显示红石粒子 | fork 增加对 pet 隐藏粒子、collared player 不能破坏；当前仍走 `AccessoriesCapability`，实际失效 | 新增 + 疑似移植断点 | 当前 `block/InvisibleFenceBlock.java:97-158`；shim `AccessoriesCapability.java:14-20` | 高 | 修迁移 |
| Collar Locker / Spatula | 原版通过 Accessories slot 修改/卸除饰品 | 当前仍走 `AccessoriesCapability` shim，可能完全不工作 | 疑似移植断点 | 当前 `item/CollarLockerItem.java:38-60`；`item/SpatulaItem.java:57-64` | 高 | 修迁移 |
| 附魔 | 原版 clicker/regeneration/short_leash/thorns | 当前新增 `laser_reach`，并加入 enchanting table tag | 新增 | `data/playercollars/enchantment/laser_reach.json`；`data/minecraft/tags/enchantment/in_enchanting_table.json` | 低 | 保留 fork |

## 4. 脚爪强制爬行专项分析

### 原版实现入口

原版 `src/main/java/org/jlortiz/playercollars/mixin/PlayerEntityMixin.java`：

- `@Redirect(method="updatePose", target="PlayerEntity#setPose")`
- 当玩家不是飞行，且目标姿态是 `CROUCHING` 或 `STANDING` 时：
- 通过 `AccessoriesCapability.get(this)` 查找 `PlayerCollarsMod.FOOT_PAWS_TAG`
- 只要 Foot Paws 已穿戴，就将 `entityPose = EntityPose.SWIMMING`
- 最后调用 `instance.setPose(entityPose)`

结论：原版确实是“穿 Foot Paws 即强制爬行”。

### fork 实现入口

fork zip 中同一方法已经改为：

- 注释掉原版 Foot Paws 检查。
- 改为遍历 collar：`cap.getEquipped(COLLAR_TAG)`。
- 如果 collar stack 上 `FORCED_CRAWL_COMPONENT_TYPE` 为 true，才将姿态改为 `SWIMMING`。

fork 的 `PawSetupItem` 新增：

- shift 使用 Paw Configurator。
- 如果 owner 副手持 food，则切换 `DIET_CONTROL_COMPONENT_TYPE`。
- 否则切换 `FORCED_CRAWL_COMPONENT_TYPE`。

结论：fork 明确移除了 Foot Paws always crawl，改成 Paw Control 可配置开关。

### 当前本地实现入口

当前本地仍是 fork 设计，但 API 迁到 Trinkets：

- `PlayerCollarsMod.java:130-133` 注册 `forced_crawl_component` 为 persistent DataComponent。
- `PawSetupItem.java:41` 通过 `EquippedTrinkets.findOwned` 找 owner 拥有的 collar。
- `PawSetupItem.java:49-61` shift 使用时写入 collar stack 的 `FORCED_CRAWL_COMPONENT_TYPE`。
- `PlayerEntityMixin.java:71-85` redirect `updatePlayerPose`，遍历当前玩家的 equipped collar，读取 `FORCED_CRAWL_COMPONENT_TYPE`，为 true 时改成 `Pose.SWIMMING`。

### 当前本地为什么不会“穿 Foot Paws 自动爬行”

能确认的原因：

- 当前本地 `PlayerEntityMixin#forceCrawl` 完全不检查 `FOOT_PAWS_TAG`。
- Foot Paws item 本身只设置 Trinkets 可装备槽位和染色组件，`FootPawsItem.java:23-30` 没有任何姿态逻辑。
- 因此“只穿 Foot Paws 不自动爬行”符合 fork 设计。

目前不能完全确认的部分：

- 如果你已经用 Paw Configurator 打开了 forced crawl，理论代码路径是闭环的：PawSetupItem 写 collar DataComponent，PlayerEntityMixin 读同一个 collar DataComponent。
- 未完成运行时 compile / game test 验证。`gradlew.bat compileJava` 因 Gradle wrapper 下载被网络沙箱拦截，没有继续。
- 仍需运行时确认 `@Redirect(method="updatePlayerPose", target="Player#setPose")` 在 Minecraft 26.1.2 中是否稳定命中，以及 Trinkets attachment 返回的是可持久修改的实际 stack 引用。

### 以后要修的路线，不在本轮实现

1. 恢复原版“穿 Foot Paws 即强制爬行”  
   在当前 `forceCrawl` 中重新检查 `FOOT_PAWS_TAG`，只要穿脚爪就设置 `Pose.SWIMMING`。优点是符合 jlortiz0 原版；缺点是违背 fork “wear paws but still play normally”的设计。

2. 保留 fork 设计，让 Paw Control 的 forceCrawl 生效  
   保持 Foot Paws 不自动爬行，重点测试并修复 forced crawl 写入/读取/姿态 redirect。优点是符合 fork 总结；缺点是玩家需要明确切换。

3. 做成配置项或 gamerule-like 选项  
   例如全局配置：`footPawsAlwaysCrawl`、`pawControlCanForceCrawl`。优点是兼容原版和 fork；缺点是实现和 UI/文档成本更高。

## 5. Paw Control 专项分析

### 原版 Paw Control 选项

原版 `PawSetupItem` 只有 GUI 打开逻辑：

- 必须是 owner。
- 打开 `PawsSelectScreen`。
- `PawsSelectScreen` 提供两个入口：block interact 配置、held item 配置。
- `PawsConfigScreenHandler` 把配置保存到 paws stack：
  - `CAN_INTERACT_COMPONENT_TYPE`：允许交互/破坏的 block tag 或 block key。
  - `HELD_ITEMS_COMPONENT_TYPE`：允许持有的 item tag 或 item key。

原版没有 forceCrawl、diet control、restrict diet。

### 当前本地 Paw Control 选项

当前本地包含四类 Paw Control 相关状态：

- `CAN_INTERACT_COMPONENT_TYPE`：保存到 paws item stack，用于 block use / break 白名单。
- `HELD_ITEMS_COMPONENT_TYPE`：保存到 paws item stack，用于主手/副手持物限制。
- `FORCED_CRAWL_COMPONENT_TYPE`：保存到 collar item stack，用于 forced crawl。
- `DIET_CONTROL_COMPONENT_TYPE`：保存到 collar item stack，用于限制 pet 自己吃食物。

### 数据保存与读取路径

- Block interact 白名单：
  - 写入：`PawsConfigScreenHandler.PawsBlockConfigScreenHandler#removed`，当前 `PawsConfigScreenHandler.java:159-164`。
  - 读取：`PawsItem.shouldPreventBlockInteraction`，当前 `PawsItem.java:29-37`。
  - 生效：`ClientPlayerInteractionManagerMixin.java:40-50`。

- Held item 白名单：
  - 写入：`PawsConfigScreenHandler.PawsItemConfigScreenHandler#removed`，当前 `PawsConfigScreenHandler.java:188-192`。
  - 读取：`PawsItem.shouldDrop`，当前 `PawsItem.java:40-48`。
  - 生效：`PlayerEntityMixin#dropPawItems`，当前 `PlayerEntityMixin.java:57-68`。

- forced crawl：
  - 写入：`PawSetupItem.java:58-60`。
  - 读取：`PlayerEntityMixin.java:76-80`。
  - 数据位置：collar ItemStack DataComponent，persistent `forced_crawl_component`。

- diet control：
  - 写入：`PawSetupItem.java:52-55`。
  - 读取/生效：`UseItemCallback`，当前 `PlayerCollarsMod.java:398-409`。
  - 数据位置：collar ItemStack DataComponent，persistent `diet_control_component`。

### GUI / item use / mixin 是否闭环

- Block/item 白名单：逻辑闭环存在，并且当前已改为 `EquippedTrinkets`，不像 Accessories shim 逻辑那样直接失效。仍需要游戏内测试保存后是否同步到穿戴的 Trinkets stack。
- forced crawl：代码闭环存在。不会因 Foot Paws 自动触发，只能由 Paw Configurator 写 collar 开关后触发。
- diet control：代码闭环存在。`UseItemCallback` 只检查玩家自己穿戴的 collar，因此限制的是 pet 自己吃食物；owner hand feeding 和 dog bowl 不走这个直接食用路径。
- 可用性问题：当前 `PawSetupItem#use` 只有 shift 自用时才调用 `interactLivingEntity`；非 shift 自用直接 PASS。对他人右键时仍应进入 `interactLivingEntity`，非 shift 打开 GUI，shift 切换 forced/diet。

## 6. 其他玩法差异

### owner / leash

- owner 数据仍在 collar DataComponent：`OWNER_COMPONENT_TYPE`。
- 原版 `filterStacksByOwner` 接收 Accessories `SlotEntryReference`；当前大部分 owner 查询改成 `EquippedTrinkets.findOwned`。
- leash 拉拽核心 `pullPlayerTowards` 保留，当前用 `ServerPlayer#push` 和 `ClientboundSetEntityMotionPacket`，并注释说明旧 `hasImpulse` 消失。
- leash knot break 限制当前已从 Accessories 改为 `EquippedTrinkets`，见 `PlayerCollarsMod.java:273-293`。
- owner attack gamerule 逻辑保留，当前 `PlayerCollarsMod.java:372-395`。

风险：leash 主路径看起来已迁移到 Trinkets，但仍需游戏内测试 lead 交互、knot break、short leash 附魔属性。

### clicker / laser pointer

- Clicker 原版和当前行为基本一致：shift 切换 `INTANGIBLE_PROJECTILE` 作为启用状态；启用后 owner 使用 clicker，使半径内 owned players 看向 owner。
- 当前 clicker owner 检测改为 `EquippedTrinkets.findOwned`，见 `ClickerItem.java:53-61`。
- Laser Pointer 是 fork 新增：
  - `LaserPointerItem.java:40-70`。
  - 对准方块 raycast，向所有 owned players 发送 `PacketLookAtLerped(targetPos)`。
  - 使用 `laser_reach` 附魔计算距离，但当前公式是 `32.0 * (4.0 + reachLevel)`，注释写“Base 32 blocks, +32 per level”，代码实际 base 为 128 blocks，level 1 为 160 blocks。这里是行为/注释不一致风险。

### grooming brush

- fork 新增 `GroomingBrushItem.java`。
- 右键任意玩家会播放心形粒子和声音。
- 当前没有 owner/collar 校验，不会改变饥饿、owner、leash 或 crawl 状态。

### hand feeding

- fork 新增，当前 `PlayerCollarsMod.java:337-369`。
- owner 手持 food 右键 owned pet 时：
  - pet 饥饿或 food 可 always eat 时，直接 `pet.getFoodData().eat(food)`。
  - owner 食物 cooldown 10 tick。
  - 播放吃东西声音和 heart 粒子。
  - 非创造扣 1 个食物。

风险：只校验 owner 拥有 collar，不要求 diet control 开启；这符合“hand feeding 新玩法”，但需要设计确认。

### diet control

- fork 新增。
- 开关保存在 collar DataComponent：`DIET_CONTROL_COMPONENT_TYPE`。
- 当前 `UseItemCallback` 检查玩家手中的 food；如果玩家穿戴的任一 collar 上 diet control 为 true，则 `InteractionResult.FAIL`。
- 该逻辑限制的是被 collar 约束的玩家自己吃东西，不限制 owner 喂食，也不限制从 dog bowl 空手吃。

### dog bowl

- 原版 dog bowl：可用食物/牛奶填充，空手取食/喝奶，BlockEntity 保存 `inBowl`。
- fork 新增：如果玩家穿 collar，则不能给碗放食物，提示 “Only your owner can feed you”。
- 当前本地仍在 `DogBowlBlock.java:104-105` 使用 `AccessoriesCapability.get(player)`；但当前 `AccessoriesCapability#get()` 固定返回 `null`。因此“pet 不能填碗”在当前本地疑似失效。
- dog bowl 的吃/喝主逻辑本身已迁移到 26.1.2 API，见 `DogBowlBlock.java:139-167`。

### invisible fence / invisible gate

- 代码中只看到 `InvisibleFenceBlock`，没有发现独立 `InvisibleGate` 类。
- 原版：
  - powered 状态下，collared entity 会被 collision 阻挡。
  - collared player 可以切换 powered 状态。
  - powered 时随机显示 redstone dust 粒子。
- fork 新增：
  - 如果本地 client player 是 pet，则不显示 redstone 粒子。
  - collared player 不能破坏 invisible fence。
- 当前本地仍直接用 `AccessoriesCapability` 判断 collar，见 `InvisibleFenceBlock.java:100-104,122-123,135-137,151-154`。由于 shim 返回 null，这些 collar 相关行为疑似全部失效。

### paw item 交互限制

- 当前仍有 tag 白名单：`src/main/resources/data/playercollars/tags/block/paws_allow_interact.json`。
- 默认允许：
  - `#minecraft:buttons`
  - `minecraft:lever`
  - `#minecraft:beds`
  - `#minecraft:cauldrons`
  - `#minecraft:crops`
  - `#minecraft:geode_invalid_blocks`
- 自定义配置保存在 paws stack DataComponent。
- 这些限制当前依赖 `EquippedTrinkets`，迁移入口比仍用 Accessories 的模块更可信。

### 附魔相关变化

- 原版已有：
  - `regeneration`
  - `short_leash`
  - `thorns`
  - `clicker`
- 当前新增：
  - `laser_reach`
  - 已加入 `data/minecraft/tags/enchantment/in_enchanting_table.json`
- `regeneration` 当前使用 `EquippedTrinkets` 查 owner 的 collar，属于已迁移路径。

### tag / data pack 变化

- 原版没有 Trinkets data。
- 当前新增：
  - `data/trinkets/entities/playercollars.json`
  - `data/trinkets/tags/item/chest/necklace.json`
  - `data/trinkets/tags/item/feet/shoes.json`
  - `data/trinkets/tags/item/hand/glove.json`
  - `data/trinkets/tags/item/offhand/glove.json`
- 当前 Foot Paws 被放入 `feet/shoes`，手爪被放入 `hand/glove` 与 `offhand/glove`。

## 7. 当前迁移断点清单

高风险：

- `AccessoriesCapability#get()` 是 no-op shim，固定返回 null。任何仍调用它的玩法检测都不会识别 Trinkets 穿戴。
- `DogBowlBlock` 的 pet 填碗限制疑似失效。
- `InvisibleFenceBlock` 的 collar collision、toggle permission、pet 隐藏粒子、collared player 不能破坏疑似失效。
- `CollarLockerItem` 和 `SpatulaItem` 仍走 Accessories slot，疑似无法操作 Trinkets 饰品。
- `PlayerEntityMixin#getBlockBreakingSpeed` 固定返回 `1.0f`，手爪挖掘速度逻辑未恢复。

中风险：

- forced crawl 逻辑已迁到 Trinkets，但未运行时验证 `updatePlayerPose` redirect。
- Paw Config GUI 保存的是穿戴 ItemStack 引用，需要游戏内确认 Trinkets stack 修改持久化。
- Laser Pointer 距离公式和注释不一致。
- 新增物品没有出现在当前 creative tab display list 中，影响测试/获取，但不影响注册。

低风险：

- Grooming Brush 没有 owner 校验，可能是设计选择。
- Reward Treat Pouch 没有看到配方/creative tab 暴露，功能本身注册并有 item 类。

## 8. 下一步建议

P0：先决定 Foot Paws 设计方向：恢复原版 always crawl，还是保留 fork 的 Paw Control forced crawl。

P0：如果保留 fork，优先游戏内验证 Paw Configurator shift 切换 forced crawl 后，collar DataComponent 是否保存，以及 `PlayerEntityMixin#forceCrawl` 是否实际触发。

P0：把仍走 `AccessoriesCapability` 的玩法入口迁到 `EquippedTrinkets` 或真正的 Trinkets API，优先 Dog Bowl、Invisible Fence、Collar Locker、Spatula。

P1：确认是否恢复手爪挖掘速度逻辑，或者按 fork 设计继续删除 cumbersome/mining speed 限制。

P1：测试手爪 block/item 白名单 GUI：保存、退出重进、穿戴后拦截、允许 tag。

P2：测试 owner/leash/clicker/laser/hand feeding/diet control 的完整多人服务端流程。

P2：修正或确认 Laser Pointer reach 公式，是 base 32 + 32/level，还是当前 128 + 32/level。

P3：决定 Grooming Brush、Reward Treat Pouch 是否需要 owner 校验、配方、creative tab 暴露。

## 9. 本轮未做事项

- 未修改任何玩法代码。
- 未恢复原版行为。
- 未删除 fork 新增功能。
- 未提交 commit。
- 未完成 `gradlew.bat compileJava`：Gradle wrapper 需要下载 `gradle-9.4.0-bin.zip`，被网络沙箱拦截。没有为了构建通过做任何代码修改。
