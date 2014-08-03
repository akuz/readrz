package com.readrz.www.facades;

import com.mongodb.DB;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.ontology.Ontology;

public final class FacadeOntology {
	
	private static final Object _lock = new Object();
	private static Ontology _instance;
	
	public static Ontology get() {
		if (_instance == null) {
			synchronized (_lock) {
				if (_instance == null) {
					
					DB db = FacadeDB.get();
					KeysIndex keysIndex = FacadeKeys.get();
					
					_instance = new Ontology(keysIndex);
					_instance.loadFromDB(db);
				}
			}
		}
		return _instance;
	}

}
