/*
 * Copyright (C) 2016  Tobias Bielefeld
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you want to contact me, send me an e-mail at tobias.bielefeld@gmail.com
 */

package de.tobiasbielefeld.solitaire.classes;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import de.tobiasbielefeld.solitaire.R;

import static de.tobiasbielefeld.solitaire.SharedData.*;

/*
 *  Contains everything around the cards. The current cards on it and the list of bitmaps for the
 *  backgroudns.
 */

public class Stack {

    public static float defaultSpacing;                                                             //The default space between cards, will be calculated in onCreate of the Main activity
    public static Bitmap backgroundDefault, backgroundTalon, background1, background2, background3, //bitmaps for the stack background images
            background4, background5, background6, background7, background8, background9, background10,
            background11, background12, background13, arrowLeft, arrowRight, backgroundTransparent;
    public CustomImageView view;                                                                          //Background of the stack
    public ArrayList<Card> currentCards = new ArrayList<>();                                        //the array of cards on the stack
    private int id;                                                                                 //id: 0 to 6 tableau. 7 to 10 foundations. 11 and 12 discard and Main stack
    private float spacing;                                                                          //direction in which the cards on the stacks are ordered (top, down, left, right)
    private SpacingDirection spacingDirection = SpacingDirection.NONE;
    private ArrowDirection arrowDirection;
    private float spacingMax;

    public enum SpacingDirection{
        NONE, UP, LEFT, RIGHT, DOWN
    }

    public enum ArrowDirection{
        LEFT, RIGHT
    }

    public Stack(int id) {                                                                          //Constructor: set id
        this.id = id;
    }

    public static void loadBackgrounds() {
        backgroundDefault = bitmaps.getStackBackground(0, 0);
        backgroundTalon = bitmaps.getStackBackground(1, 0);
        background1 = bitmaps.getStackBackground(2, 0);
        background2 = bitmaps.getStackBackground(3, 0);
        background3 = bitmaps.getStackBackground(4, 0);
        background4 = bitmaps.getStackBackground(5, 0);
        background5 = bitmaps.getStackBackground(6, 0);
        background6 = bitmaps.getStackBackground(7, 0);
        background7 = bitmaps.getStackBackground(8, 0);
        background8 = bitmaps.getStackBackground(0, 1);
        background9 = bitmaps.getStackBackground(1, 1);
        background10 = bitmaps.getStackBackground(2, 1);
        background11 = bitmaps.getStackBackground(3, 1);
        background12 = bitmaps.getStackBackground(4, 1);
        background13 = bitmaps.getStackBackground(5, 1);
        arrowLeft = bitmaps.getStackBackground(6, 1);
        arrowRight = bitmaps.getStackBackground(7, 1);
        backgroundTransparent = bitmaps.getStackBackground(8, 1);
    }

    /**
     * deletes the reference to the current cards, so the stack will be empty.
     */
    public void reset() {                                                                           //removes all cards
        currentCards.clear();
    }

    /**
     * Adds a card to this stack. This will update the spacings and flips the card if the stack
     * is a main- or discard stack.
     *
     * @param card The card to add.
     */
    public void addCard(Card card) {
        card.setStack(this);
        currentCards.add(card);

        updateSpacing();

        if (currentGame.hasMainStack() && id >= currentGame.getMainStack().getId()) {
            card.flipDown();
        } else if (currentGame.hasDiscardStack() && id >= currentGame.getDiscardStack().getId()) {
            card.flipUp();
        }
    }

    /**
     * Removes a card from this stack. Spacings will be updated then.
     * @param card The card to remove
     */
    public void removeCard(Card card) {
        currentCards.remove(currentCards.indexOf(card));
        updateSpacing();
    }

    /**
     * Returns the card on the top of the stack
     *
     * @return The card if the stack isn't empty
     * @throws ArrayIndexOutOfBoundsException If the stack is empty
     */
    public Card getTopCard() throws ArrayIndexOutOfBoundsException{
        if (!isEmpty()) {
            return currentCards.get(currentCards.size() - 1);
        } else {
            throw new ArrayIndexOutOfBoundsException("Empty Stack, check with isEmpty() before!");
        }
    }

    /**
     * Returns the cards from the Top of the stack
     *
     * @param index The index of the card from the top of the stack to return
     * @return The card if the stack isn't empty
     * @throws ArrayIndexOutOfBoundsException If the stack is empty
     */
    public Card getCardFromTop(int index) throws ArrayIndexOutOfBoundsException{
        if (!isEmpty()) {
            return currentCards.get(currentCards.size() - 1 - index);
        } else {
            throw new ArrayIndexOutOfBoundsException("Empty Stack, check with isEmpty() before!");
        }
    }

    /**
     * Test if the given location is on the stack. Used to test if the player holds a card over the
     * stack. The stack location goes from the first card to the top card.
     *
     * @param pX X-coordinate to test
     * @param pY Y-coordinate to test
     * @return True if the location is on the stack, else false
     */
    public boolean isOnLocation(float pX, float pY) {
        PointF topPoint = getPosition(0);

        switch (spacingDirection) {
            case NONE: default:
                return pX >= view.getX() && pX <= view.getX() + Card.width
                        && pY >= view.getY() && pY <= view.getY() + Card.height;
            case DOWN:
                topPoint.y += Card.height;
                return pX >= view.getX() && pX <= view.getX() + Card.width
                        && pY >= view.getY() && pY <= topPoint.y;
            case UP:
                return pX >= view.getX() && pX <= view.getX() + Card.width
                        && pY >= topPoint.y && pY <= view.getY() + Card.height;
            case LEFT:
                return pX >= topPoint.x && pX <= view.getX() + Card.width
                        && pY >= view.getY() && pY <= view.getY() + Card.height;
            case RIGHT:
                topPoint.x += Card.width;
                return pX >= view.getX() && pX <= topPoint.x
                        && pY >= view.getY() && pY <= view.getY() + Card.height;
        }
    }

    /**
     * Gets the position a new card would have according to the spacing and offset (used for hints).
     *
     * @param offset The index of the new card as seen from the current top card
     * @return The position as a point
     */
    public PointF getPosition(int offset) {
        switch (spacingDirection) {
            case NONE: default:
                return new PointF(view.getX(), view.getY());
            case DOWN:
                return new PointF(view.getX(), view.getY() + spacing * (currentCards.size() + offset));
            case UP:
                return new PointF(view.getX(), view.getY() - spacing * (currentCards.size() + offset));
            case LEFT:
                return new PointF(view.getX() - spacing * (currentCards.size() + offset), view.getY());
            case RIGHT:
                return new PointF(view.getX() + spacing * (currentCards.size() + offset), view.getY());
        }
    }

    /**
     * Save which cards are currently on this stack as a string list.
     */
    public void save() {
        ArrayList<Integer> list = new ArrayList<>();

        for (Card card : currentCards)
            list.add(card.getId());

        putIntList(STACK + id, list);
    }

    /**
     * Loads the cards which are on this stack from a string list and move the cards to this stack.
     */
    public void load() {
        reset();

        ArrayList<Integer> list = getIntList(STACK + id);

        for (Integer i : list) {
            addCard(cards[i]);
            cards[i].view.bringToFront();
        }
    }

    /**
     * Updates the spacing according to the direction. Left handed mode will affect the direction
     * for left and right direction.
     */
    private void updateSpacing() {
        switch (spacingDirection) {
            case NONE: default:
                if (!isEmpty())
                    getTopCard().setLocation(view.getX(), view.getY());
                break;
            case DOWN:
                spacing = min((spacingMax - view.getY()) / (currentCards.size() + 1), defaultSpacing);

                //PointF currentPos = new PointF(view.getX(), view.getY());
                for (int i = 0; i < currentCards.size(); i++) {

                    currentCards.get(i).setLocation(view.getX(), view.getY() + spacing * i);
                    /*currentCards.get(i).setLocation(currentPos.x, currentPos.y);


                    currentPos.y+=currentCards.get(i).isUp() ? spacing : defaultSpacing/2;*/
                }

                break;
            case UP:
                spacing = min((view.getY() - spacingMax) / (currentCards.size() + 1), defaultSpacing);
                for (int i = 0; i < currentCards.size(); i++)
                    currentCards.get(i).setLocation(view.getX(), view.getY() - spacing * i);
                break;
            case LEFT:
                if (getSharedBoolean(PREF_KEY_LEFT_HANDED_MODE, false)) {
                    spacing = min((spacingMax - view.getX()) / (currentCards.size() + 1), defaultSpacing);
                    for (int i = 0; i < currentCards.size(); i++)
                        currentCards.get(i).setLocation(view.getX() + spacing * i, view.getY());
                } else {
                    spacing = min((view.getX() - spacingMax) / (currentCards.size() + 1), defaultSpacing);
                    for (int i = 0; i < currentCards.size(); i++)
                        currentCards.get(i).setLocation(view.getX() - spacing * i, view.getY());
                }
                break;
            case RIGHT:
                if (getSharedBoolean(PREF_KEY_LEFT_HANDED_MODE, false)) {
                    spacing = min((view.getX() - spacingMax) / (currentCards.size() + 1), defaultSpacing);
                    for (int i = 0; i < currentCards.size(); i++)
                        currentCards.get(i).setLocation(view.getX() - spacing * i, view.getY());
                } else {
                    spacing = min((spacingMax - view.getX()) / (currentCards.size() + 1), defaultSpacing);
                    for (int i = 0; i < currentCards.size(); i++)
                        currentCards.get(i).setLocation(view.getX() + spacing * i, view.getY());
                }
                break;
        }
    }

    /**
     * @return the first card which is faced up
     */
    public Card getFirstUpCard() {
        for (Card card : currentCards)
            if (card.isUp())
                return card;

        return null;
    }

    /**
     * @return The position in the array list of the first card which is faced up. -1 if no card is
     * faced up.
     */
    public int getFirstUpCardPos() {
        for (int i = 0; i < currentCards.size(); i++) {
            if (currentCards.get(i).isUp())
                return i;
        }

        return -1;
    }

    /**
     * Applies the arrow image to the stack, if there should be one
     */
    public void applyArrow(){
        if (arrowDirection!=null) {
            switch (arrowDirection) {
                case LEFT:
                    if (getSharedBoolean(PREF_KEY_LEFT_HANDED_MODE, DEFAULT_LEFT_HANDED_MODE)) {
                        view.setImageBitmap(Stack.arrowRight);
                    } else {
                        view.setImageBitmap(Stack.arrowLeft);
                    }
                    break;
                case RIGHT:
                    if (getSharedBoolean(PREF_KEY_LEFT_HANDED_MODE, DEFAULT_LEFT_HANDED_MODE)) {
                        view.setImageBitmap(Stack.arrowLeft);
                    } else {
                        view.setImageBitmap(Stack.arrowRight);
                    }
                    break;
            }
        }
    }

    /**
     * Sets another stack as a border, so cards from this stack won't overlap the other stack.
     *
     * @param index Index of the stack to use as a border
     */
    public void setSpacingMax(int index) {
        Stack stack = stacks[index];

        switch (spacingDirection) {
            case NONE: default:
                break;
            case DOWN:
                spacingMax = stack.getY() - Card.height;
                break;
            case UP:
                spacingMax = stack.getY() + Card.height;
                break;
            case LEFT:
                if (getSharedBoolean(PREF_KEY_LEFT_HANDED_MODE, false)) {
                    spacingMax = stack.getX() - Card.width;
                } else {
                    spacingMax = stack.getX() + Card.width;
                }
                break;
            case RIGHT:
                if (getSharedBoolean(PREF_KEY_LEFT_HANDED_MODE, false)) {
                    spacingMax = stack.getX() + Card.width;
                } else {
                    spacingMax = stack.getX() - Card.width;
                }
                break;
        }
    }

    /**
     * Sets the screen dimensions as a border, so cards on this stack won't leave the screen.
     *
     * @param layoutGame The layout, where the cards are located in
     */
    public void setSpacingMax(RelativeLayout layoutGame) {

        switch (spacingDirection) {
            case NONE: default:
                break;
            case DOWN:
                spacingMax = (float) (layoutGame.getHeight() - Card.height);
                break;
            case UP:
                spacingMax = 0;
                break;
            case LEFT:
                if (getSharedBoolean(PREF_KEY_LEFT_HANDED_MODE, false)) {
                    spacingMax = layoutGame.getWidth() - Card.width;
                } else {
                    spacingMax = 0;
                }
                break;
            case RIGHT:
                if (getSharedBoolean(PREF_KEY_LEFT_HANDED_MODE, false)) {
                    spacingMax = 0;
                } else {
                    spacingMax = layoutGame.getWidth() - Card.width;
                }
                break;
        }
    }

    /**
     * Changes the layout, if left handed mode is enabled. By default, all important stacks are on
     * the right side of the screen, so it is easier for right handed people to reach them. Left
     * handed mode mirrors the stacks to the other side.
     *
     * @param layoutGame The layout, where the cards are located in
     */
    public void mirrorStack(RelativeLayout layoutGame) {

        view.setX(layoutGame.getWidth() - view.getX() - Card.width);

        for (int j = 0; j < getSize(); j++) {
            Card card = getCard(j);
            card.setLocationWithoutMovement(layoutGame.getWidth() -
                    card.getX() - Card.width, card.getY());
        }

        if (spacingDirection == SpacingDirection.LEFT || spacingDirection == SpacingDirection.RIGHT) {
            if (currentGame.directionBorders != null && currentGame.directionBorders[getId()] != -1)    //-1 means no border
                setSpacingMax(currentGame.directionBorders[getId()]);
            else
                setSpacingMax(layoutGame);
        }
    }

    /**
     * Gets the rectangle enclosing the stack with all current cards on it. Used to determinate if
     * moving cards are intersecting this stack.
     *
     * @return The rectangle of the stack
     */
    public RectF getRect() {

        if (isEmpty()) {
            return new RectF(view.getX(), view.getY(), view.getX() + view.getWidth(), view.getY() + view.getHeight());
        }

        switch (spacingDirection) {
            case NONE: default:
                return new RectF(view.getX(), view.getY(), view.getX() + view.getWidth(), view.getY() + view.getHeight());
            case DOWN:
                return new RectF(view.getX(), view.getY(), view.getX() + view.getWidth(), getTopCard().getY() + view.getHeight());
            case UP:
                return new RectF(view.getX(), getTopCard().getY(), view.getX() + view.getWidth(), view.getY() + view.getHeight());
            case LEFT:
                return new RectF(getTopCard().getX(), view.getY(), view.getX() + view.getWidth(), view.getY() + view.getHeight());
            case RIGHT:
                return new RectF(view.getX(), view.getY(), getTopCard().getX() + view.getWidth(), view.getY() + view.getHeight());
        }
    }

    public boolean topCardIsUp() {
        return getSize() == 0 || getTopCard().isUp();
    }

    public Card getCard(int index) {                                                                //get card from index
        return currentCards.get(index);
    }

    public int getId() {                                                                            //gets the id
        return id;
    }

    public int getIndexOfCard(Card card) {
        return currentCards.indexOf(card);
    }

    public int getSize() {                                                                          //return how many cards are on the stack
        return currentCards.size();
    }

    public boolean isEmpty() {
        return getSize() == 0;
    }

    public void setSpacingDirection(SpacingDirection value) {
        spacingDirection = value;
    }

    public void setArrow(ArrowDirection direction) {
        arrowDirection = direction;
        applyArrow();
    }

    public float getX(){
        return view.getX();
    }

    public float getY(){
        return view.getY();
    }

    public void setX(float X){
        view.setX(X);
    }

    public void setY(float Y){
        view.setY(Y);
    }
}
