package top.sducraft.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Scans SDUcraftCarpetSettings \(@Rule fields\) and ensures lang entries exist in zh\_cn.json and en\_us.json.
 *
 * Keys:
 * - carpet.rule.<id>.name
 * - carpet.rule.<id>.desc
 */
public final class Translation {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    public static void main(String[] args) throws Exception {
        // Defaults based on this project layout
        Path settingsFile = Paths.get("src/main/java/top/sducraft/SDUcraftCarpetSettings.java");
        Path zhFile = Paths.get("src/main/resources/assets/sdu/lang/zh_cn.json");
        Path enFile = Paths.get("src/main/resources/assets/sdu/lang/en_us.json");

        for (String arg : args) {
            if (arg.startsWith("--settings=")) settingsFile = Paths.get(arg.substring("--settings=".length()));
            if (arg.startsWith("--zh=")) zhFile = Paths.get(arg.substring("--zh=".length()));
            if (arg.startsWith("--en=")) enFile = Paths.get(arg.substring("--en=".length()));
        }

        if (!Files.exists(settingsFile)) {
            throw new IllegalStateException("Settings file not found: " + settingsFile.toAbsolutePath());
        }

        Set<String> ruleIds = scanRuleFieldNames(settingsFile);
        if (ruleIds.isEmpty()) {
            System.out.println("No @Rule fields found in: " + settingsFile);
            return;
        }

        Map<String, String> zh = readJsonMap(zhFile);
        Map<String, String> en = readJsonMap(enFile);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        int addedZh = 0;
        int addedEn = 0;

        List<String> sortedRuleIds = new ArrayList<>(ruleIds);
        Collections.sort(sortedRuleIds);

        for (String id : sortedRuleIds) {
            String nameKey = "carpet.rule." + id + ".name";
            String descKey = "carpet.rule." + id + ".desc";

            boolean needZhName = !zh.containsKey(nameKey);
            boolean needZhDesc = !zh.containsKey(descKey);
            boolean needEnName = !en.containsKey(nameKey);
            boolean needEnDesc = !en.containsKey(descKey);

            if (!(needZhName || needZhDesc || needEnName || needEnDesc)) continue;

            System.out.println();
            System.out.println("Missing translations for rule: " + id);

            if (needZhName || needEnName) {
                System.out.print("  zh_cn name: ");
                String zhName = safeReadLine(in);
                System.out.print("  en_us name: ");
                String enName = safeReadLine(in);

                if (needZhName) { zh.put(nameKey, zhName); addedZh++; }
                if (needEnName) { en.put(nameKey, enName); addedEn++; }
            }

            if (needZhDesc || needEnDesc) {
                System.out.print("  zh_cn desc: ");
                String zhDesc = safeReadLine(in);
                System.out.print("  en_us desc: ");
                String enDesc = safeReadLine(in);

                if (needZhDesc) { zh.put(descKey, zhDesc); addedZh++; }
                if (needEnDesc) { en.put(descKey, enDesc); addedEn++; }
            }
        }

        if (addedZh == 0 && addedEn == 0) {
            System.out.println("No missing translations. Nothing to write.");
            return;
        }

        writeJsonMapSorted(zhFile, zh);
        writeJsonMapSorted(enFile, en);

        System.out.println();
        System.out.println("Done. Added entries:");
        System.out.println("  zh_cn: " + addedZh);
        System.out.println("  en_us: " + addedEn);
    }

    private static String safeReadLine(BufferedReader in) throws IOException {
        String line = in.readLine();
        if (line == null) return "";
        return line.trim();
    }

    private static Map<String, String> readJsonMap(Path file) throws IOException {
        if (!Files.exists(file)) return new LinkedHashMap<>();
        String json = Files.readString(file, StandardCharsets.UTF_8).trim();
        if (json.isEmpty()) return new LinkedHashMap<>();
        Map<String, String> map = GSON.fromJson(json, MAP_TYPE);
        return map != null ? new LinkedHashMap<>(map) : new LinkedHashMap<>();
    }

    private static void writeJsonMapSorted(Path file, Map<String, String> map) throws IOException {
        TreeMap<String, String> sorted = new TreeMap<>(map);
        String out = GSON.toJson(sorted);
        Files.createDirectories(file.getParent());
        Files.writeString(file, out + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    private static Set<String> scanRuleFieldNames(Path javaFile) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("No system Java compiler found. Use a JDK, not a JRE.");
        }

        try (StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> files = fm.getJavaFileObjects(javaFile.toFile());
            JavacTask task = (JavacTask) compiler.getTask(
                    null,
                    fm,
                    null,
                    List.of("-proc:none"),
                    null,
                    files
            );

            Iterable<? extends CompilationUnitTree> asts = task.parse();
            Trees trees = Trees.instance(task);

            Set<String> ruleFields = new HashSet<>();
            for (CompilationUnitTree cu : asts) {
                new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitVariable(VariableTree node, Void unused) {
                        // Detect @Rule annotation on fields
                        boolean hasRule = node.getModifiers().getAnnotations().stream()
                                .anyMatch(a -> {
                                    String t = a.getAnnotationType().toString();
                                    return t.equals("Rule") || t.endsWith(".Rule");
                                });

                        if (hasRule) {
                            ruleFields.add(node.getName().toString());
                        }
                        return super.visitVariable(node, unused);
                    }
                }.scan(cu, null);
            }
            return ruleFields;
        }
    }

    private void CarpetLangSync() {}
}
