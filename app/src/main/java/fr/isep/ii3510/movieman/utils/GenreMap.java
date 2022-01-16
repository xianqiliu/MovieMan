package fr.isep.ii3510.movieman.utils;

import fr.isep.ii3510.movieman.models.Genre;

import java.util.HashMap;
import java.util.List;

public class GenreMap {

    public static void getGenresList(List<Genre> genres) {
        if (genres == null) return;
        HashMap<Integer, String> genreMap = new HashMap<>();
        for (Genre genre : genres) {
            genreMap.put(genre.getId(), genre.getName());
        }
    }
}
