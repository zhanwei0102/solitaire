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

package de.tobiasbielefeld.solitaire.helper;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.graphics.PointF;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;

import java.util.Random;

import de.tobiasbielefeld.solitaire.R;
import de.tobiasbielefeld.solitaire.classes.Card;
import de.tobiasbielefeld.solitaire.classes.CustomImageView;
import de.tobiasbielefeld.solitaire.classes.Stack;
import de.tobiasbielefeld.solitaire.handler.AfterWonHandler;
import de.tobiasbielefeld.solitaire.ui.GameManager;

import static de.tobiasbielefeld.solitaire.SharedData.*;

/**
 * class for all card animations. Like moving cards and fading them out and in for hints.
 * The win animation is split up in two parts: First move every card to the middle of the screen,
 * then move them out the screen borders
 */

public class Animate {

    public AfterWonHandler afterWonHandler;
    private GameManager gm;

    public Animate(GameManager gm) {
        this.gm = gm;
        afterWonHandler = new AfterWonHandler(gm);
    }

    /**
     * Shows the win animation: Every card will move to the center of the screen. In the handler
     * after that the phase2 will be called and move every card out the screen.
     */
    public void winAnimation() {
        for (Card card : cards) {
            card.setLocation(gm.layoutGame.getWidth() / 2 - Card.width / 2, gm.layoutGame.getHeight() / 2 - Card.height / 2);
        }

        afterWonHandler.sendEmptyMessageDelayed(0, 100);
    }

    /**
     * Moves every card out the screen as phase2 of the win animation
     */
    public void wonAnimationPhase2() {
        int direction = 0;
        int counter = 0;
        Random rand = new Random();

        for (Card card : cards) {
            switch (direction) {
                case 0:
                default://right side
                    card.setLocation(gm.layoutGame.getWidth(), counter);
                    counter += Card.height;

                    if (counter >= gm.layoutGame.getHeight()) {
                        direction = 1;
                        counter = rand.nextInt(Card.height);
                    }

                    break;
                case 1://bottom side
                    card.setLocation(counter, gm.layoutGame.getHeight() + Card.height);
                    counter += Card.width;

                    if (counter >= gm.layoutGame.getWidth()) {
                        direction = 2;
                        counter = rand.nextInt(Card.width);
                    }

                    break;
                case 2://left side
                    card.setLocation(-Card.width, counter);
                    counter += Card.height;

                    if (counter >= gm.layoutGame.getHeight()) {
                        direction = 3;
                        counter = rand.nextInt(Card.height);
                    }
                    break;
                case 3://top side
                    card.setLocation(counter, -Card.height);
                    counter += Card.width;

                    if (counter >= gm.layoutGame.getWidth()) {
                        direction = 0;
                        counter = rand.nextInt(Card.width);
                    }
                    break;
            }
        }
    }

    /**
     * Moves a card to another stack, fades the card out and fades it in on the origin as a hint
     *
     * @param card The card to move as the hint
     * @param offset The position of the card above the top card of the destination
     * @param destination The destination of the movement
     */
    public void cardHint(final Card card, final int offset, final Stack destination) {
        card.view.bringToFront();
        card.saveOldLocation();
        PointF pointAtStack = destination.getPosition(offset);
        float dist_x = pointAtStack.x - card.getX();
        float dist_y = pointAtStack.y - card.getY();
        int distance = (int) Math.sqrt((double) ((dist_x * dist_x) + (dist_y * dist_y)));

        TranslateAnimation animation = new TranslateAnimation(0, dist_x, 0, dist_y);

        animation.setDuration(distance * 100 / Card.width);
        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                PointF pointAtStack = destination.getPosition(offset);
                card.view.setX(pointAtStack.x);
                card.view.setY(pointAtStack.y);
                hideCard(card);
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });

        card.view.startAnimation(animation);
    }

    /**
     * is the second part from the hint: fade the card out the screen
     * @param card The card to fade out
     */
    private void hideCard(final Card card) {
        Animation card_fade_out = AnimationUtils.loadAnimation(
                gm.getApplicationContext(), R.anim.card_fade_out);

        card_fade_out.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                card.view.setVisibility(View.INVISIBLE);
                showCard(card);
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });

        card.view.startAnimation(card_fade_out);
    }

    /**
     * is the third part from the hint: fade the card back in at the original destination
     * @param card The card to fade in
     */
    private void showCard(final Card card) {
        Animation card_fade_in = AnimationUtils.loadAnimation(
                gm.getApplicationContext(), R.anim.card_fade_in);

        card_fade_in.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
                card.returnToOldLocation();
                card.view.setVisibility(View.VISIBLE);
            }

            public void onAnimationEnd(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });

        card.view.startAnimation(card_fade_in);
    }

    /**
     * Moves a card to a new destination. FillEnabled is necessary, or else flickering will occur.
     * The location is updated when the animation finishes, which happens in the onAnimationEnd()
     * method of the custom image view.
     *
     * @param card The card to move
     * @param pX X-coordinate of the destination
     * @param pY Y-coordinate of the destination
     */
    public void moveCard(final Card card, final float pX, final float pY) {
        final CustomImageView view = card.view;
        int distance = (int) Math.sqrt(Math.pow(pX - view.getX(), 2) + Math.pow(pY - view.getY(), 2));

        TranslateAnimation animation = new TranslateAnimation(0, pX - view.getX(), 0, pY - view.getY());

        animation.setDuration(distance * 100 / Card.width);
        animation.setFillEnabled(true);

        view.setDestination(pX,pY);
        view.startAnimation(animation);
    }

    public boolean cardIsAnimating() {
        for (Card card : cards) {
            if (card.view.isAnimating()) {
                return true;
            }
        }

        return false;
    }

    public void reset() {
        for (Card card : cards) {
            card.view.stopAnim();
        }
    }

    /**
     * is the first part of the flip animation: The drawable will shrink to its center, then grow
     * back to normal size with the new drawable
     *
     * @param card The card to animate
     * @param mode True for flipUp, false otherwise
     */
    public void flipCard(final Card card, final boolean mode) {
        AnimatorSet shrinkSet = (AnimatorSet) AnimatorInflater.loadAnimator(
                gm, R.animator.card_to_middle);
        shrinkSet.addListener(new Animator.AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                flipCard2(card, mode);
            }

            public void onAnimationCancel(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }
        });

        shrinkSet.setTarget(card.view);
        shrinkSet.start();
    }

    /**
     * is the second part of the flip animation. Grows back to normal size.
     *
     * @param card The card to animate
     * @param mode True for flipUp, false otherwise
     */
    private void flipCard2(final Card card, final boolean mode) {
        AnimatorSet growSet = (AnimatorSet) AnimatorInflater.loadAnimator(
                gm, R.animator.card_from_middle);
        growSet.addListener(new Animator.AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                if (mode)   //flip up
                    card.setCardFront();
                else //flip down
                    card.setCardBack();
            }

            public void onAnimationEnd(Animator animation) {
            }

            public void onAnimationCancel(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }
        });

        growSet.setTarget(card.view);
        growSet.start();
    }

    /**
     * shows the auto complete button with a nice fade in animation
     */
    public void showAutoCompleteButton() {
        Animation fade_in = AnimationUtils.loadAnimation(
                gm.getApplicationContext(), R.anim.button_fade_in);

        fade_in.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {

                gm.buttonAutoComplete.setVisibility(View.VISIBLE);
            }

            public void onAnimationEnd(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });

        gm.buttonAutoComplete.startAnimation(fade_in);
    }
}