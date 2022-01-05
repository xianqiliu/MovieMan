package fr.isep.ii3510.movieman.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Video {

    private String id;
    private String key;
    private String name;
    private String site;
    private Integer size;
    private String type;

    public Video(String id, String key, String name, String site, Integer size, String type) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.site = site;
        this.size = size;
        this.type = type;
    }

}
