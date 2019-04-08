import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class App {

    public static void main(String[] args) {
        File[] roots = new File[]{new File("D:\\")}/*File.listRoots()*/;
        for (int i = 0; i < roots.length; i++) {
            if(roots[i].list()==null) break;
            for (String str:
                    roots[i].list()) {
                //if(!str.startsWith("D")) continue;
                System.out.println(roots[i] + str);
                try{
                    Files.walkFileTree(Paths.get(roots[i] + str),new MyFileVision());
                } catch (IOException e){

                }
            }
        }
    }
}

class MyFileVision extends SimpleFileVisitor<Path>{

    public FileVisitResult visitFile(Path path, BasicFileAttributes attrib) throws IOException {

        System.out.println(path);
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrib) throws IOException{
        System.out.println(path);
        return FileVisitResult.CONTINUE;
    }
}