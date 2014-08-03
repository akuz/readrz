package com.readrz.zzz.parse;

import java.util.Date;

import com.readrz.zzz.ParsedPost;

public interface PostsParserListener {
	
	void onPostParsed(Date currDate, ParsedPost parsedPost);
	
}
