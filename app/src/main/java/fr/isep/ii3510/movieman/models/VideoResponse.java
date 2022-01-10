package fr.isep.ii3510.movieman.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoResponse {

    private Integer id;
    private List<Video> results;

    public VideoResponse(Integer id, List<Video> results) {
        this.id = id;
        this.results = results;
    }
}
