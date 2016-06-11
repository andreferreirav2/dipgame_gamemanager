package pt.up.fe.mieic.andreferreira.dipblue.game;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class GameManager {

    public static final String DUMBBOT = "dumbbot";
    public static final String HOLDBOT = "holdbot";
    public static final String RANDOMBOT = "randombot";
    public static final String RANDOMNEGO = "randomNego";
    public static final String RANDOMNEGOTIATIOR_DUMBBOT = "randomnegotiator_dumbbot";
    public static final String RANDOMNEGOTIATIOR_RANDOMBOT = "randomnegotiator_randombot";
    public static final String HUMAN = "human";
    public static final String EMPTY = "empty";

    public boolean isBotNegotiation = false;
    private HashMap<String, String> loadedPaths;
    private HashMap<String, String> loadedPlayers;
    private Vector<String> availablePlayerNames;
    private List<String[]> players = new Vector<String[]>(7);
    private GameRunner gameRunner;
    private List<String> alerts;

    public void setAllPlayers(String type, String name) {
        for (int i = players.size(); players.size() < 7; i++) {
            players.add(new String[]{type, name + i});
        }
    }

    public void setPlayer(String type, String name) {
        players.add(new String[]{type, name});
    }

    public void setIsBotNegotiation(boolean isBotNegotiation) {
        this.isBotNegotiation = isBotNegotiation;
    }

    public void run() throws IOException {
        alerts = new Vector<String>();

        if (!new File("logs").exists()) {
            new File("logs").mkdirs();
        }

        removeOldLogs();
        loadPaths();
        checkPaths();
        loadPlayers();

        this.gameRunner = new GameRunner(this);
        gameRunner.run(loadedPaths, loadedPlayers, players);
    }

    private void removeOldLogs() {
        File afile[];
        int j = (afile = (new File("logs")).listFiles()).length;
        for (int i = 0; i < j; i++) {
            File file = afile[i];
            file.delete();
        }

    }

    private void loadPaths() throws IOException {
        loadedPaths = new HashMap<String, String>();
        FileReader fr = new FileReader(new File("files/paths.txt"));
        LineNumberReader ln = new LineNumberReader(fr);
        for (String linea = ln.readLine(); linea != null; linea = ln.readLine()) {
            String words[] = linea.split(";");
            if (words[0].equals("JAVA_ENV")) {
                loadedPaths.put("JAVA_ENV", words[1]);
            }
            else if (words[0].equals("PARLANCE_PATH")) {
                loadedPaths.put("PARLANCE_PATH", words[1]);
            }
            else if (words[0].equals("AISERVER_PATH")) {
                loadedPaths.put("AISERVER_PATH", fixProgramFiles(words[1]));
            }
            else if (words[0].equals("AIMAPPER_PATH")) {
                loadedPaths.put("AIMAPPER_PATH", fixProgramFiles(words[1]));
            }
        }

        fr.close();
    }

    private String fixProgramFiles(String line) {
        if (line.contains("PROGRAM_FILES")) {
            try {
                String correctName = System.getenv("ProgramFiles");
                correctName = correctName.substring(2);
                correctName = (new StringBuilder("C://")).append(correctName).toString();
                return line.replaceAll("PROGRAM_FILES", correctName);
            } catch (Exception exception) {
            }
        }
        return line;
    }

    private void checkPaths() {
        List<String> pathAlerts = new Vector<String>();
        if (!loadedPaths.containsKey("JAVA_ENV")) {
            String path = (new StringBuilder(String.valueOf(System.getProperty("java.home")))).append("/bin/java")
                    .toString();
            loadedPaths.put("JAVA_ENV", path);
        }
        if (!loadedPaths.containsKey("PARLANCE_PATH") && !loadedPaths.containsKey("AISERVER_PATH")) {
            String path = fixProgramFiles("PROGRAM_FILES//daide//aiserver");
            if ((new File(path)).isDirectory()) {
                loadedPaths.put("AISERVER_PATH", path);
                pathAlerts.add((new StringBuilder("'AISERVER_PATH' is set to '")).append(path).append("'").toString());
            }
            else {
                loadedPaths.put("PARLANCE_PATH", "parlance-server");
                pathAlerts.add("'PARLANCE_PATH' is set to 'parlance-server'");
            }
        }
        if (pathAlerts.size() > 0) {
            String alertMessage = "Paths to the java and game server (parlance or AiServer) should be provided in the" +
                    " file 'paths.txt'. The application will be executed with the following paths set to default:\n";
            for (Iterator<String> iterator = pathAlerts.iterator(); iterator.hasNext(); ) {
                String pathAlert = iterator.next();
                alertMessage = (new StringBuilder(String.valueOf(alertMessage))).append(pathAlert).append("\n")
                        .toString();
            }

            alertMessage = (new StringBuilder(String.valueOf(alertMessage))).append("If Game Manager cannot find java" +
                    " and the game server, the execution of the application will fail.").toString();
            alerts.add(alertMessage);
        }
        String vers = System.getProperty("os.name").toLowerCase();
        if (vers.contains("windows")) {
            if (!loadedPaths.containsKey("AIMAPPER_PATH")) {
                String path = fixProgramFiles("PROGRAM_FILES//daide//aimapper");
                if ((new File(path)).isDirectory()) {
                    loadedPaths.put("AIMAPPER_PATH", path);
                    alerts.add((new StringBuilder("'AIMAPPER_PATH' is set to '")).append(path).append("'.\nIf Game " +
                            "Manager cannot find the AiMapper, the human player will not launch any mapper.")
                            .toString());
                }
            }
            if (!loadedPaths.containsKey("AISERVER_PATH") && !loadedPaths.containsKey("PARLANCE_PATH")) {
                String path = fixProgramFiles("PROGRAM_FILES//daide//aiserver");
                if ((new File(path)).isDirectory()) {
                    loadedPaths.put("AISERVER_PATH", path);
                    alerts.add((new StringBuilder("'AISERVER_PATH' is set to '")).append(path).append("'.\nIf Game " +
                            "Manager cannot find a game server, it will not be able to run games.").toString());
                }
                else {
                    loadedPaths.put("PARLANCE_PATH", "parlance-server");
                    alerts.add((new StringBuilder("'PARLANCE_PATH' is set to '")).append(path).append("'.\nIf Game " +
                            "Manager cannot find a game server, it will not be able to run games.").toString());
                }
            }
        }
        else {
            String as[];
            int j = (as = (new String[]{"JAVA_ENV", "PARLANCE_PATH"})).length;
            for (int i = 0; i < j; i++) {
                String key = as[i];
                if (loadedPaths.containsKey(key)) {
                    String path = fixProgramFiles(loadedPaths.get(key));
                    File filPath = new File(path);
                    if (!filPath.exists() || !filPath.canExecute()) {
                        alerts.add((new StringBuilder("Game Manager cannot find '")).append(key).append("' that is " +
                                "set to '").append(path).append("'. This should be fixed in order to be able to run " +
                                "games.").toString());
                    }
                }
            }

        }
    }

    private void loadPlayers() throws IOException {
        loadedPlayers = new HashMap<String, String>();
        availablePlayerNames = new Vector<String>();
        FileReader fr = new FileReader(new File("files/availablePlayers.txt"));
        LineNumberReader ln = new LineNumberReader(fr);
        String line = ln.readLine();
        try {
            while (line != null) {
                line = line.trim();
                if ((line.length() != 0) && !line.startsWith("//")) {
                    String key = line.substring(0, line.indexOf(';'));
                    String value = line.substring(line.indexOf('<'));
                    loadedPlayers.put(key, value);
                    availablePlayerNames.add(key);
                }
                line = ln.readLine();
            }
        } catch (StringIndexOutOfBoundsException e) {
            alerts.add((new StringBuilder("Syntax error in file 'files/availablePlayers.txt' line: '")).append(line)
                    .append("'.").toString());
        }
        ln.close();
    }
}
