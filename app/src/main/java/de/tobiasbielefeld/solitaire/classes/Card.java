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

import java.util.ArrayList;

import static de.tobiasbielefeld.solitaire.SharedData.*;

/**
 *  Contains everything related to cards. The view is a custom image view, which overrides some
 *  methods for animations. The drawable files are also updated here
 */

public class Card {

    public static int width, height;                                                                //width and height calculated in relation of the screen dimensions in Main activity
    public static Bitmap background;
    private static Bitmap[] drawables = new Bitmap[52];
    public CustomImageView view;                                                                    //the image view of the card, for easier code not private
    private int color;                                                                              //1=clubs 2=hearts 3=Spades 4=diamonds
    private int value;                                                                              //1=ace 2,3,4,5,6,7,8,9,10, 11=joker 12=queen 13=king
    private Stack stack;                                                                            //saves the stack where the card is placed
    private int id;                                                                                 //internal id
    private boolean isUp;                                                                           //indicates if the card is placed upwards or backwards
    private PointF oldLocation = new PointF();                                                      //old location so cards can be moved back if they can't placed on a new stack

    /**
     * Sets id, color and value. The cards are initialized at game start with a for loop.
     *
     * The color range is 1 to 4 and depends on the cardDrawableOrder, which is set to
     * 1 for the first 13 cards, 2 for the following 13 cards and so on.
     * After 52 cards (= one deck) it repeats. The value range is from 1 to 13 (= Ace to King).
     *
     * @param id The position in the cards array
     */
    public Card(int id) {
        this.id = id;
        color = currentGame.cardDrawablesOrder[(id % 52) / 13];
        value = (id % 13) + 1;
    }

    /**
     * Sets the card drawables according to set preferences. Each card theme has one drawable file
     * with 52 cards in it. These will be loaded in bitmaps and applied to the cards. The bitmap array
     * has the same order like the cards array. If the fourColor theme is enabled, Clubs and Diamonds
     * use another row in the bitmap file.
     */
    public static void updateCardDrawableChoice() {
        boolean fourColors = getSharedBoolean(PREF_KEY_4_COLOR_MODE, DEFAULT_4_COLOR_MODE);

        for (int i = 0; i < 13; i++) {
            drawables[i] = bitmaps.getCardFront(i, fourColors ? 1 : 0);
            drawables[13 + i] = bitmaps.getCardFront(i, 2);
            drawables[26 + i] = bitmaps.getCardFront(i, 3);
            drawables[39 + i] = bitmaps.getCardFront(i, fourColors ? 5 : 4);
        }

        if (cards != null) {
            for (Card card : cards) {
                if (card.isUp()) {
                    card.setCardFront();
                }
            }
        }
    }

    /**
     * Loads the card backgrounds for the bitmap file and applies them.
     */
    public static void updateCardBackgroundChoice() {

        int position = getSharedInt(CARD_BACKGROUND, DEFAULT_CARD_BACKGROUND) - 1;
        background = bitmaps.getCardBack(position % 8, position / 8);

        if (cards != null) {
            for (Card card : cards)
                if (!card.isUp())
                    card.setCardBack();
        }
    }

    /**
     * Save the card direction (up/down) as a string list.
     */
    public static void save() {
        ArrayList<Integer> list = new ArrayList<>();

        for (Card card : cards)
            list.add(card.isUp ? 1 : 0);

        putIntList(CARDS, list);
    }

    /**
     * Load the card direction (up/down) from a string list and applies the data.
     */
    public static void load() {
        ArrayList<Integer> list = getIntList(CARDS);

        for (int i = 0; i < cards.length; i++) {

            if (list.get(i) == 1)
                cards[i].flipUp();
            else
                cards[i].flipDown();
        }
    }

    /**
     * Sets the card front side from the bitmap array. The position is calculated with the card
     * color and value.
     */
    public void setCardFront() {
        view.setImageBitmap(drawables[(color - 1) * 13 + value - 1]);
    }

    /**
     * Sets the card background, there is only one background for all cards.
     */
    public void setCardBack() {
        view.setImageBitmap(background);
    }

    /**
     * Updates the color of the card. It is only used when a custom color order is set up
     * (like in Spider for different difficulties).
     */
    public void setColor() {
        color = currentGame.cardDrawablesOrder[(id % 52) / 13];
    }

    /**
     * Moves a card to the given coordinates (if not already there). This will use a translate
     * Animation and no interaction with cards/buttons is possible during the movement.
     *
     * @param pX The x-coordinate of the destination
     * @param pY The y-coordinate of the destination
     */
    public void setLocation(float pX, float pY) {
        if (view.getX() != pX || view.getY() != pY)
            animate.moveCard(this, pX, pY);
    }

    /**
     * Sets the location instantly WITHOUT a movement.
     *
     * @param pX The x-coordinate of the destination
     * @param pY The y-coordinate of the destination
     */
    public void setLocationWithoutMovement(float pX, float pY) {
        view.bringToFront();
        view.setX(pX);
        view.setY(pY);
    }

    /**
     * Saves the current location of the card as the old location, so it can be reverted if
     * necessary.
     */
    public void saveOldLocation() {
        oldLocation.x = view.getX();
        oldLocation.y = view.getY();
    }

    /**
     * reverts the current location to the saved one.
     */
    public void returnToOldLocation() {
        view.setX(oldLocation.x);
        view.setY(oldLocation.y);
    }

    /**
     * Sets the direction to up and updates the drawable.
     */
    public void flipUp() {
        isUp = true;
        setCardFront();
    }

    /**
     * Sets the direction to down and updates the drawable.
     */
    public void flipDown() {
        isUp = false;
        setCardBack();
    }

    /**
     * Sets the direction to the opposite of the current direction.
     */
    public void flip() {
        if (isUp())
            flipDown();
        else
            flipUp();
    }

    /**
     * Sets the direction to the opposite of the current direction, but with an animation.
     * This also updates the score (movement from the current stack to the same stack is counted
     * as a flip) and sets a new record in the record list.
     */
    public void flipWithAnim() {
        if (isUp()) {
            isUp = false;
            scores.undo(this, getStack());
            animate.flipCard(this, false);
        } else {
            isUp = true;
            scores.move(this, getStack());
            recordList.addFlip(this);
            animate.flipCard(this, true);
        }
    }

    /**
     * Tests if this card can be placed on a stack:
     * Only possible if: the cardTest returns true, the card and the top card on the destination are
     * up, and no auto complete is running.
     *
     * @param destination The destination stack to test the card on
     * @return  True if movement is possible, false otherwise
     */
    public boolean test(Stack destination) {
        return !((!isUp() || (destination.getSize() != 0 && !destination.getTopCard().isUp())) && !autoComplete.isRunning()) && currentGame.cardTest(destination, this);
        //return currentGame.cardTest(destination, this) && destination.topCardIsUp();
    }

    public int getColor() {
        return color;
    }

    public boolean isTopCard() {
        return getStack().getTopCard() == this;
    }

    public boolean isFirstCard() {
        return getStack().getCard(0) == this;
    }

    public int getIndexOnStack() {
        return getStack().getIndexOfCard(this);
    }

    public boolean isUp() {                                                                         //returns if the card is up
        return isUp;
    }

    public int getId() {
        return id;
    }

    public int getValue() {
        return value;
    }

    public Stack getStack() {
        return stack;
    }

    public void setStack(Stack stack) {
        this.stack = stack;
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

    public int getStackId(){
        return stack.getId();
    }
}