package com.del.mypass.actions;


import com.del.mypass.utils.SystemEnv;
import com.del.mypass.utils.Utils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class MainFrameActions implements WindowListener {

    final static private Logger logger = Logger.getLogger(MainFrameActions.class);

    private JFrame owner;

    public MainFrameActions(JFrame owner) {
        this.owner = owner;
    }

    @Override
    public void windowOpened(WindowEvent e) {
        logger.info("================================= WINDOW OPENED =================================");
        logger.info("Version: " + Utils.getInfo().getString("version.info"));
        logger.info("Loading system variables...");
        for (SystemEnv value : SystemEnv.values()) {
            logger.info("\t\t" + value.getName() + "=" + value.read());
        }
        logger.info("... success.");
    }

    @Override
    public void windowClosing(WindowEvent e) {
//        if (JOptionPane.showConfirmDialog(owner, "Вы действительно хотите выйти?", "Завершение работы",
//                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        logger.info("================================= WINDOW CLOSING =================================");
        System.exit(0);
//        }
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
