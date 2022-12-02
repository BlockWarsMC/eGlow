package me.MrGraycat.eGlow.Addon.BlockWars;

import ch.njol.skript.lang.function.Functions;
import me.MrGraycat.eGlow.Addon.BlockWars.functions.EnableGlow;
import me.MrGraycat.eGlow.Addon.BlockWars.functions.HideTag;
import me.MrGraycat.eGlow.Addon.BlockWars.functions.ShowTag;
import me.MrGraycat.eGlow.Addon.BlockWars.listeners.PlayerEvents;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Util.DebugUtil;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import org.bukkit.Bukkit;

import static me.MrGraycat.eGlow.Util.Text.ChatUtil.translateColors;

public class BlockWarsAddon {

    public static Boolean glowing = false;

    public static void load() {
        new PlayerEvents(EGlow.getInstance());

        Functions.registerFunction(new HideTag());
        Functions.registerFunction(new ShowTag());
        Functions.registerFunction(new EnableGlow());

        EGlow.getAPI().setPacketBlockerStatus(false);
        EGlow.getAPI().setSendTeamPackets(false);

        sendToConsole("Enabled BlockWars EGlow Addon!");
    }

    public static void sendToConsole(String message) {
        Bukkit.getConsoleSender().sendMessage(translateColors("&f[&cB&6W Glowing&f] " + message));
    }

}
