package com.del.mypass.view;

import org.apache.log4j.Logger;
import sun.awt.X11.XToolkit;

import javax.swing.*;

import java.awt.*;
import java.lang.reflect.Field;

import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

public class Launcher {

    final static Logger logger = Logger.getLogger(Launcher.class);

    public static MainFrame mainFrame;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.
                        getSystemLookAndFeelClassName());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            JFrame.setDefaultLookAndFeelDecorated(true);
            mainFrame = new MainFrame();
            mainFrame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            mainFrame.setVisible(true);
        });


        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            try {
                Toolkit xToolkit = Toolkit.getDefaultToolkit();
                Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
                awtAppClassNameField.setAccessible(true);
                awtAppClassNameField.set(xToolkit, "Hello Pingvi");


            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
