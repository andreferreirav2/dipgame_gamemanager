package pt.up.fe.mieic.andreferreira.dipblue.game;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class GameRunner {

    public GameRunner(GameManager app) {
        time = 0x6ddd00;
        processes = new Vector<Process>();
        humans = new Vector<Process>();
        gameId = "gameManager";
        this.app = app;
    }

    public void run(HashMap<?, ?> loadedPaths, HashMap<?, ?> loadedPlayers, java.util.List<?> players) {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                destroyProcesses();
            }
        });

        processes = new Vector<Process>(8);
        try {
            if (loadedPaths.containsKey("AISERVER_PATH")) {
                String cmd[] = {"cmd", "/c", (new StringBuilder(String.valueOf(loadedPaths.get("AISERVER_PATH"))))
                        .append("//AiServer").toString(), "-var=standard", "-port=16713", "-start", "-exit=0", "-h",
                        "-kill=50", "-lvl=0", "-mtl=20", "-rtl=10", "-btl=10", "-npr", "-npb", "-ptl=0"};
                ProcessBuilder processBuilder = new ProcessBuilder(cmd);
                processBuilder.directory(new File((String) loadedPaths.get("AISERVER_PATH")));
                processes.add(processBuilder.start());
            }
            else {
                String cmd[] = {(String) loadedPaths.get("PARLANCE_PATH"), "-g1", "standard"};
                processes.add(Runtime.getRuntime().exec(cmd));
            }
            String call = processProgramCall(loadedPaths, (new StringBuilder("<JAVA_ENV> -jar programs/negoServer-2" +
                    ".1-full.jar <nego_port> <ip> <port> ")).append(time).append("STDOUT").toString(), "negoServer");
            Process negoServer = Runtime.getRuntime().exec(call);
            processes.add(negoServer);

            try {
                boolean isNegoStarted = false;
                InputStreamReader reader = new InputStreamReader(negoServer.getInputStream());
                String readText = "";
                int n;
                char buffer[];

                do {
                    buffer = new char[512];
                    n = reader.read(buffer);
                    if (n > 0) {
                        readText = new String(buffer, 0, n);
						if (!isNegoStarted && (readText.contains(" YES ( MAP (") || readText.contains(" 'standard' ) )"))) {
                            isNegoStarted = true;
                            for (Iterator<?> iterator = players.iterator(); iterator.hasNext(); ) {
                                String player[] = (String[]) iterator.next();
                                if (!player[0].equals("empty")) {
                                    String programCall = processProgramCall(loadedPaths,(String) loadedPlayers.get(player[0]), player[1]);
                                    if (player[0].equals("human") || player[0].equals("testBot")) {
                                        humans.add(Runtime.getRuntime().exec(programCall));
                                    }
                                    else {
                                        processes.add(Runtime.getRuntime().exec(programCall));
                                    }
                                }
                            }
                            break;
                        }
                    }
                    Thread.currentThread();
                    Thread.sleep(100L);
                } while (true);
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (isRunning(processes.get(0))) {
                Thread.sleep(200L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
    }

    boolean isRunning(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private String processProgramCall(HashMap<?, ?> loadedPaths, String programCallTemplate, String playerName) {
        String programCall = "";
        String tag = "";
        int i = 0;
        try {
            boolean isTranslating = false;
            for (; i < programCallTemplate.length(); i++) {
                char c = programCallTemplate.charAt(i);
                if (c == '<') {
                    if (!isTranslating) {
                        isTranslating = true;
                    }
                    else {
                        System.err.println((new StringBuilder("Syntax error in ")).append(programCallTemplate
                                .substring(0, i)).toString());
                    }
                }
                else if (c == '>') {
                    if (isTranslating) {
                        programCall = (new StringBuilder(String.valueOf(programCall))).append(translateTag
                                (loadedPaths, tag, playerName)).toString();
                        tag = "";
                        isTranslating = false;
                    }
                    else {
                        System.err.println((new StringBuilder("Syntax error in ")).append(programCallTemplate
                                .substring(0, i)).toString());
                    }
                }
                else if (isTranslating) {
                    tag = (new StringBuilder(String.valueOf(tag))).append(c).toString();
                }
                else {
                    programCall = (new StringBuilder(String.valueOf(programCall))).append(c).toString();
                }
            }

        } catch (Exception e) {
            System.err.println((new StringBuilder("Syntax error in ")).append(programCallTemplate.substring(0,
                    i)).append(". ").append(e.getMessage()).toString());
        }
        return programCall;
    }

    private String translateTag(HashMap<?, ?> loadedPaths, String tag, String playerName) throws Exception {
        if (tag.equals("ip")) {
            return "localhost";
        }
        if (tag.equals("port")) {
            return "16713";
        }
        if (tag.equals("name")) {
            return playerName;
        }
        if (tag.equals("log_folder")) {
            return "logs";
        }
        if (tag.equals("nego_ip")) {
            return "localhost";
        }
        if (tag.equals("nego_port")) {
            return "16714";
        }
        if (tag.equals("game_id")) {
            return gameId;
        }
        if (tag.equals("time")) {
            return String.valueOf(time);
        }
        if (loadedPaths.containsKey(tag)) {
            return (String) loadedPaths.get(tag);
        }
        else {
            throw new Exception((new StringBuilder("Unknown tag '")).append(tag).append("'.").toString());
        }
    }

    private void destroyProcesses() {
        Process process;
        for (Iterator<Process> iterator = processes.iterator(); iterator.hasNext(); process.destroy()) {
            process = iterator.next();
        }

    }

    public void close() {
        destroyProcesses();
        Process human;
        for (Iterator<Process> iterator = humans.iterator(); iterator.hasNext(); human.destroy()) {
            human = iterator.next();
        }
    }

    private int time;
    private java.util.List<Process> processes;
    private java.util.List<Process> humans;
    private GameManager app;
    private String gameId;

}
