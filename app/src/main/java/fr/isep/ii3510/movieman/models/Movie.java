package fr.isep.ii3510.movieman.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Movie {

    private Boolean adult;
    private String backdrop_path;
    private Integer budget;
    private List<Genre> genres;
    private String homepage;
    private Integer id;
    private String imdb_id;
    private String original_language;
    private String original_title;
    private String overview;
    private Double popularity;
    private String poster_path;
    private String release_date;
    private Integer revenue;
    private Integer runtime;
    private String status;
    private String tagline;
    private String title;
    private Boolean video;
    private Double vote_average;
    private Integer vote_count;

    private List<Integer> genre_ids;

    // brief
    public Movie(Integer id, String title, String poster_path){
        this.id = id;
        this.title = title;
        this.poster_path = poster_path;
    }

    // detail
    public Movie(Boolean adult, String backdrop_path, Integer budget, List<Genre> genres, String homepage, Integer id, String imdb_id, String original_language, String original_title, String overview, Double popularity, String poster_path, String release_date, Integer revenue, Integer runtime, String status, String tagline, String title, Boolean video, Double vote_average, Integer vote_count) {
        this.adult = adult;
        this.backdrop_path = backdrop_path;
        this.budget = budget;
        this.genres = genres;
        this.homepage = homepage;
        this.id = id;
        this.imdb_id = imdb_id;
        this.original_language = original_language;
        this.original_title = original_title;
        this.overview = overview;
        this.popularity = popularity;
        this.poster_path = poster_path;
        this.release_date = release_date;
        this.revenue = revenue;
        this.runtime = runtime;
        this.status = status;
        this.tagline = tagline;
        this.title = title;
        this.video = video;
        this.vote_average = vote_average;
        this.vote_count = vote_count;
    }

}
