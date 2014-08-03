package com.readrz.parse.test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.akuz.core.HashIndex;
import me.akuz.core.Hit;
import me.akuz.core.Index;
import me.akuz.core.Pair;

import com.readrz.data.ontology.TopicModel;
import com.readrz.lang.corpus.CorpusDoc;
import com.readrz.lang.corpus.CorpusPlace;
import com.readrz.lang.corpus.CorpusSentence;

public final class HtmlBuilder {
	
	private static final DecimalFormat _fmtProb;
	private static final List<String> _colours;
	static {{
		
		_fmtProb = new DecimalFormat("0.0000");
		
		_colours = new ArrayList<>();
		_colours.add("#ddddff");
		_colours.add("#ffdddd");
		_colours.add("#cccccc");
	}}
	
	private final String _text;
	
	public HtmlBuilder(String text) {
		_text = text;
	}
	
	public String buildHtml(
			TopicModel topicModel,
			CorpusDoc doc) {
		
		Index<Integer> topicIndexIndex = new HashIndex<>();
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		sb.append("<head>");
		sb.append("<title>Topics Analysis</title>");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		sb.append("<style>p {line-height: 1.5em;} body {font-family: Arial;}</style>");
		sb.append("</head>");
		sb.append("<body style=\"margin-left: 50px; margin-top: 50px; width: 500px;\">");
		
		
	//	sb.append("<h2>Article Text</h2>");
		sb.append("<p>");
		Hit prevHit = null;
		List<CorpusSentence> sentences = doc.getSentences();
		for (int sentenceIndex=0; sentenceIndex<sentences.size(); sentenceIndex++) {

			CorpusSentence sentence = sentences.get(sentenceIndex);
			List<CorpusPlace> places = sentence.getPlaces();
			
			for (int i=0; i<places.size(); i++) {
				
				CorpusPlace place = places.get(i);
				Integer placeTopicIndex = (Integer)place.getTag();
				
				if (placeTopicIndex != null) {
					
					Hit currHit = place.getFwdHit().getHit();
					
					if (prevHit == null) {
						
						sb.append(_text.substring(0, currHit.start()));

					} else {
						
						sb.append(_text.substring(prevHit.end(), currHit.start()));
					}
					
					String word = _text.substring(currHit.start(), currHit.end());
					
					Integer indexOfTopic = topicIndexIndex.ensure(placeTopicIndex);
					int colourIndex = Math.min(_colours.size() - 1, indexOfTopic);
					String colour = _colours.get(colourIndex);
					
					String hint = topicModel.getTopicEntity(placeTopicIndex).getId();
					
					sb.append("<span style=\"cursor: pointer; background-color: ");
					sb.append(colour);
					sb.append("; border-radius: 2px; padding: 0px 2px 0px 3px;\" title=\"");
					sb.append(hint);
					sb.append("\">");
					sb.append(word);
					sb.append("</span>");
					
					prevHit = currHit;
				}
			}
		}
		
		if (prevHit == null) {
			
			sb.append(_text.substring(0, _text.length()));

		} else {
			
			sb.append(_text.substring(prevHit.end(), _text.length()));
		}
		
		sb.append("</p>");
		
		
//		sb.append("<h2>Topics</h2>");
		@SuppressWarnings("unchecked")
		List<Pair<Integer, Double>> confirmedTopicProbs = (List<Pair<Integer, Double>>)doc.getTag();
		sb.append("<hr />");
		sb.append("<br />");
		if (confirmedTopicProbs == null || confirmedTopicProbs.size() == 0) {
			sb.append("<p>No topics detected.</p>");
		} else {
			for (int i=0; i<confirmedTopicProbs.size(); i++) {
				
				Pair<Integer, Double> pair = confirmedTopicProbs.get(i);
				Integer topicIndex = pair.v1();
				Double topicProb = pair.v2();
				
				Integer indexOfTopic = topicIndexIndex.ensure(topicIndex);
				int colourIndex = Math.min(_colours.size() - 1, indexOfTopic);
				String colour = _colours.get(colourIndex);

				String topicId = topicModel.getTopicEntity(pair.v1()).getId();
				
				sb.append("<p>");
				sb.append(_fmtProb.format(topicProb));
				sb.append(" &ndash; ");

				sb.append("<span style=\"cursor: pointer; background-color: ");
				sb.append(colour);
				sb.append("; border-radius: 2px; padding: 0px 2px 0px 3px;\">");
				sb.append(topicId);
				sb.append("</span>");
				
				sb.append("</p>");
			}
		}
		

		sb.append("</body></html>");

		String html = sb.toString();
		html = html.replaceAll("\\r?\\n", "<br />");
		return html;
	}
	
	

}
