package net.ele.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * Created by eric on 10/12/14.
 */
public class ProjectIndexer extends SimpleFileVisitor<Path>  {

    private final IndexWriter writer;

    public ProjectIndexer(IndexWriter writer) {
        this.writer = writer;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException
    {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(attrs);

        return dir.toFile().getName().equals("target") ||
                dir.toFile().getName().equals(".git") ||
                dir.toFile().getName().equals(".idea") ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
    {
        Objects.requireNonNull(file);
        Objects.requireNonNull(attrs);

        // make a new, empty document
        Document doc = new Document();

        // the path is used as primary key (because it is StringField)
        // if we want to apply some transformation, it should be TextField but can't be an ID)
        Field pathField = new StringField("path", file.toFile().getPath(), Field.Store.YES);
        doc.add(pathField);

        Field sizeField = new LongField("path", file.toFile().length(), Field.Store.YES);
        doc.add(sizeField);

        System.out.println("adding " + file);
        // update will create or update the document using the PK (path)
        writer.updateDocument(new Term("path", file.toFile().getPath()), doc);

        return FileVisitResult.CONTINUE;
    }
}
