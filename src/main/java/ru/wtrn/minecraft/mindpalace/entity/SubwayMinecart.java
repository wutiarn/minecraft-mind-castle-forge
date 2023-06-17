package ru.wtrn.minecraft.mindpalace.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class SubwayMinecart extends AbstractMinecart {
    public SubwayMinecart(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected Item getDropItem() {
        return null;
    }

    @Override
    public Type getMinecartType() {
        return null;
    }

    public SubwayMinecart(Level pLevel, double pX, double pY, double pZ) {
        super(ModEntities.SUBWAY_MINECART_ENTITY.get(), pLevel, pX, pY, pZ);
    }
}
