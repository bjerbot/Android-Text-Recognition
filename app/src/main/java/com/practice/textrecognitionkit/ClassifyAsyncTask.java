//package com.practice.textrecognitionkit;
//
//import android.os.AsyncTask;
//
//import androidx.loader.content.AsyncTaskLoader;
//
///*以下是AsyncTask的運行流程：
//    1.在UI線程上創建一個AsyncTask對象，並調用execute()方法。
//    2.execute()方法調用onPreExecute()方法，執行UI初始化操作。
//    3.execute()方法調用doInBackground()方法，在非UI線程上執行耗時的操作。
//    4.doInBackground()方法調用publishProgress()方法。
//    5.publishProgress()方法調用onProgressUpdate()方法，更新UI上的進度條。
//    6.doInBackground()方法運行完畢，返回結果。
//    7.execute()方法調用onPostExecute()方法，更新UI，例如顯示下載完成的圖像。*/
//
//public class ClassifyAsyncTask extends AsyncTask<String, Integer, String> {
//
//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//    }
//
//    @Override
//    protected String doInBackground(String... strings) {
//        return null;
//    }
//
//    @Override
//    protected void onProgressUpdate(Integer... values) {
//        super.onProgressUpdate(values);
//    }
//
//    @Override
//    protected void onPostExecute(String s) {
//        super.onPostExecute(s);
//    }
//}
