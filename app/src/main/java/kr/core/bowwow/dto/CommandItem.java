package kr.core.bowwow.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class CommandItem implements Serializable {

    String cidx;
    String itemimg;
    String itemname;
    String csound;

}
