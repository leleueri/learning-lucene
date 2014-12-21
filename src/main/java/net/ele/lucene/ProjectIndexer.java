package net.ele.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.AnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.util.Version;

import java.io.FileReader;
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

        // beware, this field contains a Long. So the QueryParser must interpret this as a LongTerm too
        Field sizeField = new LongField("size", file.toFile().length(), Field.Store.YES);
        doc.add(sizeField);

        // index each java file content
        // if we want to store the Content, we have to use the Constructor (String, String, Field.Store)
        TokenStream stream1 = new WhitespaceAnalyzer().tokenStream("content", new FileReader(file.toFile()));
        Field java = new TextField("content", stream1);
        doc.add(java);

        System.out.println("adding " + file);
         // update will create or update the document using the PK (path)
        writer.updateDocument(new Term("path", file.toFile().getPath()), doc);

        return FileVisitResult.CONTINUE;
    }
}
