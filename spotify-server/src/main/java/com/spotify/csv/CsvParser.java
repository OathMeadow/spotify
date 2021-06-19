package com.spotify.csv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CsvParser implements AutoCloseable {

	private static final Pattern REGEX = Pattern.compile("\"(.*?)\"[,]*|([-]*\\d+)[,]*");
	private final BufferedReader reader;
	private List<String> headers;

	public CsvParser(FileReader reader) {
		this.reader = new BufferedReader(reader);
		this.headers = new ArrayList<>();
	}

	public List<CsvRecord> readAll() throws IOException {
		try {
			return reader.lines()
					.map(this::parse)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}

	public CsvRecord read() throws IOException {
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				CsvRecord record = parse(line);
				if (record != null) {
					return record;
				}
			}
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
		return null;
	}

	private CsvRecord parse(String line) {
		if (headers.isEmpty()) {
			headers = parseValues(line);
			return null;
		}
		List<String> values = parseValues(line);
		if (!values.isEmpty() && values.size() != headers.size()) {
			throw new UncheckedIOException(new IOException("CSV file header size do not match the value size"));
		}
		if (!values.isEmpty()) {
			return new CsvRecord(headers, values);
		}
		return null;
	}

	private List<String> parseValues(String line) {
		List<String> results = new ArrayList<>();
		Matcher m = REGEX.matcher(line);
		while (m.find()) {
			results.add(Optional.ofNullable(m.group(1)).orElse(m.group(2)));
		}
		return results;
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}
}
