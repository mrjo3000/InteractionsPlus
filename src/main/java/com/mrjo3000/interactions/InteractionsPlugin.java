package com.mrjo3000.interactions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.inventory.InventoryType;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class InteractionsPlugin extends JavaPlugin implements Listener {

    private static final Set<Material> SIMULATED_BLOCKS = new HashSet<Material>();
    static {
        SIMULATED_BLOCKS.add(Material.LOOM);
        SIMULATED_BLOCKS.add(Material.SMITHING_TABLE);
        SIMULATED_BLOCKS.add(Material.STONECUTTER);
        SIMULATED_BLOCKS.add(Material.GRINDSTONE);
        SIMULATED_BLOCKS.add(Material.CARTOGRAPHY_TABLE);
        SIMULATED_BLOCKS.add(Material.FLETCHING_TABLE);
        SIMULATED_BLOCKS.add(Material.BLAST_FURNACE);
        SIMULATED_BLOCKS.add(Material.SMOKER);
        SIMULATED_BLOCKS.add(Material.BARREL);
        SIMULATED_BLOCKS.add(Material.COMPOSTER);
        SIMULATED_BLOCKS.add(Material.CRAFTING_TABLE);
    }

    private final Map<UUID, SimulatedSession> openInventories = new HashMap<UUID, SimulatedSession>();
    private boolean viaVersionSupported = false;
    private Object viaApi = null;
    private Method getPlayerVersionMethod = null;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        setupViaVersionSupport();
    }

    private void setupViaVersionSupport() {
        try {
            Class<?> viaClass = Class.forName("com.viaversion.viaversion.api.Via");
            Method getApiMethod = viaClass.getMethod("getAPI");
            viaApi = getApiMethod.invoke(null);
            getPlayerVersionMethod = viaApi.getClass().getMethod("getPlayerVersion", UUID.class);
            viaVersionSupported = true;
            getLogger().info("ViaVersion support enabled for legacy client detection.");
        } catch (Throwable t) {
            viaVersionSupported = false;
            getLogger().info("ViaVersion not found. The plugin will simulate the UI for all players.");
        }
    }

    private boolean isLegacyClient(Player player) {
        if (!viaVersionSupported) {
            return true;
        }
        try {
            int protocolVersion = (int) getPlayerVersionMethod.invoke(viaApi, player.getUniqueId());
            return protocolVersion < 753;
        } catch (Throwable t) {
            return true;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        if (!SIMULATED_BLOCKS.contains(block.getType())) {
            return;
        }

        Player player = event.getPlayer();
        if (!isLegacyClient(player)) {
            return;
        }

        event.setCancelled(true);
        event.setUseInteractedBlock(Result.DENY);
        openSimulatedUI(player, block);
    }

    private void openSimulatedUI(Player player, Block block) {
        Inventory inventory = createInventoryForBlock(block.getType());
        if (inventory == null) {
            player.sendMessage(color("&cNo simulation available for this block yet."));
            return;
        }

        openInventories.put(player.getUniqueId(), new SimulatedSession(block, inventory.getType()));
        player.openInventory(inventory);
        player.sendMessage(color("&aSimulated UI opened for &e" + prettyName(block.getType()) + "&a."));
    }

    private Inventory createInventoryForBlock(Material type) {
        String title = color("&6Simulated " + prettyName(type) + " &7(UI for <1.16 clients)");
        Inventory inventory;

        switch (type) {
            case LOOM:
            case SMITHING_TABLE:
            case STONECUTTER:
            case GRINDSTONE:
            case CARTOGRAPHY_TABLE:
            case FLETCHING_TABLE:
            case CRAFTING_TABLE:
                inventory = Bukkit.createInventory(null, 27, title);
                fillWindow(inventory);
                addSlot(inventory, 10, Material.WHITE_STAINED_GLASS_PANE, "&fInput Slot", "Place items here.");
                addSlot(inventory, 13, Material.GREEN_STAINED_GLASS_PANE, "&aOutput Slot", "Results appear here.");
                addSlot(inventory, 16, Material.LIME_STAINED_GLASS_PANE, "&bSimulate", "Click to simulate the interface.");
                break;
            case COMPOSTER:
                inventory = Bukkit.createInventory(null, 27, title);
                fillWindow(inventory);
                addSlot(inventory, 11, Material.BROWN_STAINED_GLASS_PANE, "&6Compost Input", "Place compostable items.");
                addSlot(inventory, 15, Material.GREEN_STAINED_GLASS_PANE, "&aReady Output", "Collect compost.");
                addSlot(inventory, 22, Material.LIME_STAINED_GLASS_PANE, "&bCompost", "Click to compost and spawn particles.");
                break;
            case BARREL:
                inventory = Bukkit.createInventory(null, 54, title);
                fillWindow(inventory);
                addSlot(inventory, 20, Material.CHEST, "&eBarrel Storage", "This chest simulates the barrel inventory.");
                addSlot(inventory, 25, Material.LIME_STAINED_GLASS_PANE, "&bClose", "Click to close the simulated UI.");
                break;
            case SMOKER:
            case BLAST_FURNACE:
                inventory = Bukkit.createInventory(null, 54, title);
                fillWindow(inventory);
                addSlot(inventory, 19, Material.COOKED_BEEF, "&aInput Fuel", "Place fuel and ingredients here.");
                addSlot(inventory, 22, Material.GREEN_STAINED_GLASS_PANE, "&aResult", "The cooked item appears here.");
                addSlot(inventory, 25, Material.ORANGE_STAINED_GLASS_PANE, "&bStart", "Click to simulate cooking.");
                break;
            default:
                return null;
        }

        return inventory;
    }

    private void fillWindow(Inventory inventory) {
        ItemStack filler = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int index = 0; index < inventory.getSize(); index++) {
            inventory.setItem(index, filler);
        }
    }

    private void addSlot(Inventory inventory, int slot, Material material, String name, String lore) {
        inventory.setItem(slot, createItem(material, name, lore));
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        if (name != null) {
            meta.setDisplayName(color(name));
        }
        if (lore != null && lore.length > 0) {
            meta.setLore(Arrays.asList(color(lore[0])));
        }
        item.setItemMeta(meta);
        return item;
    }

    private String prettyName(Material type) {
        String raw = type.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
        String[] pieces = raw.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String piece : pieces) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(piece.charAt(0))).append(piece.substring(1));
        }
        return builder.toString();
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (!event.getView().getTitle().startsWith(color("&6Simulated"))) {
            return;
        }

        event.setCancelled(true);
        SimulatedSession session = openInventories.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        int slot = event.getRawSlot();
        if (slot == 16 || slot == 22 || slot == 25) {
            if (slot == 25) {
                player.closeInventory();
                return;
            }
            handleSimulationAction(player, session);
        }
    }

    private void handleSimulationAction(Player player, SimulatedSession session) {
        Block block = session.getBlock();
        Location center = block.getLocation().add(0.5, 0.5, 0.5);
        switch (block.getType()) {
            case LOOM:
            case SMITHING_TABLE:
            case STONECUTTER:
            case GRINDSTONE:
            case CARTOGRAPHY_TABLE:
            case FLETCHING_TABLE:
            case CRAFTING_TABLE:
                player.sendMessage(color("&eSimulating the &6" + prettyName(block.getType()) + " &einterface. Use the inventory grid to inspect the layout."));
                block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, center, 20, 0.2, 0.2, 0.2, 0.01);
                break;
            case COMPOSTER:
                player.sendMessage(color("&eComposting items... This is a visual simulation only."));
                block.getWorld().spawnParticle(Particle.FLAME, center, 15, 0.1, 0.4, 0.1, 0.02);
                break;
            case BARREL:
                player.sendMessage(color("&eBarrel storage simulated using a large chest interface."));
                block.getWorld().spawnParticle(Particle.ITEM_CRACK, center, 10, 0.15, 0.15, 0.15, new ItemStack(Material.CHEST));
                break;
            case SMOKER:
            case BLAST_FURNACE:
                player.sendMessage(color("&eStarting a cooking simulation... particles and chat will keep the experience alive."));
                block.getWorld().spawnParticle(Particle.SMOKE_NORMAL, center, 25, 0.2, 0.2, 0.2, 0.01);
                break;
            default:
                player.sendMessage(color("&cNo simulation action available for this block."));
                break;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        openInventories.remove(event.getPlayer().getUniqueId());
    }

    private static final class SimulatedSession {
        private final Block block;
        private final InventoryType inventoryType;

        private SimulatedSession(Block block, InventoryType inventoryType) {
            this.block = block;
            this.inventoryType = inventoryType;
        }

        public Block getBlock() {
            return block;
        }

        public InventoryType getInventoryType() {
            return inventoryType;
        }
    }
}
