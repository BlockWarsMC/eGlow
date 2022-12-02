package me.MrGraycat.eGlow.Addon.BlockWars;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import me.MrGraycat.eGlow.Addon.BlockWars.BlockWarsAddon;
import me.MrGraycat.eGlow.Addon.BlockWars.listeners.PlayerEvents;
import me.MrGraycat.eGlow.API.Enum.EGlowColor;
import me.MrGraycat.eGlow.EGlow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.time.Duration;
import java.util.UUID;

public class Utils {

    static String prefix = "[&dtColor&r] ";
    public static ClassInfo<Player> playerClassInfo = Classes.getExactClassInfo(Player.class);
    public static ClassInfo<Boolean> boolClassInfo = Classes.getExactClassInfo(Boolean.class);

    public static String t(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static Boolean send(CommandSender p, String msg) {
        p.sendMessage(t(msg));
        return true;
    }

    public static void sendP(CommandSender p, String msg) {
        p.sendMessage(t(prefix + msg));
    }

    public static void log(String msg) {
        String sMsg = t(prefix + msg);
        Bukkit.getConsoleSender().sendMessage(sMsg);
    }

    public static void logRaw(String msg) {
        Bukkit.getConsoleSender().sendMessage(t(msg));
    }

    public static Object skriptVariable(String name) {
        return Variables.getVariable(name, null, false);
    }

    public static String skriptString(String name) {
        Object var = skriptVariable(name);
        if (var == null) return null;
        return (String) var;
    }
    public static String team(Player p) {
        return skriptString("team." + p.getName());
    }

    public static void enableGlow(Player p) {
        EGlow.getAPI().enableGlow(EGlow.getAPI().getEGlowPlayer(p), EGlowColor.WHITE);
    }
    public static void disableGlow(Player p) {
        EGlow.getAPI().disableGlow(EGlow.getAPI().getEGlowPlayer(p));
    }

    public static void setNametagStateForPlayer(Player tp, Team.OptionStatus status) {
        Scoreboard sb = tp.getScoreboard();
        for (String t : PlayerEvents.teams) {
            Team team = sb.getTeam(t);
            if (team == null) continue;
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, status);
        }
    }

}
