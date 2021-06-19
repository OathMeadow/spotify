package com.spotify.csv;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CsvParserTest {

	@TempDir
	File tempDir;

	private File writeFile(List<String> lines) throws IOException {
		return Files.write(
				Path.of(tempDir.getPath(), "test.csv"),
				String.join(System.lineSeparator(), lines).getBytes(StandardCharsets.UTF_8)).toFile();
	}

	@Test
	void shouldParseAllCsv() throws Exception {
		// Arrange
		List<String> lines = new ArrayList<>();
		lines.add("\"\",\"Track.Name\",\"Artist.Name\",\"Genre\",\"Beats.Per.Minute\",\"Energy\",\"Danceability\",\"Loudness..dB..\",\"Liveness\",\"Valence.\",\"Length.\",\"Acousticness..\",\"Speechiness.\",\"Popularity\"");
		lines.add("\"1\",\"Señorita\",\"Shawn Mendes\",\"canadian pop\",117,55,76,-6,8,75,191,4,3,79");
		lines.add("\"2\",\"China\",\"Anuel AA\",\"reggaeton flow\",105,81,79,-4,8,61,302,8,9,92");
		File csvFile = writeFile(lines);

		// Act
		List<CsvRecord> records;
		try (CsvParser parser = new CsvParser(new FileReader(csvFile, StandardCharsets.UTF_8))) {
			records = parser.readAll();
		}

		// Assert
		assertEquals(2 , records.size());
		assertEquals(records.get(0).getValues().size(), records.get(0).getValues().size());
		assertTrue(records.get(0).getValues().keySet().containsAll(List.of("", "Track.Name", "Artist.Name", "Genre", "Beats.Per.Minute", "Energy", "Danceability", "Loudness..dB..", "Liveness", "Valence.", "Length.", "Acousticness..", "Speechiness.", "Popularity")));
		assertTrue(records.get(0).getValues().values().containsAll(List.of("1", "Señorita", "Shawn Mendes", "canadian pop", "117", "55", "76" , "-6", "8", "75", "191", "4", "3", "79")));
		assertTrue(records.get(1).getValues().values().containsAll(List.of("2", "China", "Anuel AA", "reggaeton flow", "105", "81", "79" , "-4", "8", "61", "302", "8", "9", "92")));
	}

	@Test
	void shouldParseCsvWithSpaces() throws Exception {
		// Arrange
		List<String> lines = new ArrayList<>();
		lines.add("\" NO\",   \"Title\", \"Date \"");
		lines.add("\"526\",\"     The Bad Mood - 3\", \"2016-09-17\"");
		File csvFile = writeFile(lines);

		// Act
		CsvRecord record;
		try (CsvParser parser = new CsvParser(new FileReader(csvFile, StandardCharsets.UTF_8))) {
			record = parser.read();
		}

		// Assert
		assertNotNull(record);
		assertEquals(record.getValues().size(), record.getValues().size());
		assertTrue(record.getValues().keySet().containsAll(List.of(" NO", "Title", "Date ")));
		assertTrue(record.getValues().values().containsAll(List.of("526", "     The Bad Mood - 3", "2016-09-17")));
	}

	@Test
	void shouldFailIfSizesDoNotMatch() throws Exception {
		// Arrange
		List<String> lines = new ArrayList<>();
		lines.add("\"Number\", \"Position\"");
		lines.add("\"55");
		File csvFile = writeFile(lines);

		// Act && Assert
		try (CsvParser parser = new CsvParser(new FileReader(csvFile, StandardCharsets.UTF_8))) {
			parser.read();
			fail("Expected exception to be thrown, but no one was seen");
		} catch (IOException e) {
			MatcherAssert.assertThat(e.getMessage(), containsString("CSV file header size do not match the value size"));
		}
	}

	@Test
	void shouldReturnNullOrEmptyIfNotRecognized() throws Exception {
		// Arrange
		List<String> lines = new ArrayList<>();
		lines.add("This, is, not, expected");
		File csvFile = writeFile(lines);

		// Act & Assert
		try (CsvParser parser = new CsvParser(new FileReader(csvFile, StandardCharsets.UTF_8))) {
			assertNull(parser.read());
			assertEquals(List.of(), parser.readAll());
		}
	}
}
