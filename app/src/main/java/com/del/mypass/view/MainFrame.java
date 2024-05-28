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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by DodolinEL
 * date: 02.07.2019
 */
public class MainFrame extends JFrame implements ActionListener {

    private SecretLocator secretLocator = new SecretLocator();
    private PasswordGenerator.PasswordGeneratorBuilder passwordBuilder;
    private Timer timer;
    private Timer cbCleaner;
    private JTextField filter;
    private Map<Integer, JList<PositionItem>> tabs;

    private Map<Integer, String> tabNames = new HashMap<Integer, String>() {{
        put(0, "Основные");
        put(1, "Другие");
    }};

    public MainFrame() {
        setTitle(String.format("Версия %s", Utils.getInfo().getString("version.info")));
        final JFrame _this = this;
        timer = new Timer(300, this);
        timer.setRepeats(false);
        cbCleaner = new Timer(10000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringSelection stringSelection = new StringSelection("");
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);

            }
        });
        cbCleaner.setRepeats(false);
        addWindowListener(new MainFrameActions(this));
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/img/ico_64x64.png")));
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        passwordBuilder = new PasswordGenerator.PasswordGeneratorBuilder()
                .useDigits(true)
                .useLower(true)
                .useUpper(true)
                .usePunctuation(false)
                .length(15);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu menuFile = new JMenu("Файл");
        menuBar.add(menuFile);
        JMenuItem menuItemGenerate = new JMenuItem("Параметры");
        JMenuItem menuItemBackup = new JMenuItem("Резервное копирование");
        JMenuItem menuItemRestore = new JMenuItem("Восстановление");
        JMenuItem menuItemExit = new JMenuItem("Выход");
        menuItemExit.addActionListener(arg0 -> dispatchEvent(new WindowEvent(MainFrame.this, WindowEvent.WINDOW_CLOSING)));
        menuItemGenerate.addActionListener(ae -> {
            PasswordDialog passwordDialog = new PasswordDialog(_this, passwordBuilder);
            passwordDialog.setLocationRelativeTo(_this);
            passwordDialog.setVisible(true);
        });
        menuItemBackup.addActionListener(actionEvent -> {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(false);
            int result = fc.showSaveDialog(_this);
            if (JFileChooser.APPROVE_OPTION == result) {
                File selectedFile = fc.getSelectedFile();
                try {
                    String pwd = JOptionPane.showInputDialog(_this, "Задайте пароль", "Резервная копия базы данных", JOptionPane.QUESTION_MESSAGE);
                    ServiceManager.getInstance().backupData(selectedFile.getCanonicalPath(), pwd, secretLocator);
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
        menuFile.add(menuItemGenerate);
        menuFile.add(new JSeparator());
        menuFile.add(menuItemBackup);
        menuFile.add(menuItemRestore);
        menuFile.add(new JSeparator());
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
                    Position p = ServiceManager.getInstance().findPosition(Utils.encodePass(name.getText(), secretLocator.read()));
                    String category = String.valueOf(tabbedPane.getSelectedIndex());
                    if (p == null) {
                        if (!StringUtil.isTrimmedEmpty(code.getText())) {
                            p = new Position();
                            p.setCategory(category);
                            p.setName(Utils.encodePass(name.getText(), secretLocator.read()));
                            p.setCode(Utils.encodePass(code.getText(), secretLocator.read()));
                            ServiceManager.getInstance().createPosition(p);
                        }
                    } else {
                        p.setCategory(category);
                        if (!StringUtil.isTrimmedEmpty(code.getText())) {
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
            code.setText(passwordBuilder.build().generate());
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
        filter.getInputMap().put(KeyStroke.getKeyStroke("ctrl alt shift N"), "translateNames");
        filter.getActionMap().put("translateNames", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    List<Position> positions = ServiceManager.getInstance().allPositions(null);
                    if (!ListUtil.isEmpty(positions)) {
                        if (JOptionPane.showConfirmDialog(_this,
                                String.format("Найдено записей: %s. Хотите начать трансляцию имен?", positions.size()),
                                "Подтвердите действие",
                                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            AtomicInteger translated = new AtomicInteger();
                            AtomicInteger updated = new AtomicInteger();
                            positions.forEach(p -> {
                                try {
                                    p.setName(Utils.encodePass(p.getName(), secretLocator.read()));
                                    translated.incrementAndGet();
                                } catch (Exception e) {
                                    Utils.getLogger().error("Не удалось транслировать запись: id=" + p.getId(), e);
                                }
                            });
                            positions.forEach(p -> {
                                try {
                                    ServiceManager.getInstance().updatePosition(p);
                                    updated.incrementAndGet();
                                } catch (CommonException e) {
                                    Utils.getLogger().error("Не удалось сохранить запись: id=" + p.getId(), e);
                                }
                            });
                            JOptionPane.showMessageDialog(_this,
                                    String.format("Транслировал: %s, сохранил: %s", translated.get(), updated.get())
                            );
                            initList();
                        }
                    }
                } catch (CommonException e) {
                    Utils.getLogger().error(e.getMessage(), e);
                }
            }
        });
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
                JPanel panel = new JPanel();
                JLabel label = new JLabel("Key:");
                JPasswordField pass = new JPasswordField(10);
                panel.add(label);
                panel.add(pass);
                String[] options = new String[]{"OK", "Cancel"};
                int option = JOptionPane.showOptionDialog(_this, panel, "Enter key",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                        null, options, options[0]);
                if (option == JOptionPane.OK_OPTION) {
                    char[] password = pass.getPassword();
                    String value = new String(password);
                    if (!StringUtil.isTrimmedEmpty(value)) secretLocator.setPrivateKey(value);
                    initList();
                }
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
                    JList<PositionItem> list = tabs.get(selectedIndex);
                    PositionItem p = list.getSelectedValue();
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
                    JList<PositionItem> list = tabs.get(selectedIndex);
                    PositionItem p = list.getSelectedValue();
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
                    cbCleaner.restart();
                }
            });
            list.getActionMap().put("delete", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    int selectedIndex = tabbedPane.getSelectedIndex();
                    JList<PositionItem> list = tabs.get(selectedIndex);
                    PositionItem p = list.getSelectedValue();
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

        setLocale(new Locale("RU"));
        pack();

        timer.start();
    }

    private void firstInitList() {
        initList();
    }

    private void initList() {
        try {
            tabs.values().forEach(list -> list.setBackground(Color.WHITE));
            List<Position> positions = ServiceManager.getInstance().allPositions(null);
            List<PositionItem> items = translate(positions);
            Map<Integer, List<PositionItem>> index = new HashMap<>();
            items.forEach(p -> {
                if (passFilter(p)) {
                    int i = p.getCategory();
                    if (!index.containsKey(i)) index.put(i, new ArrayList<>());
                    index.get(i).add(p);
                }
            });
            for (Integer i : tabs.keySet()) {
                List<PositionItem> positionList = index.get(i);
                DefaultListModel<PositionItem> listModel = new DefaultListModel<>();
                if (positionList != null) positionList.forEach(listModel::addElement);
                tabs.get(i).setModel(listModel);
            }
        } catch (CommonException e) {
            Utils.getLogger().error(e.getMessage(), e);
        }
    }

    private boolean passFilter(PositionItem p) {
        if (!StringUtil.isTrimmedEmpty(filter.getText())) {
            return p.getName().trim().toLowerCase().contains(filter.getText().trim().toLowerCase());
        }
        return true;
    }

    private List<PositionItem> translate(List<Position> positions) {
        List<PositionItem> r = new ArrayList<>();
        if (positions != null) {
            positions.forEach(p -> {
                try {
                    r.add(new PositionItem(
                            Utils.decodePass(p.getName(), secretLocator.read()),
                            parseCategory(p.getCategory()),
                            p
                    ));
                } catch (Exception e) {
                    //
                }
            });
        }
        return r;
    }

    private Integer parseCategory(String category) {
        Integer c = tabs.keySet().iterator().next();
        if (!StringUtil.isTrimmedEmpty(category))
            try {
                c = Integer.parseInt(category);
            } catch (NumberFormatException e) {
                //
            }
        return c;
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

    private JList<PositionItem> newList(int index, JTabbedPane tabbedPane) {
        JList<PositionItem> l = new JList<>();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(300, 300));
        l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setViewportView(l);
        tabbedPane.addTab(tabNames.get(index), scrollPane);
        return l;
    }
}
