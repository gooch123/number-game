package org.yu.number_game.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Member {

    @Id
    private String username;
    private String password;
    private int highScore;

    public void updateHighScore(int score) {
        this.highScore = Math.max(score, this.highScore);
    }

}
