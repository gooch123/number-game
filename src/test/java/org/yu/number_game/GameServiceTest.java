package org.yu.number_game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GameServiceTest {

    @Autowired
    GameService gameService;

    @Test
    void 수식_계산() {

        int answer = gameService.calculateAnswer("1+2");
        assertThat(answer).isEqualTo(1 + 2);

    }
}
