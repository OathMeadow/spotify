package com.spotify.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.json.MusicRecord;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class ServerVerticleTest {

	static private final int port = 8080;
	static private final String host = "localhost";
	private final ObjectMapper mapper = new ObjectMapper();

	@BeforeEach
	void deployVerticle(Vertx vertx, VertxTestContext testContext) {
		vertx.deployVerticle(new ServerVerticle(), testContext.succeedingThenComplete());
	}

	private List<MusicRecord> decodeJsonArray(String str) {
		JsonArray array = new JsonArray(str);
		return array.stream().map(v -> decodeJson(v.toString())).collect(Collectors.toList());
	}

	private MusicRecord decodeJson(String str) {
		try {
			return mapper.readValue(str, MusicRecord.class);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Test
	void shouldListRecords(Vertx vertx, VertxTestContext testContext) {
		HttpClient client = vertx.createHttpClient();
		client.request(HttpMethod.GET, port, host, "/api/records")
				.compose(req -> req.send().compose(HttpClientResponse::body))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					List<MusicRecord> records = decodeJsonArray(buffer.toString());
					assertEquals(50, records.size());
					testContext.completeNow();
				})));
	}

	@Test
	void shouldListRecordsWithArtistName(Vertx vertx, VertxTestContext testContext) {
		HttpClient client = vertx.createHttpClient();
		client.request(HttpMethod.GET, port, host, "/api/records?var=artistName&value=Daddy%20Yankee")
				.compose(req -> req.send().compose(HttpClientResponse::body))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					List<MusicRecord> record = decodeJsonArray(buffer.toString());
					assertEquals(1, record.size());
					assertEquals("Daddy Yankee", record.get(0).getArtistName());
					testContext.completeNow();
				})));
	}

	@Test
	void shouldListRecordsWithTrackName(Vertx vertx, VertxTestContext testContext) {
		HttpClient client = vertx.createHttpClient();
		client.request(HttpMethod.GET, port, host, "/api/records?var=trackName&value=Ransom")
				.compose(req -> req.send().compose(HttpClientResponse::body))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					List<MusicRecord> record = decodeJsonArray(buffer.toString());
					assertEquals(1, record.size());
					assertEquals("Ransom", record.get(0).getTrackName());
					testContext.completeNow();
				})));
	}

	@Test
	void shouldListRecordsWithPopularity(Vertx vertx, VertxTestContext testContext) {
		HttpClient client = vertx.createHttpClient();
		client.request(HttpMethod.GET, port, host, "/api/records?var=popularity&value=89")
				.compose(req -> req.send().compose(HttpClientResponse::body))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					List<MusicRecord> record = decodeJsonArray(buffer.toString());
					assertEquals(8, record.size());
					assertEquals("89", record.get(0).getPopularity());
					testContext.completeNow();
				})));
	}

	@Test
	void shouldListRecordsWithGenre(Vertx vertx, VertxTestContext testContext) {
		HttpClient client = vertx.createHttpClient();
		client.request(HttpMethod.GET, port, host, "/api/records?var=genre&value=latin")
				.compose(req -> req.send().compose(HttpClientResponse::body))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					List<MusicRecord> record = decodeJsonArray(buffer.toString());
					assertEquals(5, record.size());
					assertEquals("latin", record.get(0).getGenre());
					testContext.completeNow();
				})));
	}

	@Test
	void shouldListRecordsWithLength(Vertx vertx, VertxTestContext testContext) {
		HttpClient client = vertx.createHttpClient();
		client.request(HttpMethod.GET, port, host, "/api/records?var=length&value=164")
				.compose(req -> req.send().compose(HttpClientResponse::body))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					List<MusicRecord> record = decodeJsonArray(buffer.toString());
					assertEquals(1, record.size());
					assertEquals("164", record.get(0).getLength());
					assertEquals("Martin Garrix", record.get(0).getArtistName());
					testContext.completeNow();
				})));
	}

	@Test
	void shouldFetchRecord(Vertx vertx, VertxTestContext testContext) {
		HttpClient client = vertx.createHttpClient();
		client.request(HttpMethod.GET, port, host, "/api/records/5")
				.compose(req -> req.send().compose(HttpClientResponse::body))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					MusicRecord record = decodeJson(buffer.toString());
					assertEquals("Post Malone", record.getArtistName());
					assertEquals("dfw rap", record.getGenre());
					assertEquals("175", record.getLength());
					assertEquals("94", record.getPopularity());
					assertEquals("5", record.getRank());
					testContext.completeNow();
				})));
	}

	@Test
	void shouldNotFetchRecordWhenRankDoesNotExist(Vertx vertx, VertxTestContext testContext) {
		HttpClient client = vertx.createHttpClient();
		client.request(HttpMethod.GET, port, host, "/api/records/51")
				.compose(req -> req.send().onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					assertEquals(404, buffer.statusCode());
					testContext.completeNow();
				}))));
	}

	@Test
	void shouldNotFetchRecordWhenNoVarSpecified(Vertx vertx, VertxTestContext testContext) {
		HttpClient client = vertx.createHttpClient();
		client.request(HttpMethod.GET, port, host, "/api/records?value=Daddy%20Yankee")
				.compose(req -> req.send().onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					assertEquals(400, buffer.statusCode());
					testContext.completeNow();
				}))));
	}

	@Test
	void shouldNotFetchRecordWhenNoValueSpecified(Vertx vertx, VertxTestContext testContext) {
		HttpClient client = vertx.createHttpClient();
		client.request(HttpMethod.GET, port, host, "/api/records?var=rank")
				.compose(req -> req.send().compose(HttpClientResponse::body))
				.onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					assertTrue(buffer.toJsonArray().isEmpty());
					testContext.completeNow();
				})));
	}

	@Test
	void shouldNotFetchRecordWhenBadUri(Vertx vertx, VertxTestContext testContext) {
		HttpClient client = vertx.createHttpClient();
		client.request(HttpMethod.GET, port, host, "/api/record")
				.compose(req -> req.send().onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					assertEquals(404, buffer.statusCode());
					testContext.completeNow();
				}))));
	}

	@Test
	void shouldNotFetchRecordWhenUsingIllegalVar(Vertx vertx, VertxTestContext testContext) {
		HttpClient client = vertx.createHttpClient();
		client.request(HttpMethod.GET, port, host, "/api/records?var=ranking&value=1")
				.compose(req -> req.send().onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
					assertEquals(400, buffer.statusCode());
					testContext.completeNow();
				}))));
	}
}
