package com.wardenplus.client;

import com.wardenplus.WardenPlus;

import net.minecraft.client.renderer.entity.WardenRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = WardenPlus.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        WardenRenderer warden = event.getRenderer(EntityType.WARDEN);
        if (warden != null) {
            WardenRenderer renderer = warden;
            renderer.addLayer(new WardenResonanceLayer(renderer));
        }
    }

    @SubscribeEvent
    public static void onPipelines(RegisterRenderPipelinesEvent event) {
        WpRenderTypes.registerPipelines(event);
    }

    @SubscribeEvent
    public static void onGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, Identifier.fromNamespaceAndPath(WardenPlus.MOD_ID, "screen_fx"), ScreenFx::render);
    }
}






