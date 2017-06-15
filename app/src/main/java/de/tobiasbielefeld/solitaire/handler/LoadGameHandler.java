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

package de.tobiasbielefeld.solitaire.handler;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import de.tobiasbielefeld.solitaire.ui.GameManager;

import static de.tobiasbielefeld.solitaire.SharedData.*;

/**
 * load the game data in a handler which waits a bit, so the initial card deal looks smoother
 */

public class LoadGameHandler extends Handler {

    GameManager gm;

    public LoadGameHandler(GameManager gm) {
        this.gm = gm;
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        gameLogic.load();

        if (currentGame.hasLimitedRedeals()) {
            gm.mainTextViewRedeals.setVisibility(View.VISIBLE);
            gm.mainTextViewRedeals.setX(currentGame.getMainStack().getX());
            gm.mainTextViewRedeals.setY(currentGame.getMainStack().getY());
        }

        gm.hasLoaded = true;
    }
}
