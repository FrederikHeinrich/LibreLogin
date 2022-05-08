package xyz.kyngs.librepremium.bungeecord;

import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.kyngs.librepremium.api.provider.LibrePremiumProvider;

import java.util.concurrent.TimeUnit;

public class BungeeCordPlugin extends Plugin implements LibrePremiumProvider {

    private BungeeAudiences adventure;
    private BungeeComponentSerializer serializer;
    private BungeeCordLibrePremium librePremium;

    public @NonNull BungeeAudiences getAdventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
        }
        return this.adventure;
    }

    public BungeeComponentSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void onEnable() {
        this.adventure = BungeeAudiences.create(this);
        this.serializer = BungeeComponentSerializer.of(
                GsonComponentSerializer.builder().downsampleColors().emitLegacyHoverEvent().build(),
                LegacyComponentSerializer.builder().flattener(adventure.flattener()).build()
        );

        this.librePremium = new BungeeCordLibrePremium(this);
        librePremium.makeEnabled();

        getProxy().getPluginManager().registerListener(this, new Blockers(librePremium.getAuthorizationProvider(), librePremium.getConfiguration()));
        getProxy().getPluginManager().registerListener(this, new BungeeCordListener(this));

        var millis = librePremium.getConfiguration().milliSecondsToRefreshNotification();

        if (millis > 0) {
            getProxy().getScheduler().schedule(this, () -> librePremium.getAuthorizationProvider().notifyUnauthorized(), 0, millis, TimeUnit.MILLISECONDS);
        }

    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    @Override
    public BungeeCordLibrePremium getLibrePremium() {
        return librePremium;
    }

}
