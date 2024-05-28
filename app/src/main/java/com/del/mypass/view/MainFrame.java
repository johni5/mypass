package com.del.mypass.view;


import com.del.mypass.actions.MainFrameActions;
import com.del.mypass.dao.ServiceManager;
import com.del.mypass.db.Position;
import com.del.mypass.utils.*;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.crypto.SecretKey;
import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by DodolinEL
 * date: 02.07.2019
 */
public class MainFrame extends JFrame {

    private PasswordGenerator.PasswordGeneratorBuilder passwordBuilder;
    private Timer timer;
    private Timer cbCleaner;
    private Timer blinkTimer;
    private JTextField filter;
    private Map<String, JList<PositionItem>> tabs;
    private SystemInfo systemInfo;
    private SecretKey secretKey;
    private int lastIndexTab;

    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenuItem menuItemGenerate;
    private JMenuItem menuItemRename;
    private JMenuItem menuItemBackup;
    private JMenuItem menuItemRestore;
    private JMenuItem menuItemExit;
    private JPanel filterPanel;
    private JPanel editPanel;
    private JTabbedPane tabbedPane;
    private JTextField nameTF;
    private JTextField codeTF;

    private JButton addBtn;
    private JButton genBtn;
    private JButton delBtn;

    final JFrame _this;

    private List<String> tabNames = Lists.newArrayList("Основные", "Другие");

    public MainFrame() {
        setTitle(String.format("Версия %s", Utils.getInfo().getString("version.info")));
        _this = this;
        systemInfo = new SystemInfo();

        timer = new Timer(300, e -> initList());
        timer.setRepeats(false);

        cbCleaner = new Timer(10000, e -> {
            StringSelection stringSelection = new StringSelection("");
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
        cbCleaner.setRepeats(false);

        blinkTimer = new Timer(100, e -> filter.setBackground(Color.WHITE));
        blinkTimer.setRepeats(false);

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

        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        menuFile = new JMenu("Файл");
        menuBar.add(menuFile);
        menuItemGenerate = new JMenuItem("Параметры");
        menuItemRename = new JMenuItem("Переименовать группу");
        menuItemBackup = new JMenuItem("Резервное копирование");
        menuItemRestore = new JMenuItem("Восстановление");
        menuItemExit = new JMenuItem("Выход");
        menuItemRename.addActionListener(a -> {
            String oldName = tabNames.get(tabbedPane.getSelectedIndex());
            String name = JOptionPane.showInputDialog(_this, "Введите новое имя", oldName);
            if (!StringUtil.isTrimmedEmpty(name)) {
                try {
                    if (ServiceManager.isReady())
                        ServiceManager.getInstance().renameGroup(oldName, name);
                    tabNames.set(tabbedPane.getSelectedIndex(), name);
                    initTabs();
                    initList();
                    tabbedPane.setSelectedIndex(lastIndexTab);

                } catch (Exception e) {
                    Utils.getLogger().error(e.getMessage(), e);
                    blinkRed();
                }
            }
        });
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
                    ServiceManager.getInstance().restoreData(selectedFile.getCanonicalPath(), pwd);
                    tabNames.clear();
                    initList();
                } catch (Exception e1) {
                    Utils.getLogger().error(e1.getMessage(), e1);
                }
            }
        });
        menuFile.add(menuItemGenerate);
        menuFile.add(menuItemRename);
        menuFile.add(new JSeparator());
        menuFile.add(menuItemBackup);
        menuFile.add(menuItemRestore);
        menuFile.add(new JSeparator());
        menuFile.add(menuItemExit);

        filterPanel = new JPanel(new BorderLayout());
        editPanel = new JPanel();

        tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(300, 300));
        tabbedPane.getModel().addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == tabNames.size()) {
                String n = JOptionPane.showInputDialog(MainFrame.this, "Название", "Группа " + tabNames.size());
                if (n != null && n.trim().length() > 0 && !tabNames.contains(n)) {
                    tabbedPane.remove(tabNames.size());
                    tabNames.add(n);
                    addTab(n);
                    addLastTab();
                    tabbedPane.setSelectedIndex(tabNames.size() - 1);
                } else {
                    tabbedPane.setSelectedIndex(lastIndexTab);
                }
            } else {
                lastIndexTab = tabbedPane.getSelectedIndex();
            }
        });
        tabs = new LinkedHashMap<>();

        filter = new JTextField();
        setStyle(filter, 14.0f, 3, null, null);
        filterPanel.add(filter);

        nameTF = new JTextField();
        setStyle(nameTF, 14.0f, 3, 200, null);

        codeTF = new JTextField();
        setStyle(codeTF, 14.0f, 3, 200, null);
        int wh = codeTF.getPreferredSize().height;
        addBtn = new JButton(getImage("/img/ok.png", wh, wh));
        setStyle(addBtn, 14.0f, 3, 30, 30);
        genBtn = new JButton(getImage("/img/sync.png", wh, wh));
        setStyle(genBtn, 14.0f, 3, 30, 30);
        delBtn = new JButton(getImage("/img/del.png", wh, wh));
        setStyle(delBtn, 14.0f, 3, 30, 30);
        editPanel.add(nameTF);
        editPanel.add(new JLabel(":"));
        editPanel.add(codeTF);
        editPanel.add(addBtn);
        editPanel.add(genBtn);
        editPanel.add(delBtn);

        getContentPane().add(filterPanel, BorderLayout.NORTH);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(editPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(ev -> {
            if (!StringUtil.isTrimmedEmpty(nameTF.getText())) {
                try {
                    Position p = ServiceManager.getInstance().findPosition(nameTF.getText());
                    int selectedIndex = tabbedPane.getSelectedIndex();
                    String category = tabNames.get(selectedIndex);
                    if (p == null) {
                        if (!StringUtil.isTrimmedEmpty(codeTF.getText())) {
                            p = new Position();
                            p.setCategory(category);
                            p.setName(nameTF.getText());
                            p.setCode(codeTF.getText());
                            ServiceManager.getInstance().createPosition(p);
                        }
                    } else {
                        p.setCategory(category);
                        if (!StringUtil.isTrimmedEmpty(codeTF.getText())) {
                            p.setCode(codeTF.getText());
                        }
                        ServiceManager.getInstance().updatePosition(p);
                    }
                    initList();
                    nameTF.setText("");
                    codeTF.setText("");
                } catch (Exception e) {
                    Utils.getLogger().error(e.getMessage(), e);
                    blinkRed();
                }
            }
        });
        genBtn.addActionListener(ev -> {
            codeTF.setText(passwordBuilder.build().generate());
        });
        delBtn.addActionListener(ev -> {
            codeTF.setText("");
            filter.setText("");
            nameTF.setText("");
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
                                    p.setName(p.getName());
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
                    blinkRed();
                }
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
                    if (!StringUtil.isTrimmedEmpty(value)) {
                        try {
                            byte[] salt = systemInfo.toString().getBytes();
                            secretKey = Utils.getKeyFromPassword(value, salt);
                            if (!ServiceManager.isReady()) {
                                ServiceManager sm = ServiceManager.begin(secretKey);
                                if (sm.getSize() > 0) tabNames.clear();
                            }
                        } catch (Exception e) {
                            Utils.getLogger().error(e.getMessage(), e);
                        }
                    }
                    initList();
                }
            }
        });
        initTabs();

        setLocale(new Locale("RU"));
        pack();

        timer.start();
    }

    private void addTab(String s) {
        JList<PositionItem> list = newTabItems(s);
        tabs.put(s, list);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    nameTF.setText(list.getSelectedValue().getName());
                }
            }
        });
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
                    BitMatrix byteMatrix = qrCodeWriter.encode(p.getCode(), BarcodeFormat.QR_CODE, 200, 200, hintMap);
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
                    blinkRed();
                }

            }
        });
        list.getActionMap().put("copyKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JList<PositionItem> list = tabs.get(tabNames.get(tabbedPane.getSelectedIndex()));
                PositionItem p = list.getSelectedValue();
                String pass = p.getCode();
                blinkGreen();
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
                String title = tabNames.get(selectedIndex);
                JList<PositionItem> list = tabs.get(title);
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

    }

    private void addLastTab() {
        if (tabNames.size() > 5) return;
        tabbedPane.addTab("+", new JPanel());
    }

    private void initTabs() {
        tabs.clear();
        tabbedPane.removeAll();
        tabNames.forEach(this::addTab);
        addLastTab();
    }

    private void blinkRed() {
        filter.setBackground(Color.RED);
        blinkTimer.start();
    }

    private void blinkGreen() {
        filter.setBackground(Color.GREEN);
        blinkTimer.start();
    }

    private void initList() {
        try {
            tabs.values().forEach(list -> list.setBackground(Color.WHITE));
            if (ServiceManager.isReady()) {
                List<Position> positions = ServiceManager.getInstance().allPositions(null);

                if (!ListUtil.isEmpty(positions)) {
                    List<PositionItem> items = positions.stream().map(PositionItem::new).collect(Collectors.toList());
                    Multimap<String, PositionItem> index = LinkedHashMultimap.create();
                    items.forEach(p -> {
                        if (passFilter(p)) {
                            String i = p.getPosition().getCategory();
                            index.put(i, p);
                        }
                    });
                    if (tabNames.isEmpty()) {
                        tabNames.addAll(index.keySet());
                        tabNames.sort(String::compareTo);
                        initTabs();
                    }

                    for (String s : tabs.keySet()) {
                        Collection<PositionItem> positionList = index.get(s);
                        DefaultListModel<PositionItem> listModel = new DefaultListModel<>();
                        if (positionList != null) positionList.forEach(listModel::addElement);
                        tabs.get(s).setModel(listModel);
                    }
                }
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

    private JList<PositionItem> newTabItems(String title) {
        JList<PositionItem> l = new JList<>();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(300, 300));
        l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setViewportView(l);
        tabbedPane.addTab(title, scrollPane);
        int index = tabbedPane.indexOfTab(title);


        JPanel pnlTab = new JPanel(new GridBagLayout());
        pnlTab.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        JLabel btnClose = new JLabel("×");
        btnClose.setHorizontalAlignment(SwingConstants.CENTER);
        btnClose.setVerticalAlignment(SwingConstants.CENTER);
        btnClose.setForeground(Color.BLACK);
        btnClose.setPreferredSize(new Dimension(16, 12));
        btnClose.setFont(new Font("Serif", Font.BOLD, 14));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        pnlTab.add(lblTitle, gbc);

        gbc.gridx++;
        gbc.weightx = 0;
        pnlTab.add(btnClose, gbc);

        tabbedPane.setTabComponentAt(index, pnlTab);
        btnClose.addMouseListener(new ClickMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tabs.get(title).getModel().getSize() > 0) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Нельзя удалить вкладку в которой есть записи!");
                } else if (tabNames.size() == 1) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Нельзя удалять все вкладки!");
                } else {
                    tabNames.remove(title);
                    tabbedPane.remove(tabbedPane.indexOfTab(title));
                    if (tabbedPane.getSelectedIndex() == tabNames.size()) {
                        tabbedPane.setSelectedIndex(tabNames.size() - 1);
                        lastIndexTab = tabNames.size() - 1;
                    }
                }
            }
        });

        return l;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}
