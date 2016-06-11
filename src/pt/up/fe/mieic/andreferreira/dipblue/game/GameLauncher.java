package pt.up.fe.mieic.andreferreira.dipblue.game;

import es.csic.iiia.fabregues.dip.Observer;
import es.csic.iiia.fabregues.dip.comm.CommException;
import es.csic.iiia.fabregues.dip.comm.IComm;
import es.csic.iiia.fabregues.dip.comm.daide.DaideComm;
import pt.up.fe.mieic.andreferreira.dipblue.bot.*;
import pt.up.fe.mieic.andreferreira.dipblue.bot.adviser.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GameLauncher {

    public static Integer ROUND = 0;
    private static FileWriter writer;

    public static void main(String args[]) throws Exception {
        System.out.println("GameLauncher starting");
        
//        gameBatch(70, Archetype.NO_PRESS);
//        gameBatch(70, Archetype.SLAVE);
//        gameBatch(70, Archetype.NAIVE);
//        gameBatch(70, Archetype.DIPBLUE);
//        gameBatch(70, Archetype.SLAVE, Archetype.NAIVE);
//        gameBatch(70, Archetype.NAIVE, Archetype.DIPBLUE);
//        gameBatch(70, Archetype.DIPBLUE, Archetype.DIPBLUE);

        System.out.println("GameLauncher exiting");
    }

    private static void gameBatch(int games, Archetype... players) throws IOException, IllegalAccessException, InstantiationException, InvocationTargetException, InterruptedException {
        initWriter(games, players);

        for (ROUND = 1; ROUND <= games; ROUND++) {
            System.out.println("Starting Round " + ROUND);
            launchNewGame(players);
            Thread.sleep(100);
        }

        writer.close();
        writer = null;
    }

    private static void initWriter(int games, Archetype[] players) throws IOException {
        String playersNames = "";
        for (Archetype arch : players) {
            playersNames += "_" + arch.name();
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String date = dateFormat.format(Calendar.getInstance().getTime());
        String fileName = "csv/" + date + "__x" + games + "_" + playersNames + ".csv";

        File folder = new File("csv");
        if (!folder.exists()) {
            folder.mkdir();
        }

        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        writer = new FileWriter(file, true);
        GameLauncher.outputCVSLine(
                "Round",
                "BotName",
                "Power",
                "Years",
                "Place",
                "Winner",
                "AUS YearOfDeath",
                "RUS YearOfDeath",
                "TUR YearOfDeath",
                "FRA YearOfDeath",
                "ENG YearOfDeath",
                "ITA YearOfDeath",
                "GER YearOfDeath",
                "AUS Position",
                "RUS Position",
                "TUR Position",
                "FRA Position",
                "ENG Position",
                "ITA Position",
                "GER Position",
                "Alliances",
                "AlliesAvgDistance",
                "Enemies",
                "EnemiesAvgDistance",
                "# Negotiators",
                "# AcceptedMessages",
                "# RejectedMessages",
                "Holds",
                "Moves",
                "CutMoves",
                "AttackSelf",
                "AttackAlly",
                "AttackOther",
                "AttackEnemy",
                "Walk",
                "Support",
                "SupportAlly");
    }

    public static synchronized void outputCVSLine(Object... args) {
        String line = "";
        for (int i = 0; i < args.length; i++) {
            line += args[i];
            if (i != args.length - 1) {
                line += ", ";
            } else {
                line += "\n";
            }
        }

        try {
            writer.append(line);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void launchNewGame(Archetype[] players) throws IOException, IllegalAccessException, InstantiationException, InvocationTargetException {
        GameManager game = new GameManager();

        for (int i = 0; i < players.length; i++) {
            game.setPlayer(GameManager.EMPTY, "player" + i);
            launch(createDipBlue(players[i]), players[i].toString() + (i + 1));
        }

        game.setAllPlayers(GameManager.DUMBBOT, "player");
        launch(new Scout(logs), "Scout");
        game.run();
    }

    private static DipBlue createDipBlue(Archetype bot) throws UnknownHostException, IllegalAccessException, InvocationTargetException, InstantiationException {
        DipBlueBuilder dipblueBuilder = new DipBlueBuilder();
        dipblueBuilder
                .withInetAddress(InetAddress.getByName("localhost"))
                .withPort(16714)
                .withLogPath("logs")
                .withCOMM(bot.COMM)
                .withACTIVE_COMM(bot.ACTIVE_COMM)
                .withTRUST(bot.TRUST)
                .withSTRATEGY_BALANCED(bot.STRATEGY_BALANCED)
                .withSTRATEGY_YES(bot.STRATEGY_YES);

        for (int i = 0, advisersLength = bot.advisers.length; i < advisersLength; i++) {
            Class adviser = bot.advisers[i];
            Double weight = bot.weights[i];
            dipblueBuilder.withAdviser((Adviser) adviser.getDeclaredConstructors()[0].newInstance(weight));
        }

        return dipblueBuilder.build();
    }

    private static void launch(final Observer observer, final String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    IComm comm = new DaideComm(InetAddress.getByName("localhost"), 16713, name);
                    observer.start(comm);
                } catch (UnknownHostException e) {
                    System.err.println("Unknown host name.");
                } catch (CommException e) {
                    System.err.println("Cannot connect to the server.");
                }
            }
        }).start();
    }

    public static enum Archetype {
        /**
         * No communication at all
         */
        NO_PRESS(false, false, false, false, false,
                new Class[]{AdviserMapTactician.class},
                new double[]{1.0}),

        /**
         * With communication, always accepts
         */
        SLAVE(true, false, true, false, false,
                new Class[]{AdviserMapTactician.class, AdviserAgreementExecutor.class, AdviserTeamBuilder.class, AdviserWordKeeper.class, AdviserFortuneTeller.class},
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0}),

        /**
         * With communication, always accepts and also requests
         */
        ACTIVE_SLAVE(true, true, true, false, false,
                new Class[]{AdviserMapTactician.class, AdviserAgreementExecutor.class, AdviserTeamBuilder.class, AdviserWordKeeper.class, AdviserFortuneTeller.class},
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0}),

        /**
         * With communication, accepts with criteria and also requests but doesn't keep trust scores
         */
        NAIVE(true, true, false, true, false,
                new Class[]{AdviserMapTactician.class, AdviserAgreementExecutor.class, AdviserTeamBuilder.class, AdviserWordKeeper.class, AdviserFortuneTeller.class},
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0}),

        /**
         * With communication, accepts with criteria, requests and keeps trust scores
         */
        DIPBLUE(true, true, false, true, true,
                new Class[]{AdviserMapTactician.class, AdviserAgreementExecutor.class, AdviserTeamBuilder.class, AdviserWordKeeper.class},
                new double[]{1.0, 1.0, 1.0, 1.0, 1.0});


        public boolean COMM;
        public boolean ACTIVE_COMM;
        public boolean STRATEGY_YES;
        public boolean STRATEGY_BALANCED;
        public boolean TRUST;
        public Class[] advisers;
        public double[] weights;

        Archetype(boolean COMM, boolean ACTIVE_COMM, boolean STRATEGY_YES, boolean STRATEGY_BALANCED, boolean TRUST, Class[] advisers, double[] weights) {
            this.COMM = COMM;
            this.ACTIVE_COMM = ACTIVE_COMM;
            this.STRATEGY_YES = STRATEGY_YES;
            this.STRATEGY_BALANCED = STRATEGY_BALANCED;
            this.TRUST = TRUST;
            this.advisers = advisers;
            this.weights = weights;
        }
    }
}
