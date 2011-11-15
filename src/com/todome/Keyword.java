package com.todome;

import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "accounts", daoClass = KeywordDaoImpl.class)
public class Keyword {

	String keyword;

	// TagList tags;

	String description;

	Keyword(String keyword, String type, String description) {
		this.keyword = keyword;

		this.description = description;
	}
}