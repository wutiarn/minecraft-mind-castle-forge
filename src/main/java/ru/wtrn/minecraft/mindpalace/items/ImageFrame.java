package ru.wtrn.minecraft.mindpalace.items;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ImageFrame extends HangingEntity {
    public ImageFrame(EntityType<ImageFrame> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public void dropItem(@Nullable Entity pBrokenEntity) {

    }

    @Override
    public void playPlacementSound() {

    }

    @Override
    public boolean survives() {
        return true;
    }
}
