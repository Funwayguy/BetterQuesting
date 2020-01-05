package betterquesting.commands.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class BooleanArgument implements ArgumentType<Boolean>
{
    public static final BooleanArgument INSTANCE = new BooleanArgument();
    
    public static boolean getValue(CommandContext<?> context, String name)
    {
        return context.getArgument(name, Boolean.class);
    }
    
    private final Collection<String> egList = Collections.unmodifiableList(Arrays.asList("true", "false"));
    
    @Override
    public Boolean parse(StringReader reader) throws CommandSyntaxException
    {
        return Boolean.parseBoolean(reader.readString());
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        return builder.suggest("true").suggest("false").buildFuture();
    }
    
    @Override
    public Collection<String> getExamples()
    {
        return egList;
    }
}
