package com.readrz.data;

import org.junit.Test;

import com.readrz.data.PathsId;
import com.readrz.data.PathsIdBuilder;
import com.readrz.search.QueryKeyIds;

public final class PathsIdBuilderTest {

	@Test
	public void testPathsIdBuilder() {
		
		QueryKeyIds queryKeyIds = new QueryKeyIds();
		queryKeyIds.addDocumentKeyId(77);
		queryKeyIds.addDocumentKeyId(-9001);
		queryKeyIds.addSenCheckKeyId(-11);
		queryKeyIds.addSenCheckKeyId(-77);
		queryKeyIds.addSentenceKeyId(20);
		queryKeyIds.addSentenceKeyId(-1220);
		queryKeyIds.addSentenceKeyId(8888);
		
		System.out.println("Building paths id...");
		int periodId = 234;
		PathsIdBuilder builder = new PathsIdBuilder(periodId);
		builder.setQueryKeyIds(queryKeyIds);

		System.out.println("Deserializing paths id...");
		PathsId pathsId = new PathsId(builder.getData());
		
		System.out.println("Checking paths id...");
		if (pathsId.getPeriodId() != periodId) {
			throw new IllegalStateException("Invalid period id");
		}
		if (pathsId.getQueryKeyIds().getDocumentKeyIds().containsAll(queryKeyIds.getDocumentKeyIds()) == false) {
			throw new IllegalStateException("Invalid key ids");
		}
		if (queryKeyIds.getDocumentKeyIds().containsAll(pathsId.getQueryKeyIds().getDocumentKeyIds()) == false) {
			throw new IllegalStateException("Invalid key ids");
		}
		if (pathsId.getQueryKeyIds().getSenCheckKeyIds().containsAll(queryKeyIds.getSenCheckKeyIds()) == false) {
			throw new IllegalStateException("Invalid key ids");
		}
		if (queryKeyIds.getSenCheckKeyIds().containsAll(pathsId.getQueryKeyIds().getSenCheckKeyIds()) == false) {
			throw new IllegalStateException("Invalid key ids");
		}
		if (pathsId.getQueryKeyIds().getSentenceKeyIds().containsAll(queryKeyIds.getSentenceKeyIds()) == false) {
			throw new IllegalStateException("Invalid key ids");
		}
		if (queryKeyIds.getSentenceKeyIds().containsAll(pathsId.getQueryKeyIds().getSentenceKeyIds()) == false) {
			throw new IllegalStateException("Invalid key ids");
		}

		System.out.println("Matched paths id.");
	}
}
