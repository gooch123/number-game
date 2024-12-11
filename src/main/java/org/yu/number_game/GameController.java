package org.yu.number_game;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.yu.number_game.entity.Member;

import java.util.List;

@Controller
@RequestMapping("/game")
@Slf4j
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public String gameHome(Model model) {
        model.addAttribute("message", "로그인 후 게임을 시작하세요!");
        return "game/index";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        Member member = gameService.getMember(username);
        if (member == null) {
            Member saveMember = gameService.saveMember(username, password);
            session.setAttribute("member", saveMember);
            return "redirect:/game/ready";
        } else {
            if (member.getPassword().equals(password)) {
                model.addAttribute("round", 1);
                model.addAttribute("score", 0);
                session.setAttribute("member", member);
                return "redirect:/game/ready";
            }
            model.addAttribute("error", "비밀번호가 틀렸습니다");
            model.addAttribute("username", username);
        }
        return "game/index";
    }

    @GetMapping("/ready")
    public String gameReady(HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("member");

        if (member == null) {
            // 로그인하지 않은 경우 로그인 화면으로 리다이렉트
            return "game/index";
        }
        List<Member> memberRanking = gameService.findMemberRanking();
        model.addAttribute("ranking", memberRanking);
        model.addAttribute("member", member);
        return "game/ready"; // 준비 화면 템플릿
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 무효화
        return "game/index"; // 로그인 화면으로 이동
    }

    @GetMapping("/start")
    public String startGame(Model model) {
        model.addAttribute("round", 1);
        model.addAttribute("score", 0);
        return "game/start";
    }

    @GetMapping("/round/{roundNumber}")
    public String playRound(@PathVariable int roundNumber,
                            @RequestParam int score,
                            Model model) {
        if (roundNumber > 10) {
            model.addAttribute("finalScore", score);
            return "game/finish";
        }

        String problem = gameService.generateProblem(roundNumber);
        model.addAttribute("problem", problem);
        model.addAttribute("round", roundNumber);
        model.addAttribute("score", score);
        model.addAttribute("startTime", System.currentTimeMillis());
        return "game/round";
    }

    @PostMapping("/round/{roundNumber}/submit")
    public String submitAnswer(
            @PathVariable int roundNumber,
            @RequestParam String problem,
            @RequestParam(defaultValue = "0") int userAnswer,
            @RequestParam long startTime,
            @RequestParam int score,
            Model model) {

        int correctAnswer = gameService.calculateAnswer(problem);
        long currentTime = System.currentTimeMillis();
        long remainingTime = 30000 - (currentTime - startTime);

        if (remainingTime <= 0) {
            return "redirect:/game/gameover?round=" + roundNumber + "&score=" + score; // 게임 오버
        }

        if (userAnswer == correctAnswer) {
            int earnedScore = gameService.calculateScore(remainingTime);
            score += earnedScore;

            if (roundNumber == 10) { // 마지막 라운드를 클리어
                return "redirect:/game/finish?round=" + roundNumber + "&score=" + score;
            }

            return "redirect:/game/round/" + (roundNumber + 1) + "?score=" + score;
        }

        // 오답 처리
        model.addAttribute("message", "오답입니다! 다시 시도하세요.");
        model.addAttribute("problem", problem);
        model.addAttribute("round", roundNumber);
        model.addAttribute("score", score);
        model.addAttribute("startTime", startTime);
        model.addAttribute("remainingTime", remainingTime);
        return "game/round";
    }


    // 게임 오버 처리
    @GetMapping("/gameover")
    public String gameOver(@RequestParam int round, @RequestParam int score, Model model,
                           @SessionAttribute("member") Member member) {
        model.addAttribute("round", round - 1); // 클리어한 라운드 수
        model.addAttribute("score", score); // 최종 점수
        model.addAttribute("message", "게임 오버! 결과를 확인하세요.");
        gameService.updateScore(member.getUsername(), score);
        log.info("member = {}",member.getUsername());
        return "game/result";
    }

    // 게임 클리어 처리
    @GetMapping("/finish")
    public String finishGame(@RequestParam int round, @RequestParam int score, Model model,
                             @SessionAttribute("member") Member member) {
        model.addAttribute("round", round); // 클리어한 라운드 수
        model.addAttribute("score", score); // 최종 점수
        model.addAttribute("message", "축하합니다! 모든 라운드를 클리어했습니다!");
        gameService.updateScore(member.getUsername(), score);
        log.info("member = {}",member.getUsername());
        return "game/result";
    }

}

