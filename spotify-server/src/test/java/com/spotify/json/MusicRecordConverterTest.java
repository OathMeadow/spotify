package com.spotify.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class MusicRecordConverterTest {

	private final MusicRecordConverter converter = new MusicRecordConverter();

	@Test
	void shouldConvertRecord() {
		// Arrange
		Map<String, String> data = new HashMap<>();
		data.put("", "10");
		data.put("Track.Name", "Simon is Best");
		data.put("Popularity", "1000");
		data.put("Genre", "Super heavy pop");
		data.put("Length.", "50000");
		data.put("Artist.Name", "Eric Madson");


		// Act
		MusicRecord record = converter.convert(data);

		// Assert
		assertEquals("10", record.getRank());
		assertEquals("Simon is Best", record.getTrackName());
		assertEquals("1000", record.getPopularity());
		assertEquals("Super heavy pop", record.getGenre());
		assertEquals("Eric Madson", record.getArtistName());
		assertEquals("50000", record.getLength());
	}

	@Test
	void shouldConvertRecordWithSomeNullValues() {
		// Arrange
		Map<String, String> data = new HashMap<>();
		data.put("", "55");
		data.put("Track.Name", null);

		// Act
		MusicRecord record = converter.convert(data);

		//Assert
		assertEquals("55", record.getRank());
		assertNull(record.getTrackName());
	}

	@Test
	void shouldConvertRecordWithBadHeaders() {
		// Arrange
		Map<String, String> data = new HashMap<>();
		data.put("test", "nothing to declare");
		data.put("Artist.Name", "Janne");

		// Act
		MusicRecord record = converter.convert(data);

		// Assert
		assertNull(record.getGenre());
		assertNull(record.getRank());
		assertNull(record.getPopularity());
		assertNull(record.getLength());
		assertNull(record.getTrackName());
		assertEquals("Janne", record.getArtistName());
	}
}
