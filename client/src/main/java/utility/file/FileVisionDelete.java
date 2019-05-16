package utility.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
/**
 * Класс для рекурсивного удаления вложенных файлов и каталогов
 * */
public class FileVisionDelete extends SimpleFileVisitor<Path> {

    private StringBuilder report = new StringBuilder();

    public FileVisitResult visitFile(Path path, BasicFileAttributes attrib) throws IOException {
        delete(path);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        delete(dir);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        report.append("Failed\t" + file.toString() + "\n");
        return FileVisitResult.CONTINUE;
    }

    private void delete(Path path){
        try{
            Files.delete(path);
            report.append("Success\t" + path.toString() + "\n");
        } catch (IOException e){
            report.append("Failed\t" + path.toString() + "\n");
            report.append(e.getMessage() + "\n");
        }
    }

    public StringBuilder getReport() {
        return report;
    }
    public void clear() {
        report.setLength(0);
    }
}