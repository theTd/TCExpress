package org.totemcraft.bukkitplugin.TCExpress;

import org.bukkit.block.BlockFace;
import org.bukkit.material.Rails;

public enum RailType {
    Z_FLAT, X_FLAT, Z_SLOPE, X_SLOPE, X_Z_CURVE, X_NZ_CURVE, NX_Z_CURVE, NX_NZ_CURVE;

    public static RailType get(Rails rail) {
        if (rail.getDirection() == BlockFace.SOUTH) {
            return rail.isOnSlope() ? Z_SLOPE : Z_FLAT;
        } else if (rail.getDirection() == BlockFace.EAST) {
            return rail.isOnSlope() ? X_SLOPE : X_FLAT;
        }
        switch (rail.getDirection()) {
            case NORTH_EAST:
                return X_Z_CURVE;
            case NORTH_WEST:
                return X_NZ_CURVE;
            case SOUTH_EAST:
                return NX_Z_CURVE;
            case SOUTH_WEST:
                return NX_NZ_CURVE;
        }
        return null;
    }
}
