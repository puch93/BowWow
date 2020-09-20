package kr.core.bowwow.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import kr.core.bowwow.dto.ChatItem;

public class DBHelper {

    private final int DB_VERSION = 1;
    private final String DB_NAME = "bowwowchat.db";
    private final String DB_CHAT_TABLE = "CHAT";

    public ChatItem getLastItem(Context context){
        ChatSql dbHelper = new ChatSql(context,DB_NAME,null,DB_VERSION);
        return dbHelper.getLastData();
    }

    public void chatItemInsert(Context context, ChatItem chatItem){
        ChatSql dbHelper = new ChatSql(context,DB_NAME,null,DB_VERSION);
        dbHelper.chatInsert(chatItem);
    }

    public ArrayList<ChatItem> getChatList(Context context){
        ChatSql dbHelper = new ChatSql(context,DB_NAME,null,DB_VERSION);
        return dbHelper.getChatList();
    }

    public ArrayList<ChatItem> getChatSearchList(Context context,String keyword){
        ChatSql dbHelper = new ChatSql(context,DB_NAME,null,DB_VERSION);
        return dbHelper.getChatSearchList(keyword);
    }

    private class ChatSql extends SQLiteOpenHelper{
        public ChatSql(Context context, String name, SQLiteDatabase.CursorFactory factory,int ver){
            super(context,name,factory,ver);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "+ DB_CHAT_TABLE
                    +"(idx INTEGER PRIMARY KEY AUTOINCREMENT ,t_idx TEXT, t_site TEXT, t_user_idx TEXT, t_type TEXT, t_msg TEXT, t_sound TEXT, t_regdate TEXT, t_editdate TEXT, num TEXT, duration TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        public ChatItem getLastData(){
            SQLiteDatabase db = getReadableDatabase();
            ChatItem chatItem = null;

            StringBuffer sb = new StringBuffer();
            sb.append(" SELECT t_idx, t_site, t_user_idx, t_type, t_msg, t_sound, t_regdate, t_editdate, num, duration FROM " + DB_CHAT_TABLE + " ORDER BY idx DESC limit 1");

            Cursor cursor =  db.rawQuery(sb.toString(), null);

            while (cursor.moveToNext()) {
                chatItem = new ChatItem();
                chatItem.setT_idx(cursor.getString(0));
                chatItem.setT_site(cursor.getString(1));
                chatItem.setT_user_idx(cursor.getString(2));
                chatItem.setT_type(cursor.getString(3));
                chatItem.setT_msg(cursor.getString(4));
                chatItem.setT_sound(cursor.getString(5));
                chatItem.setT_regdate(cursor.getString(6));
                chatItem.setT_editdate(cursor.getString(7));
                chatItem.setNum(cursor.getString(8));
                chatItem.setDuration(cursor.getString(9));

                db.close();

            }

            cursor.close();

            return chatItem;
        }

        public void chatInsert(ChatItem item){
            SQLiteDatabase db = getWritableDatabase();

            StringBuffer sb = new StringBuffer();
            sb.append(" INSERT INTO " + DB_CHAT_TABLE);
            sb.append(" ( t_idx, t_site, t_user_idx, t_type, t_msg, t_sound, t_regdate, t_editdate, num, duration ) ");
            sb.append(" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");

            db.execSQL(sb.toString(),new Object[]{
                    item.getT_idx(),
                    item.getT_site(),
                    item.getT_user_idx(),
                    item.getT_type(),
                    item.getT_msg(),
                    item.getT_sound(),
                    item.getT_regdate(),
                    item.getT_editdate(),
                    item.getNum(),
                    item.getDuration()
            });

            db.close();
        }

        public ArrayList<ChatItem> getChatList(){
            SQLiteDatabase db = getReadableDatabase();
            ArrayList<ChatItem> chatList = new ArrayList<>();
            ChatItem chatItem;

            StringBuffer sb = new StringBuffer();
            sb.append(" SELECT t_idx, t_site, t_user_idx, t_type, t_msg, t_sound, t_regdate, t_editdate, num, duration FROM " + DB_CHAT_TABLE + " ORDER BY idx ASC");

            Cursor cursor =  db.rawQuery(sb.toString(), null);
            while (cursor.moveToNext()) {
                chatItem = new ChatItem();
                chatItem.setT_idx(cursor.getString(0));
                chatItem.setT_site(cursor.getString(1));
                chatItem.setT_user_idx(cursor.getString(2));
                chatItem.setT_type(cursor.getString(3));
                chatItem.setT_msg(cursor.getString(4));
                chatItem.setT_sound(cursor.getString(5));
                chatItem.setT_regdate(cursor.getString(6));
                chatItem.setT_editdate(cursor.getString(7));
                chatItem.setNum(cursor.getString(8));
                chatItem.setDuration(cursor.getString(9));

                chatList.add(chatItem);
            }

            db.close();

            return chatList;
        }

        public ArrayList<ChatItem> getChatSearchList(String keyword){
            SQLiteDatabase db = getReadableDatabase();
            ArrayList<ChatItem> chatList = new ArrayList<>();
            ChatItem chatItem;

            StringBuffer sb = new StringBuffer();

            sb.append(" SELECT t_idx, t_site, t_user_idx, t_type, t_msg, t_sound, t_regdate, t_editdate, num, duration FROM " + DB_CHAT_TABLE
                    + " WHERE t_msg like '%" + keyword + "%' ORDER BY idx ASC ");

            Cursor cursor =  db.rawQuery(sb.toString(), null);
            while (cursor.moveToNext()) {
                chatItem = new ChatItem();
                chatItem.setT_idx(cursor.getString(0));
                chatItem.setT_site(cursor.getString(1));
                chatItem.setT_user_idx(cursor.getString(2));
                chatItem.setT_type(cursor.getString(3));
                chatItem.setT_msg(cursor.getString(4));
                chatItem.setT_sound(cursor.getString(5));
                chatItem.setT_regdate(cursor.getString(6));
                chatItem.setT_editdate(cursor.getString(7));
                chatItem.setNum(cursor.getString(8));
                chatItem.setDuration(cursor.getString(9));

                chatList.add(chatItem);
            }

            db.close();

            return chatList;
        }

    }


}
