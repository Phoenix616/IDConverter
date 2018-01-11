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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Gui extends JFrame {
    
    List<File> selectedFiles = new ArrayList<>();
    
    public Gui(String title) {
        super(title);
    
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel regexLine = new JPanel();
        regexLine.add(new JLabel("ID strings have to match:"));
        JTextField regexField = new JTextField("(\\W*)(\\d+)(\\W*)", 20);
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
            JFileChooser chooser = selectedFiles.isEmpty() ? new JFileChooser() : new JFileChooser(selectedFiles.get(0).getParentFile());
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(true);
            chooser.showOpenDialog(null);
            if (chooser.getSelectedFiles() != null) {
                selectedFiles.clear();
                selectedFiles.addAll(Arrays.asList(chooser.getSelectedFiles()));
                pathField.setText(Arrays.stream(chooser.getSelectedFiles()).map(f -> "\"" + f.getPath() + "\"").collect(Collectors.joining(" ")));
            } else if (chooser.getSelectedFile() != null) {
                selectedFiles.clear();
                selectedFiles.add(chooser.getSelectedFile());
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
            String regex = regexField.getText();
            boolean lowercase = lowercaseBox.getState();
            String fileRegex = fileField.getText();
            ReturnState r = IdConverter.replace(regex, lowercase, selectedFiles.stream().map(File::toPath).collect(Collectors.toList()), fileRegex);
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
