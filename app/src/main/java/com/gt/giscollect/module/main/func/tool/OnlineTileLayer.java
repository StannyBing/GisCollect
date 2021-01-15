//package com.gt.giscollect.module.main.func.tool;
//
//import android.content.ContentValues;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.util.Log;
//
//import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
//import com.zx.zxutils.ZXApp;
//import com.zx.zxutils.util.ZXSystemUtil;
//
//import java.io.BufferedInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.text.DecimalFormat;
//
//public class OnlineTileLayer extends ArcGISTiledMapServiceLayer {
//
//    private String mUrl;
//    private SQLiteDatabase mDb;
//
//    public OnlineTileLayer(String url, String name) {
//        super(url);
//        mUrl = url;
//        setName(name);
//        MapDbHelper mapDbHelper = new MapDbHelper(ZXApp.getContext(), MapDbHelper.TABLE_NAME);
//        mDb = mapDbHelper.getWritableDatabase();
////        setDefaultSpatialReference(SpatialReference.create(102100));
//    }
//
//    @Override
//    protected byte[] getTile(int level, int col, int row){
//        byte[] result = null;
//        String tileIndex = "tile_" + level + "_" + row + "_" + col;
//        String sql = "select * from " + MapDbHelper.TABLE_NAME + " where TILEINDEX = '" + getName() + tileIndex + "'";
//        Cursor mCursor = mDb.rawQuery(sql, null);
//        boolean hasData = false;
//        while (mCursor.moveToNext()) {//判断是否存在数据
//            hasData = true;
//        }
//        if (hasData && mCursor.moveToFirst()) {
//            result = mCursor.getBlob(mCursor.getColumnIndex("TILEDATA"));
//        } else {
//            result = getNetworkData(tileIndex);
//        }
//        return result;
//    }
//
//    private byte[] getNetworkData(String tile) {
//        byte[] result = null;
//        try {
//            String tileIndex = "/" + tile.replaceAll("_", "/");
//            URL url = new URL(mUrl + tileIndex);
//            // URL url = new URL(mUrl + "&level=" + level + "&col=" + col +
//            // "&row=" + row);
//            byte[] buf = new byte[1024];
//            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
//            httpConnection.connect();
//            BufferedInputStream is = new BufferedInputStream(httpConnection.getInputStream());
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            int temp = -1;
//            while ((temp = is.read(buf)) > 0) {
//                bos.write(buf, 0, temp);
//            }
//            is.close();
//            httpConnection.disconnect();
//            result = bos.toByteArray();
//            insertOrUpdateToDb(tile, result);
////            saveFileWithBase64(new Base64().encode(result), path);
//        } catch (Exception e) {
////            e.printStackTrace();
//        }
//        return result;
//    }
//
//    //添加数据
//    private void insertOrUpdateToDb(String tileIndex, byte[] result) {
//        ContentValues values = new ContentValues();
//        values.put("TILEINDEX", getName() + tileIndex);
//        values.put("TILEDATA", result);
//        values.put("TILETIME", System.currentTimeMillis() + "");
//
//        //首选判断数据库表中文件大小时候超过六百兆，如果超过，就进行一次删除
//        File file = new File("data/data/" + ZXSystemUtil.getPackageName() + "/databases/" + MapDbHelper.TABLE_NAME);
//        Cursor cs = mDb.rawQuery("select * from " + MapDbHelper.TABLE_NAME, null);
//        int count = cs.getCount();
//        cs.close();
//        if (getFileSize(file) > 300) {//当缓存大于600兆时,删除数据库中前百分之三十的数据
//            Log.e("当前文件大小:", getFileSize(file) + "");
//            int deleteLimit = (int) (count * 0.3);
//            String sqlDelete = "delete from " + MapDbHelper.TABLE_NAME + " where TILEINDEX in ( select TILEINDEX from " + MapDbHelper.TABLE_NAME + " order by TILETIME desc limit 0," + deleteLimit + " )";
//            mDb.execSQL(sqlDelete);
//            Log.e("删除后文件大小:", getFileSize(file) + "");
//        }
//
//        String sql = "select * from " + MapDbHelper.TABLE_NAME + " where TILEINDEX = '" + getName() + tileIndex + "'";
//        Cursor cursor = mDb.rawQuery(sql, null);
//        boolean hasData = false;
//        while (cursor.moveToNext()) {
//            hasData = true;
//        }
//        cursor.close();
//        if (hasData) {
//            mDb.update(MapDbHelper.TABLE_NAME, values, "TILEINDEX = ?", new String[]{getName() + tileIndex});
//        } else {
//            mDb.insert(MapDbHelper.TABLE_NAME, "value", values);
//        }
//    }
//
//    //获取文件大小
//    private double getFileSize(File file) {
//        DecimalFormat df = new DecimalFormat("#.00");
//        double size = 0;
//        try {
//            if (file.exists()) {
//                FileInputStream fIs = new FileInputStream(file);
//                size = Double.valueOf(df.format((double) fIs.available() / 1048576));
//            }
//        } catch (Exception e) {
////            e.printStackTrace();
//        }
//        return size;
//    }
//
//}
