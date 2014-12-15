package net.ele.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        writer.commit();
        writer.close();



        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        System.out.println(reader.numDocs());

        System.out.println("====== KNOWN ======");
        int nbDocs = reader.numDocs();
        for (int i = 0; i<nbDocs; ++i){
            Document doc = indexSearcher.doc(i);
            System.out.println("DocID:" + i);
            System.out.println("  path:" + doc.get("path"));
            System.out.println("  size:" + doc.get("size"));
        }

        QueryParser parser = new CustomQueryParser("title", analyzer);// we have to create a custom parser to handle the Field type properly

        Query q = parser.parse("+size:[0 TO 1500]");
        final int hitsPerPage = 4;
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
        indexSearcher.search(q, collector);

        System.out.println("Total Hits : " + collector.getTotalHits());
        ScoreDoc[] scoreDocs = collector.topDocs().scoreDocs;

        System.out.println("====== FOUND ======");
        System.out.println("ScoreDocs.length : " + scoreDocs.length);
        for (ScoreDoc sdoc : scoreDocs){
            System.out.println("DocID:" + sdoc.doc);
            System.out.println("  path:" + indexSearcher.doc(sdoc.doc).get("path"));
            System.out.println("  size:" + indexSearcher.doc(sdoc.doc).get("size"));
        }
    }
}