package jp.co.aim_yamaguchi.ube.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Xml;
import android.webkit.JavascriptInterface;

/**
[GetXmlInterface]

Copyright (c) 2015 AIM All rights reserved.

This software is released under the MIT License.
http://opensource.org/licenses/mit-license.php
*/
@SuppressLint("SetJavaScriptEnabled")
public class GetXmlInterface extends Activity {
	Context con;

	private static final String ACTIVITY_LIST = "activitylist.xml";

	public GetXmlInterface(Context c) {
		this.con = c;
	}

	@JavascriptInterface
	/**
	 * データ一覧のXmlをローカルファイルから取得する
	 * @return Xml文字列
	 * @throws Exception
	 */
	public String getDataListXml() throws Exception {
		AssetManager assetManager = con.getAssets();
		InputStream in = assetManager.open("datalist.xml");

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

		return sb.toString();
	}

	@JavascriptInterface
	/**
	 * データのXmlをローカルファイルから取得する
	 * @param xmlNameStr XML名
	 * @return Xml文字列
	 * @throws Exception
	 */
	public String getDataXml(String xmlNameStr) throws Exception {
		AssetManager assetManager = con.getAssets();
		InputStream in = assetManager.open("data/" + xmlNameStr + ".xml");

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

		return sb.toString();
	}

	@JavascriptInterface
	/**
	 *
	 * @param urlStr XML取得元URL
	 * @return Xml文字列
	 * @throws Exception
	 */
	public String getXml(String urlStr) throws Exception {
		HttpURLConnection urlCon = null;
		StringBuilder sb = new StringBuilder();

		try {
			//URLの作成
			URL url = new URL(urlStr);

			//接続用HttpURLConnectionオブジェクト作成
			urlCon = (HttpURLConnection)url.openConnection();
			//リクエストメソッドの設定
			urlCon.setRequestMethod("GET");
			//リダイレクトを自動で許可しない設定
			urlCon.setInstanceFollowRedirects(false);
			//ヘッダーの設定(複数設定可能)
			urlCon.setRequestProperty("Accept-Language", "jp");
			//接続
			urlCon.connect();

			//本文の取得
			InputStream is = urlCon.getInputStream();

			String str;
			//1行読み取り
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			str = reader.readLine();
			while (str != null) {
				sb.append(str);
				//次を読み込む
				str = reader.readLine();
			}

			is.close();

			//切断
			urlCon.disconnect();
		} catch (Exception e) {
			if (urlCon != null) {
				urlCon.disconnect();
			}
			throw e;
		}

		return sb.toString();
	}

	//XMLを解析する。
	@JavascriptInterface
	public void openBrowse(String url) {
		Uri uri = Uri.parse(url);
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		con.startActivity(i);
	}

	/**
	 * Activityファイルを更新する
	 * @param itemkey 保存アイテムキー
	 * @param itemid 保存アイテムID
	 * @return １行文字列
	 * @throws Exception
	 */
	public String readActivityFile(String itemkey, String itemid) throws Exception {

		String retVal = "";
		StringBuilder sb = new StringBuilder();
		InputStream is = con.openFileInput(ACTIVITY_LIST);

		String str;
		//1行読み取り
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		str = reader.readLine();
		while (str != null) {
			sb.append(str);
			//次を読み込む
			str = reader.readLine();
		}

		ArrayList<Item> itemList = getArray(sb.toString(), itemkey);

		if (itemList != null) {
			for (Item item : itemList) {
				if (item.id == itemid) {
					retVal = item.value;
					break;
				}
			}
		}

		is.close();

		return retVal;
	}

	//XMLを解析する。
	public ArrayList<Item> getArray(String xmlStr, String itemkey) {
		XmlPullParser parser = null;
		//1アイテムごとの情報格納用
		Item currentMsg = null;
		//アイテムの情報を格納用
		ArrayList<Item> list = null;
		String itemkeyMsg = null;

		String tag = null;		//タグ名取得用

		//XmlPullParseクラスのインスタンスを取得する。
		parser = Xml.newPullParser();

		//XMLパーサー解析開始
		try {
			//XMLのストリームを渡す
			parser.setInput(new StringReader(xmlStr));

			//イベント取得
			int eventType = parser.getEventType();
			//ドキュメント終端までループ
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
					//ドキュメントの最初
					case XmlPullParser.START_DOCUMENT:
						//ドキュメントが始まったら準備
						//今回は何もしていない。
						break;

					//開始タグ時
					case XmlPullParser.START_TAG:
						tag = parser.getName();
						if (tag.equals("itemkey")) {
							itemkeyMsg = tag;
						}
						//itemタグ開始時
						//必要なのは、itemタグの中身の子タグのみなので
						//itemのスタートタグでitemクラスインスタンス作成し、
						//作成していなかったら、item内の情報ではない。
						if (tag.equals("item")) {
							currentMsg = new Item();
						}
						break;
					case XmlPullParser.TEXT:
						String text = parser.getText();
						//テキストタグ時
						if (itemkeyMsg != null && text.equals(itemkey)) {
							list = new ArrayList<Item>();
						}
						if (list != null && currentMsg != null) {
							//itemタグ内の子タグごとの処理
							//タグ名称と取得したいタグ名を比較して
							//同じであったら、nextText()により内容取得。
							if (tag.equals("id")) {
								currentMsg.setId(text);
							} else if(tag.equals("value")) {
								currentMsg.setValue(text);
							}
						}
						break;
					//終了タグ時
					case XmlPullParser.END_TAG:
						tag = parser.getName();
						//itemタグが終わったら、そこで１記事のセットが終了したとして
						//listに追加。
						if (tag.equals("item")) {
							if (currentMsg != null) {
								//Itemタグ終了時に格納。
								list.add(currentMsg);
								currentMsg = null;
							}
						}
						if (tag.equals("itemkey")) {
							itemkeyMsg = null;
						}
						break;
				}
				//次のイベントへ遷移させループ
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			return null;
		}catch (IOException e) {
			return null;
		}

		return list;
	}

	//XML内容を保存するためのクラス
	private class Item {
		/** ID */
		private String id = "";
		/** 値 */
		private String value = "";

		/**
		 * @return id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @param id セットする id
		 */
		public void setId(String id) {
			this.id = id;
		}

		/**
		 * @return value
		 */
		public String getValue() {
			return value;
		}

		/**
		 * @param value セットする value
		 */
		public void setValue(String value) {
			this.value = value;
		}
	}

}
