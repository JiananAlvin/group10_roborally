package model.game.board.map.element;

import content.RobotName;
import lombok.Data;
import model.Game;
import model.game.board.map.GameMap;
import model.game.board.map.Orientation;
import model.game.board.map.Position;
import model.game.card.Card;

import java.lang.Math;

@Data
public class Robot {
    private String name;
    private boolean onBoard;
    private Orientation orientation;
    private int lives;
    private Position position;

    public Robot(RobotName robotName) {
        this.name = robotName.getName();
        this.onBoard = false;
        this.position = new Position();
        this.orientation = Orientation.E;
    }

    public Robot(String name, int row, int col) {
        this.name = name;
        this.onBoard = false;
        this.position = new Position(row, col);
        this.orientation = Orientation.E;
    }


    public void setPosition(Position position)
    {
        setPosition(position.getRow(), position.getCol());
    }

    public void setPosition(int row, int col) {
        if(Game.validatePosition(this, row, col)) {
            this.position.setRow(row);
            this.position.setCol(col);
        }

    }

    public int distanceToAntenna() {
        return Math.abs(this.position.getRow() - Antenna.getInstance().getPosition().getRow()) + Math.abs(this.position.getCol() - Antenna.getInstance().getPosition().getCol());
    }

    @Override
    public String toString() {
        return this.name;
    }

    public void applyCard(Card card) {
        card.action(this);
    }

    /**
     * @return: This function returns true if the robot is still alive after taking -some- damage
     */
    public boolean takeDamage(int damage) {
        this.lives -= damage;
        return checkAlive();
    }

    private boolean checkAlive() {
        if (this.lives <= 0) {
            return false; //here we reboot
        }
        return true; // here nothing happens
    }

    public boolean imInsideBoard(int maxRow, int maxCol) {
        if (this.getPosition().getRow() > maxRow || this.getPosition().getCol() > maxCol) {
            return false;
        } else return this.getPosition().getRow() >= 0 && this.getPosition().getCol() >= 0;
    }
}


