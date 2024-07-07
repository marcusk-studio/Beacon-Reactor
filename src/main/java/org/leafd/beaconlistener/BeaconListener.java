package org.leafd.beaconlistener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;



public class BeaconListener extends JavaPlugin implements Listener {

    private final Set<Block> activeBeacons = new HashSet<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        startStructureCheckTask();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        checkStructure(event.getBlock());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        checkStructure(event.getBlock());
    }

    private void checkStructure(Block block) {
        Block beacon = findNearbyBeacon(block);
        if (beacon != null) {
            if (isStructureValid(beacon)) {
                activeBeacons.add(beacon);
            } else {
                activeBeacons.remove(beacon);
            }
        }
    }

    private Block findNearbyBeacon(Block block) {
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Block nearbyBlock = block.getRelative(x, y, z);
                    if (nearbyBlock.getType() == Material.BEACON) {
                        return nearbyBlock;
                    }
                }
            }
        }
        return null;
    }

    private boolean isStructureValid(Block beacon) {
        // Define the structure
        Material[][][] structure = {
                {
                        {Material.IRON_BLOCK, Material.COPPER_BLOCK, Material.IRON_BLOCK},
                        {Material.COPPER_BLOCK, Material.IRON_BLOCK, Material.COPPER_BLOCK},
                        {Material.IRON_BLOCK, Material.COPPER_BLOCK, Material.IRON_BLOCK}
                },
                {
                        {Material.COPPER_BLOCK, Material.GLASS, Material.COPPER_BLOCK},
                        {Material.GLASS, Material.BEACON, Material.GLASS},
                        {Material.COPPER_BLOCK, Material.GLASS, Material.COPPER_BLOCK}
                },
                {
                        {Material.IRON_BLOCK, Material.COPPER_BLOCK, Material.IRON_BLOCK},
                        {Material.COPPER_BLOCK, Material.IRON_BLOCK, Material.COPPER_BLOCK},
                        {Material.IRON_BLOCK, Material.COPPER_BLOCK, Material.IRON_BLOCK}
                }
        };

        Block baseBlock = beacon.getRelative(BlockFace.DOWN, 1).getRelative(BlockFace.WEST, 1).getRelative(BlockFace.NORTH, 1); // Bottom layer, bottom-left corner
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    if (baseBlock.getRelative(x, y, z).getType() != structure[y][x][z]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void startStructureCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Set<Block> beaconsToRemove = new HashSet<>();
                for (Block beacon : activeBeacons) {
                    if (!isStructureValid(beacon)) {
                        beaconsToRemove.add(beacon);
                    }
                }
                activeBeacons.removeAll(beaconsToRemove);

                for (Block beacon : activeBeacons) {
                    applyEffects(beacon);
                }
            }
        }.runTaskTimer(this, 0L, 100L);
    }

    private void applyEffects(Block beacon) {
        for (Player player : beacon.getWorld().getPlayers()) {
            if (player.getLocation().distance(beacon.getLocation()) <= 50) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 200, 0, true, true));
            }
        }
    }
}
