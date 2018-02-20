package com.github.hexeditor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

class BinUtil
{

	public static final String[] ACCENTS = new String[]
	{ "@AaÀÁÂÃÄÅàáâãäåĀāĂăĄąǍǎǞǟǠǡǺǻȀȁȂȃȦȧȺḀḁẚẠạẢảẤấẦầẨẩẪẫẬậẮắẰằẲẳẴẵẶặ₳ÅⒶⓐⱥⱯ＠Ａａ", "BbƀƁƂƃƄƅɃɓḂḃḄḅḆḇℬ␢ⒷⓑＢｂ",
			"Cc¢©ÇçĆćĈĉĊċČčƇƈȻȼḈḉ₡₢ℂ℃℄ℭⒸⓒꜾꜿＣｃ", "DdÐðĎďĐđƉƊƋƌȡɖɗḊḋḌḍḎḏḐḑḒḓ₫₯ⅅⅆ∂ⒹⓓꝩꝺＤｄ",
			"EeÈÉÊËèéêëĒēĔĕĖėĘęĚěƎǝȄȅȆȇȨȩḔḕḖḗḘḙḚḛḜḝẸẹẺẻẼẽẾếỀềỂểỄễỆệ₠€ℇ℮ℯℰⅇⒺⓔⱸⱻＥｅ", "FfƑƒḞḟ₣℉ℱℲ⅍ⒻⓕꜰꝫꝼꟻＦｆ",
			"GgĜĝĞğĠġĢģƓǤǥǦǧǴǵɠɡḠḡ₲ℊ⅁ⒼⓖꝽꝾꝿＧｇ", "HhĤĥĦħȞȟḢḣḤḥḦḧḨḩḪḫẖ₴ℋℌℍℎℏⒽⓗⱧⱨⱵⱶＨｈ",
			"IiÌÍÎÏìíîïĨĩĪīĬĭĮįİıƗǏǐȈȉȊȋɨӀḬḭḮḯỈỉỊịℐℑℹⅈⒾⓘꟾＩｉ", "JjĴĵǰȷɈɉⅉⒿⓙⱼＪｊ", "KkĶķĸƘƙǨǩḰḱḲḳḴḵ₭KⓀⓚⱩⱪꝀꝁꝂꝃꝄꝅＫｋ",
			"Ll£ĹĺĻļĽľĿŀŁłƚȴȽḶḷḸḹḺḻḼḽ₤₶ℒℓ℔⅂⅃ⓁⓛⱠⱡⱢꝆꝇꝈꝉꞀꞁＬｌ", "MmƜɯḾḿṀṁṂṃ₥ℳⓂⓜⱮꟽꟿＭｍ",
			"NnÑñŃńŅņŇňŉŊŋƝƞǸǹȠȵɲṄṅṆṇṈṉṊṋ₦₪ℕ№ⓃⓝＮｎ",
			"OoÒÓÔÕÖØòóôõöøŌōŎŏŐőƆƟƠơǑǒǪǫǬǭǾǿȌȍȎȏȪȫȬȭȮȯȰȱɔɵṌṍṎṏṐṑṒṓỌọỎỏỐốỒồỔổỖỗỘộỚớỜờỞởỠỡỢợℴ∅ⓄⓞⱺꝊꝋꝌꝍＯｏ",
			"PpƤƥṔṕṖṗ₧₱℗℘ℙⓅⓟⱣꝐꝑꝒꝓꝔꝕꟼＰｐ", "QqɊɋℚ℺ⓆⓠꝖꝗꝘꝙＱｑ", "Rr®ŔŕŖŗŘřȐȑȒȓɌɍṘṙṚṛṜṝṞṟ₨ℛℜℝ℞℟ⓇⓡⱤⱹꝚꝛꞂꞃＲｒ",
			"$SsŚśŜŝŞşŠšſȘșȿṠṡṢṣṤṥṦṧṨṩẛẜẝ₷⅀ⓈⓢⱾꜱꞄꞅ＄Ｓｓ", "TtŢţŤťŦŧƫƬƭƮȚțȶȾʈṪṫṬṭṮṯṰṱẗ₮₸ⓉⓣⱦꞆꞇＴｔ",
			"UuÙÚÛÜùúûüŨũŪūŬŭŮůŰűŲųƯưǓǔǕǖǗǘǙǚǛǜȔȕȖȗɄʉṲṳṴṵṶṷṸṹṺṻỤụỦủỨứỪừỬửỮữỰựⓊⓤＵｕ", "VvƲɅʋʌṼṽṾṿỼỽⓋⓥⱱⱴⱽꝞꝟＶｖ",
			"WwŴŵẀẁẂẃẄẅẆẇẈẉẘ₩ⓌⓦⱲⱳＷｗ", "Xx×ẊẋẌẍⓍⓧＸｘ", "Yy¥ÝýÿŶŷŸƳƴȲȳɎɏẎẏẙỲỳỴỵỶỷỸỹỾỿ⅄ⓎⓨＹｙ",
			"ZzŹźŻżŽžƵƶȤȥɀẐẑẒẓẔẕℤℨⓏⓩⱫⱬⱿꝢꝣＺｚ", "ÞþꝤꝥꝦꝧ", "ÆæǢǣǼǽ", "Œœɶ", "0⁰⓪⓿０", "1¹ⁱⅠⅰ①⑰⒈⓵１", "2²⁲Ⅱⅱ②⑵⒉⓶２",
			"3³⁳Ⅲⅲ③⑶⒊⓷３", "4⁴Ⅳⅳ④⑷⒋⓸４", "5⁵Ⅴⅴ⑤⑸⒌⓹５", "6⁶Ⅵⅵ⑥⑹⒍⓺６", "7⁷Ⅶⅶ⑦⑺⒎⓻７", "8Ȣȣ⁸Ⅷⅷ⑧⑻⒏⓼８", "9⁹Ⅸⅸ⑨⑼⒐⓽９", "ƷƸƹǮǯȜȝʒ",
			"ꜸꜹꜺꜻ", "ƿǷᚹ", "ǄǅǆǱǲǳ", "Ǉǈǉ", "Ǌǋǌ", "℡☎☏✆", "ɑΆΑάα∝ⱭⱰ", "ßΒβϐẞ", "ƔɣΓγℽℾ", "ƍΔδẟ∆", "Εεϵ϶∍ƐɛΈέ", "ΗηΉή",
			"Θθϑ", "ƖɩͅͺΊΙΪίιϊι℩", "Κκϰ", "ƛΛλ", "µΜμ", "ΌΟοό", "Ππϖℼℿ∏", "Ρρϱϼ", "ƩʃͻͼͽΣςσϚϛϲϹϽϾϿ∑", "ƱʊΎΥΫΰυϋύϒϔ℧☋",
			"ɸΦφϕⱷ", "ΏΩωώΩ☊", "ϘϙϞϟ", "ͲͳϠϡ", "Ͱͱ", "Ͷͷ" };
	
	

	/**
	 * Retrieves the unsigned big endian 32-bit integer from a location in a byte array.
	 * 
	 * @param fileBytes the bytes to get the integer from
	 * @param offset the location to get the integer from
	 * @return the requested integer
	 */
	public static int getUint32Big(byte[] fileBytes, int offset)
	{
		byte[] bytes = Arrays.copyOfRange(fileBytes, offset, offset + 4);
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.BIG_ENDIAN);
		return bb.getInt();
	}

	/**
	 * Modifies the hex loaded in the editor. Takes the bytes and replaces them at the supplied position.
	 * 
	 * @param editor the editor to modify
	 * @param bytes the bytes to insert
	 * @param position the location to insert the bytes
	 */
	public static void modifyEditor(BinEdit editor, byte[] bytes, int position)
	{
		editor.pushHObjBytes(new EditState(position, (long) bytes.length, 4), bytes);
	}
	
	/**
	 * This method was created out of frustration to have an easy way to just get all of the bytes.
	 * I mean, we're only working with relatively small files, who cares about memory right?
	 * 
	 * @param binEdit the BinEdit to get the file from
	 * @return all of the bytes from the currently loaded file
	 */
	public static byte[] getFileBytes(BinEdit binEdit)
	{
		byte[] bytes = null;
		try
		{
			bytes = new byte[(int) binEdit.currentFile.length()];
			binEdit.currentFile.seek(0);
			binEdit.currentFile.readFully(bytes);
		}
		catch (IOException e)
		{
			System.out.println("whoops");
		}
		return bytes;
	}
	
	/**
	 * Reads shift-jis text starting from a byte array starting at offset.
	 * Stops when it encounters the hex values 00 0A (terminators).
	 * The first two 00 bytes are ignored. The second two are swapped (big to little endian).
	 * Newlines are replaced with downward arrows.
	 * 
	 * @param bytes the byte array to get the shift-jis text from
	 * @param offset the offset to start grabbing shift-jis text from
	 * @return shift-jis text
	 */
	public static String readShiftJisText(byte[] bytes, int offset)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if (bytes.length < 2)
		{
			return "Not enough bytes to parse, move cursor back.";
		}
		if (bytes[0] != 0x00 || bytes[1] != 0x00)
		{
			return "Move cursor to start of double zero byte.";
		}
		for (int i = offset; i < bytes.length; i += 4)
		{
			// Reverse order to change big to little endian
			byte byte1 = bytes[i + 3];
			byte byte2 = bytes[i + 2];
			if (byte1 == 0x1A && byte2 == 0x00)
			{
				break;
			}
			else
			{
				// Newlines are two characters in Windows, let's just replace them completely
				if (byte1 == 0x0A && byte2 == 0x00)
				{
					byte1 = (byte) 129;
					byte2 = (byte) 171;
				}
				out.write(byte1);
				out.write(byte2);
			}
		}
		byte[] textBytes = out.toByteArray();
		
		Charset cs = Charset.forName("shift-jis");
		return new String(textBytes, cs);
	}
	
	/**
	 * Returns the string representation of the first hex digit of a byte.
	 * @param fullByte the byte to grab the first hex digit of
	 * @return the string representation of the first hex digit
	 */
	public static String firstHex(byte fullByte)
	{
		return Integer.toHexString((255 & fullByte) >> 4).toUpperCase();
	}

	/**
	 * Returns the string representation of the second hex digit of a byte.
	 * @param fullByte the byte to grab the second hex digit of
	 * @return the string representation of the second hex digit
	 */
	public static String secondHex(byte fullByte)
	{
		return Integer.toHexString((255 & fullByte) % 16).toUpperCase();
	}
}
