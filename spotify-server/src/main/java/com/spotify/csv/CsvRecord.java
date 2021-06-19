package com.spotify.csv;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;

public class CsvRecord {

	@Getter
	private final Map<String, String> values;

	public CsvRecord(List<String> headers, List<String> values) {
		if (headers.size() != values.size()) {
			throw new IllegalArgumentException("Headers and values must have the same size");
		}
		this.values = IntStream.range(0, headers.size())
				.boxed()
				.collect(Collectors.toMap(headers::get, values::get));
	}

	@Override
	public String toString() {
		return "CsvRecord{" +
				"values=" + values +
				'}';
	}
}
