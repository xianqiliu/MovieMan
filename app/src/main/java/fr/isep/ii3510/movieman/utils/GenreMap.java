package fr.isep.ii3510.movieman.utils;

import fr.isep.ii3510.movieman.models.Genre;

import java.util.HashMap;
import java.util.List;

public class GenreMap {

    public static void getGenreList(List<Genre> genreList) {
        if (genreList == null) return;
        HashMap<Integer, String> genreMap = new HashMap<>();
        for (Genre genre : genreList) {
            genreMap.put(genre.getId(), genre.getName());
        }
    }

    public static String getGenreListString(List<Genre> genreList) {
        String genres = "Type: ";

        if (genreList != null) {
            for (int i = 0; i < genreList.size(); i++) {
                if (genreList.get(i) == null) continue;
                if (i == genreList.size() - 1) genres = genres.concat(genreList.get(i).getName());
                else genres = genres.concat(genreList.get(i).getName() + ", ");
            }
        }

        return genres;
    }
}
