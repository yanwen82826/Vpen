package org.geometerplus.zlibrary.text.view;

import java.util.ArrayList;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class SaveValue {
	
	//�Ϥ��W��
	public static String picUri = "";
	//�P�_�O�_���ӧ�
	public static Boolean finishTakpic = false;
	//��r���O-------------------------------------------------------------------
	//�ثe�I��highLight
	public static int nowIndex = -1;
	//��r���O�O�_���
	//public static int[] annoKey; 
	public static ArrayList annoKey = new ArrayList(); //�������ǵ��O����� �B�z�ۤv�����O
	public static ArrayList annoKeySync = new ArrayList(); //�������ǵ��O����� �B�z��L�H�����O(�C���������ϥΪ̡A�}�C���ȭn�M��)
	public static int key = 0; //annoKey���Ӽ�
	
	//��r���O-------------------------------------------------------------------
	//�ۦ�s�W���Ϥ�(�D���O)---------------------------------------------------------------
	public static String picLesson = null; //��ܩ��᪺�Ϥ��ɥ�(takepic)
	public static String  picNow = null; //��e��ܪ��Ϥ��W��(�ɮצW�١A�D�ϥΪ̩R�W)
	public static String nowLesson = null;//�ҿ�Ϥ����ݪ����`(�b�ϥΪ̿�ܩҩ��᪺�Ӥ��ɤ~�|�i��s���Atakepic_anno��)
	public static String syncsrcPath_pic = null;//�P�B�ɡA�n��ܪ��Ϥ����|
	//�Ϥ����O-------------------------------------------------------------------
	//�ثe�I��Ϥ����O
	public static int picNowIndex = -1;
	public static int tempIndex = -1; //�x�s�����Ҧ��e������(�ĴX��)
	//width( �������r�e�� )
	public static float wordWidth = 0;
	//�Ѯv���Ѧ���
	public static int lecture_num = 0;
	//�Ϥ����O-------------------------------------------------------------------		
	
	//page information
	public static int pageIndex = 1;
	public static boolean draw = false;
	/*
	public static String[] log_Username = {   "stu001", "stu002", "stu003", "stu004", "stu005", 
												"stu006", "stu007", "stu008", "stu009", "stu010",
												"stu011", "stu012", "stu013", "stu014", "stu015",
												"stu016", "stu017", "stu018", "stu019", "stu020",
												"stu021", "stu022", "stu023", "stu024", "stu025",
												"stu026", "stu027", "stu028", "stu029", "stu030",
												"stu031", "stu032", "stu033", "stu034", "stu035",
												"stu036", "stu037", "stu038", "stu039", "stu040"};
	
	*/
	public static String[] log_Username = {   "stu001", "stu002", "stu003", "stu004", "stu005", 
		"stu006", "stu007", "stu008", "stu009", "stu010",
		"stu011", "stu012", "stu013", "stu014", "stu015",
		"stu016", "stu017", "stu018", "stu019", "stu020",
		"stu021", "stu022", "stu023", "stu024", "stu025",
		"stu026", "stu027", "stu028", "stu029", "stu030"};

	public static ArrayList syncUserOK = new ArrayList();
	public static String UserName; //�ϥΪ̦W��
	public static String SyncUserName = ""; //�P���ϥΪ̦W��

	public static boolean picNoteOpen = false;
	public static boolean NoteOpen = true;
	
	//test ����y
	public static int y_range = 0;
	
	//�Ψӭp��C�@��r( �@�y )������
	public static int w = 0;
	public static boolean w_key = true;
	
	//���O��ܮإX�{
	public static boolean IsNote = true;
	
	//�O�_�Ѯv���Ѫ����O
	public static boolean Islecture = false;
	
	//�O�_��s����( �����ؿ��ɻݭn )
	public static boolean IsRepaint = false;
	
	//�����ثe�O�_��������t�Ϊ���LActivity(�DFBReader Activity)
	public static boolean IsSystemActivity = false;
	
	//�O�_�a���ѻP
	public static boolean IsParent = false; //�w�]������
	
	//�O�_��s�u
	public static boolean IsConnect = false; //�w�]���L�s�u���A
	
	//���y�W��
	public static String bookName = "content20140428.epub";
	//

	//����P�B�ʧ@���_��( false �h���P�B )( true �h�C10��P�B�@����� )
	public static boolean IsUPdate = false;

	//�ثe���b�P�B��L�H���ƾڶ�?( �[���Ҧ� )
	public static boolean IsSyncData = false;
	
	//( �[���Ҧ��O�_���} )
	public static boolean IsListVisible = false;
	
	//����ƤW��?
	public static boolean IsUpdateSyncTime = false;
	
	//�O�_��slistView
	public static boolean IsUpdateListView = false;
	
	
	public static void setSyncDataOn(String userName){
		final SQLiteDB db = ZLApplication.Instance().db;
		IsSyncData = true;
		IsUpdateListView = true;
		SyncUserName = userName; //�ŭ�
		db.insertSyncTime(SaveValue.UserName, SaveValue.SyncUserName);
		annoKeySync.clear(); //�M�����ê����O��ơA�����O��Ƥ�����
	}
	
	public static void setSyncDataOff(){
		final SQLiteDB db = ZLApplication.Instance().db;
		IsSyncData = false;
		SyncUserName = ""; //�ŭ�
		int temp = db.getTableCount(ActionCode.ANNOTATION_SYNC_TIME); //���o�Ӽ�
		db.updateSyncTime(String.valueOf(temp), "_id");
		annoKeySync.clear(); //�M�����ê����O��ơA�����O��Ƥ�����
	}
	
	//Animation
	public static Animation animationOn=new AlphaAnimation(0, 1);
	public static Animation animationOff=new AlphaAnimation(1, 0);
	
	//���}
	public static String webIP = "http://140.115.135.135";
	//public static String webIP = "http://140.115.135.135";
	//public static String webIP = "http://140.115.126.197";
	//public static String webIP = "http://140.115.126.179";
	
	//�ثe�O�_���\�P�B
	public static boolean IsNowSync = false;
	
	//�O�_�ʵe���ܤ�
	public static boolean IsAnimation = false;
	
	//�O�_�����ؿ�
	public static boolean IsToc = false;
	public static boolean IsSubToc = false;
	
	//���զP�B��
	public static boolean test = false;
	
	//���զP�B��
	public static boolean syncOpen = false;
	
	public static int tttt = 0;
	public static int tttt2 = 0;
}