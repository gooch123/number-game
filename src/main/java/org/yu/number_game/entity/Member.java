package org.yu.number_game.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Member {

    @Id
    private String username;
    private String password;
    private String nickname;

}
