package is.hi.hbv501g.demo.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MediaStorage {

    private final Path root = Path.of("uploads");

    public MediaStorage() throws IOException {
        Files.createDirectories(root);
    }

    public String upload(MultipartFile file) throws IOException {
        return upload(file.getInputStream(), file.getOriginalFilename(), file.getContentType());
    }

    public String upload(InputStream file, String originalName, String mime) throws IOException {
        String extension = guessExtension(originalName, mime);
        String filename = UUID.randomUUID() + "." + extension;
        Path datedDir = root.resolve(LocalDate.now().toString());
        Files.createDirectories(datedDir);
        Path target = datedDir.resolve(filename);
        Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
        return "/media/" + datedDir.getFileName() + "/" + filename;
    }

    private String guessExtension(String originalName, String mime) {
        if (originalName != null && originalName.contains(".")) {
            return originalName.substring(originalName.lastIndexOf('.') + 1);
        }
        if (mime != null) {
            return switch (mime) {
                case "image/png" -> "png";
                case "image/jpeg" -> "jpg";
                case "video/mp4" -> "mp4";
                default -> "bin";
            };
        }
        return "bin";
    }
}
