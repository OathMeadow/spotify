package com.spotify.server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.spotify.csv.CsvParser;
import com.spotify.json.MusicRecord;
import com.spotify.json.MusicRecordConverter;

public class MusicRecordCollection {

	private final Map<String, MusicRecord> musicRecordMap = new LinkedHashMap<>();
	private static final Map<String, Function<MusicRecord, String>> SEARCH_FUNC = Map.of(
			"rank",  MusicRecord::getRank,
			"artistName",  MusicRecord::getArtistName,
			"trackName",  MusicRecord::getTrackName,
			"genre",  MusicRecord::getGenre,
			"length",  MusicRecord::getLength,
			"popularity",  MusicRecord::getPopularity
	);

	private MusicRecordCollection(List<MusicRecord> records) {
		records.stream()
				.sorted(Comparator.comparing(MusicRecord::getRank))
				.forEach(m -> musicRecordMap.put(m.getRank(), m));
	}

	public static MusicRecordCollection getCollection(File csv) throws IOException {
		List<MusicRecord> musicRecords;
		MusicRecordConverter converter = new MusicRecordConverter();
		try (CsvParser parser = new CsvParser(new FileReader(csv))) {
			musicRecords = parser.readAll().stream()
					.map(v -> converter.convert(v.getValues()))
					.collect(Collectors.toList());
		}
		return new MusicRecordCollection(musicRecords);
	}

	public Optional<MusicRecord> getRecordByTank(String rank) {
		return Optional.ofNullable(musicRecordMap.get(rank));
	}

	public List<MusicRecord> getRecords() {
		return new ArrayList<>(musicRecordMap.values());
	}

	public List<MusicRecord> searchRecord(String var, String value) {
		// Not very good performance search
		final Function<MusicRecord, String> searchFunc = Optional.ofNullable(var)
				.map(SEARCH_FUNC::get)
				.orElseThrow(() -> new IllegalArgumentException("No such field \"" + var + "\""));

		if (value == null) {
			return List.of();
		}
		return musicRecordMap.values().stream()
				.filter(v -> searchFunc.apply(v).equals(value))
				.collect(Collectors.toList());
	}
}
