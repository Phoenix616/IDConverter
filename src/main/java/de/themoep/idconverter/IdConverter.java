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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class IdConverter {
    
    private static Properties p = new Properties();
    
    public static void main(String[] args) {
        try {
            InputStream s = IdConverter.class.getClassLoader().getResourceAsStream("app.properties");
            p.load(s);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    
        new Gui(p.getProperty("application.name") + " v" + p.getProperty("application.version")).setVisible(true);
    }
    
    /**
     * Replace the ids in a certain path that match a regex
     * @param regexString       The regex to match
     * @param lowercase         Whether the material name should be lowercase
     * @param paths             The paths to match
     * @param fileRegexString   The regex that the file names have to match
     * @return The return state
     */
    public static ReturnState replace(String regexString, boolean lowercase, List<Path> paths, String fileRegexString) {
        if (paths.isEmpty()) {
            return new ReturnState(ReturnType.MISSING_FILE, "Please select a path!");
        }
        Pattern regex;
        Pattern fileRegex;
        try {
            regex = Pattern.compile(regexString);
            fileRegex = Pattern.compile(fileRegexString);
        } catch (PatternSyntaxException e) {
            return new ReturnState(ReturnType.INVALID_REGEX, e.getMessage());
        }
        
        for (Path path : paths) {
            if (Files.isRegularFile(path)) {
                return replaceInFile(path, fileRegex, regex, lowercase);
            } else if (Files.isDirectory(path)) {
                return replaceInDirectory(path, fileRegex, regex, lowercase);
            }
        }
        return new ReturnState(ReturnType.SUCCESS);
    }
    
    private static ReturnState replaceInFile(Path path, Pattern fileRegex, Pattern regex, boolean lowercase) {
        if (!fileRegex.matcher(path.toFile().getName()).matches()) {
            return new ReturnState(ReturnType.SUCCESS);
        }
        
        Charset charset = StandardCharsets.UTF_8;
        try {
            String content = new String(Files.readAllBytes(path), charset);
            Matcher matcher = regex.matcher(content);
            Map<String, String> replacements = new HashMap<>();
            while (matcher.find()) {
                try {
                    String idStr = matcher.group(2);
                    String group = matcher.group();
                    
                    int id = Integer.parseInt(idStr);
                    
                    Material mat = Material.getMaterial(id);
                    
                    if (mat != null) {
                        String matString = mat.toString();
                        if (lowercase) {
                            matString = matString.toLowerCase();
                        }
                        replacements.put(group, matcher.group(1) + matString + matcher.group(3));
                    }
                } catch (IndexOutOfBoundsException e) {
                    return new ReturnState(ReturnType.INVALID_REGEX, "The regex is missing a group! There must be three groups for the free parts (before the numeric ID, the numeric ID and after the numeric ID!");
                } catch (NumberFormatException e) {
                    return new ReturnState(ReturnType.INVALID_REGEX, "The first group in the regex matched a non-numeric character! (The ID must be the second group!)");
                }
            }
            
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                content = content.replace(entry.getKey(), entry.getValue());
            }
            
            Files.write(path, content.getBytes(charset));
        } catch (IOException e) {
            return new ReturnState(ReturnType.UNKNOWN_ERROR, e.getMessage());
        }
        return new ReturnState(ReturnType.SUCCESS);
    }
    
    private static ReturnState replaceInDirectory(Path path, Pattern fileRegex, Pattern regex, boolean lowercase) {
        ReturnState r = new ReturnState(ReturnType.SUCCESS);
        try {
            Files.list(path).forEach(p -> {
                ReturnState rs = new ReturnState();
                
                if (!Files.exists(p)) {
                    rs = new ReturnState(ReturnType.MISSING_FILE);
                } else if (!Files.isWritable(p)) {
                    rs = new ReturnState(ReturnType.FILE_NOT_WRITABLE);
                } else if (!Files.isReadable(p)) {
                    rs = new ReturnState(ReturnType.FILE_NOT_READABLE);
                } else if (Files.isRegularFile(p)) {
                    rs = replaceInFile(p, fileRegex, regex, lowercase);
                } else if (Files.isDirectory(p)) {
                    rs = replaceInDirectory(p, fileRegex, regex, lowercase);
                }
                if (rs.getType() != ReturnType.SUCCESS) {
                    r.setType(rs.getType());
                    r.setMessage(rs.getMessage());
                }
            });
        } catch (IOException e) {
            new ReturnState(ReturnType.UNKNOWN_ERROR, e.getMessage());
        }
        return r;
    }
}
