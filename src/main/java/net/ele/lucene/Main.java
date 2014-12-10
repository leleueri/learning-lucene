package net.ele.lucene;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Main {

    public static void main(String[] args) throws Exception {
        Directory dir = FSDirectory.open(new File("target/indexes"));

        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_2, analyzer);
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);

        // RAM buffer to improve perf
        // iwc.setRAMBufferSizeMB(256.0);
        IndexWriter writer = new IndexWriter(dir, iwc);

        File file = new File(".");
        Path projectDir = Paths.get(".");

        Files.walkFileTree(projectDir, new ProjectIndexer(writer));
        writer.close();


        IndexReader reader = DirectoryReader.open(dir);
        System.out.println(reader.numDocs());
    }
}