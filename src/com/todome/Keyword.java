package com.todome;

import java.io.Serializable;

// TODO daoClass=KeywordDaoImpl http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_2.html#ANC25
public class Keyword {
	
	String keyword;
	String type;
	String description;

	Keyword(String keyword, String type, String description) {
		this.keyword = keyword;
		this.type = type;
		this.description = description;
	}
}