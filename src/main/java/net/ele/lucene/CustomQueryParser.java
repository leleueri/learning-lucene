package net.ele.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;

/**
 * Created by eric on 15/12/14.
 */
public class CustomQueryParser extends QueryParser {
    public CustomQueryParser(String f, Analyzer a) {
        super(f, a);
    }
    @Override
    protected Query getRangeQuery(final String field, final String part1, final String part2, final boolean inclusive, final boolean inclusive2) throws ParseException {

        if ("size".equals(field)) {
            //if field type is LongField , the Range query must be newLongRange (IntRange doesn't work even if the value can be handled by an integer)
            return NumericRangeQuery.newLongRange(field, Long.parseLong(part1), Long.parseLong(part2), inclusive, inclusive2);
        }

        // return default
        return super.getRangeQuery(field, part1, part2, inclusive, inclusive2);
    }

}