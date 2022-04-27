package gui.game;

import content.Application;
import content.MapNameEnum;
import content.RobotNameEnum;
import lombok.SneakyThrows;
import model.Game;
import model.game.Room;
import model.game.Player;
import model.game.board.map.GameMap;
import model.game.board.map.element.Robot;
import model.game.board.mat.ProgrammingDeck;
import model.game.board.mat.RegisterArea;
import model.game.card.Card;
import org.json.JSONArray;
import org.json.JSONObject;
import server.controller.ProgrammingRecordController;
import server.controller.RoomController;
import server.controller.UserController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/*
 * The whole GamePanel is like this
 * ________________________
 * |    board Panel |user1|
 * |                |_____|
 * |                |user2|
 * |                |-----|
 * |                |.log.|
 * |________________|_____|
 * |       MatPanel       |
 * |______________________|
 */
public class GamePanel extends JPanel {

    private BoardPanel boardPanel;
    private InfoPanel infoPanel;
    private MatPanel matPanel;
    public static final Color[] userColors = new Color[]{Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.MAGENTA};
    private Timer programmingTimer;
    private Timer activationPhaseTimer;
    public static final int MAX_PROGRAMMING_TIME = 30;

    public GamePanel(Game game) {
        super(true);

        this.infoPanel = new InfoPanel(game.getParticipants(), game.getUser());
        this.boardPanel = new BoardPanel(game);
        this.setLayout(null);

        this.boardPanel.setBounds(0, 0, 780, 600);
        this.infoPanel.setBounds(800, 0, 725, 600);

        this.add(this.boardPanel);
        this.add(this.infoPanel);

        this.setSize(1650, 1080);

        this.programmingTimer = this.invokeProgrammingTimer(game);
        this.programmingTimer.start();
    }

    private void reloadMatPanel(Game game) {
        if (this.matPanel != null)
            this.remove(matPanel);
        this.matPanel = new MatPanel(game);
        this.matPanel.setBounds(0, 600, 1650, 500);
        this.add(this.matPanel);
        this.revalidate();
        this.repaint();
    }

    public static void init(JFrame frame, Game game) {
        frame.getContentPane().removeAll();
        frame.getContentPane().add(new GamePanel(game));
        //Display the window.
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 800);
        frame.setVisible(true);
    }

    private Timer invokeProgrammingTimer(Game game) {
        // init the timer in ProgrammingPanel
        return new Timer(1000, new ActionListener() {
            int remainingTime = MAX_PROGRAMMING_TIME;

            public void actionPerformed(ActionEvent e) {
                if (remainingTime-- == MAX_PROGRAMMING_TIME) {
                    // the user draws cards
                    game.getUser().drawCards();
                    reloadMatPanel(game);
                }
                if (remainingTime >= 0)
                    matPanel.getLblTimer().setText("<html><br/>" + remainingTime + "&nbsp</html>");
                if (remainingTime < 0) {
                    game.getUser().getRegisterArea().setRegisters(new ArrayList<>(game.getUser().getCardsInHand().subList(0, RegisterArea.REGISTER_QUEUE_SIZE)));
                    game.getUser().discard(new ArrayList<>(game.getUser().getCardsInHand().subList(0, ProgrammingDeck.NUMBER_OF_CARDS_DRAWN_IN_EACH_ROUND)));
                    game.getUser().getCardsInHand().removeAll(game.getUser().getCardsInHand());
                    new ProgrammingRecordController().createProgrammingRecord(
                            game.getUser().getName(),
                            game.getRoom().getRoomNumber(),
                            game.getCurrentRoundNum(),
                            getRegisterStr(game, 0),
                            getRegisterStr(game, 1),
                            getRegisterStr(game, 2),
                            getRegisterStr(game, 3),
                            getRegisterStr(game, 4)
                    );
                    programmingTimer.stop();
                    excuteProgRecordsWorker(game);
                    remainingTime = MAX_PROGRAMMING_TIME;
                    infoPanel.addLogToLogPanel("Programming phase done and inform worker to communicate with server", null);
                }
            }
        });
    }

    private void excuteProgRecordsWorker(Game game) {

        SwingWorker<JSONArray, Void> progRecordsWorker = new SwingWorker<>() {

            @Override
            protected JSONArray doInBackground() throws Exception {
                while (true) {
                    Thread.sleep(1000);
                    JSONObject temp = new ProgrammingRecordController().getProgrammingRecords(game.getRoom().getRoomNumber(), game.getCurrentRoundNum());
                    JSONArray programmingRecords = temp.getJSONArray(ProgrammingRecordController.RESPONSE_PROGRAMMING_RECORDS);
                    if (programmingRecords.length() == game.getParticipants().size()) {
                        return programmingRecords;
                    }
                }
            }

            @SneakyThrows
            @Override
            protected void done() {
                updateParticipantRegisters(get(), game);
                infoPanel.addLogToLogPanel("ProgRecordsWorker done and start activation phase", null);
                activationPhaseTimer = invokeActivationPhaseTimer(game);
                activationPhaseTimer.start();
            }
        };
        // executes the swingworker on worker thread
        infoPanel.addLogToLogPanel("updateProgRecordsWorker starts", null);
        progRecordsWorker.execute();
    }

    //TODO
    private Timer invokeActivationPhaseTimer(Game game) {
        return new Timer(1000, new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                int round = game.getCurrentRoundNum();
                int registerIndex = game.getCurrentRegisterNum();
                int currenPlayerIndex = game.getCurrentPlayerOrderedIndex();
                if (registerIndex == 0 && currenPlayerIndex == 0) {
                    game.setParticipants(game.orderOfPlayers());
                }
                Player currentPlayer = game.getParticipants().get(currenPlayerIndex);
                Card currentRegisterCard = currentPlayer.getRegisterArea().getCard(registerIndex);
                boardPanel.getBoard()[currentPlayer.getRobot().getPosition().getRow()][currentPlayer.getRobot().getPosition().getCol()].unsetRobot();

                currentRegisterCard.actsOn(currentPlayer.getRobot());

//                System.out.println(currentRegisterCard);
//                System.out.println("Acted on");
                boardPanel.getBoard()[currentPlayer.getRobot().getPosition().getRow()][currentPlayer.getRobot().getPosition().getCol()].setRobot(currentPlayer.getRobot().getOrientation(), currentPlayer);
                boardPanel.repaint();

                infoPanel.addLogToLogPanel(currentPlayer.getRobot().getName() + ": " + currentPlayer.getRobot().getOrientation().toString(), currentPlayer );
                game.setCurrentPlayerOrderedIndex(++currenPlayerIndex);
                if (currenPlayerIndex == game.getParticipants().size()) {
                    game.setCurrentRegisterNum(++registerIndex);
                    game.setCurrentPlayerOrderedIndex(0);
                }
                if (registerIndex == RegisterArea.REGISTER_QUEUE_SIZE) {
                    game.setCurrentRoundNum(++round);
//                    TODO debug
                    System.out.println(round);
                    game.setCurrentRegisterNum(0);
                    activationPhaseTimer.stop();
                    infoPanel.addLogToLogPanel("activation phase done and start programming phase", null);
                    programmingTimer = invokeProgrammingTimer(game);
                    programmingTimer.start();
                }
            }
        });
    }

    private String getRegisterStr(Game game, int index) {
        return game.getUser().getRegisterArea().getRegisters().get(index).toString();
    }

    private void updateParticipantRegisters(JSONArray programmingRecords, Game game) {
        for (Object record : programmingRecords) {
            ArrayList<Card> cards = new ArrayList<>() {
                {
                    add(createCardInstance(((JSONObject) record).getString(ProgrammingRecordController.RESPONSE_REGISTER1)));
                    add(createCardInstance(((JSONObject) record).getString(ProgrammingRecordController.RESPONSE_REGISTER2)));
                    add(createCardInstance(((JSONObject) record).getString(ProgrammingRecordController.RESPONSE_REGISTER3)));
                    add(createCardInstance(((JSONObject) record).getString(ProgrammingRecordController.RESPONSE_REGISTER4)));
                    add(createCardInstance(((JSONObject) record).getString(ProgrammingRecordController.RESPONSE_REGISTER5)));
                }
            };
            for (Player player : game.getParticipants()) {
                if (player.getName().equals(((JSONObject) record).getString(ProgrammingRecordController.RESPONSE_USER))) {
                    player.getRegisterArea().setRegisters(cards);
                    break;
                }
            }
        }
    }

    @SneakyThrows
    private Card createCardInstance(String className) {
        final String PATH_TO_CARD_CLASS = "model.game.card.programming.";
        Class<?> clz = Class.forName(PATH_TO_CARD_CLASS + className);
        return (Card) clz.getDeclaredConstructor().newInstance();
    }

    @SneakyThrows
    public static void main(String[] args) {
        Player user = new Player("SpongeBob", new Robot(RobotNameEnum.SQUASH_BOT));
        UserController userController = new UserController();
        userController.deleteUser("SpongeBob");
        userController.createUser("SpongeBob");
//        userController.createUser("PatrickStar");
        //RobotController robotController = new RobotController();
        userController.chooseRobot(user.getName(), user.getRobot().getName());
//        userController.chooseRobot("PatrickStar", "ZOOM_BOT");
        RoomController roomController = new RoomController();
        System.out.println(roomController.createRoom(user.getName(), "BEGINNER"));
        int roomNumber = roomController.createRoom(user.getName(), "BEGINNER").getInt("room_number");
//        userController.joinRoom("PatrickStar", roomNumber);
        GameMap gameMap = new GameMap(MapNameEnum.ADVANCED);
        Room room = new Room(roomNumber);
        Game game = new Game();
        game.init(user, room, gameMap, roomController.roomInfo(roomNumber));

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame(Application.APP_TITLE);
                frame.setSize(1650, 1080);
                frame.add(new GamePanel(game));
                frame.setVisible(true);
//                roomController.deleteRoom(roomNumber);
            }
        });

    }

}
