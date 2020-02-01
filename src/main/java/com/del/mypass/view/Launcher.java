package com.del.mypass.view;

import org.apache.log4j.Logger;

import javax.swing.*;

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
    }

}
