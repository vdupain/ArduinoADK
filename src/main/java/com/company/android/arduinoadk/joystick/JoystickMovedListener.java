package com.company.android.arduinoadk.joystick;

/**
 * http://code.google.com/p/mobile-anarchy-widgets
 */
public interface JoystickMovedListener {
    public void OnMoved(int pan, int tilt);
    public void OnReleased();
    public void OnReturnedToCenter();
}