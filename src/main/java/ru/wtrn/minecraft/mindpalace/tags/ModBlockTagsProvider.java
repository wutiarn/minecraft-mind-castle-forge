package ru.wtrn.minecraft.mindpalace.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import ru.wtrn.minecraft.mindpalace.block.ModBlocks;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends TagsProvider<Block> {
    public ModBlockTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, ModBlocks.BLOCKS.getRegistryKey(), pLookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(BlockTags.RAILS).add(ModBlocks.FAST_RAIL_BLOCK.getKey());
        this.tag(BlockTags.RAILS).add(ModBlocks.ROUTING_RAIL_BLOCK.getKey());
    }
}
