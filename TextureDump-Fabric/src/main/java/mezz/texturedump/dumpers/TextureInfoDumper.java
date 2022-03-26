package mezz.texturedump.dumpers;

import com.google.gson.stream.JsonWriter;
import net.fabricmc.texturedump.TextureDump;
import net.fabricmc.texturedump.mixin.SpriteAtlasTextureSpritesGetter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TextureInfoDumper {
	public static List<Path> saveTextureInfoDataFiles(String name, SpriteAtlasTextureSpritesGetter map, int mipmapLevels, Path outputFolder) throws IOException {
		TextureDump.LOGGER.info("Dumping TextureMap info to file");
		//StartupMessageManager.addModMessage("Dumping TextureMap info to file");

		List<Path> dataFiles = new ArrayList<>();
		for (int level = 0; level < mipmapLevels; level++) {
			final String filename = name + "_mipmap_" + level;
			Path dataFile = outputFolder.resolve(filename + ".js");

			StringWriter out = new StringWriter();
			JsonWriter jsonWriter = new JsonWriter(out);
			jsonWriter.setIndent("    ");

			Collection<Sprite> values = map.getSprites().values();
			TextureDump.LOGGER.info("Mipmap Level " + level);
			//StartupMessageManager.addModMessage("Mipmap Level " + level);

			jsonWriter.beginArray();
			{
				for (Sprite sprite : values) {
					Identifier iconName = sprite.getId();
					boolean animated = (sprite.getAnimation() != null);
					jsonWriter.beginObject()
							.name("name").value(iconName.toString())
							.name("animated").value(animated)
							.name("x").value(sprite.getX() / (1L << level))
							.name("y").value(sprite.getY() / (1L << level))
							.name("width").value(sprite.getWidth() / (1L << level))
							.name("height").value(sprite.getHeight() / (1L << level))
							.endObject();
				}
			}
			jsonWriter.endArray();
			jsonWriter.close();
			out.close();

			FileWriter fileWriter;
			fileWriter = new FileWriter(dataFile.toFile());
			fileWriter.write("var textureData = \n//Start of Data\n" + out);
			fileWriter.close();

			dataFiles.add(dataFile);
		}
		return dataFiles;
	}
}
