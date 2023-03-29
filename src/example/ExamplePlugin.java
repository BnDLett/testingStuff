package example;

import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.net.Packets;

import java.util.concurrent.*;

import static arc.input.KeyCode.call;

public class ExamplePlugin extends Plugin {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> gameOverFuture;
    private void run() {
        if (Groups.player.size() != 0) return;
        Events.fire(new EventType.GameOverEvent(Team.get(0)));
    }
    @Override
    public void init() {
        Events.on(EventType.PlayerLeave.class, e -> {
            if (Groups.player.size() <= 1) {
                this.gameOverFuture = executor.schedule(this::run, 30, TimeUnit.SECONDS);
            }
        });
        Events.on(EventType.PlayerJoin.class, a -> {
            if (gameOverFuture == null) return;
            gameOverFuture.cancel(false);
        });
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        //handler.<Player>register("reply", "<text...>", "A simple ping command that echoes a player's text.", (args, player) -> {
        //player.sendMessage("You said: [accent] " + args[0]);
        //});
        handler.<Player>register("greetNeko", "A command that greets Neko Shark.", (args, player) -> {
            player.sendMessage("Hi Neko!!!");
        });
        handler.<Player>register("kick", "<player> <reason...>", "Kick someone.", (args, player) -> {
            String defaultStart = "[blue][Mod-Command]:[] ";
            if (!player.admin) {
                player.sendMessage(defaultStart + "You do not have enough permission to run this command.");
                return;
            }
            String playerName = args[0].replace("/_", " ");
            Player toKick = Groups.player.find(p -> Strings.stripColors(p.name).equalsIgnoreCase(playerName));
            if (toKick == null) {
                player.sendMessage(defaultStart + "Unable to find the player " + args[0]);
                return;
            }
            toKick.kick(args[1]);
        });
        handler.<Player>register("ban", "<player>", "Ban a player.", (args, player) -> {
            String defaultStart = "[blue][Mod-Command]:[] ";
            if (!player.admin) {
                player.sendMessage(defaultStart + "You do not have enough permission to run this command.");
                return;
            }
            String playerName = args[0].replace("/_", " ");
            Player toBan = Groups.player.find(p -> Strings.stripColors(p.name).equalsIgnoreCase(playerName));

            if (toBan == null) {
                player.sendMessage(defaultStart + "Unable to find the player " + args[0]);
                return;
            }
            toBan.kick(Packets.KickReason.banned);
        });
        handler.<Player>register("team", "<team>", "Change your team by int.", (args, player) -> {
            Team toTeam = Team.get(Integer.parseInt(args[0]));
            player.team(toTeam);
        });
    }
}