package ru.wtrn.minecraft.mindpalace.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.level.Level;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StationNameArgumentType implements ArgumentType<String> {
    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Collection<String> availableStations = getAvailableStations(Minecraft.getInstance().level);
        return SharedSuggestionProvider.suggest(availableStations, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return getAvailableStations(Minecraft.getInstance().level);
    }

    private Collection<String> getAvailableStations(Level level) {
        return RoutingService.INSTANCE.getStations(level).keySet();
    }
}
