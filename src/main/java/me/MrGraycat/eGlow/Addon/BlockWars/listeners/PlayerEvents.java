package me.MrGraycat.eGlow.Addon.BlockWars.listeners;

import me.MrGraycat.eGlow.API.EGlowAPI;
import me.MrGraycat.eGlow.API.Enum.EGlowColor;
import me.MrGraycat.eGlow.Addon.BlockWars.BlockWarsAddon;
import me.MrGraycat.eGlow.Addon.BlockWars.Utils;
import me.MrGraycat.eGlow.EGlow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.Objects;

public class PlayerEvents implements Listener {

    private final EGlow plugin;
    private static EGlowAPI api;

    public static final String[] teams = { "admin", "staff", "spectator", "red", "orange", "yellow", "lime", "aqua", "dark_blue", "pink", "purple" };
    public PlayerEvents(EGlow plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        api = EGlow.getAPI();
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        //initTeams(e.getPlayer());
        new BukkitRunnable(){
            @Override
            public void run() {
                String pTeam = Utils.skriptString("team." + e.getPlayer().getName());

                Bukkit.getOnlinePlayers().forEach(tp -> {
                    addToTeam(pTeam, e.getPlayer(), tp);

                    String tpTeam = Utils.skriptString("team." + tp.getName());
                    addToTeam(tpTeam, tp, e.getPlayer());
                });
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void leave(PlayerQuitEvent e) {
        Bukkit.getOnlinePlayers().forEach(tp -> removeFromTeam(e.getPlayer(), tp));
        api.disableGlow(api.getEGlowPlayer(e.getPlayer()));
    }

    @EventHandler
    public void command(PlayerCommandPreprocessEvent e) {
        // /setteam red AsoDesu_
        // 0        1   2
        new BukkitRunnable(){
            @Override
            public void run() {
                if (e.getMessage().contains("/setteam")) {
                    String[] args = e.getMessage().split(" ");
                    Player p = Bukkit.getPlayer(args[2]);
                    if (p == null) return;
                    boolean isRealTeam = Arrays.stream(teams).anyMatch(t -> t.equalsIgnoreCase(args[1]));

                    Bukkit.getOnlinePlayers().forEach(tp -> {
                        removeFromTeam(p, tp);
                        if (isRealTeam) addToTeam(args[1], p, tp);
                    });
                }
            }
        }.runTask(plugin);
    }

    private static Team initTeam(String teamname, Player tp) {
        ChatColor colour = getGlowColour(teamname);
        Team team;
        team = tp.getScoreboard().getTeam(teamname);
        if (team == null) team = tp.getScoreboard().registerNewTeam(teamname);
        team.setColor(colour);
        team.setPrefix(getChatPrefix(teamname));
        return team;
    }

    private static void addToTeam(String teamname, Player p,  Player tp) {
        Team team = tp.getScoreboard().getTeam(teamname);
        if (team == null) team = initTeam(teamname, tp);

        team.addEntry(p.getName());

        if (sameTeam(p, tp)) {
            api.addCustomGlowReceiver(api.getEGlowPlayer(p), tp);
            api.addCustomGlowReceiver(api.getEGlowPlayer(tp), p);
        }
    }
    private static void removeFromTeam(Player p,  Player tp) {
        Scoreboard sb = tp.getScoreboard();
        Team team = sb.getEntryTeam(p.getName());
        if (team == null) return;
        team.removeEntry(p.getName());

        api.removeCustomGlowReceiver(api.getEGlowPlayer(p), tp);
        api.removeCustomGlowReceiver(api.getEGlowPlayer(tp), p);
    }

    private static ChatColor getGlowColour(String team) {
        ChatColor colour;
        String tGlow = Utils.skriptString("color::" + team);
        try {
            assert tGlow != null;
            colour = ChatColor.getByChar(tGlow.replaceAll("§", "")); }
        catch (Exception ignored) { colour = ChatColor.DARK_GRAY; }
        return colour;
    }

    private static String getChatPrefix(String team) {
        String prefix = Utils.skriptString("prefix::" + team);
        if (prefix == null) prefix = "§7X";
        return "§f" + prefix.stripTrailing() + " ";
    }

    private static boolean sameTeam(Player p, Player tp) {
        return Objects.equals(Utils.skriptString("team." + p.getName()), Utils.skriptString("team." + tp.getName()));
    }

}