package jp.co.aim_yamaguchi.ube.util;

import java.io.InputStream;

/**
[FileUtil]

Copyright (c) 2015 AIM All rights reserved.

This software is released under the MIT License.
http://opensource.org/licenses/mit-license.php
*/
public class FileUtil {

	/**
	 * InputStreamより１行だけ読む（読めなければnullを返す）
	 * @param is InputStream
	 * @return １行文字列
	 * @throws Exception
	 */
	public static String readLine(InputStream is) throws Exception {
		int byteLen;
		int readChar;
		byte lineByte[] = new byte[2048];

		//１文字読む
		readChar = is.read();
		if (readChar < 0) {
			//ファイルを読み終わっていたら、nullを返す
			return null;
		}
		byteLen = 0;
		//行の終わりまで読む
		while(readChar > 10) {
			//何かの文字であれば、バイトに追加
			if (readChar >= ' ') {
				lineByte[byteLen] = (byte) readChar;
				byteLen++;
			}
			//次を読む
			readChar = is.read();
		}
		//文字列に変換
		return new String(lineByte, 0, byteLen);
	}

}
