package kr.core.bowwow.dto;

import lombok.Data;

@Data
public class DictionaryData {
    private String image;
    private String name;

    public DictionaryData(String image, String name) {
        this.image = image;
        this.name = name;
    }
}
