package me.MrGraycat.eGlow;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

import me.MrGraycat.eGlow.Addon.BlockWars.BlockWarsAddon;
import me.MrGraycat.eGlow.Addon.Internal.AdvancedGlowVisibilityAddon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.MrGraycat.eGlow.API.EGlowAPI;
import me.MrGraycat.eGlow.Command.EGlowCommand;
import me.MrGraycat.eGlow.Config.EGlowCustomEffectsConfig;
import me.MrGraycat.eGlow.Config.EGlowMainConfig;
import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig;
import me.MrGraycat.eGlow.Config.Playerdata.EGlowPlayerdataManager;
import me.MrGraycat.eGlow.Event.EGlowEventListener;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Util.DebugUtil;
import me.MrGraycat.eGlow.Util.Packets.NMSHook;
import me.MrGraycat.eGlow.Util.Packets.ProtocolVersion;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;

public class EGlow extends JavaPlugin {
	private static EGlow instance;
	private static EGlowAPI API;
	private boolean UP_TO_DATE = true;

	private AdvancedGlowVisibilityAddon glowAddon;
	// BW - Remove unused addons
	
	@Override
	public void onEnable() {
		setInstance(this);
		setAPI(new EGlowAPI());

		if (versionIsCompactible()) {
			ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromServerString(Bukkit.getBukkitVersion().split("-")[0]);

			NMSHook.initialize();

			loadConfigs();
			
			DataManager.initialize();
			
			registerEventsAndCommands();
			checkForUpdates();
			runAddonHooks();
			runPlayerCheckOnEnable();

			BlockWarsAddon.load(); // BW - Add BlockWars Addon
		} else {
			ChatUtil.sendToConsole("Disabling eGlow! Your server version is not compatible! (" + DebugUtil.getServerVersion() + ")", false);
			getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	@Override
	public void onDisable() {
		if (getAdvancedGlowVisibility() != null) {
			getAdvancedGlowVisibility().shutdown();
		}

		runPlayerCheckOnDisable();
	}
	
	private boolean versionIsCompactible() {
		return !DebugUtil.getServerVersion().equals("v_1_9_R1") && DebugUtil.getMinorVersion() >= 9 && DebugUtil.getMinorVersion() <= 19;
	}
	
	private void loadConfigs() {
		EGlowMainConfig.initialize();
		EGlowMessageConfig.initialize();
		EGlowCustomEffectsConfig.initialize();
		EGlowPlayerdataManager.initialize();
	}
	
	private void registerEventsAndCommands() {
		Objects.requireNonNull(getCommand("eglow")).setExecutor(new EGlowCommand());
		new EGlowEventListener();
	}

	private void runAddonHooks() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (MainConfig.ADVANCED_GLOW_VISIBILITY_ENABLE.getBoolean() && getAdvancedGlowVisibility() == null)
					setAdvancedGlowVisibility(new AdvancedGlowVisibilityAddon());
				// BW - Remove unused addons
			}
		}.runTask(this);
	}
	
	private void runPlayerCheckOnEnable() {
		if (!getServer().getOnlinePlayers().isEmpty()) {
			for (Player player : getServer().getOnlinePlayers()) {
				if (DataManager.getEGlowPlayer(player) == null)
					EGlowEventListener.PlayerConnect(player, player.getUniqueId());
			}
		}
	}
	
	private void runPlayerCheckOnDisable() {
		if (!getServer().getOnlinePlayers().isEmpty()) {
			for (Player player : getServer().getOnlinePlayers()) {
				if (DataManager.getEGlowPlayer(player) == null)
					EGlowEventListener.PlayerDisconnect(player, true);
			}
		}
	}
	
	private void checkForUpdates() {
		  try { 			
			URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=63295");
			String currentVersion = getInstance().getDescription().getVersion();
			String latestVersion = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream())).readLine();
			
			if (currentVersion.contains("PRE")) {
				String betaVersion = currentVersion.split("-")[0];
				setUpToDate(!betaVersion.equals(latestVersion));
			} else {
				if (!latestVersion.contains(currentVersion)) {
					setUpToDate(false);
				}
			}		
		  } catch (Exception e) {
			  //None would care if this fails
		  }
	}
	
	//Setter
	private static void setInstance(EGlow instance) {
		EGlow.instance = instance;
	}
	
	private void setAPI(EGlowAPI api) {
		EGlow.API = api;
	}
	
	private void setUpToDate(boolean up_to_date) {
		this.UP_TO_DATE = up_to_date;
	}

	public void setAdvancedGlowVisibility(AdvancedGlowVisibilityAddon glowAddon) {
		this.glowAddon = glowAddon;
	}
	
	//Getter
	public static EGlow getInstance() {
		return EGlow.instance;
	}
	
	public static EGlowAPI getAPI() {
		return API;
	}
	
	public boolean isUpToDate() {
		return UP_TO_DATE;
	}

	public AdvancedGlowVisibilityAddon getAdvancedGlowVisibility() {
		return this.glowAddon;
	}
}