package me.gamercoder215.socketmc.fabric.mixin.events;

import me.gamercoder215.socketmc.fabric.FabricSocketMC;
import me.gamercoder215.socketmc.fabric.screen.FabricScreenUtil;
import me.gamercoder215.socketmc.screen.AbstractScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

import static me.gamercoder215.socketmc.fabric.FabricSocketMC.minecraft;

@Mixin(Minecraft.class)
public class PlayerChangeScreenEvent {

    @Inject(method = "setScreen", at = @At("HEAD"))
    public void onScreenChange(Screen screen, CallbackInfo ci) {
        if (!FabricSocketMC.eventsEnabled) return;

        AbstractScreen oldScreen = FabricScreenUtil.fromMinecraft(minecraft.screen);
        if (oldScreen == null) return;

        AbstractScreen newScreen = FabricScreenUtil.fromMinecraft(screen);
        if (newScreen == null) return;

        FabricSocketMC.sendEvent(4, Map.of("old", oldScreen, "new", newScreen));
    }

}
