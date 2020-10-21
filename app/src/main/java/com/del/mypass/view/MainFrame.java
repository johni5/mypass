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
import javax.swing.Timer;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created by DodolinEL
 * date: 02.07.2019
 */
public class MainFrame extends JFrame implements ActionListener {

    private SecretLocator secretLocator = new SecretLocator();
    private PasswordGenerator passwordGenerator;
    private Timer timer;
    private JTextField filter;
    private Map<Integer, JList<Position>> tabs;

    private Map<Integer, String> tabNames = new HashMap<Integer, String>() {{
        put(0, "Основные");
        put(1, "Другие");
    }};

    public MainFrame() {
        setTitle(String.format("Версия %s", Utils.getInfo().getString("version.info")));
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

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(300, 300));
        tabs = new LinkedHashMap<>();
        tabNames.keySet().forEach(i -> tabs.put(i, newList(i, tabbedPane)));

        filter = new JTextField();
        setStyle(filter, 14.0f, 3, null, null);
        filterPanel.add(filter);

        JTextField name = new JTextField();
        setStyle(name, 14.0f, 3, 200, null);

        JTextField code = new JTextField();
        setStyle(code, 14.0f, 3, 200, null);
        int wh = code.getPreferredSize().height;
        JButton add = new JButton(getImage("/img/ok.png", wh, wh));
        setStyle(add, 14.0f, 3, 30, 30);
        JButton gen = new JButton(getImage("/img/sync.png", wh, wh));
        setStyle(gen, 14.0f, 3, 30, 30);
        JButton del = new JButton(getImage("/img/del.png", wh, wh));
        setStyle(del, 14.0f, 3, 30, 30);
        editPanel.add(name);
        editPanel.add(new JLabel(":"));
        editPanel.add(code);
        editPanel.add(add);
        editPanel.add(gen);
        editPanel.add(del);

        getContentPane().add(filterPanel, BorderLayout.NORTH);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(editPanel, BorderLayout.SOUTH);

        tabs.values().forEach(list -> {
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        name.setText(list.getSelectedValue().getName());
                    }
                }
            });
        });


        add.addActionListener(ev -> {
            if (!StringUtil.isTrimmedEmpty(name.getText())) {
                try {
                    Position p = ServiceManager.getInstance().findPosition(name.getText());
                    String category = String.valueOf(tabbedPane.getSelectedIndex());
                    if (p == null) {
                        if (!StringUtil.isTrimmedEmpty(code.getText())) {
                            p = new Position();
                            p.setCategory(category);
                            p.setName(name.getText());
                            p.setCode(Utils.encodePass(code.getText(), secretLocator.read()));
                            ServiceManager.getInstance().createPosition(p);
                        }
                    } else {
                        if (!Objects.equals(p.getCategory(), category)) {
                            p.setCategory(category);
                        } else {
                            if (!StringUtil.isTrimmedEmpty(code.getText()))
                                p.setCode(Utils.encodePass(code.getText(), secretLocator.read()));
                        }
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
        del.addActionListener(ev -> {
            code.setText("");
            filter.setText("");
            name.setText("");
            timer.start();
        });

        filter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (timer.isRunning()) timer.restart();
                else timer.start();
            }
        });

        filter.getInputMap().put(KeyStroke.getKeyStroke("alt shift E"), "enterKey");
        filter.getInputMap().put(KeyStroke.getKeyStroke("ctrl alt shift E"), "showKey");
        filter.getInputMap().put(KeyStroke.getKeyStroke("ctrl alt shift P"), "manualPath");
        filter.getActionMap().put("manualPath", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String value = JOptionPane.showInputDialog(_this, "Enter path", secretLocator.getPath());
                if (value != null) secretLocator.setManualPath(value);
            }
        });
        filter.getActionMap().put("showKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(_this, secretLocator.read(), "My code", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        filter.getActionMap().put("enterKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String value = JOptionPane.showInputDialog(_this, "Enter code", secretLocator.getPrivateKey());
                if (value != null) secretLocator.setPrivateKey(value);
            }
        });

        tabs.values().forEach(list -> {
            list.getInputMap().put(KeyStroke.getKeyStroke("ctrl shift C"), "copyKey");
            list.getInputMap().put(KeyStroke.getKeyStroke("ctrl shift Q"), "showQR");
            list.getInputMap().put(KeyStroke.getKeyStroke("DELETE"), "delete");

            list.getActionMap().put("showQR", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    int selectedIndex = tabbedPane.getSelectedIndex();
                    JList<Position> list = tabs.get(selectedIndex);
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
                        int w = byteMatrix.getWidth();
                        BufferedImage image = new BufferedImage(w, w, BufferedImage.TYPE_INT_RGB);
                        image.createGraphics();

                        Graphics2D graphics = (Graphics2D) image.getGraphics();
                        graphics.setColor(Color.WHITE);
                        graphics.fillRect(0, 0, w, w);
                        graphics.setColor(Color.BLACK);

                        for (int i = 0; i < w; i++) {
                            for (int j = 0; j < w; j++) {
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
            list.getActionMap().put("copyKey", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    int selectedIndex = tabbedPane.getSelectedIndex();
                    JList<Position> list = tabs.get(selectedIndex);
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
                    int selectedIndex = tabbedPane.getSelectedIndex();
                    JList<Position> list = tabs.get(selectedIndex);
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
        });

        firstInitList();
        setLocale(new Locale("RU"));
        pack();
    }

    private void firstInitList() {
        initList();
    }

    private void initList() {
        try {
            tabs.values().forEach(list -> list.setBackground(Color.WHITE));
            List<Position> positions = ServiceManager.getInstance().allPositions(filter.getText());
            Map<Integer, List<Position>> index = new HashMap<>();
            positions.forEach(p -> {
                int i = tabs.keySet().iterator().next();
                try {
                    if (p.getCategory() != null) i = Integer.parseInt(p.getCategory());
                } catch (NumberFormatException e) {
                    //
                }
                if (!index.containsKey(i)) index.put(i, new ArrayList<>());
                index.get(i).add(p);
            });
            for (Integer i : tabs.keySet()) {
                List<Position> positionList = index.get(i);
                DefaultListModel<Position> listModel = new DefaultListModel<>();
                if (positionList != null) positionList.forEach(listModel::addElement);
                tabs.get(i).setModel(listModel);
            }
        } catch (CommonException e) {
            Utils.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        initList();
    }

    private void setStyle(JComponent c, Float fontSize, Integer padding, Integer width, Integer height) {
        if (fontSize != null) c.setFont(c.getFont().deriveFont(fontSize));
        if (padding != null)
            c.setBorder(BorderFactory.createCompoundBorder(
                    c.getBorder(),
                    BorderFactory.createEmptyBorder(padding, padding, padding, padding)));
        if (width != null || height != null) {
            Integer w = width != null ? width : c.getPreferredSize().width;
            Integer h = height != null ? height : c.getPreferredSize().height;
            c.setPreferredSize(new Dimension(w, h));
        }
    }

    private ImageIcon getImage(String path, int w, int h) {
        Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(path));
        return new ImageIcon(image.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH));
    }

    private JList<Position> newList(int index, JTabbedPane tabbedPane) {
        JList<Position> l = new JList<>();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(300, 300));
        l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setViewportView(l);
        tabbedPane.addTab(tabNames.get(index), scrollPane);
        return l;
    }
}
