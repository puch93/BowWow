package kr.core.bowwow.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class DictionaryData implements Serializable {
    private int image;
    private String name;

    public DictionaryData(int image, String name) {
        this.image = image;
        this.name = name;
    }
}
