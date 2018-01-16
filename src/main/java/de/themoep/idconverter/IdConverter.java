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

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
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
    
        if (System.console() == null && !GraphicsEnvironment.isHeadless()) {
            new Gui(p.getProperty("application.name") + " v" + p.getProperty("application.version")).setVisible(true);
        } else if (!run(args)){
            System.out.print("Usage: " + p.getProperty("application.name") + ".jar <file/folder name>\n" +
                    " -rf,--replace-from        The type of ID to replace from. Possible values: numeric, legacy (pre 1.13), flattening (Default: numeric)" +
                    " -rt,--replace-to          The type of ID to replace to. Possible values: numeric, legacy (pre 1.13), flattening (Default: flattening)" +
                    " -r,--regex <regex>        Regex for matching the ID string. Needs to have 3 groups. (One before, one the ID and the third the stuff after the ID) (Default: depending on replace-from)\n" +
                    " -f,--file-match <regex>   Files need to match this regex for the tool to replace stuff inside them (Default: \\w+\\.yml)\n" +
                    " -l,--lowercase true/false Should the material name be lowercase? (Default: true)\n" +
                    "All parameters are optional\n");
        }
    }
    
    private static boolean run(String[] args) {
        if (args.length == 0) {
            return false;
        }
        String path = args[0];
    
        String regex = null;
        boolean lowercase = true;
        String fileRegex = "\\w+\\.yml";
    
        IdMappings.IdType replaceFrom = IdMappings.IdType.NUMERIC;
        IdMappings.IdType replaceTo = IdMappings.IdType.FLATTENING;
        
        String par = "";
        int i = 0;
        while (i + 2 < args.length) {
            i++;
            int start = 0;
            if (args[i].startsWith("-")) {
                start = 1;
            } else if (args[i].startsWith("--")) {
                start = 2;
            } else if (par.isEmpty()){
                System.out.print("Wrong parameter " + args[i] + "!\n");
                return false;
            }
            
            par = args[i].substring(start);
            i++;
            String value = args[i];
            if (value.startsWith("\"")) {
                boolean endFound = false;
                StringBuilder sb = new StringBuilder(value);
                for (int j = i + 1; j < args.length; j++) {
                    sb.append(" ").append(args[j]);
                    if (args[j].endsWith("\"")) {
                        endFound = true;
                        i = j;
                        break;
                    }
                }
                if (endFound) {
                    value = sb.toString();
                    value = value.substring(1, value.length() - 1);
                }
            }
            
            if ("r".equals(par) || "regex".equalsIgnoreCase(par)) {
                regex = value;
            } else if ("f".equals(par) || "file-matches".equalsIgnoreCase(par)) {
                fileRegex = value;
            } else if ("l".equals(par) || "lowercase".equalsIgnoreCase(par)) {
                lowercase = Boolean.parseBoolean(value);
            } else if ("rf".equals(par) || "replace-from".equalsIgnoreCase(par)) {
                try {
                    replaceFrom = IdMappings.IdType.valueOf(value.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.print(value + " is not a valid replace type!\n\n");
                    return false;
                }
            } else if ("rt".equals(par) || "replace-to".equalsIgnoreCase(par)) {
                try {
                    replaceTo = IdMappings.IdType.valueOf(value.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.print(value + " is not a valid replace type!\n\n");
                    return false;
                }
            }
        }
        
        if (regex == null) {
            regex = replaceFrom.getRegex();
        }
        
        ReturnState r = replace(Collections.singletonList(Paths.get(path)), fileRegex, replaceFrom, replaceTo, regex, lowercase);
        if (r.getType() == ReturnType.SUCCESS) {
            System.out.print("Successfully replaced IDs in file(s) with Material names!\n");
        } else if (r.getMessage().isPresent()) {
            System.out.print(r.getType().toHuman() +": " + r.getMessage().get() + "\n");
        } else {
            System.out.print(r.getType().toHuman() + "!" + "\n");
        }
        return true;
    }
    
    /**
     * Replace the ids in a certain path that match a regex
     * @param paths             The paths to match
     * @param fileRegexString   The regex that the file names have to match
     * @param replaceFrom       Which type of id do we want to replace from
     * @param replaceTo         Which type of id do we want to replace to
     * @param regexString       The regex to match
     * @param lowercase         Whether the material name should be lowercase
     * @return The return state
     */
    public static ReturnState replace(List<Path> paths, String fileRegexString, IdMappings.IdType replaceFrom, IdMappings.IdType replaceTo, String regexString, boolean lowercase) {
        if (paths.isEmpty()) {
            return new ReturnState(ReturnType.MISSING_FILE, "Please select a path!");
        }
        if (replaceFrom == replaceTo) {
            return new ReturnState(ReturnType.INVALID_TYPE_COMBINATION, "Please select two different types to replace from and to.");
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
                return replaceInFile(path, replaceFrom, replaceTo, regex, lowercase);
            } else if (Files.isDirectory(path)) {
                return replaceInDirectory(path, replaceFrom, replaceTo, fileRegex, regex, lowercase);
            }
        }
        return new ReturnState(ReturnType.SUCCESS);
    }
    
    private static ReturnState replaceInFile(Path path, IdMappings.IdType replaceFrom, IdMappings.IdType replaceTo, Pattern regex, boolean lowercase) {
        Charset charset = StandardCharsets.UTF_8;
        try {
            String content = new String(Files.readAllBytes(path), charset);
            Matcher matcher = regex.matcher(content);
            int offset = 0;
            while (matcher.find()) {
                try {
                    String idStr = matcher.group(2);
                    
                    IdMappings.Mapping mat = IdMappings.get(replaceFrom, idStr);
                    
                    String matString = null;
                    if (mat != null) {
                        matString = mat.get(replaceTo);
                        if (matString == null) {
                            if (mat.getNote() != null) {
                                matString = "NOTE_" + idStr + ": " + mat.getNote().getText();
                            } else {
                                matString = "REMOVED_" + idStr;
                            }
                        }
                    }
                    if (matString == null) {
                        matString = "UNKNOWN_" + idStr;
                    }
                    if (lowercase) {
                        matString = matString.toLowerCase();
                    }
                    content = content.substring(0, matcher.start(2) + offset) + matString + content.substring(matcher.end(2) + offset);
                    offset += matString.length() - idStr.length();
                    //replacements.put(group, matcher.group(1) + matString + matcher.group(3));
                } catch (IndexOutOfBoundsException e) {
                    return new ReturnState(ReturnType.INVALID_REGEX, "The regex is missing a group! There must be three groups for the free parts (before the numeric ID, the numeric ID and after the numeric ID!");
                } catch (NumberFormatException e) {
                    return new ReturnState(ReturnType.INVALID_REGEX, "The first group in the regex matched a non-numeric character! (The ID must be the second group!)");
                }
            }
            
            Files.write(path, content.getBytes(charset));
        } catch (IOException e) {
            return new ReturnState(ReturnType.UNKNOWN_ERROR, e.getMessage());
        }
        return new ReturnState(ReturnType.SUCCESS);
    }
    
    private static ReturnState replaceInDirectory(Path path, IdMappings.IdType replaceFrom, IdMappings.IdType replaceTo, Pattern fileRegex, Pattern regex, boolean lowercase) {
        ReturnState r = new ReturnState(ReturnType.SUCCESS);
        try {
            
            Files.list(path).forEach(p -> {
                ReturnState rs = new ReturnState();
                
                if (!Files.exists(p)) {
                    rs = new ReturnState(ReturnType.MISSING_FILE, p.toString());
                } else if (!Files.isWritable(p)) {
                    rs = new ReturnState(ReturnType.FILE_NOT_WRITABLE, p.toString());
                } else if (!Files.isReadable(p)) {
                    rs = new ReturnState(ReturnType.FILE_NOT_READABLE, p.toString());
                } else if (Files.isRegularFile(p)) {
                    if (fileRegex.matcher(p.toFile().getName()).matches()) {
                        rs = replaceInFile(p, replaceFrom, replaceTo, regex, lowercase);
                    }
                } else if (Files.isDirectory(p)) {
                    rs = replaceInDirectory(p, replaceFrom, replaceTo, fileRegex, regex, lowercase);
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
