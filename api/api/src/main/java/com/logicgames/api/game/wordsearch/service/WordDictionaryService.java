package com.logicgames.api.game.wordsearch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WordDictionaryService {

    private final ObjectMapper objectMapper;

    // 1. Diccionario masivo (General) - Cargado en memoria
    private Map<String, List<String>> massiveDictionary = new HashMap<>();

    // 2. Diccionarios Temáticos (Cargados dinámicamente desde /themes)
    private Map<String, List<String>> thematicLists = new HashMap<>();

    // 3. Mapa para guardar las etiquetas (ej: "TECH" -> "💻 Tecnología")
    private Map<String, String> themeLabels = new HashMap<>();

    // 4. RESPALDO DE EMERGENCIA (Hardcoded)
    // Si no hay JSONs, usaremos esto. Solo los temas oficiales.
    private static final Map<String, List<String>> BACKUP_THEMES = Map.of(
            "TECH", List.of("JAVA", "SPRING", "DOCKER", "ANGULAR", "LINUX", "CLOUD", "API", "GIT", "MAVEN", "LOMBOK", "DEPLOY", "MICROSERVICE"),
            "ANIMALS", List.of("LEON", "TIGRE", "CEBRA", "ELEFANTE", "JIRAFA", "MONO", "PANDA", "KOALA", "AGUILA", "TIBURON", "BALLENA", "DELFIN"),
            "COUNTRIES", List.of("ESPAÑA", "FRANCIA", "ITALIA", "JAPON", "BRASIL", "CANADA", "CHINA", "INDIA", "MEXICO", "PERU", "ALEMANIA")
    );

    // DTO Interno para leer los JSON de temas
    @Data
    private static class ThemeData {
        private String id;
        private String label;
        private List<String> words;
    }

    @PostConstruct
    public void init() {
        loadMassiveDictionary();
        loadThemesFromResources();
    }

    private void loadMassiveDictionary() {
        try {
            System.out.println("📚 Cargando diccionario masivo...");
            ClassPathResource resource = new ClassPathResource("es_words_by_length.json");
            if (resource.exists()) {
                massiveDictionary = objectMapper.readValue(resource.getInputStream(), new TypeReference<Map<String, List<String>>>() {});
                System.out.println("✅ Diccionario General cargado.");
            } else {
                System.err.println("⚠️ No se encontró es_words_by_length.json.");
            }
        } catch (IOException e) {
            System.err.println("❌ Error cargando diccionario masivo: " + e.getMessage());
        }
    }

    private void loadThemesFromResources() {
        try {
            System.out.println("📂 Escaneando carpeta 'themes'...");
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:themes/*.json");

            for (Resource r : resources) {
                try {
                    ThemeData data = objectMapper.readValue(r.getInputStream(), ThemeData.class);
                    thematicLists.put(data.getId(), data.getWords());
                    themeLabels.put(data.getId(), data.getLabel());
                    System.out.println("   ➜ Tema cargado: " + data.getId());
                } catch (Exception ex) {
                    System.err.println("   ⚠️ Error leyendo archivo " + r.getFilename() + ": " + ex.getMessage());
                }
            }
            System.out.println("✅ Carga de temas completada. Total: " + thematicLists.size());

        } catch (IOException e) {
            System.err.println("❌ Error accediendo a carpeta themes: " + e.getMessage());
        }
    }

    /**
     * Lógica de obtención de palabras (Con Doble Fallback)
     */
    public List<String> getWords(String theme, String difficulty, int countNeeded) {
        List<String> candidates;

        if ("GENERAL".equals(theme)) {
            // GENERAL: Mantenemos la lógica estricta de longitud
            candidates = getWordsFromMassiveDictionary(difficulty);
        } else {
            // TEMAS ESPECÍFICOS:
            // Buscamos en el mapa temático
            candidates = new ArrayList<>(thematicLists.getOrDefault(theme, new ArrayList<>()));
            if (candidates.isEmpty()) {
                candidates = new ArrayList<>(BACKUP_THEMES.getOrDefault(theme, new ArrayList<>()));
            }

            // --- CAMBIO CLAVE ---
            // NO filtramos por longitud aquí.
            // Si el usuario quiere animales dificiles, le damos animales (aunque sean cortos).
            // La dificultad vendrá dada por el tamaño del tablero (15x15) y la cantidad (12 palabras).

            // Si la lista está vacía (tema no existe), fallback a GENERAL
            if (candidates.isEmpty()) {
                System.out.println("⚠️ Tema vacio. Fallback a GENERAL.");
                candidates = getWordsFromMassiveDictionary(difficulty);
            }
        }

        Collections.shuffle(candidates);

        return candidates.stream()
                .limit(countNeeded)
                .map(this::cleanWord)
                .collect(Collectors.toList());
    }

    public Map<String, String> getAvailableThemes() {
        return themeLabels;
    }

    // --- Lógica Interna ---

    private List<String> getWordsFromMassiveDictionary(String difficulty) {
        List<String> result = new ArrayList<>();
        int minLen = 4; int maxLen = 8;

        if ("EASY".equals(difficulty)) { minLen = 3; maxLen = 6; }
        if ("MEDIUM".equals(difficulty)) { minLen = 5; maxLen = 9; }
        if ("HARD".equals(difficulty)) { minLen = 7; maxLen = 13; }

        for (int i = minLen; i <= maxLen; i++) {
            List<String> wordsOfLen = massiveDictionary.get(String.valueOf(i));
            if (wordsOfLen != null) {
                result.addAll(wordsOfLen);
            }
        }
        // Si el diccionario masivo falla, usamos TECH como último recurso
        if (result.isEmpty()) {
            return new ArrayList<>(BACKUP_THEMES.get("TECH"));
        }
        return result;
    }

    private String cleanWord(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toUpperCase();
    }
}
