package net.ele.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws Exception {
        Directory dir = FSDirectory.open(new File("target/indexes"));

        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_2, analyzer);
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);

        // RAM buffer to improve perf
        // iwc.setRAMBufferSizeMB(256.0);
        IndexWriter writer = new IndexWriter(dir, iwc);

        Path projectDir = Paths.get(".");

        Files.walkFileTree(projectDir, new ProjectIndexer(writer));

        // CommitData ==> used to rollback previous commit??
        HashMap<String, String> cd = new HashMap<>();
        cd.put("a","e3");
        writer.setCommitData(cd);

        writer.commit();

        System.out.println("====== DISPLAY COMMITS ======");
        System.out.println(writer.getCommitData());
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

        System.out.println("====== PARSER QUERY ======");
        QueryParser parser = new CustomQueryParser("title", analyzer);// we have to create a custom parser to handle the Field type properly
        Query q = parser.parse("+size:[0 TO 1500]");
        executeAndDisplaySearch(indexSearcher, q);

        System.out.println("====== TERM QUERY ======");
        Term path = new Term("path", "./src/main/java/net/ele/lucene/CustomQueryParser.java");
        q = new TermQuery(path);
        executeAndDisplaySearch(indexSearcher, q);

        System.out.println("====== PREFIX QUERY ======");
        path = new Term("path", "./src/main/java/");
        q = new PrefixQuery(path);
        executeAndDisplaySearch(indexSearcher, q);

        /**
         * If a field is stored with TermPosition and Freq, we can search a sequence of word with the PhraseQuery
         */
    }

    private static void executeAndDisplaySearch(IndexSearcher indexSearcher, Query q) throws IOException {
        final int hitsPerPage = 4;
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
        ScoreDoc[] scoreDocs;
        indexSearcher.search(q, collector);

        System.out.println("Total Hits : " + collector.getTotalHits());
        scoreDocs = collector.topDocs().scoreDocs;

        System.out.println("====== FOUND ======");
        System.out.println("ScoreDocs.length : " + scoreDocs.length);
        for (ScoreDoc sdoc : scoreDocs){
            System.out.println("DocID:" + sdoc.doc);
            System.out.println("  path:" + indexSearcher.doc(sdoc.doc).get("path"));
            System.out.println("  size:" + indexSearcher.doc(sdoc.doc).get("size"));
        }
    }
}