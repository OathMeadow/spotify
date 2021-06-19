package com.spotify.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MusicRecord {
	@JsonProperty(required = true)
	private String rank;
	private String artistName;
	private String trackName;
	private String genre;
	private String length;
	private String popularity;
}
