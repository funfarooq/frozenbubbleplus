/*
 *                 [[ Frozen-Bubble ]]
 *
 * Copyright (c) 2000-2003 Guillaume Cottenceau.
 * Java sourcecode - Copyright (c) 2003 Glenn Sanson.
 * Additional source - Copyright (c) 2013 Eric Fortin.
 *
 * This code is distributed under the GNU General Public License
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 or 3, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to:
 * Free Software Foundation, Inc.
 * 675 Mass Ave
 * Cambridge, MA 02139, USA
 *
 * Artwork:
 *    Alexis Younes <73lab at free.fr>
 *      (everything but the bubbles)
 *    Amaury Amblard-Ladurantie <amaury at linuxfr.org>
 *      (the bubbles)
 *
 * Soundtrack:
 *    Matthias Le Bidan <matthias.le_bidan at caramail.com>
 *      (the three musics and all the sound effects)
 *
 * Design & Programming:
 *    Guillaume Cottenceau <guillaume.cottenceau at free.fr>
 *      (design and manage the project, whole Perl sourcecode)
 *
 * Java version:
 *    Glenn Sanson <glenn.sanson at free.fr>
 *      (whole Java sourcecode, including JIGA classes
 *             http://glenn.sanson.free.fr/jiga/)
 *
 * Android port:
 *    Pawel Aleksander Fedorynski <pfedor@fuw.edu.pl>
 *    Eric Fortin <videogameboy76 at yahoo.com>
 *    Copyright (c) Google Inc.
 *
 *          [[ http://glenn.sanson.free.fr/fb/ ]]
 *          [[ http://www.frozen-bubble.org/   ]]
 */

package com.efortin.frozenbubble;

import org.jfedor.frozenbubble.FrozenBubble;
import org.jfedor.frozenbubble.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class SplashScreen extends Activity {
  /*
   * Provide unique IDs for the views associated with the relative
   * layout.  These are used to define relative view layout positions
   * with respect to other views in the layout.
   */
  private final static int BTN1_ID   = 101;
  private final static int BTN2_ID   = 102;
  private final static int SCREEN_ID = 100;

  private Boolean homeShown = false;
  private Boolean musicOn = true;
  private ImageView myImageView = null;
  private RelativeLayout myLayout = null;
  private ModPlayer myModPlayer = null;
  private Thread splashThread = null;

  private void addHomeButtons() {
    // Construct the 2 player game button.
    Button start2pGameButton = new Button(this);
    start2pGameButton.setOnClickListener(new Button.OnClickListener(){

      public void onClick(View v){
        // process the button tap
        Toast.makeText(getApplicationContext(),
                       "Still working on it...", Toast.LENGTH_SHORT).show();
      }
    });
    start2pGameButton.setText("Start 2p Game");
    start2pGameButton.setWidth((int) (start2pGameButton.getTextSize() *
                                      start2pGameButton.getText().length()));
    start2pGameButton.setHorizontalFadingEdgeEnabled(true);
    start2pGameButton.setFadingEdgeLength(5);
    start2pGameButton.setShadowLayer(5, 5, 5, R.color.black);
    start2pGameButton.setId(BTN2_ID);
    LayoutParams myParams1 = new LayoutParams(LayoutParams.WRAP_CONTENT,
                                             LayoutParams.WRAP_CONTENT);
    myParams1.addRule(RelativeLayout.CENTER_HORIZONTAL);
    myParams1.addRule(RelativeLayout.CENTER_VERTICAL);
    // Add view to layout.
    myLayout.addView(start2pGameButton, myParams1);
    // Construct the 1 player game button.
    Button start1pGameButton = new Button(this);
    start1pGameButton.setOnClickListener(new Button.OnClickListener(){

      public void onClick(View v){
        // process the button tap
        startFrozenBubble();
      }
    });
    start1pGameButton.setText("Start 1p Game");
    start1pGameButton.setWidth((int) (start1pGameButton.getTextSize() *
                                      start1pGameButton.getText().length()));
    start1pGameButton.setHorizontalFadingEdgeEnabled(true);
    start1pGameButton.setFadingEdgeLength(5);
    start1pGameButton.setShadowLayer(5, 5, 5, R.color.black);
    start1pGameButton.setId(BTN1_ID);
    LayoutParams myParams2 = new LayoutParams(LayoutParams.WRAP_CONTENT,
                                             LayoutParams.WRAP_CONTENT);
    myParams2.addRule(RelativeLayout.CENTER_HORIZONTAL);
    myParams2.addRule(RelativeLayout.ABOVE, start2pGameButton.getId());
    // Add view to layout.
    myLayout.addView(start1pGameButton, myParams2);
  }

  private void cleanUp() {
    if (myModPlayer != null) {
      myModPlayer.destroyMusicPlayer();
      myModPlayer = null;
    }
  }

  private boolean displaySplashScreen() {
    SharedPreferences sp = getSharedPreferences(FrozenBubble.PREFS_NAME,
                                                Context.MODE_PRIVATE);
    boolean showSplashScreen = sp.getBoolean("showSplashScreen", true);
    if (showSplashScreen) {
      SharedPreferences.Editor editor = sp.edit();
      editor.putBoolean("showSplashScreen", false);
      editor.commit();
    }
    return showSplashScreen;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent msg) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      cleanUp();
      //
      // Terminate the splash screen activity.
      //
      //
      finish();
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * @see android.app.Activity#onCreate(android.os.Bundle)
   * 
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    restoreGamePrefs();
    // Configure the window presentation and layout.
    setWindowLayout();
    myLayout = new RelativeLayout(this);
    myLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                                              LayoutParams.FILL_PARENT));
    myImageView = new ImageView(this);

    if (!displaySplashScreen()) {
      startHomeScreen();
    }
    else {
      setBackgroundImage(R.drawable.splash);
      setContentView(myLayout);
      //
      // Thread for managing the splash screen.
      //
      //
      splashThread = new Thread() {
        @Override
        public void run() {
          try {
            synchronized(this) {
              //
              // TODO: The splash screen waits before launching the
              //       game activity.  Change this so that the game
              //       activity is started immediately, and notifies
              //       the splash screen activity when it is done
              //       loading saved state data and preferences, so the
              //       splash screen functions as a distraction from
              //       game loading latency.  There is no advantage in
              //       doing this right now, because there is no lag.
              //
              //
              wait(3000);  // wait 3 seconds
            }
          } catch (InterruptedException e) {
          } finally {
            runOnUiThread(new Runnable() {
              public void run() {
                startHomeScreen();
              }
            });
          }
        }
      };
      splashThread.start();
    }
  }

  /*
   * (non-Javadoc)
   * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
   * 
   * Invoked when the screen is touched.
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      synchronized(splashThread) {
        splashThread.notifyAll();
      }
    }
    return true;
  }

  private void restoreGamePrefs() {
    SharedPreferences mConfig = getSharedPreferences(FrozenBubble.PREFS_NAME,
                                                     Context.MODE_PRIVATE);
    musicOn = mConfig.getBoolean("musicOn", true );
  }

  private void setBackgroundImage(int resId) {
    if (myImageView.getParent() != null)
      myLayout.removeView(myImageView);

    myImageView.setBackgroundColor(getResources().getColor(R.color.black));
    myImageView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                                                 LayoutParams.FILL_PARENT));
    myImageView.setImageResource(resId);
    myImageView.setId(SCREEN_ID);
    myLayout.addView(myImageView);
  }

  private void startHomeScreen() {
    if (!homeShown) {
      homeShown = true;
      setBackgroundImage(R.drawable.home_screen);
      addHomeButtons();
      setContentView(myLayout);
      myModPlayer = new ModPlayer(this, R.raw.introzik, musicOn, false);
    }
  }

  /**
   * Set the window layout according to the settings in the specified
   * layout XML file.  Then apply the full screen option according to
   * the player preference setting.
   * 
   * <p>Note that the title bar is desired for the splash screen, so
   * do not request that it be removed.
   *
   * <p>Requesting that the title bar be removed <b>must</b> be
   * performed before setting the view content by applying the XML
   * layout, or it will generate an exception.
   * 
   * @param  layoutResID
   *         - The resource ID of the XML layout to use for the window
   *         layout settings.
   */
  private void setWindowLayout() {
    final int flagFs   = WindowManager.LayoutParams.FLAG_FULLSCREEN;
    final int flagNoFs = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
    // Remove the title bar.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    // Set full screen mode based on the game preferences.
    SharedPreferences mConfig =
      getSharedPreferences(FrozenBubble.PREFS_NAME, Context.MODE_PRIVATE);
    boolean fullscreen = mConfig.getBoolean("fullscreen", true);

    if (fullscreen) {
      getWindow().addFlags(flagFs);
      getWindow().clearFlags(flagNoFs);
    }
    else {
      getWindow().clearFlags(flagFs);
      getWindow().addFlags(flagNoFs);
    }
  }

  private void startFrozenBubble() {
    //
    // Since the default game activity creates its own player,
    // destroy the current player.
    //
    //
    cleanUp();
    //
    // Create an intent to launch the activity to play the game.
    //
    //
    Intent intent = new Intent(this, FrozenBubble.class);
    startActivity(intent);
    //
    // Terminate the splash screen activity.
    //
    //
    finish();
  }
}
