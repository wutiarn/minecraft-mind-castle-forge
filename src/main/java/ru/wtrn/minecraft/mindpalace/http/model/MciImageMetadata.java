package ru.wtrn.minecraft.mindpalace.http.model;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

import static net.minecraft.network.chat.Style.FORMATTING_CODEC;
import static ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs.MCI_SERVER_URL;

public class MciImageMetadata {
    public long id;
    public long createdAt;
    public String description;
    public int width;
    public int height;
    public String mimetype;

    public MutableComponent toChatInfo() {
        return Component.literal(
                "Image #" + id + "\n" +
                        "Resolution: " + width + "x" + height + "\n" +
                        "Description: " + description
                )
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, MCI_SERVER_URL.get() + "/i/" + id)));
    }
}
