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
}
 // just some random stuff