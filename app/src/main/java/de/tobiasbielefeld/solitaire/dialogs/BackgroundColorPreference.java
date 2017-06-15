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

package de.tobiasbielefeld.solitaire.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Locale;

import de.tobiasbielefeld.solitaire.R;
import yuku.ambilwarna.AmbilWarnaDialog;

import static de.tobiasbielefeld.solitaire.SharedData.*;

/**
 * Dialog for changing the background color. It uses a custom layout, so I can dynamically update
 * the widget icon of the preference. The user can choose between 6 pre defined colors or set a custom
 * color. The custom color chooser uses this library: https://github.com/yukuku/ambilwarna
 *
 * To distinguish between the pre defined and custom colors, I use another entry in the sharedPref.
 * I also planned to add a "Add background from gallery" option, but it would require the
 * permission to the external storage, and i wanted my app to use no permissions.
 */

public class BackgroundColorPreference extends DialogPreference implements View.OnClickListener {

    private ArrayList<LinearLayout> linearLayouts;

    private Context context;
    private ImageView image;

    int backgroundType;
    int backgroundValue;
    int savedCustomColor;

    public BackgroundColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_background_color);
        setDialogIcon(null);
        this.context = context;
    }

    @Override
    protected void onBindDialogView(View view) {

        backgroundType = getSharedInt(PREF_KEY_BACKGROUND_COLOR_TYPE,DEFAULT_BACKGROUND_COLOR_TYPE);
        backgroundValue = Integer.parseInt(getSharedString(PREF_KEY_BACKGROUND_COLOR,DEFAULT_BACKGROUND_COLOR));
        savedCustomColor = getSharedInt(PREF_KEY_BACKGROUND_COLOR_CUSTOM, DEFAULT_BACKGROUND_COLOR_CUSTOM);

        linearLayouts = new ArrayList<>();
        linearLayouts.add((LinearLayout) view.findViewById(R.id.dialogBackgroundColorBlue));
        linearLayouts.add((LinearLayout) view.findViewById(R.id.dialogBackgroundColorGreen));
        linearLayouts.add((LinearLayout) view.findViewById(R.id.dialogBackgroundColorRed));
        linearLayouts.add((LinearLayout) view.findViewById(R.id.dialogBackgroundColorYellow));
        linearLayouts.add((LinearLayout) view.findViewById(R.id.dialogBackgroundColorOrange));
        linearLayouts.add((LinearLayout) view.findViewById(R.id.dialogBackgroundColorPurple));

        for (LinearLayout linearLayout : linearLayouts){
            linearLayout.setOnClickListener(this);
        }

        super.onBindDialogView(view);
    }


    @SuppressWarnings("SuspiciousMethodCalls")
    public void onClick(View view) {
        if (view == ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE)){
            AmbilWarnaDialog dialog = new AmbilWarnaDialog(context, savedCustomColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    backgroundType = 2;
                    backgroundValue = savedCustomColor = color;

                    putSharedInt(PREF_KEY_BACKGROUND_COLOR_TYPE,backgroundType);
                    putSharedInt(PREF_KEY_BACKGROUND_COLOR_CUSTOM,backgroundValue);
                    updateSummary();
                    getDialog().dismiss();
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // cancel was selected by the user
                }
            });
            dialog.show();
        } else if (view == ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE)){
            getDialog().dismiss();
        } else {
            backgroundValue = linearLayouts.indexOf(view) + 1;
            backgroundType = 1;

            putSharedInt(PREF_KEY_BACKGROUND_COLOR_TYPE,backgroundType);
            putSharedString(PREF_KEY_BACKGROUND_COLOR,Integer.toString(backgroundValue));
            updateSummary();
            getDialog().dismiss();
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(this);
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
    }

    /*
     * Get the layout from the preference, so I can get the imageView from the widgetLayout
     */
    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);

        image = (ImageView) view.findViewById(R.id.preference_background_color_imageView);
        updateSummary();

        return view;
    }

    /**
     * Gets the saved data and updates the summary according to it
     */
    private void updateSummary(){

        if (getSharedInt(PREF_KEY_BACKGROUND_COLOR_TYPE,DEFAULT_BACKGROUND_COLOR_TYPE)==1){
            int drawableID;
            int stringID;
            switch (getSharedString(PREF_KEY_BACKGROUND_COLOR, DEFAULT_BACKGROUND_COLOR)) {
                case "1":default:
                    stringID = R.string.blue;
                    drawableID = R.drawable.background_color_blue;
                    break;
                case "2":
                    stringID = R.string.green;
                    drawableID = R.drawable.background_color_green;
                    break;
                case "3":
                    stringID = R.string.red;
                    drawableID = R.drawable.background_color_red;
                    break;
                case "4":
                    stringID = R.string.yellow;
                    drawableID = R.drawable.background_color_yellow;
                    break;
                case "5":
                    stringID = R.string.orange;
                    drawableID = R.drawable.background_color_orange;
                    break;
                case "6":
                    stringID = R.string.purple;
                    drawableID = R.drawable.background_color_purple;
                    break;
            }

            image.setImageResource(drawableID);
            setSummary(context.getString(stringID));
        } else {
            setSummary("");                                                                         //this forces redrawing of the color preview
            setSummary(context.getString(R.string.settings_background_color_custom));

            image.setImageResource(0);
            image.setBackgroundColor(getSharedInt(PREF_KEY_BACKGROUND_COLOR_CUSTOM, DEFAULT_BACKGROUND_COLOR_CUSTOM));
        }
    }
}
