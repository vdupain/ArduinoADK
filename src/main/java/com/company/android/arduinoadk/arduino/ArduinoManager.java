package com.company.android.arduinoadk.arduino;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.company.android.arduinoadk.WhatAbout;
import com.company.android.arduinoadk.usb.UsbAccessoryManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ArduinoManager implements Runnable {
    private static final String TAG = ArduinoManager.class.getSimpleName();

    private FileInputStream inputStream;
    private FileOutputStream outputStream;

    private Handler handler;

    public ArduinoManager(UsbAccessoryManager usbAccessoryManager) {
        if (usbAccessoryManager!=null) {
            this.outputStream = usbAccessoryManager.getOutputStream();
            this.inputStream = usbAccessoryManager.getInputStream();
        }
    }

    public void sendStickCommand(double valueX, double valueY) {
        byte x = (byte) (valueX);
        byte y = (byte) (valueY);
        this.sendCommand((byte) 3, x, y);
    }

    @Override
    public void run() {
        int ret = 0;
        byte[] buffer = new byte[16384];
        int i;

        while (ret >= 0) { // read data
            try {
                ret = inputStream.read(buffer);
            } catch (IOException e) {
                break;
            }

            i = 0;
            while (i < ret) {
                int len = ret - i;
                // command
                switch (buffer[i]) {
                    case 0x4:
                        if (len >= 3) {
                            // unsigned byte on arduino and signed in Java so...
                            int angleServo1 = buffer[i + 1] & 0xFF;
                            int angleServo2 = buffer[i + 2] & 0xFF;
                            Log.d(TAG, "position servos:" + angleServo1 + " - "
                                    + angleServo2);
                            Message message = Message.obtain(handler,
                                    WhatAbout.ARDUINO.ordinal(), angleServo1
                                    + " - " + angleServo2);
                            this.handler.sendMessage(message);
                        }
                        i += 3;
                        break;
                    case 0x6:
                        if (len >= 3) {
                            Message m = Message.obtain(handler,
                                    WhatAbout.TELEMETRY.ordinal());
                            // unsigned byte on arduino and signed in Java so...
                            int degree = buffer[i + 1] & 0xFF;
                            int distance = buffer[i + 2] & 0xFF;
                            // m.obj = new ArduinoMessage(degree, distance);
                            // handler.sendMessage(m);
                        }
                        i += 3;
                        break;

                    default:
                        Log.d(TAG, "Unknown msg: " + buffer[i]);
                        i = len;
                        break;
                }
            }
        }
    }

    /**
     * @param command
     * @param target
     * @param value
     */
    public void sendCommand(byte command, byte target, int value) {
        byte[] buffer = new byte[3];
        if (value > 255)
            value = 255;

        buffer[0] = command;
        buffer[1] = target;
        buffer[2] = (byte) value;
        if (outputStream != null &&  buffer[1] != -1) {
            try {
                outputStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

}
