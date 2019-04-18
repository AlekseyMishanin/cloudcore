package utility.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileVisionSearch extends SimpleFileVisitor<Path> {

    private String nameSearchObject;
    private StringBuilder report = new StringBuilder();

    public FileVisitResult visitFile(Path path, BasicFileAttributes attrib) throws IOException {
        if(path.endsWith(nameSearchObject)) report.append("Success\t" + path.toString() + "\n");
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if(dir.endsWith(nameSearchObject)) report.append("Success\t" + dir.toString() + "\n");
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    public StringBuilder getReport() {
        return report;
    }
    public void clear() {
        report.setLength(0);
        nameSearchObject = null;
    }

    public void setNameSearchObject(String nameSearchObject) {
        this.nameSearchObject = nameSearchObject;
    }
}
