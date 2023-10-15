package ru.wtrn.minecraft.mindpalace.commands.argument;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

public class StationNameArgumentSerializer implements ArgumentTypeInfo<StationNameArgumentType, StationNameArgumentSerializer.Template> {
   public void serializeToNetwork(Template pTemplate, FriendlyByteBuf pBuffer) {
   }

   public Template deserializeFromNetwork(FriendlyByteBuf pBuffer) {
      return new Template();
   }

   public void serializeToJson(Template pTemplate, JsonObject pJson) {

   }

   public Template unpack(StationNameArgumentType pArgument) {
      return new Template();
   }

   public final class Template implements ArgumentTypeInfo.Template<StationNameArgumentType> {

      public Template() {

      }

      public StationNameArgumentType instantiate(CommandBuildContext pContext) {
         return new StationNameArgumentType();
      }

      public ArgumentTypeInfo<StationNameArgumentType, ?> type() {
         return StationNameArgumentSerializer.this;
      }
   }
}