package com.del.mypass.view;


import com.del.mypass.actions.MainFrameActions;
import com.del.mypass.dao.ServiceManager;
import com.del.mypass.db.Position;
import com.del.mypass.utils.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by DodolinEL
 * date: 02.07.2019
 */
public class MainFrame extends JFrame implements ActionListener {

    private SecretLocator secretLocator = new SecretLocator();
    private PasswordGenerator passwordGenerator;
    private Timer timer;
    private JTextField filter;
    private JList<Position> list;

    public MainFrame() {
        setTitle("Готов к работе");
        final JFrame _this = this;
        timer = new Timer(300, this);
        timer.setRepeats(false);
        addWindowListener(new MainFrameActions(this));
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/img/ico_64x64.png")));
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        passwordGenerator = new PasswordGenerator.PasswordGeneratorBuilder()
                .useDigits(true)
                .useLower(true)
                .useUpper(true)
                .usePunctuation(true)
                .build();

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu menuFile = new JMenu("Файл");
        menuBar.add(menuFile);
        JMenuItem menuItemBackup = new JMenuItem("Резервное копирование");
        JMenuItem menuItemRestore = new JMenuItem("Восстановление");
        JMenuItem menuItemExit = new JMenuItem("Выход");
        menuItemExit.addActionListener(arg0 -> dispatchEvent(new WindowEvent(MainFrame.this, WindowEvent.WINDOW_CLOSING)));
        menuItemBackup.addActionListener(actionEvent -> {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(false);
            int result = fc.showSaveDialog(_this);
            if (JFileChooser.APPROVE_OPTION == result) {
                File selectedFile = fc.getSelectedFile();
                try {
                    String pwd = JOptionPane.showInputDialog(_this, "Задайте пароль", "Резервная копия базы данных", JOptionPane.QUESTION_MESSAGE);
                    if (!StringUtil.isTrimmedEmpty(pwd)) {
                        ServiceManager.getInstance().backupData(selectedFile.getCanonicalPath(), pwd);
                    }
                } catch (Exception e1) {
                    Utils.getLogger().error(e1.getMessage(), e1);
                }
            }
        });
        menuItemRestore.addActionListener(actionEvent -> {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(false);
            int result = fc.showOpenDialog(_this);
            if (JFileChooser.APPROVE_OPTION == result) {
                File selectedFile = fc.getSelectedFile();
                try {
                    String pwd = JOptionPane.showInputDialog(_this, "Введите пароль", "Восстановление базы данных", JOptionPane.QUESTION_MESSAGE);
                    if (!StringUtil.isTrimmedEmpty(pwd)) {
                        ServiceManager.getInstance().restoreData(selectedFile.getCanonicalPath(), pwd);
                        initList();
                    }
                } catch (Exception e1) {
                    Utils.getLogger().error(e1.getMessage(), e1);
                }
            }
        });
        menuFile.add(menuItemBackup);
        menuFile.add(menuItemRestore);
        menuFile.add(menuItemExit);

        JPanel filterPanel = new JPanel(new BorderLayout());
        JPanel editPanel = new JPanel();

        JScrollPane scrollPane = new JScrollPane();
        list = new JList<>();
        scrollPane.setPreferredSize(new Dimension(300, 300));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setViewportView(list);

        filter = new JTextField();
        filterPanel.add(filter);
        filter.setPreferredSize(new Dimension(300, 40));

        JTextField name = new JTextField();
        JTextField code = new JTextField();
        JButton add = new JButton("V");
        JButton gen = new JButton("G");
        editPanel.add(name).setPreferredSize(new Dimension(200, 40));
        editPanel.add(new JLabel(":"));
        editPanel.add(code).setPreferredSize(new Dimension(200, 40));
        editPanel.add(add).setPreferredSize(new Dimension(40, 40));
        editPanel.add(gen).setPreferredSize(new Dimension(40, 40));

        getContentPane().add(filterPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(editPanel, BorderLayout.SOUTH);

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    name.setText(list.getSelectedValue().getName());
                }
            }
        });


        add.addActionListener(ev -> {
            if (!StringUtil.isTrimmedEmpty(name.getText()) && !StringUtil.isTrimmedEmpty(code.getText())) {
                try {
                    Position p = ServiceManager.getInstance().findPosition(name.getText());
                    if (p == null) {
                        p = new Position();
                        p.setName(name.getText());
                        p.setCode(Utils.encodePass(code.getText(), secretLocator.read()));
                        ServiceManager.getInstance().createPosition(p);
                    } else {
                        p.setCode(Utils.encodePass(code.getText(), secretLocator.read()));
                        ServiceManager.getInstance().updatePosition(p);
                    }
                    initList();
                    name.setText("");
                    code.setText("");
                } catch (Exception e) {
                    Utils.getLogger().error(e.getMessage(), e);
                }
            }
        });
        gen.addActionListener(ev -> {
            code.setText(passwordGenerator.generate(16));
        });

        filter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (timer.isRunning()) timer.restart();
                else timer.start();
            }
        });

        list.getInputMap().put(KeyStroke.getKeyStroke("alt shift E"), "enterKey");
        list.getInputMap().put(KeyStroke.getKeyStroke("ctrl alt shift E"), "showKey");
        list.getInputMap().put(KeyStroke.getKeyStroke("ctrl alt shift P"), "manualPath");
        list.getInputMap().put(KeyStroke.getKeyStroke("ctrl shift C"), "copyKey");
        list.getInputMap().put(KeyStroke.getKeyStroke("ctrl shift Q"), "showQR");
        list.getInputMap().put(KeyStroke.getKeyStroke("DELETE"), "delete");

        list.getActionMap().put("showQR", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Position p = list.getSelectedValue();
                if (p == null) return;
                Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
                hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

                // Now with zxing version 3.2.1 you could change border size (white border size to just 1)
                hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
                hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                try {
                    String pass = Utils.decodePass(p.getCode(), secretLocator.read());
                    BitMatrix byteMatrix = qrCodeWriter.encode(pass, BarcodeFormat.QR_CODE, 200, 200, hintMap);
                    int CrunchifyWidth = byteMatrix.getWidth();
                    BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB);
                    image.createGraphics();

                    Graphics2D graphics = (Graphics2D) image.getGraphics();
                    graphics.setColor(Color.WHITE);
                    graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
                    graphics.setColor(Color.BLACK);

                    for (int i = 0; i < CrunchifyWidth; i++) {
                        for (int j = 0; j < CrunchifyWidth; j++) {
                            if (byteMatrix.get(i, j)) {
                                graphics.fillRect(i, j, 1, 1);
                            }
                        }
                    }

                    JLabel imgLabel = new JLabel();
                    imgLabel.setPreferredSize(new Dimension(200, 200));
                    ImageIcon ii = new ImageIcon();
                    ii.setImage(image);
                    imgLabel.setIcon(ii);

                    JDialog jd = new JDialog(_this, "QR Code", true);
                    jd.setLocationRelativeTo(_this);
                    jd.getContentPane().add(imgLabel);
                    jd.pack();
                    jd.setVisible(true);

                } catch (Exception e) {
                    Utils.getLogger().error(e.getMessage(), e);
                }

            }
        });
        list.getActionMap().put("manualPath", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String value = JOptionPane.showInputDialog(_this, "Enter path", secretLocator.getPath());
                if (value != null) secretLocator.setManualPath(value);
            }
        });
        list.getActionMap().put("showKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(_this, secretLocator.read(), "My code", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        list.getActionMap().put("enterKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String value = JOptionPane.showInputDialog(_this, "Enter code", secretLocator.getPrivateKey());
                if (value != null) secretLocator.setPrivateKey(value);
            }
        });
        list.getActionMap().put("copyKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Position p = list.getSelectedValue();
                String pass = "VeryFunny";
                try {
                    pass = Utils.decodePass(p.getCode(), secretLocator.read());
                    list.setBackground(Color.GREEN);
                } catch (Exception e) {
                    Utils.getLogger().error(e.getMessage(), e);
                    list.setBackground(Color.RED);
                }
                timer.start();
                StringSelection stringSelection = new StringSelection(pass);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
        });
        list.getActionMap().put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Position p = list.getSelectedValue();
                if (p != null) {
                    try {
                        if (JOptionPane.showConfirmDialog(_this, String.format("Вы действительно хотите удалить '%s'?", p.getName()), "Удалить запись",
                                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            ServiceManager.getInstance().deletePosition(p.getId());
                            initList();
                        }
                    } catch (Exception e) {
                        Utils.getLogger().error(e.getMessage(), e);
                    }
                }
            }
        });

        firstInitList();
        setLocale(new Locale("RU"));
        pack();
    }

    private void firstInitList() {
        list.setEnabled(false);
        Thread t = new Thread(() -> {
            initList();
            list.setEnabled(true);
        });
        t.start();
    }

    private void initList() {
        try {
            list.setBackground(Color.WHITE);
            List<Position> positions = ServiceManager.getInstance().allPositions(filter.getText());
            DefaultListModel<Position> listModel = new DefaultListModel<>();
            positions.forEach(listModel::addElement);
            list.setModel(listModel);
        } catch (CommonException e) {
            Utils.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        initList();
    }
}
