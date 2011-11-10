package com.todome;

import java.sql.SQLException;
import java.util.HashSet;

import com.j256.ormlite.dao.BaseDaoImpl;

public class KeywordDaoImpl extends BaseDaoImpl<Keyword, String> implements KeywordDao {

	private static final String TAG = "KeywordDatabase";

	protected KeywordDaoImpl(Class<Keyword> dataClass) throws SQLException {
		super(dataClass);
		// TODO Auto-generated constructor stub
	}
}
