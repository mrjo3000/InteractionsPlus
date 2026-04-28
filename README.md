# Interactions+

## Overview
`Interactions+` is a Minecraft plugin for Spigot/Paper servers that provides a safe and friendly simulation of newer block interfaces for players using client versions below 1.16.

Older clients cannot open some of the modern block screens, so this plugin uses chest inventories, glass panes, particles, and chat messages to give players a helpful alternative.

## Supported Blocks
- Loom
- Smithing Table
- Stonecutter
- Grindstone
- Cartography Table
- Fletching Table
- Blast Furnace
- Smoker
- Barrel
- Composter
- Crafting Table

## Features
- Detects legacy clients when ViaVersion is installed and running.
- Opens a simulated chest or large chest UI when a legacy player interacts with a supported block.
- Uses colored panes and item labels to mimic modern interface layouts.
- Sends chat messages and particle effects to explain interactions.
- Keeps the experience safe by blocking unsupported client UIs and replacing them with a readable simulation.

## Installation
1. Build the plugin with Maven:
   ```bash
   mvn clean package
   ```
2. Copy the generated `target/interactions-1.0.0.jar` into your server's `plugins/` folder.
3. Restart the server.

## Compatibility
- Server API: Spigot/Paper 1.16.x
- Legacy client support: clients older than 1.16 are simulated.
- ViaVersion support: if ViaVersion is installed, the plugin uses it to detect legacy clients automatically.

## Configuration
No configuration file is required. The plugin works with default settings immediately after installation.

## Notes
- This plugin focuses on providing a friendly visual simulation, not on reproducing every block function exactly.
- For best results, use this plugin on a 1.16+ server with ViaVersion installed for accurate client detection.

## Note:
- Updates will come soon for bug fixes.
- This isnt a perfect plugin.
- It depends on ViaVersion And ViaBackwards already installed onto the server to work.
- This update is for testing. its not the full plugin yet.

## License
This project is provided as-is and may be used on your server.
