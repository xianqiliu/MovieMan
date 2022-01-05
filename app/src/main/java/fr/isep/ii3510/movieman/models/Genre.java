package fr.isep.ii3510.movieman.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Genre {

    private Integer id;
    private String name;

    public Genre(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

}
