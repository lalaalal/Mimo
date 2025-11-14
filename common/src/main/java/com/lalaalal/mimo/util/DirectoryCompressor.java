package com.lalaalal.mimo.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DirectoryCompressor extends SimpleFileVisitor<Path> implements AutoCloseable {
    private final Path root;
    private final ZipOutputStream outputStream;

    public DirectoryCompressor(Path root, Path output) throws IOException {
        this.root = root;
        this.outputStream = new ZipOutputStream(new FileOutputStream(output.toFile()));
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path target = root.relativize(file);
        ZipEntry entry = new ZipEntry(target.toString());

        outputStream.putNextEntry(entry);
        Files.copy(file, outputStream);
        outputStream.closeEntry();

        return FileVisitResult.CONTINUE;
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

}
