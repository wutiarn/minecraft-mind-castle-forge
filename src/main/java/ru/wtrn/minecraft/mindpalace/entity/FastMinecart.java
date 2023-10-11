package ru.wtrn.minecraft.mindpalace.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;

public class FastMinecart extends Minecart {
    public FastMinecart(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public FastMinecart(Level pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ);
    }

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return false;
    }
}
