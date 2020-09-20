package kr.core.bowwow.dto;

import lombok.Data;

@Data
public class DogstatsItem {

    String itemtype;        // 유래 : m1, 특징 : m2, 성격 : m3, 질병 : m4
    String breed;
    String mTitle;
    String explanation;
    String stats01;
    String stats02;
    String stats03;
    String stats04;

}
