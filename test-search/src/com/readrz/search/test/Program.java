package com.readrz.search.test;

import java.net.UnknownHostException;
import java.util.List;

import me.akuz.core.StringUtils;
import me.akuz.core.UtcDate;
import me.akuz.nlp.porter.PorterStemmer;

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.readrz.data.Snap;
import com.readrz.data.SnapInfo;
import com.readrz.data.Source;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.mongo.MongoColls;
import com.readrz.data.ontology.Ontology;
import com.readrz.search.Query;
import com.readrz.search.QueryParser;
import com.readrz.search.SnapSearchResult;
import com.readrz.search.SnapSearcher;

public class Program {

	public static void main(String[] args) throws UnknownHostException {

		String queryString = null;
		{
			if (args != null) {
				for (int i=0; i<args.length; i++) {
					if ("-q".equals(args[i])) {
						if (i+1 < args.length) {
							queryString = StringUtils.unquote(args[i+1]);
							i++;
						}
					}
				}
			}
		}

		MongoClient mc = new MongoClient();
		
		try {
			
			PorterStemmer porterStemmer = new PorterStemmer("_");
			
			DB db = mc.getDB("readrz");
			
			KeysIndex keysIndex = new KeysIndex(db.getCollection(MongoColls.keys));
			keysIndex.loadFromDB();
			
			Ontology ontology = new Ontology(keysIndex);
			ontology.loadFromDB(db);
			
			QueryParser queryParser = new QueryParser(
					porterStemmer,
					keysIndex, 
					ontology);
			
			SnapSearcher searcher = new SnapSearcher(
					db.getCollection(MongoColls.snaps),
					db.getCollection(MongoColls.snapsidx),
					db.getCollection(MongoColls.feeds),
					db.getCollection(MongoColls.sources));

			Query query = queryParser.parse(queryString);
			
			long ms = System.currentTimeMillis();
			List<SnapSearchResult> results = searcher.findWithLimit(
					20, 
					true,
					query.getQueryKeyIds(), 
					null, 
					null, 
					10000);

			for (int i=0; i<results.size(); i++) {

				SnapSearchResult result = results.get(i);
				ObjectId snapId = result.getSnapId();
				SnapInfo snapInfo = searcher.findSnapInfo(snapId);
				if (snapInfo != null) {
				
					Snap snap = snapInfo.getSnap();
					Source source = snapInfo.getSource();
					
					StringBuilder sb2 = new StringBuilder();
					
					UtcDate utcDate = new UtcDate(snap.getSrcDate());
					sb2.append(utcDate.toString());
					sb2.append(" | ");
					sb2.append(StringUtils.trimOrFillSpaces(snap.getTitle(), 80));
					sb2.append(" | ");
					sb2.append(source.getName());
					
					System.out.println(sb2.toString());	
				}
			}
			
			double durSec = (System.currentTimeMillis() - ms) / 1000.0;
			System.out.println("Found " + results.size() + " results for \"" + queryString + "\" in " + durSec + " seconds.");
			
			if (query.getIsSomeTermsNotFound()) {
				System.out.println("WARNING: Some query terms not found");
			}
			if (query.getIsEmpty()) {
				System.out.println("WARNING: Query is empty");
			}
			
		} finally {
			
			mc.close();
		}

	}

}
