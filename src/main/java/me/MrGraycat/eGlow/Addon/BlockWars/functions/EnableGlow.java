package me.MrGraycat.eGlow.Addon.BlockWars.functions;

import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.lang.function.Parameter;
import me.MrGraycat.eGlow.Addon.BlockWars.BlockWarsAddon;
import me.MrGraycat.eGlow.Addon.BlockWars.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class EnableGlow extends JavaFunction<Boolean> {
    public EnableGlow() {
        super("setGlowEnabled",
                new Parameter[]{
                        new Parameter<>("glow", Utils.boolClassInfo, false, null)
                },
                Utils.boolClassInfo,
                true);
    }

    @Override
    public Boolean[] execute(FunctionEvent e, Object[][] params) {
        Boolean enable = (Boolean) params[0][0];

        BlockWarsAddon.glowing = enable;
        if (enable) {
            Bukkit.getOnlinePlayers().forEach(Utils::enableGlow);
        } else {
            Bukkit.getOnlinePlayers().forEach(Utils::disableGlow);
        }

        return new Boolean[]{true};
    }
}
