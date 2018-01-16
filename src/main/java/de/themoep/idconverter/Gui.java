package de.themoep.idconverter;

/*
 * Minecraft numeric ID to Bukkit Material enum converter
 * Copyright (C) 2017  Max Lee (https://github.com/Phoenix616)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version dirt of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Gui extends JFrame {
    
    public Gui(String title) {
        super(title);
    
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JTextField regexField = new JTextField("", 20);
        
        JPanel replaceTypeLine = new JPanel();
        
        JPanel replaceFromColumn = new JPanel();
        replaceFromColumn.add(new JLabel("Replace from:"));
        JList<IdMappings.IdType> replaceFromList = new JList<>(IdMappings.IdType.values());
        replaceFromList.setSelectedValue(IdMappings.IdType.NUMERIC, true);
        replaceFromList.setLayout(new BorderLayout(5, 5));
        replaceFromList.addListSelectionListener(e -> {
            for (IdMappings.IdType type : IdMappings.IdType.values()) {
                if (type.getRegex().equals(regexField.getText())) {
                    regexField.setText(replaceFromList.getSelectedValue().getRegex());
                    return;
                }
            }
        });
        replaceFromColumn.add(replaceFromList);
        
        replaceTypeLine.add(replaceFromColumn);
    
        JPanel replaceToColumn = new JPanel();
        replaceToColumn.add(new JLabel("Replace to:"));
        JList<IdMappings.IdType> replaceToList = new JList<>(IdMappings.IdType.values());
        replaceToList.setSelectedValue(IdMappings.IdType.FLATTENING, true);
        replaceToList.setLayout(new BorderLayout(5, 5));
        replaceToColumn.add(replaceToList);
        
        replaceTypeLine.add(replaceToColumn);
        
        getContentPane().add(replaceTypeLine);
        
        JPanel regexLine = new JPanel();
        regexLine.add(new JLabel("ID strings have to match:"));
    
        regexField.setText(replaceFromList.getSelectedValue().getRegex());
        regexLine.add(regexField);
        regexLine.add(new JLabel("(Regex)"));
        getContentPane().add(regexLine);
    
        JPanel lowercaseLine = new JPanel();
        Checkbox lowercaseBox = new Checkbox("Lowercase Material string?", true);
        lowercaseLine.add(lowercaseBox, BorderLayout.LINE_START);
        getContentPane().add(lowercaseLine);
        
        JPanel pathLine = new JPanel();
        JTextField pathField = new JTextField(20);
        pathLine.add(pathField);
        JButton buttonSelectPath = new JButton("Select File/Directory");
        buttonSelectPath.addActionListener(e -> {
            String[] paths = pathField.getText().split("\" \"");
            JFileChooser chooser = pathField.getText().isEmpty() || paths.length == 0 ? new JFileChooser()
                    : new JFileChooser(new File(paths[0].substring(1, paths[0].length() - 1)).getParentFile());
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(true);
            chooser.showOpenDialog(null);
            if (chooser.getSelectedFiles() != null) {
                pathField.setText(Arrays.stream(chooser.getSelectedFiles()).map(f -> "\"" + f.getPath() + "\"").collect(Collectors.joining(" ")));
            } else if (chooser.getSelectedFile() != null) {
                pathField.setText(chooser.getSelectedFile().getPath());
            }
        });
        pathLine.add(buttonSelectPath);
        getContentPane().add(pathLine);
        
        JPanel fileLine = new JPanel();
        fileLine.add(new JLabel("Replace in files that match:"));
        JTextField fileField = new JTextField("\\w+\\.yml", 20);
        fileLine.add(fileField);
        fileLine.add(new JLabel("(Regex)"));
        getContentPane().add(fileLine);
    
        JPanel convertPanel = new JPanel();
        JButton buttonConvert = new JButton("Convert");
        buttonConvert.addActionListener(e -> {
            IdMappings.IdType replaceFrom = replaceFromList.getSelectedValue();
            IdMappings.IdType replaceTo = replaceToList.getSelectedValue();
            String regex = regexField.getText();
            boolean lowercase = lowercaseBox.getState();
            String fileRegex = fileField.getText();
            ReturnState r = IdConverter.replace(Arrays.stream(pathField.getText().split("\" \"")).map(s -> Paths.get(s.substring(1, s.length() - 1))).collect(Collectors.toList()), fileRegex, replaceFrom, replaceTo, regex, lowercase);
            if (r.getType() == ReturnType.SUCCESS) {
                JOptionPane.showMessageDialog(this, "Successfully replaced IDs in file(s) with Material names!", "SUCCESS!", JOptionPane.INFORMATION_MESSAGE);
            } else if (r.getMessage().isPresent()) {
                JOptionPane.showMessageDialog(this, r.getMessage().get(), r.getType().toHuman() + "!", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, r.getType().toHuman(), r.getType().toHuman() + "!", JOptionPane.ERROR_MESSAGE);
            }
        });
        convertPanel.add(buttonConvert);
        getContentPane().add(convertPanel, BorderLayout.CENTER);
        
        setLocationRelativeTo(null);
        pack();
    }

}
