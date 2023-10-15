package ru.wtrn.minecraft.mindpalace.http.model;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs.MCI_SERVER_URL;

public class MciImageMetadata {
    public long id;
    public String url;
    public long updatedAt;
    public String description;

    public MutableComponent toChatInfo() {
        String timestamp = Instant.ofEpochSecond(updatedAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME);
        return Component.literal(
                "---"+ "\n" +
                        description + "\n" +
                        "Timestamp: " + timestamp + "\n" +
                        "ID #" + id + "\n"
                )
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
    }
}
