package fr.isep.ii3510.movieman.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieResponse {

    private List<Movie> results;
    private Integer page;
    private Integer total_results;
    private Integer total_pages;

    public MovieResponse(List<Movie> results, Integer page, Integer total_results, Integer total_pages) {
        this.results = results;
        this.page = page;
        this.total_results = total_results;
        this.total_pages = total_pages;
    }
}
