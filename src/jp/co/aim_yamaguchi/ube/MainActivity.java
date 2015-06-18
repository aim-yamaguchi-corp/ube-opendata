package jp.co.aim_yamaguchi.ube;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import jp.co.aim_yamaguchi.ube.util.FileUtil;
import jp.co.aim_yamaguchi.ube.util.GetXmlInterface;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
[MainActivity]

Copyright (c) 2015 AIM All rights reserved.

This software is released under the MIT License.
http://opensource.org/licenses/mit-license.php
*/
@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {

	private static final String ACTIVITY_LIST = "activitylist.xml";

	private WebView wv;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		try {
			super.onCreate(savedInstanceState);

			wv = new WebView(this);

			setContentView(wv);

			//以下の設定はGeolocation使用に必要
			wv.setWebViewClient(new WebViewClient(){
				@Override
				public boolean shouldOverrideUrlLoading(
						WebView view,
					String url) {
					return false;
				}
			});

			wv.setWebChromeClient(new WebChromeClient(){
				@Override
				public void onGeolocationPermissionsShowPrompt(
					String origin,
					Callback callback) {
					callback.invoke(origin, true, false);
				}
			});

			// WebView内でJavaScriptを有効化
			wv.getSettings().setJavaScriptEnabled(true);
			wv.getSettings().setGeolocationEnabled(true);

			wv.addJavascriptInterface(new GetXmlInterface(this), "GetXml");

			//初回起動時、ファイルが存在しない場合はactivitylist.xmlをコピーする
			File file = this.getFileStreamPath(ACTIVITY_LIST);
			file.delete();
			if (!file.exists()) {

				AssetManager assetManager = this.getAssets();
				InputStream in = assetManager.open(ACTIVITY_LIST);

				//HTMLソースを読み出す
				StringBuilder sb = new StringBuilder();

				String str;
				//1行読み取り
				str = FileUtil.readLine(in);
				while (str != null) {
					sb.append(str);
					//次を読み込む
					str = FileUtil.readLine(in);
				}

				OutputStream os = openFileOutput(ACTIVITY_LIST, MODE_APPEND);
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
				pw.write(sb.toString());
				pw.close();
				os.close();
			}

			// WebView内に，アプリが保持するHTMLを表示
			wv.loadUrl("file:///android_asset/index.html");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 戻るボタンをタップされた際にアプリ終了や一つ前のActivityに戻すのではなく
	// WebViewで表示した一つ前のページを表示させる
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && wv.canGoBack()) {
			wv.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
