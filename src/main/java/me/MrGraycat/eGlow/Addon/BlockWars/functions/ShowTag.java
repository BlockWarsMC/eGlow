package me.MrGraycat.eGlow.Addon.BlockWars.functions;

import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.lang.function.Parameter;
import me.MrGraycat.eGlow.Addon.BlockWars.Utils;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class ShowTag extends JavaFunction<Boolean> {
    public ShowTag() {
        super("showTagsFor",
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
            Utils.setNametagStateForPlayer(tp, Team.OptionStatus.ALWAYS);
        }
        return new Boolean[]{true};
    }
}
