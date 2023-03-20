package me.MrGraycat.eGlow.Addon.BlockWars.functions.glow;

import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.lang.function.Parameter;
import me.MrGraycat.eGlow.API.EGlowAPI;
import me.MrGraycat.eGlow.Addon.BlockWars.Utils;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class AddCustomGlowReceivers extends JavaFunction<Boolean> {
    public AddCustomGlowReceivers() {
        super("addCustomGlowReceivers",
                new Parameter[]{
                        new Parameter<>("player", Utils.playerClassInfo, true, null),
                        new Parameter<>("targets", Utils.playerClassInfo, false, null)
                },
                Utils.boolClassInfo,
                true);
    }

    @Override
    public Boolean[] execute(FunctionEvent e, Object[][] params) {
        Player player = (Player) params[0][0];
        Player[] targets = (Player[]) params[1];

        IEGlowPlayer eGlowPlayer = EGlow.getAPI().getEGlowPlayer(player);
        for (Player tp : targets) {
            EGlow.getAPI().addCustomGlowReceiver(eGlowPlayer, tp);
        }
        return new Boolean[]{true};
    }
}
