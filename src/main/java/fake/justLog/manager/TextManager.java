package fake.justLog.manager;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextManager {

    public static void doDebug(String str, Integer debugLevel) {
        if (ConfigManager.Debug >= debugLevel){
            Bukkit.getConsoleSender().sendMessage(translateColorCodes("["+"JustLog"+"][DEBUG "+debugLevel+"]" + " ".repeat(debugLevel) + str));
        }
//           logger.info(("[RBAP][DEBUG "+debugLevel+"] - ") + str);
    }

//    @Deprecated
//    public static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";
    private static final Pattern HEX_PATTERN = Pattern.compile("(&#[0-9a-fA-F]{6})");

    /**
     * @param text The string of text to apply color/effects to
     * @return Returns a string of text with color/effects applied
     */
    public static String translateColorCodes(String text) {
        //good thing we're stuck on java 8, which means we can't use this (:
        // String hexColored = HEX_PATTERN.matcher(text)
        //      .replaceAll(match -> "" + ChatColor.of(match.group(1)));
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1).substring(1);
            matcher.appendReplacement(sb, "" + ChatColor.of(hex));
        }
        matcher.appendTail(sb);

        String hexColored = sb.toString();

        return ChatColor.translateAlternateColorCodes('&', hexColored);
    }

    public static void sendPlayerMessage(Player player, boolean prefix, String message, String defaultMSG) {
        player.sendMessage(translateColorCodes("&6NPC&8>&f" + message));
    }
}
