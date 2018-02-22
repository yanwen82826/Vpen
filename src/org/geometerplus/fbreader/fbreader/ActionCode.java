/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.fbreader;

public interface ActionCode {
	String SHOW_LIBRARY = "library";
	String SHOW_PREFERENCES = "preferences";
	String SHOW_BOOK_INFO = "bookInfo";
	String SHOW_TOC = "toc";
	String SHOW_BOOKMARKS = "bookmarks";
	String SHOW_NETWORK_LIBRARY = "networkLibrary";

	String SWITCH_TO_NIGHT_PROFILE = "night";
	String SWITCH_TO_DAY_PROFILE = "day";

	String SEARCH = "search";
	String FIND_PREVIOUS = "findPrevious";
	String FIND_NEXT = "findNext";
	String CLEAR_FIND_RESULTS = "clearFindResults";

	String SET_TEXT_VIEW_MODE_VISIT_HYPERLINKS = "hyperlinksOnlyMode";
	String SET_TEXT_VIEW_MODE_VISIT_ALL_WORDS = "dictionaryMode";

	String TURN_PAGE_BACK = "previousPage";
	String TURN_PAGE_FORWARD = "nextPage";

	String VOLUME_KEY_SCROLL_FORWARD = "volumeKeyScrollForward";
	String VOLUME_KEY_SCROLL_BACK = "volumeKeyScrollBackward";
	String SHOW_MENU = "menu";
	String SHOW_NAVIGATION = "navigate";

	String GO_BACK = "goBack";
	String EXIT = "exit";
	String SHOW_CANCEL_MENU = "cancelMenu";

	String ROTATE = "rotate";
	String INCREASE_FONT = "increaseFont";
	String DECREASE_FONT = "decreaseFont";

	String PROCESS_HYPERLINK = "processHyperlink";

	String SELECTION_SHOW_PANEL = "selectionShowPanel";
	String SELECTION_HIDE_PANEL = "selectionHidePanel";
	String SELECTION_CLEAR = "selectionClear";
	String SELECTION_COPY_TO_CLIPBOARD = "selectionCopyToClipboard";
	String SELECTION_SHARE = "selectionShare";
	String SELECTION_TRANSLATE = "selectionTranslate";
	String SELECTION_BOOKMARK = "selectionBookmark";
	
	//test
	String SELECTION_BACKGROUND = "selectionBackground";
	String SHOW_ANNO_TEXT_MENU = "showAnnoTextMenu";
	String SHOW_ANNO_PIC_MENU = "showAnnoPicMenu";
	String PIC_NOTE_OPEN = "picNoteOpen";
	String RECORD_TEA = "recordTeacher";
	String RECORD_PAENTS = "recordParents";
	String PARENTS_INVOLVEMENT = "parentsInvolvement";
	String SYNC = "sync";
	String SHOW_SHAREDATA = "shareData";
	String SHOW_SELFDATA = "selfData";
	//dbTable
	String ANNOTATION_TAKEPIC = "takepic";
	String ANNOTATION_TAKEPIC_ANNO = "takepic_anno";
	String ANNOTATION_TEXT = "annotext";
	String ANNOTATION_TEXT_RANGE = "annotext_range";
	String ANNOTATION_IMAGE = "annoimage";
	String ANNOTATION_TRANSLATION = "translation";
	String ANNOTATION_RECORD = "record";
	String ANNOTATION_TRANSLATION_NUMBER = "translation_num";
	String ANNOTATION_TTS_NUMBER = "tts_num";
	String ANNOTATION_PARENTIN_TIME = "parentin_time";
	String ANNOTATION_SYNC_DATA = "sync_local_data";
	String ANNOTATION_SYNC_TIME = "sync_time";
	String ANNOTATION_SYNC_ACTION = "sync_action";
	
	String SYNC_RECORD = "sync_record";
	
	//還原資料
	String GET_ALL_DATA = "get_all_data";
	
}
