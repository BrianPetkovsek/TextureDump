package mezz.texturedump.dumpers;

import com.google.gson.stream.JsonWriter;
import net.fabricmc.texturedump.TextureDump;
import net.fabricmc.texturedump.mixin.SpriteAtlasTextureSpritesGetter;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.Person;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ModStatsDumper {
	private static final Logger LOGGER = TextureDump.LOGGER;

	public Path saveModStats(String name, SpriteAtlasTextureSpritesGetter map, Path modStatsDir) throws IOException {
		Map<String, Long> modPixelCounts = map.getSprites().values().stream()
				.collect(Collectors.groupingBy(
						sprite -> sprite.getId().getNamespace(),
						Collectors.summingLong(sprite -> (long) sprite.getWidth() * sprite.getHeight()))
				);

		final long totalPixels = modPixelCounts.values().stream().mapToLong(longValue -> longValue).sum();

		final String filename = name + "_mod_statistics";
		Path output = modStatsDir.resolve(filename + ".js");

		List<Map.Entry<String, Long>> sortedEntries = modPixelCounts.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.collect(Collectors.toList());

		LOGGER.info("Dumping Mod TextureMap Statistics");
		//StartupMessageManager.addModMessage("Dumping Mod TextureMap Statistics");
		FileWriter fileWriter = new FileWriter(output.toFile());
		fileWriter.write("var modStatistics = \n//Start of Data\n");
		JsonWriter jsonWriter = new JsonWriter(fileWriter);
		jsonWriter.setIndent("    ");
		jsonWriter.beginArray();
		{
			for (Map.Entry<String, Long> modPixels : sortedEntries) {
				String resourceDomain = modPixels.getKey();
				long pixelCount = modPixels.getValue();
				writeModStatisticsObject(jsonWriter, resourceDomain, pixelCount, totalPixels);
			}
		}
		jsonWriter.endArray();
		jsonWriter.close();
		fileWriter.close();

		LOGGER.info("Saved mod statistics to {}.", output.toString());
		return output;
	}

	private static void writeModStatisticsObject(JsonWriter jsonWriter, String resourceDomain, long pixelCount, long totalPixels) throws IOException {
		ModContainer modInfo = getModMetadata(resourceDomain);
		String modName = modInfo != null ? modInfo.getMetadata().getName() : "";

		jsonWriter.beginObject()
				.name("resourceDomain").value(resourceDomain)
				.name("pixelCount").value(pixelCount)
				.name("percentOfTextureMap").value(pixelCount * 100f / totalPixels)
				.name("modName").value(modName)
				.name("url").value(modInfo.getMetadata().getDescription()) //getModConfigValue(modInfo, "displayURL")
				.name("issueTrackerUrl").value(modInfo.getMetadata().getDescription()); // getModConfigValue(modInfo, "issueTrackerURL")

		jsonWriter.name("authors").beginArray();
		{
			Collection<Person> authors = modInfo.getMetadata().getAuthors();
			if (!authors.isEmpty()) {
				for (Person author : authors) {
					jsonWriter.value(author.getName().trim());
				}
			}
		}
		jsonWriter.endArray();

		jsonWriter.endObject();
	}

	@Nullable
	private static ModContainer getModMetadata(String resourceDomain) {
		Optional<ModContainer> a = FabricLoader.getInstance().getModContainer(resourceDomain);
		ModContainer b = a.get();
		return b;
		/*
		b.getMetadata()
		ModList modList = ModList.get();
		IModFileInfo modFileInfo = modList.getModFileById(resourceDomain);
		if (modFileInfo == null) {
			return null;
		}
		return modFileInfo.getMods()
				.stream()
				.findFirst()
				.orElse(null);

		 */
	}
}
