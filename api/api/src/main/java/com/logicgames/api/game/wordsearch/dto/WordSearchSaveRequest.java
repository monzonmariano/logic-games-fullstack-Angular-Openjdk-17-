package com.logicgames.api.game.wordsearch.dto;


import lombok.Data;
import java.util.List;


@Data
public class WordSearchSaveRequest {

    private List<String> foundWords;
    private Long timeElapsedSeconds;

}
