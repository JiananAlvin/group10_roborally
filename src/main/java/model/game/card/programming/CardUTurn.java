package model.game.card.programming;

import content.OrientationEnum;
import model.game.board.map.element.Robot;
import model.game.card.Card;

public class CardUTurn extends Card {
    public Card actsOn(Robot robot) {
        robot.setOrientation(OrientationEnum.matchOrientation((robot.getOrientation().getAngle() + 180) % 360));
        return this;
    }
}
