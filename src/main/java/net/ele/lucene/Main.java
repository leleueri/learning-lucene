package net.ele.lucene;

import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Main
{

    public static void main(String[] args) throws Exception
    {
        Directory dir = FSDirectory.open(new File("indexes"));

        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_2, analyzer);
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
        // RAM buffer to improve perf
        // iwc.setRAMBufferSizeMB(256.0);
        IndexWriter writer = new IndexWriter(dir, iwc);

        File file = new File("src/main/java/net/ele/lucene/Main.java");
        
        // make a new, empty document
        Document doc = new Document();
        Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
        doc.add(pathField);
                
        System.out.println("adding " + file);
//        writer.addDocument(doc);
        writer.updateDocument(new Term("path", file.getPath()), doc);
        
        
        writer.close();
    }
}
