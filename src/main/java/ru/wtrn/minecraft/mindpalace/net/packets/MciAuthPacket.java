package ru.wtrn.minecraft.mindpalace.net.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.wtrn.minecraft.mindpalace.commands.argument.StationNameArgumentType;
import ru.wtrn.minecraft.mindpalace.config.ModClientConfigs;
import ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs;
import ru.wtrn.minecraft.mindpalace.events.EventsHandler;
import ru.wtrn.minecraft.mindpalace.http.PlayerMemosTokensHolder;
import ru.wtrn.minecraft.mindpalace.net.ModPacketHandler;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public class MciAuthPacket {
    private static final Logger logger = LoggerFactory.getLogger(MciAuthPacket.class);
    public String memosToken;

    public MciAuthPacket(String memosToken) {
        this.memosToken = memosToken;
    }

    public static void reportMciAuthToServer() {
        String memosToken = ModClientConfigs.MCI_MEMOS_TOKEN.get();
        MciAuthPacket packet = new MciAuthPacket(memosToken);
        ModPacketHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), packet);
    }

    public static void handle(MciAuthPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        logger.info("Received mci memos token for player " + player.getName().getString());
        PlayerMemosTokensHolder.INSTANCE.setToken(player.getUUID(), packet.memosToken);
    }

    public static void encode(MciAuthPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.memosToken.length());
        buf.writeCharSequence(packet.memosToken, StandardCharsets.UTF_8);
    }

    public static MciAuthPacket decode(FriendlyByteBuf buf) {
        int memosTokenLength = buf.readInt();
        String memosToken = buf.readCharSequence(memosTokenLength, StandardCharsets.UTF_8).toString();
        return new MciAuthPacket(memosToken);
    }
}
