package com.spotify.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.json.MusicRecord;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class ServerVerticle extends AbstractVerticle {

	private static final Logger LOG = Logger.getLogger(ServerVerticle.class.getName());
	private static final String MEDIA_TYPE_APPLICATION_JSON = "application/json";
	private static final int DEFAULT_PORT = 8080;

	private static final String PARAM_PORT = "PORT";
	private static final String PARAM_CSV_FILE = "CSV_FILE";

	private HttpServer server;
	private MusicRecordCollection recordCollection;
	private final ObjectMapper mapper = new ObjectMapper();

	private String getDefaultCsv() {
		InputStream resource = ServerVerticle.class.getResourceAsStream("top50.csv");
		if (resource == null) {
			return "";
		}
		try (resource) {
			Path dest = Path.of(Files.createTempDirectory("spotify").toString(), "top50.csv");
			Files.copy(resource, dest);
			return dest.toString();
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Could not load default csv file: ", e.getMessage());
		}
		return "";
	}

	@Override
	public void start(Promise<Void> startPromise) {
		final int port = Vertx.currentContext().config().getInteger(PARAM_PORT, DEFAULT_PORT);
		String csvFile = Vertx.currentContext().config().getString(PARAM_CSV_FILE, getDefaultCsv());

		if (csvFile.isBlank()) {
			startPromise.fail("No CSV file specified");
			LOG.log(Level.SEVERE, () -> "No CSV file specified in parameter \"" + PARAM_CSV_FILE + "\"");
			return;
		}

		try {
			recordCollection = MusicRecordCollection.getCollection(new File(csvFile));
		} catch (IOException e) {
			startPromise.fail(e);
			LOG.log(Level.SEVERE, () -> "Unable to parse CSV file " + csvFile + ": " + e.getMessage());
			return;
		}

		server = vertx.createHttpServer(new HttpServerOptions().setAlpnVersions(List.of(HttpVersion.HTTP_2)));
		server.requestHandler(initRouters()).listen(port, http -> {
			if (http.succeeded()) {
				startPromise.complete();
				LOG.log(Level.INFO, () -> "HTTP server started on port " + port);
			} else {
				startPromise.fail(http.cause());
				LOG.log(Level.SEVERE, () -> "HTTP server failed to start on port " + port);
			}
		});
	}

	private Router initRouters() {
		Router router = Router.router(vertx);
		router.route().failureHandler(h -> {
			if (h.failure() instanceof UncheckedIOException) {
				LOG.log(Level.INFO, () -> h.request().connection().remoteAddress() +
						" - Decoding failure in endpoint " + h.request().path() + " " + h.failure().getMessage());
				h.response().setStatusCode(500).end();
			} else if (h.failure() instanceof IllegalArgumentException) {
				h.response().setStatusCode(400).end(h.failure().getMessage());
			}
		});
		router.get("/api/records").produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::handleListRecords);
		router.get("/api/records/:recordID").produces(MEDIA_TYPE_APPLICATION_JSON).handler(this::handleFetchRecord);
		return router;
	}

	private void handleListRecords(RoutingContext ctx) {
		List<MusicRecord> records = new ArrayList<>();
		if (ctx.queryParams().isEmpty()) {
			records = recordCollection.getRecords();
		} else {
			String var = ctx.queryParams().get("var");
			String value = ctx.queryParams().get("value");
			Optional.of(recordCollection.searchRecord(var, value)).ifPresent(records::addAll);
		}
		ctx.response().setStatusCode(200);
		ctx.response().putHeader("Content-Type", MEDIA_TYPE_APPLICATION_JSON);
		ctx.response().end(writeJson(records));
	}

	private void handleFetchRecord(RoutingContext ctx) {
		String recordID = ctx.pathParam("recordID");
		recordCollection.getRecordByTank(recordID).ifPresentOrElse(m -> {
			ctx.response().setStatusCode(200);
			ctx.response().putHeader("Content-Type", MEDIA_TYPE_APPLICATION_JSON);
			ctx.response().end(writeJson(m));
		}, () -> {
			ctx.response().setStatusCode(404);
			ctx.response().end();
		});
	}

	private String writeJson(Object record) {
		try {
			return mapper.writeValueAsString(record);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void stop() {
		server.close();
	}
}
