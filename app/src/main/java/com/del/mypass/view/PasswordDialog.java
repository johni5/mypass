package com.del.mypass.view;

import com.del.mypass.utils.PasswordGenerator;

import javax.swing.*;
import java.awt.*;

public class PasswordDialog extends JDialog {

    public PasswordDialog(Frame owner, PasswordGenerator.PasswordGeneratorBuilder passwordBuilder) {
        super(owner, "Параметры генератора", true);
        JPanel editPanel = new JPanel();
        getContentPane().add(editPanel, BorderLayout.CENTER);

        JCheckBox checkDigits = new JCheckBox("Цифры", passwordBuilder.isUseDigits());
        JCheckBox checkLower = new JCheckBox("Нижний регистр", passwordBuilder.isUseLower());
        JCheckBox checkUpper = new JCheckBox("Верхний регистр", passwordBuilder.isUseUpper());
        JCheckBox checkPunctuation = new JCheckBox("Пунктуация", passwordBuilder.isUsePunctuation());

        SpinnerModel value = new SpinnerNumberModel(passwordBuilder.getLength(), //initial value
                1, //minimum value
                100, //maximum value
                1); //step
        JSpinner jsSize = new JSpinner(value);

        editPanel.add(checkDigits);
        editPanel.add(checkLower);
        editPanel.add(checkUpper);
        editPanel.add(checkPunctuation);
        editPanel.add(jsSize);

        checkDigits.addActionListener(a -> passwordBuilder.useDigits(checkDigits.isSelected()));
        checkLower.addActionListener(a -> passwordBuilder.useLower(checkLower.isSelected()));
        checkUpper.addActionListener(a -> passwordBuilder.useUpper(checkUpper.isSelected()));
        checkPunctuation.addActionListener(a -> passwordBuilder.usePunctuation(checkPunctuation.isSelected()));
        jsSize.addChangeListener(ch -> passwordBuilder.length(Integer.parseInt(jsSize.getValue().toString())));

        pack();
    }
}
