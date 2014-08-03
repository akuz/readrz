package com.readrz.www.facades;

import me.akuz.nlp.porter.PorterStemmer;

import com.readrz.data.index.KeysIndex;
import com.readrz.data.ontology.Ontology;
import com.readrz.search.QueryParser;

public final class FacadeQueryParser {
	
	private static final Object _lock = new Object();
	private static QueryParser _instance;
	
	public static QueryParser get() {
		if (_instance == null) {
			synchronized (_lock) {
				if (_instance == null) {
					
					KeysIndex keys = FacadeKeys.get();
					Ontology ontology = FacadeOntology.get();
					
					_instance = new QueryParser(
						new PorterStemmer("_"),
						keys,
						ontology);
				}
			}
		}
		return _instance;
	}

}
