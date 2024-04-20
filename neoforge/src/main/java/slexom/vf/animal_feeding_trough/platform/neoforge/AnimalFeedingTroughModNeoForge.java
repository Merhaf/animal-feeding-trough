package slexom.vf.animal_feeding_trough.platform.neoforge;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import slexom.animal_feeding_trough.platform.common.AnimalFeedingTroughMod;
import slexom.animal_feeding_trough.platform.common.screen.FeedingTroughScreen;

@Mod(AnimalFeedingTroughMod.MOD_ID)
public class AnimalFeedingTroughModNeoForge {

	public AnimalFeedingTroughModNeoForge(IEventBus modEventBus) {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			modEventBus.addListener(this::setupClient);
		}

		AnimalFeedingTroughMod.onInitialize();
	}

	private void setup(final FMLCommonSetupEvent event) {
	}

	@OnlyIn(Dist.CLIENT)
	private void setupClient(final FMLClientSetupEvent event) {
		HandledScreens.register(AnimalFeedingTroughMod.FEEDING_TROUGH_SCREEN_HANDLER.get(), FeedingTroughScreen::new);
	}

}
