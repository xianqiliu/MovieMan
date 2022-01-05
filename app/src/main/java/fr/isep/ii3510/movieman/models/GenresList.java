package fr.isep.ii3510.movieman.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenresList {

    private List<Genre> genres;

    public GenresList(List<Genre> genres) {
        this.genres = genres;
    }

}
