package mezz.texturedump;

import mezz.texturedump.dumpers.ResourceWriter;
import mezz.texturedump.dumpers.ModStatsDumper;
import mezz.texturedump.dumpers.TextureImageDumper;
import mezz.texturedump.dumpers.TextureInfoDumper;
import net.fabricmc.texturedump.mixin.SpriteAtlasTextureSpritesGetter;
import net.fabricmc.texturedump.mixin.TextureManagerTexturesGetter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

//@Mod(value = Constants.MOD_ID)
public class TextureDump {
	private static final Logger LOGGER = LogManager.getLogger();
	public static int titleScreenOpened = 0;

	private TextureDump() {}

	public static void dumpTextureMaps() throws IOException {
		Path outputFolder = Paths.get("texture_dump");
		outputFolder = Files.createDirectories(outputFolder);

		//TextureManagerTexturesGetter textureManager = (TextureManagerTexturesGetter) MinecraftClient.getInstance().getTextureManager();

		try {
			Path mipmapsDir = createSubDirectory(outputFolder, "mipmaps");
			Path resourceDir = createSubDirectory(outputFolder, "resources");
			Path modStatsDir = createSubDirectory(outputFolder, "modStats");
			Path texturesDir = createSubDirectory(outputFolder, "textures");
			Path textureInfoDir = createSubDirectory(outputFolder, "textureInfo");
			ResourceWriter.writeResources(resourceDir);

			for (Map.Entry<Identifier, AbstractTexture> entry : ((TextureManagerTexturesGetter) MinecraftClient.getInstance().getTextureManager()).getTextures().entrySet()) {
				AbstractTexture textureObject = entry.getValue();
				if (textureObject instanceof SpriteAtlasTexture) {
					String name = entry.getKey().toString().replace(':', '_').replace('/', '_');
					dumpTextureMap((SpriteAtlasTexture) textureObject, name, outputFolder, mipmapsDir, resourceDir, modStatsDir, texturesDir, textureInfoDir);
				}
			}
		} catch (IOException e) {
			LOGGER.error("Failed to dump texture maps.", e);
		}
	}

	private static void dumpTextureMap(SpriteAtlasTexture map, String name, Path outputFolder, Path mipmapsDir, Path resourceDir, Path modStatsDir, Path texturesDir, Path textureInfoDir) {
		try {
			ModStatsDumper modStatsDumper = new ModStatsDumper();
			Path modStatsFile = modStatsDumper.saveModStats(name, (SpriteAtlasTextureSpritesGetter)map, modStatsDir);

			List<Path> textureImageJsFiles = TextureImageDumper.saveGlTextures(name, map.getGlId(), texturesDir);
			int mipmapLevels = textureImageJsFiles.size();
			List<Path> textureInfoFiles = TextureInfoDumper.saveTextureInfoDataFiles(name, (SpriteAtlasTextureSpritesGetter)map, mipmapLevels, textureInfoDir);

			ResourceWriter.writeFiles(name, outputFolder, mipmapsDir, textureImageJsFiles, textureInfoFiles, modStatsFile, resourceDir, mipmapLevels);
		} catch (IOException e) {
			LOGGER.error(String.format("Failed to dump texture map: %s.", name), e);
		}
	}

	public static Path createSubDirectory(Path outputFolder, String subfolderName) throws IOException {
		Path subfolder = outputFolder.resolve(subfolderName);
		return Files.createDirectories(subfolder);
	}
}
