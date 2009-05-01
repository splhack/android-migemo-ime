package org.monazilla.migemo;

import org.splhack.android.migemizesoftkeyboard.*;
import java.io.*;
import java.util.*;

class CharUtil {
	static final Map mRoma2Hira;
	static final String[] slRomaji;

	//      0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F
	// 304    ぁ あ ぃ い ぅ う ぇ え ぉ お か が き ぎ く
	// 305 ぐ け げ こ ご さ ざ し じ す ず せ ぜ そ ぞ た
	// 306 だ ち ぢ っ つ づ て で と ど な に ぬ ね の は
	// 307 ば ぱ ひ び ぴ ふ ぶ ぷ へ べ ぺ ほ ぼ ぽ ま み
	// 308 む め も ゃ や ゅ ゆ ょ よ ら り る れ ろ ゎ わ
	// 309 ゐ ゑ を ん ヴ ヵ ヶ
	static final char clToHWKana1[] = {
		'\uFF67','\uFF71','\uFF68','\uFF72','\uFF69','\uFF73','\uFF6A',
		'\uFF74','\uFF6B','\uFF75','\uFF76','\uFF76','\uFF77','\uFF77','\uFF78',
		'\uFF78','\uFF79','\uFF79','\uFF7A','\uFF7A','\uFF7B','\uFF7B','\uFF7C',
		'\uFF7C','\uFF7D','\uFF7D','\uFF7E','\uFF7E','\uFF7F','\uFF7F','\uFF80',
		'\uFF80','\uFF81','\uFF81','\uFF6F','\uFF82','\uFF82','\uFF83','\uFF83',
		'\uFF84','\uFF84','\uFF85','\uFF86','\uFF87','\uFF88','\uFF89','\uFF8A',
		'\uFF8A','\uFF8A','\uFF8B','\uFF8B','\uFF8B','\uFF8C','\uFF8C','\uFF8C',
		'\uFF8D','\uFF8D','\uFF8D','\uFF8E','\uFF8E','\uFF8E','\uFF8F','\uFF90',
		'\uFF91','\uFF92','\uFF93','\uFF6C','\uFF94','\uFF6D','\uFF95','\uFF6E',
		'\uFF96','\uFF97','\uFF98','\uFF99','\uFF9A','\uFF9B',0,'\uFF9C',
		0,0,'\uFF66','\uFF9D'
	};
	static final char DT='\uFF9E'; // Dakuten
	static final char HDT='\uFF9F'; // HanDakuten
	static final char clToHWKana2[] = {
		DT,0,DT,0,
		DT,0,DT,0,DT,0,DT,0,DT,0,DT,0,DT,0,DT,0,
		DT,0,DT,0,0,DT,0,DT,0,DT,0,0,0,0,0,0,
		DT,HDT,0,DT,HDT,0,DT,HDT,0,DT,HDT,0,DT,HDT,0,0,
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		0,0,0,0
	};
	static final char[] clVowels = {
		'あ',0,0,0,'え',0,0,0,'い',0,0,0,0,0,'お',0,0,0,0,0,'う'
	};
	static final String[] slContracted = {
		"ぁ","ぃ","ぅ","ぇ","ぉ","ヵ","ヶ","っ","ゃ","ゅ","ょ"
	};
	static final char[] clToContracted = {
		'ヵ',0,0,0,0,
		0,'ヶ',0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		0,0,0,0,'っ',0,0,0,0,0,0,0,0,0,0,0,
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		0,0,0,0,'ゃ',0,'ゅ',0,'ょ'
	};
	static final boolean[] blEscapeChar = {
		false,true,false,false,true,false,true,false, // !"#$%&'
		true,true,true,true,true,true,true,false, //()*+,-./
		false,false,false,false,false,false,false,false, //01234567
		false,false,true,false,true,true,true,true, //89:;<=>?
		false,false,false,false,false,false,false,false, //@ABCDEFG
		false,false,false,false,false,false,false,false, //HIJKLMNO
		false,false,false,false,false,false,false,false, //PQRSTUVW
		false,false,false,true,true,true,true,false, //XYZ[\]^_
		false,false,false,false,false,false,false,false, //`abcdefg
		false,false,false,false,false,false,false,false, //hijklmno
		false,false,false,false,false,false,false,false, //pqrstuvw
		false,false,false,true,true,true,false //xyz{|}~
	};

	static {
		HashMap hm = new HashMap();
		mRoma2Hira = hm;
		BufferedReader br=null;
		try {
			br = new BufferedReader(new InputStreamReader(
				MigemizeSoftKeyboard.me.getResources().getAssets().open("r2h"),
					"UTF-8"));
			String sl;
			while ((sl=br.readLine())!=null) {
				String[] sll = sl.split("\t");
				if ((sll==null)||(sll.length<2))
					continue;
				String sv = sll[0];
				for (int i=sll.length-1; i>0; i--) {
					hm.put(sll[i],sv);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br!=null) {
				try {
					br.close();
				} catch (IOException e) {}
			}
		}
		slRomaji = (String[])hm.keySet().toArray(new String[hm.size()]);
	}


	private CharUtil() {}

	static char toVowel(char c) {
		return ((c>='a')&&(c<='u'))?clVowels[c-'a']:0;
	}

	static char toContracted(char c) {
		return ((c>='か')&&(c<='よ'))?clToContracted[c-'か']:0;
	}

	static String[] toHiragana(String si, boolean be) {
		return be?expandHiragana(si):new String[] {toHiragana(si)};
	}

	static String toHWKana(String si) {
		StringBuffer sb = new StringBuffer(si);
		int ns = si.length();
		for (int i=0; i<ns; i++) {
			char ci = sb.charAt(i);
			if (ci=='ー') {
				sb.setCharAt(i,'\uFF70');
			} else if (ci=='ヴ') {
				sb.setCharAt(i,'\uFF73');
				sb.insert(++i,DT);
				ns++;
			} else if ((ci>='ぁ')&&(ci<='ん')) {
				char c0 = clToHWKana1[ci-'ぁ'];
				if (c0!=0) {
					sb.setCharAt(i,c0);
					if (ci>='が') {
						char c1 = clToHWKana2[ci-'が'];
						if (c1!=0) {
							sb.insert(++i,c1);
							ns++;
						}
					}
				}
			}
		}
		return sb.toString();
	}

	static String toKatakana(String si) {
		StringBuffer sb = new StringBuffer(si);
		int ns = si.length();
		for (int i=0; i<ns; i++) {
			char ci = si.charAt(i);
			if ((ci>='ぁ')&&(ci<='ん')) {
				sb.setCharAt(i,(char)(ci+0x60));
			}
		}
		return sb.toString();
	}

	static String[] expandHiragana(String si) {
		HashSet hs = new HashSet();
		String s0 = toHiragana(si,hs);
		int nex = hs.size();
		if (nex==0) {
			return new String[] {s0};
		}
		String[] sl = (String[])hs.toArray(new String[nex]);
		if (s0.length()>0) {
			for (int i=0; i<nex; i++) {
				sl[i] = s0 + sl[i];
			}
		}
		Arrays.sort(sl);

		return sl;
	}

	static String toHiragana(String si) {
		return toHiragana(si,null);
	}
	static String toHiragana(String si, Set set) {
		int ns = si.length();
		Map mh = mRoma2Hira;
		StringBuffer sb = new StringBuffer(ns);
		char c=0,cp;
		int iv=0;
		for (int i=0; i<ns; i++) {
			cp = c;
			c = si.charAt(i);
			char cv = toVowel(c);
			if (cv==0) {
				int iac = i;
				int ivn = i + 1;
				char ca = 0;
				if ((cp=='n')&&((c!=0)&&(c!='y'))) {
					iac--;
					ca = 'ん';
					if ((c!='n')&&(c!='\'')) {
						ivn--;
					}
					c = 0;
				} else if (c=='-') {
					ca = 'ー';
				} else if (c=='\'') {
					ca = '\'';
				}
				if (ca!=0) {
					appendConsonant(si,iv,iac,sb);
					sb.append(ca);
					iv = ivn;
				}
			} else {
				int i0=i;
				String sh=null;
				boolean bh=false;
				if (iv<=i-2) {
					sh = (String)mh.get(si.substring(i-2,i+1));
					if (sh!=null) {
						bh = (iv<=i-3)&&(si.charAt(i-3)==si.charAt(i-2));
						i0 = i-2;
					}
				}
				if ((sh==null)&&(iv<=i-1)) {
					sh = (String)mh.get(si.substring(i-1,i+1));
					if (sh!=null) {
						bh = (iv<=i-2)&&(si.charAt(i-2)==si.charAt(i-1));
						i0 = i-1;
					}
				}
				if (bh) {
					i0--;
				} else {
					if ((i0>0)&&(si.charAt(i0-1)=='x')) {
						if (sh!=null) {
							if (sh.length()==1) {
								char cx = toContracted(sh.charAt(0));
								if (cx!=0) {
									sh = null;
									cv = cx;
									i0--;
								}
							}
						} else {
							cv--;
							i0--;
						}
					}
				}
				appendConsonant(si,iv,i0,sb);
				if (bh) {
					sb.append('っ');
				}
				if (sh!=null) {
					sb.append(sh);
				} else {
					sb.append(cv);
				}
				iv = i+1;
			}
		}
		if (set!=null) {
			expandTrailingConsonant(si,iv,ns,set);
		} else {
			appendConsonant(si,iv,ns,sb);
		}
		return sb.toString();
	}

	static class ScoreHolder {
		String sKana;
		int nScore;
		ScoreHolder(String sk, int ns) {
			sKana = sk;
			nScore = ns;
		}
	}

	static void expandTrailingConsonant(String si, int iv, int ns, Set set) {
		if (iv>=ns)
			return;
		char c1 = si.charAt(ns-1);
		if (c1=='x') {
			set.add(si.substring(iv,ns));
			String s0 = (iv<ns-1)?si.substring(iv,ns-1):null;
			String[] slx = slContracted;
			for (int i=0; i<slx.length; i++) {
				String sx = slx[i];
				set.add((s0!=null)?(s0+sx):sx);
			}
			return;
		}
		Map mh = mRoma2Hira;
		String[] slr = slRomaji;
		int nl = slr.length;
		StringBuffer sb = new StringBuffer();
		ArrayList al = new ArrayList();
		boolean b2 = (iv<=ns-2);
		char c2 = (b2?si.charAt(ns-2):0);
		int nsm = ns - iv;
		for (int i=0; i<nl; i++) {
			String sk = slr[i];
			int ncm = 0;
			if (b2&&(sk.length()>2)) {
				if ((sk.charAt(0)==c2)&&(sk.charAt(1)==c1)) {
					ncm = 2;
				}
			}
			if (ncm==0) {
				if (sk.charAt(0)==c1) {
					ncm = 1;
				}
			}
			if (ncm==0) {
				continue;
			}
			String sh = (String)mh.get(sk);
			boolean bh;
			if (ncm==2) {
				bh = (iv<=ns-3)&&(si.charAt(ns-3)==c2);
			} else {
				bh = b2&&(c2==c1);
			}
			int i0 = ns - ncm;
			char cx = 0;
			if (bh) {
				i0--;
			} else {
				if ((i0>iv)&&(si.charAt(i0-1)=='x')) {
					if (sh.length()==1) {
						cx = toContracted(sh.charAt(0));
						if (cx!=0) {
							i0--;
						}
					}
				}
			}
			int nsc = i0 - iv;
			if (nsc>nsm) {
				continue;
			}
			if (nsc<nsm) {
				nsm = nsc;
			}
			sb.setLength(0);
			if (nsc>0) {
				CharUtil.appendSubstring(sb,si,iv,i0);
			}
			if (bh) {
				sb.append('っ');
			}
			if (cx!=0) {
				sb.append(cx);
			} else {
				sb.append(sh);
			}
			al.add(new ScoreHolder(sb.toString(),nsc));
			if ((ns==iv+1)&&(c1!='n')) {
				sb.setLength(0);
				sb.append('っ');
				if (cx!=0) {
					sb.append(cx);
				} else {
					sb.append(sh);
				}
				al.add(new ScoreHolder(sb.toString(),0));
			}
		}
		if ((c1=='n')&&(!b2||(c2!='n'))) {
			int nsc = ns - iv - 1;
			if (nsc<=nsm) {
				sb.setLength(0);
				if (nsc>0) {
					CharUtil.appendSubstring(sb,si,iv,ns-1);
				}
				sb.append("ん");
				al.add(new ScoreHolder(sb.toString(),nsc));
			}
		}
		int ne = al.size();
		if (ne==0) {
			return;
		}
		for (int i=ne-1; i>=0; i--) {
			ScoreHolder shi = (ScoreHolder)al.get(i);
			if (shi.nScore==nsm) {
				al.set(i,shi.sKana);
			} else {
				al.remove(i);
			}
		}
		ne = al.size();
		if (ne==1) {
			set.add(al.get(0));
			return;
		}
		String[] sl = (String[])al.toArray(new String[ne]);
		Arrays.sort(sl);
		String svp=sl[0];
		set.add(svp);
		for (int i=1; i<ne; i++) {
			String sv = sl[i];
			if (!sv.startsWith(svp)) {
				svp = sv;
				set.add(sv);
			}
		}
	}

	static List expandConsonant(String sc) {
		Map mh = mRoma2Hira;
		ArrayList al = new ArrayList();
		String[] slr = slRomaji;
		int nl = slr.length;
		for (int i=0; i<nl; i++) {
			if (slr[i].startsWith(sc)) {
				al.add(mh.get(slr[i]));
			}
		}
		return al;
	}

	static void appendConsonant(String si, int i0, int i1, StringBuffer sb) {
		if (i0>=i1) {
			return;
		}
		if (si.charAt(i1-1)=='n') {
			i1--;
			if (i0<i1) {
				CharUtil.appendSubstring(sb,si,i0,i1);
			}
			sb.append('ん');
		} else {
			CharUtil.appendSubstring(sb,si,i0,i1);
		}
	}


	static String toFullWidth(String si) {
		StringBuffer sb = new StringBuffer(si);
		int ns = si.length();
		for (int i=0; i<ns; i++) {
			char ci = si.charAt(i);
			if ((ci>='!')&&(ci<='~')) {
				sb.setCharAt(i,(char)(ci+('！'-'!')));
			}
		}
		return sb.toString();
	}

	static void appendSubstring(StringBuffer sb, String si, int i0, int i1) {
		if (i0+10<i1) {
			sb.append(si.substring(i0,i1));
		} else {
			for (int i=i0; i<i1; i++) {
				sb.append(si.charAt(i));
			}
		}
	}

	static void escapeAndAppend(String si, boolean br, StringBuffer sb) {
		escapeAndAppend(si,0,si.length(),br,sb);
	}
	static void escapeAndAppend(String si, int i0, int i1, boolean br, StringBuffer sb) {
		for (int i=i0; i<i1; i++) {
			escapeAndAppend(si.charAt(i),br,sb);
		}
	}
	static void escapeAndAppend(char c, boolean br, StringBuffer sb) {
		if (br) {
			sb.append(c);
		}
		if ((c>='!')&&(c<='}')&&blEscapeChar[c-' ']) {
			sb.append('\\');
		}
		if (!br) {
			sb.append(c);
		}
	}
}
