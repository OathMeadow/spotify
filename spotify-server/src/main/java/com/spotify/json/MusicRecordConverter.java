package com.spotify.json;

import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MusicRecordConverter {

	private final ObjectMapper mapper = new ObjectMapper();

	private static final Map<String, BiConsumer<String, MusicRecord>> TRANSLATOR = Map.of(
			"", (v, m) -> m.setRank(v),
			"Track.Name", (v, m) -> m.setTrackName(v),
			"Artist.Name", (v, m) -> m.setArtistName(v),
			"Genre", (v, m) -> m.setGenre(v),
			"Length.", (v, m) -> m.setLength(v),
			"Popularity", (v, m) -> m.setPopularity(v)
	);

	public MusicRecord convert(Map<String, String> record) throws UncheckedIOException {
		MusicRecord mr = new MusicRecord();
		record.forEach((key, value) ->
				Optional.ofNullable(TRANSLATOR.get(key)).ifPresent(consumer -> consumer.accept(value, mr))
		);

		try {
			mapper.writeValueAsString(mr);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
		return mr;
	}
}
