package me.MrGraycat.eGlow.Addon.BlockWars.functions.glow;

import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.lang.function.Parameter;
import me.MrGraycat.eGlow.Addon.BlockWars.Utils;
import me.MrGraycat.eGlow.EGlow;
import me.MrGraycat.eGlow.Manager.Interface.IEGlowPlayer;
import org.bukkit.entity.Player;

public class RemoveCustomGlowReceivers extends JavaFunction<Boolean> {
    public RemoveCustomGlowReceivers() {
        super("removeCustomGlowReceivers",
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
            EGlow.getAPI().removeCustomGlowReceiver(eGlowPlayer, tp);
        }
        return new Boolean[]{true};
    }
}
