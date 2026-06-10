# Player Collars

Lets players wear pet collars. See branches for possible versions. Some pre-compiled versions are available on [Modrinth](https://modrinth.com/mod/leashable-collars).

This mod requires [Trinkets](https://modrinth.com/mod/trinkets) under Fabric.

## PlayerCollars / Leashable Collars for Minecraft 26.1.2 Fabric

This pre-release is a community maintenance build of PlayerCollars / Leashable Collars for Minecraft 26.1.2 on Fabric.

This port is based on ElsaCounteragent's Fabric fork. Special thanks to Elsa for the previous fork work, which made this migration possible.

The goal of this build is to restore core playability on the modern Fabric stack, especially with Trinkets Updated, while keeping the mod stable enough for practical multiplayer testing.

### Main changes

- Updated the project to target Minecraft 26.1.2, Fabric Loader, Fabric API, and Trinkets Updated.
- Restored core collar, paws, leash, pet bowl, dog bed, grooming, and laser pointer functionality.
- Fixed compatibility issues with the current Trinkets Updated slot system.
- Fixed crashes related to leash-related enchantments and tooltip rendering.
- Fixed pet feeding behavior with pet bowls.
- Added functional mining speed reduction when wearing hand paws.
- Reworked pet item use and block interaction restrictions for better stability.
- Restored essential pet interactions such as doors, trapdoors, buttons, levers, dog beds, and leash-to-fence behavior.
- Fixed leash anchor visibility and rendering issues.
- Fixed several registration, resource, recipe, creative tab, and build-related issues.

### Notes

Although this build is based on ElsaCounteragent's fork, it should not be considered a complete recreation of every feature or configuration system from that fork.

Some complex configurable interaction features were simplified or replaced with more stable hardcoded behavior. This was done to prioritize stability, compatibility, and practical usability on Minecraft 26.1.2.

As a result, this pre-release is best treated as a playable compatibility restoration build for Minecraft 26.1.2 Fabric, rather than a full feature-complete continuation of all upstream fork additions.

### Disclaimer

This is still a very early personal maintenance project. The mod has not necessarily been fully tested in all gameplay situations, modpack environments, or multiplayer conditions, and the maintainer cannot guarantee full stability.

Players should use this build at their own risk. Before installing the mod or using it for long-term gameplay, players should back up their world and important save data.

## Usage

Recipe is
```
 L 
LIL
 D 
```

where L is leather, I is a gold ingot, and D is a dye.

When crafted, collars are red. They can be dyed similar to leather armor. Alternatively, shift right click to edit the colors manually. This menu also allows editing of the paw color (blue by default) and setting the collar's owner.

### Owner mechanics

Collars can have an "owner", the presence of which will affect the wearer of the collar. Owners can use leads to move the wearer of the collar. Some enchantments will provide additional effects.

### Enchantments

- Healing: Wearer will recieve Regeneration when within 16 blocks of the owner.
- Tight Leash: Wearer will be pulled closer to the owner when a lead is used. Default follow distance is 4 blocks, each level of Loyalty reduces this by 1 to a minimum of 2 blocks.
- Spiked: Works like Thorns, but with no durability penalty.

### Clickers

...
```
 # 
OIO
 O
```

where `#` is a button, `O` is planks, and `I` is an iron ingot.

If an owner uses a clicker, nearby owned players will be forced to look at the owner. The radius is determined by the level of Audible the clicker is enchanted with.

Clickers can be dyed in the same manner as leather armor.

## Attribution

The player leashing code was derived from [Leashable Players](https://modrinth.com/mod/leashable-players).

This mod is licensed under the MIT license. Please try not to bully your players/partner(s) too hard with this mod. That's for me only.

i really have no shame do i
