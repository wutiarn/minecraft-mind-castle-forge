package ru.wtrn.minecraft.mindpalace.http.model;

import com.google.gson.annotations.JsonAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
        String timestamp = Instant.ofEpochSecond(createdAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME);
        return Component.literal(
                "---"+ "\n" +
                        "Resolution: " + width + "x" + height + "\n" +
                        "Description: " + description + "\n" +
                        "Timestamp: " + timestamp + "\n" +
                        "ID #" + id + "\n"
                )
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, MCI_SERVER_URL.get() + "/i/" + id)));
    }
}
