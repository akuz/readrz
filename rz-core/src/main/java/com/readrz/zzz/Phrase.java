package com.readrz.zzz;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.readrz.zzz.categ.Feature;
import com.readrz.zzz.parse.matches.AnyMatch;

public final class Phrase {

	public static final double NON_TITLE_PHRASE_WEIGHT_ADJUSTMENT = 0.33;
	public static final double PHRASE_WEIGHT_MATCHES_FROM_START_STD_DEV = 3;
	public static final double STEM_WEIGHT_MATCHES_FROM_CENTER_STD_DEV  = 5;
	
	public static final DecimalFormat FMT_0_00 = new DecimalFormat("0.00");

	private final int _srcId;
	private final int _postId;
	private final Date _date;
	List<AnyMatch> _titleMatches;
	private final FieldName _fieldName;
	private final Feature _feature;
	private double _weight;
	private final List<PhrasePlace> _places;
	private final Map<String, Double> _stemWeights;

	public Phrase(
			int srcId,
			int postId,
			Date date,
			List<AnyMatch> titleMatches,
			FieldName fieldName, 
			Feature feature,
			double weight) {
		
		_srcId = srcId;
		_postId = postId;
		_date = date;
		_titleMatches = titleMatches;
		_fieldName = fieldName;
		_feature = feature;
		_weight = weight;
		_places = new ArrayList<PhrasePlace>();
		_stemWeights = new HashMap<String, Double>();
	}
	
	public int getSrcId() {
		return _srcId;
	}
	
	public int getPostId() {
		return _postId;
	}
	
	public List<AnyMatch> getTitleMatches() {
		return _titleMatches;
	}
	
	public Date getDate() {
		return _date;
	}
	
	public FieldName getFieldName() {
		return _fieldName;
	}
	
	public double getWeight() {
		return _weight;
	}

	public void setWeight(double weight) {
		_weight = weight;
	}
	
	public Feature getFeature() {
		return _feature;
	}
	
	public void addPlace(AnyMatch match, double weight) {
		
		_places.add(new PhrasePlace(match, weight));
		
		if (match.isEntityOrNonStopWordMatch()) {
			
			String stem = match.getStem();
			Double  currWeight = _stemWeights.get(stem);

			if (currWeight == null) {
				currWeight = weight;
			} else {
				if (currWeight < weight) {
					currWeight = weight;
				}
			}
			_stemWeights.put(stem, currWeight);
		}
	}
	
	public List<PhrasePlace> getPlaces() {
		return _places;
	}
	
	public Map<String, Double> getStemWeights() {
		return _stemWeights;
	}
	
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		
		for (int i=0; i<_places.size(); i++) {
			PhrasePlace place = _places.get(i);
			if (i > 0) {
				sb.append(" ");
			}
			sb.append(place.getMatch().getText());
//			sb.append("/");
//			sb.append(_fmt_0_00.format(place.getWeight()));
		}
		
		return sb.toString();
	}
}
