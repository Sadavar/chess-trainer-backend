package com.spring.chess;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class ChessController {

    @GetMapping("/getPGNs")
    @ResponseBody
    public String getPGNs(@RequestParam String username) {
//      https://api.chess.com/pub/player/{username}/games/{YYYY}/{MM}/pgn
//      https://api.chess.com/pub/player/{username}/games/{YYYY}/{MM}
        System.out.println("username: " + username);
        WebClient client = WebClient.builder()
                .baseUrl("https://api.chess.com")
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build();

        return client.get().uri("/pub/player/{username}/games/2023/07", username)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }



    // getTactics input: json of game PGNs, output: json of tactic PGNs where tactic was missed
    @GetMapping("getTactics")
    @ResponseBody
    public List<String> getTactics(@RequestParam String pgns) throws Exception {
        if(pgns == null || pgns.isEmpty()) {
            System.out.println("pgn is null");
            return List.of();
        } else {
            System.out.println("pgn is not null!");
            System.out.println("pgn: " + pgns);
        }
        return givenPythonScript_whenPythonProcessInvoked_thenSuccess(pgns);
    }

    public List<String> givenPythonScript_whenPythonProcessInvoked_thenSuccess(String pgns) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("python3", resolvePythonScriptPath("src/main/python/script.py"));
        processBuilder.redirectErrorStream(true);

        PrintWriter writer = new PrintWriter("src/main/python/pgn.txt");
        writer.print("");
        writer.close();

        PrintWriter out = new PrintWriter("src/main/python/pgn.txt");
        out.println(pgns);
        out.close();

        Process process = processBuilder.start();
        System.out.println("starting!");
        List<String> results = readProcessOutput(process.getInputStream());
        for(String result : results) {
            System.out.println("Results: " + result);
        }
        int exitCode = process.waitFor();
        return results;
    }
    private String resolvePythonScriptPath(String path) {
        File file = new File(path);
        return file.getAbsolutePath();
    }
    private List<String> readProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))) {
            return output.lines()
                    .collect(Collectors.toList());
        }
    }


}
