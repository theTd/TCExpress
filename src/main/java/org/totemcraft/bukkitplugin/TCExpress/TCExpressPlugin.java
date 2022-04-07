package org.totemcraft.bukkitplugin.TCExpress;

import com.google.common.collect.Iterables;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.material.Rails;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TCExpressPlugin extends JavaPlugin implements Listener {
    private final static int BUFFER_LENGTH = 5;
    private final static int ADJUST_LENGTH = 20;
    private final static double NORMAL_SPEED = 0.4;

    private double maxSpeed = 2;

    private final Set<String> effectiveWorlds = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadEffectiveWorlds();
        if (effectiveWorlds.isEmpty()) {
            getLogger().warning("no effective worlds defined, plugin may not work.");
        }

        maxSpeed = NORMAL_SPEED * getConfig().getDouble("mul", 2);

        getServer().getPluginManager().registerEvents(this, this);
    }

    private void loadEffectiveWorlds() {
        List<String> list = getConfig().getStringList("effective_worlds");
        if (list != null) {
            for (String w : list) {
                effectiveWorlds.add(w.toLowerCase());
            }
        }
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    void onExit(VehicleExitEvent e) {
        if (!(e.getVehicle() instanceof Minecart)) return;
        Minecart minecart = (Minecart) e.getVehicle();
        Entity t = Iterables.getFirst(minecart.getPassengers(), null);
        if (t == null || t.getType() != EntityType.PLAYER) return;
        minecart.setMaxSpeed(NORMAL_SPEED);
    }

    @EventHandler
    void onMove(VehicleMoveEvent e) {
        if (!(e.getVehicle() instanceof Minecart)) return;
        if (!effectiveWorlds.contains(e.getVehicle().getWorld().getName().toLowerCase())) return;

        Minecart minecart = ((Minecart) e.getVehicle());
        Entity passenger = Iterables.getFirst(minecart.getPassengers(), null);
        if (passenger == null || passenger.getType() != EntityType.PLAYER) return;

        Block curBlock = minecart.getLocation().getBlock();
        if (!isRail(curBlock)) {
            minecart.setMaxSpeed(NORMAL_SPEED);
            return;
        }

        Rails curRail = (Rails) curBlock.getState().getData();
        RailType curRailType = RailType.get(curRail);
        if (curRailType != RailType.X_FLAT && curRailType != RailType.Z_FLAT) {
            minecart.setMaxSpeed(NORMAL_SPEED);
            return;
        }

        Vector vector = e.getVehicle().getVelocity();
        if (vector.getY() != 0) {
            minecart.setMaxSpeed(NORMAL_SPEED);
            return;
        }
        double x = vector.getX();
        double z = vector.getZ();

        if (x == 0 && z == 0) {
            minecart.setMaxSpeed(NORMAL_SPEED);
            return;
        }

        boolean isX = x != 0 && z == 0;
        boolean n = isX ? x < 0 : z < 0;
        BlockFace direction = isX ? (n ? BlockFace.WEST : BlockFace.EAST) : (n ? BlockFace.NORTH : BlockFace.SOUTH);

        int flatLength = 0;
        while ((curBlock = nextRail(direction, curBlock)) != null && flatLength < BUFFER_LENGTH + ADJUST_LENGTH) {
            RailType railType = RailType.get((Rails) curBlock.getState().getData());
            if (isX) {
                if (railType != RailType.X_FLAT && railType != RailType.X_SLOPE) break;
            } else {
                if (railType != RailType.Z_FLAT && railType != RailType.Z_SLOPE) break;
            }

            flatLength++;
        }

        if (flatLength < BUFFER_LENGTH) {
            minecart.setMaxSpeed(NORMAL_SPEED);
            return;
        }

        int freeLength = flatLength - BUFFER_LENGTH;

        double s = (double) freeLength / ADJUST_LENGTH;
        if (s > 1) s = 1;
        double speed = NORMAL_SPEED + (maxSpeed - NORMAL_SPEED) * s;
        minecart.setMaxSpeed(speed);
    }

    private static Block nextRail(BlockFace direction, Block block) {
        Block b = block.getRelative(direction);
        return isRail(b) ? b : null;
    }

    private static boolean isRail(Block block) {
        Material mat = block.getType();
        return mat == Material.RAILS || mat == Material.ACTIVATOR_RAIL || mat == Material.DETECTOR_RAIL || mat == Material.POWERED_RAIL;
    }
}
