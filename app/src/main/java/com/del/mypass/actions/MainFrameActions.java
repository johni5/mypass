package com.del.mypass.actions;


import com.del.mypass.dao.ServiceManager;
import com.del.mypass.utils.CommonException;
import com.del.mypass.utils.SystemEnv;
import com.del.mypass.utils.Utils;
import com.del.mypass.view.MainFrame;
import org.apache.log4j.Logger;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class MainFrameActions implements WindowListener {

    final static private Logger logger = Logger.getLogger(MainFrameActions.class);

    private MainFrame owner;

    public MainFrameActions(MainFrame owner) {
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
        logger.info("================================= WINDOW CLOSING =================================");
        if (ServiceManager.isReady()) {
            try {
                ServiceManager.getInstance().save(owner.getSecretKey());
            } catch (CommonException ex) {
                logger.error(ex.getMessage(), ex);
            }
            try {
                ServiceManager.getInstance().closeConnection();
            } catch (CommonException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        System.exit(0);
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
