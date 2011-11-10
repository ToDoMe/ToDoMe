/*
 * Copyright (C) 2011  Chris Baines
 * Copyright (C) 2011  Rebecca Brannum
 * Copyright (C) 2011  Harry Cutts
 * Copyright (C) 2011  John Preston
 * Copyright (C) 2011  James Robinson
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.todome;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

import com.j256.ormlite.dao.Dao;

public interface KeywordDao extends Dao<Keyword, String> {

	/*
	 * Match the words in the task name with the keywords in the dictionary, The
	 * original implementation did a String.contains, which gave interesting
	 * results, for instance "Catch bus" would match with bct, bcs, shop.pet (as
	 * the cat in Catch) ...
	 */

}