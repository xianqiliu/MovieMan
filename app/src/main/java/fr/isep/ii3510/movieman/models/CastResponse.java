package fr.isep.ii3510.movieman.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CastResponse {

    private Integer id;
    private List<Cast> cast;

    public CastResponse(Integer id, List<Cast> cast) {
        this.id = id;
        this.cast = cast;
    }
}
