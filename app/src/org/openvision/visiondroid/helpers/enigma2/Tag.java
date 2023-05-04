/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.helpers.enigma2;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Tag {
	/**
	 * @param selectedTags
	 * @return
	 */
	@NonNull
	public static String implodeTags(@NonNull ArrayList<String> selectedTags){
		String tags = "";
		for (String tag : selectedTags) {
			if ("".equals(tags)) {
				tags = tags.concat(tag);
			} else {
				tags = tags.concat(" ").concat(tag);
			}
		}
		
		return tags;
	}
}
