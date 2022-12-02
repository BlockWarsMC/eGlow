package me.MrGraycat.eGlow.Event;

import me.MrGraycat.eGlow.API.Enum.EGlowEffect;
import me.MrGraycat.eGlow.Addon.BlockWars.BlockWarsAddon;
import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.Config.Playerdata.EGlowPlayerdataManager;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.GUI.Menu;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowEffect;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.Packets.PacketUtil;
import me.MrGraycat.eGlow.Util.Packets.PipelineInjector;
import me.MrGraycat.eGlow.Util.Packets.ProtocolVersion;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class EGlowEventListener implements Listener {
	public EGlowEventListener() {
		EGlow.getInstance().getServer().getPluginManager().registerEvents(this, EGlow.getInstance());
		
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			new EGlowEventListener113AndAbove();
		}
	}

	@EventHandler
	public void PlayerConnectEvent(PlayerJoinEvent e) {
		PlayerConnect(e.getPlayer(), e.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void PlayerKickedEvent(PlayerKickEvent e) {
		PlayerDisconnect(e.getPlayer(), false);
	}

	@EventHandler
	public void PlayerDisconnectEvent(PlayerQuitEvent e) {
		PlayerDisconnect(e.getPlayer(), false);
	}
	
	@EventHandler
	public void onMenuClick(InventoryClickEvent e) {
		InventoryHolder holder = e.getInventory().getHolder();
		
		if (holder == null)
			return;
		
		if (holder instanceof Menu) {
			e.setCancelled(true);
			
			if (e.getView().getBottomInventory().equals(e.getClickedInventory()) || e.getCurrentItem() == null) 
				return;
			
			Menu menu = (Menu) holder;
			menu.handleMenu(e);
		}
	}
	
	@EventHandler
	public void onPlayerWorldChange(PlayerChangedWorldEvent e) {
		Player p = e.getPlayer();
		IEGlowPlayer eglowPlayer = DataManager.getEGlowPlayer(p);
		
		if (eglowPlayer != null && MainConfig.WORLD_ENABLE.getBoolean()) {
			if (eglowPlayer.isInBlockedWorld()) {
				if (eglowPlayer.getGlowStatus() || eglowPlayer.getFakeGlowStatus()) {
					eglowPlayer.toggleGlow();
					eglowPlayer.setGlowDisableReason(GlowDisableReason.BLOCKEDWORLD);
					ChatUtil.sendMsg(p, Message.WORLD_BLOCKED.get(), true);
				}
			} else {
				if (eglowPlayer.getGlowDisableReason() != null && eglowPlayer.getGlowDisableReason().equals(GlowDisableReason.BLOCKEDWORLD)) {
					eglowPlayer.toggleGlow();
					eglowPlayer.setGlowDisableReason(GlowDisableReason.NONE);
					ChatUtil.sendMsg(p, Message.WORLD_ALLOWED.get(), true);
				}
			}
		}
	}
	
	/**
	 * Code to initialise the player
	 * @param p player to initialise
	 */
	public static void PlayerConnect(Player p, UUID uuid) {
		//Fixes permanent player glows from old eGlow versions/other glow plugins that use Player#setGlowing(true)
		if (p.isGlowing())
			p.setGlowing(false);
		
		IEGlowPlayer eglowPlayer = DataManager.addEGlowPlayer(p, uuid.toString());
		PipelineInjector.inject(eglowPlayer);
		PacketUtil.scoreboardPacket(eglowPlayer, true);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				EGlowPlayerdataManager.loadPlayerdata(eglowPlayer);
				
				if (!EGlow.getInstance().isUpToDate() && MainConfig.SETTINGS_NOTIFICATIONS_UPDATE.getBoolean() && p.hasPermission("eglow.option.update"))
					ChatUtil.sendPlainMsg(p, "&aA new update is available&f!", true);

				if (EGlowPlayerdataManager.getMySQL_Failed() && p.hasPermission("eglow.option.update"))
					ChatUtil.sendPlainMsg(p, "&cMySQL failed to enable properly, have a look at this asap&f.", true);
				
				eglowPlayer.updatePlayerTabname();

				new BukkitRunnable() {
					@Override
					public void run() {
						PacketUtil.updatePlayer(eglowPlayer);
					}
				}.runTask(EGlow.getInstance());
				IEGlowEffect effect = DataManager.getEGlowEffect("WHITE");

				if (BlockWarsAddon.glowing) eglowPlayer.activateGlow(effect);
				else eglowPlayer.disableGlow(true);

			}
		}.runTaskLaterAsynchronously(EGlow.getInstance(), 2L);
	}
	
	/**
	 * Code to unload the player from eGlow
	 * @param p player to unload
	 */
	public static void PlayerDisconnect(Player p, boolean shutdown) {
		IEGlowPlayer eglowPlayer = DataManager.getEGlowPlayer(p);
		PacketUtil.scoreboardPacket(eglowPlayer, false);
		
		if (!shutdown) {
			new BukkitRunnable() {
				@Override
				public void run() {
					PlayerDisconnectNext(eglowPlayer);
				}
			}.runTaskAsynchronously(EGlow.getInstance());
		} else {
			PlayerDisconnectNext(eglowPlayer);
		}
	}
	
	private static void PlayerDisconnectNext(IEGlowPlayer eglowPlayer) {
		if (eglowPlayer != null) {
			eglowPlayer.setActiveOnQuit(eglowPlayer.getFakeGlowStatus() || eglowPlayer.getGlowStatus());
			EGlowPlayerdataManager.savePlayerdata(eglowPlayer);
			
			PipelineInjector.uninject(eglowPlayer);
			DataManager.removeEGlowPlayer(eglowPlayer.getPlayer());
			if (EGlow.getInstance().getAdvancedGlowVisibility() != null) {
				EGlow.getInstance().getAdvancedGlowVisibility().uncachePlayer(eglowPlayer);
			}
		}
	}
}