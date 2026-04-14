import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecursiveFileScanner {

    public static void main(String[] args) {
        // --- STEP 1: CONFIGURE YOUR PROJECT DETAILS HERE ---

        // List of the root folders you want to start scanning from.
        List<String> foldersToScan = Arrays.asList(
            "/home/kureem/kairos-platform/kairos-platform"
           // "/home/kureem/kairos-platform/kairos-platform/kairos-agentic-framework",
           // "/home/kureem/kairos-platform/kairos-platform/kairos-ai-abstraction",
           // "/home/kureem/kairos-platform/kairos-platform/kairos-core",
          //  "/home/kureem/kairos-platform/kairos-platform/kairos-crawler",
          //  "/home/kureem/kairos-platform/kairos-platform/kairos-ingestion",
         //   "/home/kureem/kairos-platform/kairos-platform/kairos-storage",
         //   "/home/kureem/kairos-platform/kairos-platform/kairos-vector-search",
         //   "/home/kureem/kairos-platform/kairos-platform/kairos-notification"
            
            //"C:\\cognicraft\\frontend" 
            // "tests",
            // "scripts"
        ); 

        // List of file extensions to include in the output.
        List<String> fileExtensionsToInclude = Arrays.asList(
           // ".ts", ".tsx", ".java", ".css", ".html", "pom.xml", ".json", ".properties"
        		".java", "pom.xml", ".properties"
        );
 
        // List of directory names to completely ignore during the scan.
        List<String> directoriesToIgnore = Arrays.asList(
            "node_modules", ".git", "target", "dist", "build", ".vscode" ,".settings" ,".jsweet", "templates", "crawler4j", "cultural-archive-app", "kairos-are-core", "sports-atlas-app"
        );
 
        // --- STEP 2: DEFINE THE OUTPUT FILENAME ---
        String outputFileName = "gateway.txt";

        // --- DO NOT MODIFY BELOW THIS LINE ---
        StringBuilder combinedContent = new StringBuilder();
        System.out.println("Starting recursive scan...");

        for (String folderPath : foldersToScan) {
            Path startPath = Paths.get(folderPath);
            if (!Files.exists(startPath)) {
                System.err.println("WARNING: Starting directory not found, skipping: " + folderPath);
                continue;
            }
 
            System.out.println("Scanning directory: " + folderPath);
            
            try (Stream<Path> stream = Files.walk(startPath)) {
                List<Path> filePaths = stream
                    // Filter 1: Must be a regular file, not a directory.
                    .filter(Files::isRegularFile)
                    // Filter 2: The file's path must NOT contain any of the ignored directory names.
                    .filter(path -> {
                        String pathString = path.toString();
                        return directoriesToIgnore.stream().noneMatch(dir -> pathString.contains(File.separator + dir + File.separator));
                    })
                    // Filter 3: The filename must end with one of the desired extensions.
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        return fileExtensionsToInclude.stream().anyMatch(fileName::endsWith);
                    })
                    .collect(Collectors.toList());

                for (Path filePath : filePaths) {
                    String relativePath = startPath.relativize(filePath).toString();
                    System.out.println("  + Adding file: " + relativePath);
                    
                    try {
                        String content = new String(Files.readAllBytes(filePath));
                        
                        combinedContent.append("// --- START OF FILE ").append(relativePath).append(" ---\n");
                        combinedContent.append(content);
                        if (!content.endsWith("\n")) {
                            combinedContent.append("\n");
                        }
                        combinedContent.append("// --- END OF FILE ").append(relativePath).append(" ---\n\n");
                    } catch (IOException e) {
                         System.err.println("  - ERROR: Could not read file: " + relativePath + ". Skipping.");
                    }
                }

            } catch (IOException e) {
                System.err.println("FATAL ERROR: Failed to scan directory " + folderPath + ". Error: " + e.getMessage());
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
            writer.write(combinedContent.toString());
            System.out.println("\nScan complete. Combined all found files into " + outputFileName);
        } catch (IOException e) {
            System.err.println("FATAL ERROR: Could not write to output file: " + e.getMessage());
        }
    }
}