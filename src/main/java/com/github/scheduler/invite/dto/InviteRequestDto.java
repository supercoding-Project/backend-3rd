package com.github.scheduler.invite.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class InviteRequestDto {
    private List<String> emailList;
}
