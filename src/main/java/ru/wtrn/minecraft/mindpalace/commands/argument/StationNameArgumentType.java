package ru.wtrn.minecraft.mindpalace.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class StationNameArgumentType implements ArgumentType<String> {
    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(getAvailableStations(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return getAvailableStations();
    }

    private Collection<String> getAvailableStations() {
        return RoutingService.INSTANCE.getStations().keySet();
    }
}
