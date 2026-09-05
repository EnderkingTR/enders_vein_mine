# Ender's Vein Mine Changelog

## [1.7.1] - 2026-09-04

### Changed
- Updated the `.jar` file output naming convention (now includes Minecraft version and mod loader) while keeping the in-game version display clean.

### Fixed
- Fixed an issue where the "Stairs (Up)" and "Stairs (Down)" shape mode translation keys were not displaying correctly on certain system languages (e.g., Turkish).

## [1.7.0] - 2026-09-03

### Added
- **Fabric Conventional Tags Support:** The mod now utilizes Fabric Convention tags (`c:ores`, `c:logs`, `c:dirts`, `c:leaves`, `c:stones`, vb.) and Vanilla Minecraft tags to intelligently group blocks during Vein Mining.

### Changed
- **Improved Block Detection:** Removed hardcoded block groups (like vanilla stones). The vein mining and block interaction logic now dynamically checks if blocks share common tags.
- **Enhanced Mod Compatibility:** Vein mining will now seamlessly work with custom blocks added by other mods, as long as they use standard Fabric or Minecraft block tags.

### Fixed
- Fixed potential edge cases where similar blocks from different mods were not mined together.
