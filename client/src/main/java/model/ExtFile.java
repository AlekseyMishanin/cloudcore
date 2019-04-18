package model;

import java.io.File;

public class ExtFile extends File{

        public ExtFile(String pathname) {
            super(pathname);
        }

        public ExtFile(File parent, String child) {
            super(parent, child);
        }

        @Override
        public String toString() {
            return getName().isEmpty() ? getPath() : getName();
        }
}
