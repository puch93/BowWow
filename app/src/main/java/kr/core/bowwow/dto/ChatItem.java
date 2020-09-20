package kr.core.bowwow.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ChatItem implements Serializable {

    String t_idx;
    String t_site;
    String t_user_idx;
    String t_type;
    String t_msg;
    String t_sound;
    String t_regdate;
    String t_editdate;
    String num;
    String duration;
    String currTime;
    byte[] bytes;
    boolean isSelected = false;
    boolean isPlay = false;

}
