package fr.isep.ii3510.movieman.utils;

import fr.isep.ii3510.movieman.models.Genre;

import java.util.HashMap;
import java.util.List;

public class GenreMap {

    private static HashMap<Integer, String> genreMap;

    public static boolean isGenresListLoaded() {
        return (genreMap != null);
    }

    public static void getGenresList(List<Genre> genres) {
        if (genres == null) return;
        genreMap = new HashMap<>();
        for (Genre genre : genres) {
            genreMap.put(genre.getId(), genre.getName());
        }
    }

    public static String getGenreName(Integer genreId) {
        if (genreId == null) return null;
        return genreMap.get(genreId);
    }
}
