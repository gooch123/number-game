package org.yu.number_game;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yu.number_game.entity.Member;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class GameService {

    private final Random random = new Random();
    private final MemberRepository memberRepository;

    public GameService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 점수 계산 로직
    public int calculateScore(long remainingTime) {
        return (int) remainingTime * 10;
    }

    // 라운드용 문제 생성 로직
    public String generateProblem(int roundNumber) {
        int numberOfTerms = roundNumber + 1;
        StringBuilder problem = new StringBuilder();

        for (int i = 0; i < numberOfTerms; i++) {
            int number = random.nextInt(1000) + 1; // 1 ~ 1000 사이 숫자
            if (i > 0) {
                problem.append(random.nextBoolean() ? " + " : " - ");
            }
            problem.append(number);
        }

        log.info("problem = {}",problem);
        return problem.toString();
    }

    // 수식 결과 계산 로직
    public int calculateAnswer(String problem) {
        return (int) Math.round(eval(problem)); // 간단히 수식 평가
    }

    // 간단한 수식 평가 함수 (eval 대체)
    private double eval(String expression) {
        // 공백 제거
        expression = expression.replace(" ", "");

        // 숫자와 연산자를 분리
        List<Double> numbers = new ArrayList<>();
        List<Character> operators = new ArrayList<>();

        int i = 0;
        while (i < expression.length()) {
            StringBuilder num = new StringBuilder();

            // 숫자 파싱
            while (i < expression.length() && Character.isDigit(expression.charAt(i))) {
                num.append(expression.charAt(i));
                i++;
            }
            numbers.add(Double.parseDouble(num.toString()));

            // 연산자 파싱
            if (i < expression.length()) {
                char operator = expression.charAt(i);
                if (operator == '+' || operator == '-') {
                    operators.add(operator);
                } else {
                    throw new IllegalArgumentException("Unsupported operator: " + operator);
                }
                i++;
            }
        }

        // 숫자와 연산자 계산
        double result = numbers.get(0);
        for (int j = 0; j < operators.size(); j++) {
            char operator = operators.get(j);
            double nextNumber = numbers.get(j + 1);
            if (operator == '+') {
                result += nextNumber;
            } else if (operator == '-') {
                result -= nextNumber;
            }
        }

        return result;
    }

    public Member getMember(String username) {
        return memberRepository.findByUsername(username);
    }

    public Member saveMember(String username, String password) {
        return memberRepository.save(new Member(
                username,
                password,
                0
        ));
    }

    @Transactional
    public void updateScore(String username, int score) {
        Member member = memberRepository.findByUsername(username);
        log.info("score = {}",score);
        member.updateHighScore(score);
    }

    public List<Member> findMemberRanking() {
        return memberRepository.findAllByOrderByHighScoreDesc(PageRequest.of(0,10));
    }
}

