package me.MrGraycat.eGlow.Manager.Interface;

import me.MrGraycat.eGlow.Config.EGlowMainConfig.MainConfig;
import me.MrGraycat.eGlow.Config.EGlowMessageConfig.Message;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.DataManager;
import me.MrGraycat.eGlow.Util.DebugUtil;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowDisableReason;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowTargetMode;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowVisibility;
import me.MrGraycat.eGlow.Util.EnumUtil.GlowWorldAction;
import me.MrGraycat.eGlow.Util.Packets.Chat.EnumChatFormat;
import me.MrGraycat.eGlow.Util.Packets.PacketUtil;
import me.MrGraycat.eGlow.Util.Packets.ProtocolVersion;
import me.MrGraycat.eGlow.Util.Text.ChatUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class IEGlowPlayer {
	private final String entityType;
	private Player player;
	private final String name;
	private UUID uuid;
	private ProtocolVersion version = ProtocolVersion.SERVER_VERSION;

	private ChatColor activeColor = ChatColor.RESET;
	private boolean glowStatus = false;
	private boolean fakeGlowStatus = false;

	private IEGlowEffect glowEffect;

	private final List<IEGlowEffect> forcedEffects = new ArrayList<>();

	private boolean glowOnJoin;
	private boolean activeOnQuit;
	private boolean saveData = false;
	private GlowDisableReason glowDisableReason = GlowDisableReason.NONE;
	private GlowVisibility glowVisibility;

	private GlowTargetMode glowTarget = GlowTargetMode.CUSTOM;
	private List<Player> customTargetList = new ArrayList<>();

	public IEGlowPlayer(Player player) {
		this.entityType = "PLAYER";
		this.player = player;
		this.name = player.getName();
		this.uuid = player.getUniqueId();
		this.customTargetList = new ArrayList<>(Collections.singletonList(player));
		this.version = ProtocolVersion.getPlayerVersion(this);

		if (this.version.getNetworkId() <= 110 && !this.version.getFriendlyName().equals("1.9.4")) {
			this.glowVisibility = GlowVisibility.UNSUPPORTEDCLIENT;
		} else {
			this.glowVisibility = GlowVisibility.ALL;
		}

		setupForceGlows();
	}

	// Glowing stuff
	public void setGlowing(boolean status, boolean fake) {
		if (!fake && status == getGlowStatus())
			return;

		setGlowStatus(status);
		setFakeGlowStatus(fake);

		switch (entityType) {
			case ("PLAYER"):
				PacketUtil.updateGlowing(this, status);
				break;
		}
	}

	public void setColor(ChatColor color, boolean status, boolean fake) {
		if (getSaveData())
			setSaveData(true);

		setFakeGlowStatus(fake);

		if (color.equals(ChatColor.RESET)) {
			setGlowing(false, fake);
		} else {
			setGlowing(status, fake);

			if (getActiveColor() != null && getActiveColor().equals(color))
				return;

			setGlowStatus(status);
			setActiveColor(color);
		}

		if (getEntityType().equals("NPC"))
			return;

		updatePlayerTabname();
		DataManager.sendAPIEvent(this, fake);
	}

	public boolean isSameGlow(IEGlowEffect newGlowEffect) {
		return getGlowStatus() && getEffect() != null && newGlowEffect.equals(getEffect());
	}

	public void activateGlow() {
		setGlowStatus(true);

		if (getEffect() != null) {
			activateGlow(getEffect());
		} else {
			setGlowStatus(false);
		}
	}

	public void activateGlow(IEGlowEffect newGlow) {
		disableGlow(true);
		setEffect(newGlow);

		newGlow.activateForEntity(getEntity());
		setGlowing(true, false);
	}

	public void toggleGlow() {
		if (getFakeGlowStatus() || getGlowStatus()) {
			disableGlow(false);
			setFakeGlowStatus(false);
			DataManager.sendAPIEvent(this, false);
			updatePlayerTabname();
		} else {
			activateGlow();
		}
	}

	public void disableGlow(boolean hardReset) {
		if (getFakeGlowStatus() || getGlowStatus()) {
			if (getEffect() != null) {
				getEffect().deactivateForEntity(getEntity());
			}

			if (hardReset)
				setEffect(DataManager.getEGlowEffect("none"));

			setActiveColor(ChatColor.RESET);
			setGlowing(false, false);
		}
	}

	/**
	 * Update the tabname of the player
	 */
	public void updatePlayerTabname() {
		if (!MainConfig.FORMATTING_TABLIST_ENABLE.getBoolean())
			return;

		if (getPlayer() == null)
			return;

		String format = MainConfig.FORMATTING_TABLIST_FORMAT.getString();

		if (format.contains("%name%"))
			format = format.replace("%name%", player.getDisplayName());

		if (DebugUtil.isPAPIInstalled())
			format = PlaceholderAPI.setPlaceholders(getPlayer(), format);

		getPlayer().setPlayerListName(ChatUtil.translateColors(format));
	}

	public String getTeamName() {
		String playerName = name;
		return (playerName.length() > 15) ? "E" + playerName.substring(0,14) : "E" + playerName;
	}

	public String getEntityType() {
		return entityType;
	}

	public Object getEntity() {
		return player;
	}

	public String getDisplayName() {
		return this.name;
	}

	public Player getPlayer() {
		return this.player;
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public ProtocolVersion getVersion() {
		return this.version;
	}

	public void setupForceGlows() {
		if (!MainConfig.SETTINGS_JOIN_FORCE_GLOWS_ENABLE.getBoolean() || getPlayer() == null || isInBlockedWorld() && MainConfig.SETTINGS_JOIN_FORCE_GLOWS_BYPASS_BLOCKED_WORLDS.getBoolean())
			return;

		for (String permission : MainConfig.SETTINGS_JOIN_FORCE_GLOWS_LIST.getConfigSection()) {
			ChatUtil.sendToConsole(permission, false);
			if (getPlayer().hasPermission("eglow.force." + permission.toLowerCase())) {
				IEGlowEffect effect = DataManager.getEGlowEffect(MainConfig.SETTINGS_JOIN_FORCE_GLOWS_LIST.getString(permission)); //TODO test!

				if (!forcedEffects.contains(effect))
					forcedEffects.add(effect);
			}
		}
	}

	public boolean isForcedGlow(IEGlowEffect effect) {
		return forcedEffects.contains(effect);
	}

	public IEGlowEffect getForceGlow() {
		if (forcedEffects.isEmpty() || !MainConfig.SETTINGS_JOIN_FORCE_GLOWS_ENABLE.getBoolean()|| getPlayer() == null || isInBlockedWorld() && MainConfig.SETTINGS_JOIN_FORCE_GLOWS_BYPASS_BLOCKED_WORLDS.getBoolean())
			return null;
		return forcedEffects.get(0);
	}

	public boolean isInBlockedWorld() {
		if (!MainConfig.WORLD_ENABLE.getBoolean())
			return false;

		GlowWorldAction action;

		try {
			action = GlowWorldAction.valueOf(MainConfig.WORLD_ACTION.getString().toUpperCase() + "ED");
		} catch (IllegalArgumentException e) {
			action = GlowWorldAction.UNKNOWN;
		}

		switch (action) {
			case BLOCKED:
				if (MainConfig.WORLD_LIST.getStringList().contains(getPlayer().getWorld().getName().toLowerCase()))
					return true;
				break;
			case ALLOWED:
				if (!MainConfig.WORLD_LIST.getStringList().contains(getPlayer().getWorld().getName().toLowerCase()))
					return true;
				break;
			case UNKNOWN:
				return false;
		}
		return false;
	}

	public ChatColor getActiveColor() {
		return this.activeColor;
	}

	public void setActiveColor(ChatColor color) {
		this.activeColor = color;
	}

	public boolean getGlowStatus() {
		return this.glowStatus;
	}

	public void setGlowStatus(boolean status) {
		this.glowStatus = status;
	}

	public boolean getFakeGlowStatus() {
		return this.fakeGlowStatus;
	}

	public void setFakeGlowStatus(boolean status) {
		this.fakeGlowStatus = status;
	}

	public GlowDisableReason getGlowDisableReason() {
		return this.glowDisableReason;
	}

	public void setGlowDisableReason(GlowDisableReason reason) {
		this.glowDisableReason = reason;
	}

	public GlowVisibility getGlowVisibility() {
		return this.glowVisibility;
	}

	public void setGlowVisibility(GlowVisibility visibility) {
		if (!this.glowVisibility.equals(GlowVisibility.UNSUPPORTEDCLIENT))
			this.glowVisibility = visibility;
	}

	public GlowTargetMode getGlowTargetMode() {
		return glowTarget;
	}

	public void setGlowTargetMode(GlowTargetMode glowTarget) {
		if (glowTarget != this.glowTarget) {
			this.glowTarget = glowTarget;
			PacketUtil.updateGlowTarget(this);
		}
	}

	public List<Player> getGlowTargets() {
		return customTargetList;
	}

	public void addGlowTarget(Player p) {
		if (!customTargetList.contains(p)) {
			customTargetList.add(p);
		}
		customTargetList.remove(player);
		PacketUtil.glowTargetChange(this, p, true);
		if (glowTarget.equals(GlowTargetMode.ALL))
			setGlowTargetMode(GlowTargetMode.CUSTOM);
	}

	public void removeGlowTarget(Player p) {
		if (customTargetList.contains(p))
			PacketUtil.glowTargetChange(this, p, false);
		customTargetList.remove(p);
	}

	public void setGlowTargets(List<Player> targets) {
		if (targets == null) {
			customTargetList.clear();
		} else {
			if (!targets.contains(player))
				targets.add(player);

			customTargetList = targets;
		}

		if (glowTarget.equals(GlowTargetMode.ALL)) {
			setGlowTargetMode(GlowTargetMode.CUSTOM);
		} else {
			for (Player player : Objects.requireNonNull(targets, "Can't loop over 'null'")) {
				PacketUtil.glowTargetChange(this, player, true);
			}
		}
	}

	public void resetGlowTargets() {
		customTargetList.clear();
	}

	public IEGlowEffect getEffect() {
		return this.glowEffect;
	}

	public void setEffect(IEGlowEffect effect) {
		this.glowEffect = effect;
	}

	public boolean getGlowOnJoin() {
		return this.glowOnJoin;
	}

	public void setGlowOnJoin(boolean status) {
		if (this.glowOnJoin != status) {
			if (getSaveData())
				setSaveData(true);
		}

		this.glowOnJoin = status;
	}

	public boolean getActiveOnQuit() {
		return this.activeOnQuit;
	}

	public void setActiveOnQuit(boolean status) {
		this.activeOnQuit = status;
	}

	public boolean getSaveData() {
		return !this.saveData;
	}

	public void setSaveData(boolean saveData) {
		this.saveData = saveData;
	}

	public void setDataFromLastGlow(String lastGlow) {
		if (lastGlow.contains("SOLID:") || lastGlow.contains("EFFECT:"))
			lastGlow = lastGlow.replace("SOLID:", "").replace("EFFECT:", "");

		IEGlowEffect effect = DataManager.getEGlowEffect(lastGlow);
		setEffect(effect);
	}

	public String getLastGlowName() {
		return (getEffect() != null) ? getEffect().getDisplayName() : Message.COLOR.get("none");
	}

	public String getLastGlow() {
		return (getEffect() != null) ? getEffect().getName() : "none";
	}
}