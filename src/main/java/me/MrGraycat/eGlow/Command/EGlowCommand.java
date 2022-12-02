package me.MrGraycat.eGlow.Command;

import com.google.common.base.Strings;
import me.MrGraycat.eGlow.API.EGlowAPI;
import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Event.EGlowEventListener;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EGlowCommand implements CommandExecutor, TabExecutor {
	private final ArrayList<String> colors = new ArrayList<>(Arrays.asList("red","darkred", "gold", "yellow", "green", "darkgreen", "aqua", "darkaqua", "blue", "darkblue", "purple", "pink", "white", "gray", "darkgray", "black", "none"));
	
	/**
	 * Register the subcommands & command alias if enabled
	 */
	public EGlowCommand() {
		try{
		    final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
		    String alias = MainConfig.COMMAND_ALIAS.getString();
		    
		    if (MainConfig.COMMAND_ALIAS_ENABLE.getBoolean() && alias != null) {
		    	 commandMapField.setAccessible(true);
				 CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
				 commandMap.register(alias, alias, Objects.requireNonNull(EGlow.getInstance().getCommand("eglow"), "Unable to retrieve eGlow command to register alias"));
		    }
		} catch (NoSuchFieldException  | IllegalArgumentException | IllegalAccessException e){
		    ChatUtil.reportError(e);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("eglow.debug")) return true;
		if (!(sender instanceof Player p)) return true;
		IEGlowPlayer eGlowPlayer = EGlow.getAPI().getEGlowPlayer(p);

		sender.sendMessage("§r  ");
		for (Field field : IEGlowPlayer.class.getDeclaredFields()) {
			field.setAccessible(true);
			try {
				sender.sendMessage("§b" + field.getName() + "§f - §6" + field.get(eGlowPlayer));
			} catch (IllegalAccessException e) {}
		}

		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return new ArrayList<>();
	}
}