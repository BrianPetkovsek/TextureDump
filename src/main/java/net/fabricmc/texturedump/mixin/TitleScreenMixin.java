package net.fabricmc.texturedump.mixin;

import net.fabricmc.texturedump.TextureDump;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

	@Inject(at = @At("HEAD"), method = "init()V")
	private void init(CallbackInfo info) {
		TextureDump.LOGGER.info("This line is printed by an example mod mixin!");

		if(mezz.texturedump.TextureDump.titleScreenOpened++ == 1){
			try {
				mezz.texturedump.TextureDump.dumpTextureMaps();
			} catch (IOException e) {
				TextureDump.LOGGER.error("Failed to dump texture maps with error.", e);
			}
			TextureDump.LOGGER.info("title screen open");
		}

	}


}
