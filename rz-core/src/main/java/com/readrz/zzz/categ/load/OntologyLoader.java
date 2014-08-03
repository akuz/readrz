package com.readrz.zzz.categ.load;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import me.akuz.core.FileUtils;
import me.akuz.core.Pair;
import me.akuz.core.StringUtils;
import me.akuz.core.gson.GsonSerializers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.readrz.zzz.categ.EntityFeature;
import com.readrz.zzz.categ.Feature;
import com.readrz.zzz.categ.GroupFeature;
import com.readrz.zzz.categ.OntologyOld;
import com.readrz.zzz.categ.TopicFeature;

public final class OntologyLoader {
	
	private final OntologyOld _ontology;
	
	public OntologyLoader(String dir) throws IOException {

		OntologyOld ontology = new OntologyOld();
		
		Gson gson = GsonSerializers.NoHtmlEscapingPretty;

		System.out.println("Loading entities features...");
		String entitiesDir = StringUtils.concatPath(dir, "entities");
		List<File> entitiesFiles = FileUtils.getFiles(entitiesDir);
		for (File file : entitiesFiles) {
			String jsonStr = FileUtils.readEntireFile(file);
			Type collectionType = new TypeToken<List<EntityFeature>>(){}.getType();
			List<EntityFeature> entityFeatures = gson.fromJson(jsonStr, collectionType);
			for (int i=0; i<entityFeatures.size(); i++) {
				ontology.addEntityFeature(entityFeatures.get(i));
			}
		}

		System.out.println("Loading topics features...");
		String topicsDir = StringUtils.concatPath(dir, "topics");
		List<File> topicsFiles = FileUtils.getFiles(topicsDir);
		for (File file : topicsFiles) {
			if (file.getName().endsWith(".txt") == false) {
				continue;
			}
			String jsonStr = FileUtils.readEntireFile(file);
			Type collectionType = new TypeToken<List<TopicFeature>>(){}.getType();
			List<TopicFeature> topicFeatures = gson.fromJson(jsonStr, collectionType);
			for (int i=0; i<topicFeatures.size(); i++) {
				ontology.addTopicFeature(topicFeatures.get(i));
			}
		}

		System.out.println("Loading groups features...");
		String groupsDir = StringUtils.concatPath(dir, "groups");
		List<File> groupsFiles = FileUtils.getFiles(groupsDir);
		for (File file : groupsFiles) {
			
			// load group definition
			String jsonStr = FileUtils.readEntireFile(file);
			GroupFeatureDef rootGroupFeatureDef = gson.fromJson(jsonStr, GroupFeatureDef.class);
			GroupFeature rootGroupFeature = new GroupFeature(
					rootGroupFeatureDef.getKey(), 
					rootGroupFeatureDef.getName(),
					rootGroupFeatureDef.isUncrossable(),
					rootGroupFeatureDef.isTaxonomy(),
					rootGroupFeatureDef.isSecondaryTaxonomy(),
					rootGroupFeatureDef.isIgnoredForFullName());

			// process hierarchy of group definitions
			Queue<Pair<GroupFeatureDef, GroupFeature>> queue = new LinkedList<Pair<GroupFeatureDef, GroupFeature>>();
			queue.add(new Pair<GroupFeatureDef, GroupFeature>(rootGroupFeatureDef, rootGroupFeature));
			while (queue.size() > 0) {
				
				Pair<GroupFeatureDef, GroupFeature> pair = queue.poll();
				GroupFeatureDef groupFeatureDef = pair.v1();
				GroupFeature groupFeature = pair.v2();
				
				// create child groups, and schedule their processing
				List<GroupFeatureDef> childGroupDefs = groupFeatureDef.getChildGroups();
				if (childGroupDefs != null) {
					for (int i=0; i<childGroupDefs.size(); i++) {
						
						GroupFeatureDef childGroupDef = childGroupDefs.get(i);

						String childFullKey = combineKeys(groupFeature.getKey(), childGroupDef.getKey());
						
						GroupFeature childGroup = new GroupFeature(
								childFullKey, 
								childGroupDef.getName(), 
								childGroupDef.isUncrossable(),
								childGroupDef.isTaxonomy(),
								childGroupDef.isSecondaryTaxonomy(),
								childGroupDef.isIgnoredForFullName());
						
						groupFeature.addChildGroup(childGroup);
						queue.add(new Pair<GroupFeatureDef, GroupFeature>(childGroupDef, childGroup));
					}
				}

				// process features of the current group
				List<String> leafIds = groupFeatureDef.getFeatureKeys();
				if (leafIds != null) {
					for (int i=0; i<leafIds.size(); i++) {
						String leafId = leafIds.get(i);
						Feature leafFeature = ontology.getLeafFeature(leafId);
						if (leafFeature == null) {
							throw new IllegalStateException("Cannot find leaf feature with id " + leafId + " referenced from group feature " + groupFeature.getKey());
						}
						groupFeature.addLeafFeature(leafFeature);
					}
				}
			}

			// register level two (child of root) group feature
			ontology.addGroupFeature(rootGroupFeature);				
		}

		System.out.println("Assigning entities to groups...");
		assignLeafFeaturesToGroups(ontology.getEntityFeatures().values(), ontology);

		System.out.println("Assigning topics to groups...");
		assignLeafFeaturesToGroups(ontology.getTopicFeatures().values(), ontology);

		System.out.println("Ontology loaded.");
		_ontology = ontology;
	}
	
	private final static void assignLeafFeaturesToGroups(Collection<? extends Feature> leafFeatures, OntologyOld ontology) {
		
		for (Feature leafFeature : leafFeatures) {
			String[] groupKeys = leafFeature.getGroupKeys();
			if (groupKeys != null) {
				for (int i=0; i<groupKeys.length; i++) {
					String groupKey = groupKeys[i];
					GroupFeature groupFeature = ontology.getGroupFeature(groupKey);
					if (groupFeature == null) {
						throw new IllegalStateException("Group feature " + groupKey 
								+ ", specified for " + leafFeature.getKey() + " does not exist");
					}
					groupFeature.addLeafFeature(leafFeature);
				}
			}
		}
	}
	
	public final static String combineKeys(String key1, String key2) {
		return key1 + "/" + key2;
	}

	public OntologyOld getOntology() {
		return _ontology;
	}

}
