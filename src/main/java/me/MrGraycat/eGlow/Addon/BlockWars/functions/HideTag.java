package me.MrGraycat.eGlow.Addon.BlockWars.functions;

import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.lang.function.Parameter;
import me.MrGraycat.eGlow.Addon.BlockWars.listeners.PlayerEvents;
import me.MrGraycat.eGlow.Addon.BlockWars.Utils;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class HideTag extends JavaFunction<Boolean> {
    public HideTag() {
        super("hideTagsFor",
                new Parameter[]{
                        new Parameter<>("players", Utils.playerClassInfo, false, null)
                },
                Utils.boolClassInfo,
                true);
    }

    @Override
    public Boolean[] execute(FunctionEvent e, Object[][] params) {
        Player[] players = (Player[]) params[0];
        for (Player tp : players) {
            Utils.setNametagStateForPlayer(tp, Team.OptionStatus.NEVER);
        }
        return new Boolean[]{true};
    }
}
