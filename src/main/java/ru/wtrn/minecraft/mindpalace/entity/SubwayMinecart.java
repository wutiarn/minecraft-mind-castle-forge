package ru.wtrn.minecraft.mindpalace.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;

public class SubwayMinecart extends Minecart {
    public SubwayMinecart(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public SubwayMinecart(Level pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ);
    }
}
