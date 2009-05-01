package org.monazilla.migemo;

import java.util.regex.*;
import java.io.*;
import java.util.*;

/**
 * Migemo converts a roman alphabet string into a regular expression
 * for japanese text search without invoking InputMethod.
 * <p>
 * Migemo requires a dictionary file, which can be found in
 * migemo-0.XX.tar.gz from <a href="http://0xcc.net/migemo/">Migemo Site</a>
 * as "migemo-0.XX/migemo-dict".
 * <p>
 * Example:
 * <blockquote><pre>
 * Migemo.loadDictionary(new File("migemo-dict"));
 * String sr = Migemo.lookup("nyuuryokuQnoBunkatu");
 * System.out.println(sr);
 * </pre></blockquote>
 *
 * @see <a href="http://0xcc.net/migemo/">Migemo (Original/Ruby)</a>
 * @see <a href="http://www.kaoriya.net/#CMIGEMO">C/Migemo (C)</a>
 */

public class Migemo {
	static final List lstDictionaries=new ArrayList();
	static final Pattern pAltPair=Pattern.compile("(?<!\\\\)\\(\\?\\:([^(|)]+)(?<!\\\\)\\|([^(|)]+)(?<!\\\\)\\)");

	private Migemo() {}

	/**
	 * Unload all the dictionaries.
	 */
	public static void clearDictionary() {
		lstDictionaries.clear();
	}

	/**
	 * Load a Migemo dictionary.
	 * The encoding of the dictionary is automatically detected
	 * from Shift-JIS, EUC-JP or ISO 2022 JP.
	 *
     * @param fd The dictionary file
     * @return <code>true</code> if the dictionary was successfully loaded.
	 */
	public static boolean loadDictionary(File fd) {
		return loadDictionary(fd,null);
	}

	/**
	 * Load a Migemo dictionary.
	 *
     * @param fd The dictionary file
     * @param sc The encoding of the dictionary.
	 *        If null, <code>"JISAutoDetect"</code> is used.
     * @return <code>true</code> if the dictionary was successfully loaded.
	 */
	public static boolean loadDictionary(File fd, String sc) {
		Dictionary dict = Dictionary.create(fd,sc);
		if (dict!=null) {
			lstDictionaries.add(dict);
			return true;
		} else {
			return false;
		}
	}

	public static boolean loadDictionary() {
		Dictionary dict = Dictionary.create();
		if (dict!=null) {
			lstDictionaries.add(dict);
			return true;
		} else {
			return false;
		}
	}

	static String flushStringBuffer(StringBuffer sb) {
		if (sb.length()==0) {
			return null;
		}
		String sq = sb.toString();
		sb.setLength(0);
		return sq;
	}

	static void lookupDictionary(String sk0, boolean be, Set set) {
		int nd = lstDictionaries.size();
		for (int i=0; i<nd; i++) {
			((Dictionary)lstDictionaries.get(i)).lookup(sk0,be,set);
		}
	}

	/**
	 * Convert a roman alphabet input string into a regular expression
	 * for japanese text search.
	 *
     * @param si The input string
     * @return The regular expression for japanese text search
	 */
	public static String lookup(String si) {
		int nc;
		if ((si==null)||((nc=si.length())==0)) {
			return "";
		}

		ArrayList al = new ArrayList();
		StringBuffer sb = new StringBuffer();
		boolean bl=true; // letter
		boolean bn=false; // number
		boolean bd=true; // lookup dictionary
		for (int i=0; i<nc; i++) {
			char c = si.charAt(i);
			String sq=null;
			boolean bd0=bd;
			boolean bl0=bl;
			if (((c>='a')&&(c<='z'))||(c=='-')||(c=='\'')) {
				if (!bl) {
					sq = flushStringBuffer(sb);
					bl = true;
					bn = false;
				}
			} else if ((c>='A')&&(c<='Z')) {
				sq = flushStringBuffer(sb);
				bd = (c!='Q');
				c = bd?(char)(c+('a'-'A')):0;
				bl = true;
				bn = false;
			} else if ((c>='0')&&(c<='9')) {
				if (!bn) {
					sq = flushStringBuffer(sb);
					bl = false;
					bn = true;
				}
			} else {
				if (bl||bn) {
					sq = flushStringBuffer(sb);
					bl = false;
					bn = false;
				}
			}
			if (c!=0) {
				sb.append(c);
			}
			if (sq!=null) {
				al.add(singleLookup(sq,bl0,bd0,false));
			}
		}
		if (sb.length()>0) {
			al.add(singleLookup(sb.toString(),bl,bd,true));
		}

		int ns = al.size();
		String sr;
		if (ns==1) {
			sr = (String)al.get(0);
		} else {
			StringBuffer sbr = new StringBuffer();
			for (int i=0; i<ns; i++) {
				String sri = (String)al.get(i);
				boolean bg = (sri.indexOf('|')>=0);
				if (bg) {
					sbr.append("(?:");
				}
				sbr.append(sri);
				if (bg) {
					sbr.append(')');
				}
			}
			sr = sbr.toString();
		}

		Matcher m = pAltPair.matcher(sr);
		int im=0;
		while (m.find(im)) {
			im = m.end();
			String s1 = m.group(1);
			int ns1 = s1.length();
			String s2 = m.group(2);
			int ns2 = s2.length();
			if (ns1!=ns2) {
				if (ns1>ns2) {
					String st = s2;
					s2 = s1;
					s1 = st;
					ns1 = s1.length();
					ns2 = s2.length();
				}
				boolean b2=(ns2>ns1+1);
				boolean be=false;
				String sq=null;
				if (s2.endsWith(s1)) {
					be = true;
					sq = s2.substring(0,ns2-ns1);
				} else if (s2.startsWith(s1)) {
					sq = s2.substring(ns1,ns2);
				} else {
					continue;
				}
				sb.setLength(0);
				CharUtil.appendSubstring(sb,sr,0,m.start());
				if (!be) {
					sb.append(s1);
				}
				if (b2) {
					sb.append("(?:");
				}
				sb.append(sq);
				if (b2) {
					sb.append(')');
				}
				sb.append('?');
				if (be) {
					sb.append(s1);
				}
				im = sb.length();
				CharUtil.appendSubstring(sb,sr,m.end(),sr.length());
				sr = sb.toString();
				m.reset(sr);
			}
		}
		return sr;
	}

	public static void lookups(String si, ArrayList<String> list) {
		int nc;
		if ((si==null)||((nc=si.length())==0)) {
			return;
		}
		boolean bh=true; // hiragana
		boolean bl=true; // letter
		boolean bn=false; // number
		boolean bd=true; // lookup dictionary
		boolean be=true; // expand consonant
		HashSet hs = new HashSet();
		if (bd) {
			lookupDictionary(si,be,hs);
		}
		hs.add(CharUtil.toFullWidth(si));
		if (bh) {
			String[] slh = CharUtil.toHiragana(si,be);
			for (int i=0; i<slh.length; i++) {
				String shi = slh[i];
				if (shi.length()==0) {
					continue;
				}
				hs.add(shi);
				hs.add(CharUtil.toKatakana(shi));
				hs.add(CharUtil.toHWKana(shi));
				if (bd) {
					lookupDictionary(shi,be,hs);
				}
			}
		}
		Iterator it = hs.iterator();
		while (it.hasNext()) {
			list.add((String)it.next());
		}
	}

	/**
	 * @param si The segmented input
	 * @param bh If <code>true</code>, si is converted to hiragana (letter segment)
	 * @param bd If <code>true</code>, lookup dictionary
	 * @param be If <code>true</code>, expand consonant (last segment)
	 */
	static String singleLookup(String si, boolean bh, boolean bd, boolean be) {
		HashSet hs = new HashSet();
		hs.add(si);
		if (bd) {
			lookupDictionary(si,be,hs);
		}
		hs.add(CharUtil.toFullWidth(si));
		if (bh) {
			String[] slh = CharUtil.toHiragana(si,be);
			for (int i=0; i<slh.length; i++) {
				String shi = slh[i];
				if (shi.length()==0) {
					continue;
				}
				hs.add(shi);
				hs.add(CharUtil.toKatakana(shi));
				hs.add(CharUtil.toHWKana(shi));
				if (bd) {
					lookupDictionary(shi,be,hs);
				}
			}
		}
		int ns = hs.size();
		String[] sl = (String[])hs.toArray(new String[ns]);
		Arrays.sort(sl);
		String svp=null;
		int nvp=Integer.MAX_VALUE;
		int ip=0;
		for (int i=0; i<ns; i++) {
			String sv = sl[i];
			int nv = sv.length();
			if ((nv>=nvp)&&sv.startsWith(svp)) {
				continue;
			}
			svp = sv;
			nvp = nv;
			if (ip<i) {
				sl[ip] = sv;
			}
			ip++;
		}
		ns = ip;

		ArrayList al = new ArrayList();
		StringBuffer sb = new StringBuffer();
		constructQuery(sl,0,ns,0,false,sb,al);
		StringBuffer sbt = new StringBuffer();
		int ne=0;
		for (int i=0; i<ns; i++) {
			String sv = sl[i];
			if (sv==null) {
				continue;
			}
			sbt.append(sv);
			sbt.reverse();
			sl[ne++] = sbt.toString();
			sbt.setLength(0);
		}
		if (ne>0) {
			Arrays.sort(sl,0,ne);
			constructQuery(sl,0,ne,0,true,sbt,al);
			if (sbt.length()>0) {
				if (sb.length()>0) {
					sb.append('|');
				}
				sbt.reverse();
				sb.append(sbt);
				sbt.setLength(0);
			}
			for (int i=0; i<ne; i++) {
				String sv = sl[i];
				if (sv!=null) {
					CharUtil.escapeAndAppend(sv,true,sbt);
					sbt.append('|');
				}
			}
			if (sbt.length()>0) {
				if (sb.length()==0) {
					sbt.setLength(sbt.length()-1);
				}
				sbt.reverse();
				sb.append(sbt);
			}
		}
		return sb.toString();
	}

	static void constructQuery(String[] sl, int i0, int i1, int nh, boolean br, StringBuffer sb, List lst) {
		int nh1 = nh + 1;
		int isl0 = sb.length();
		int ne=0;
		lst.clear();
		for (int i=i0; i<i1; i++) {
			String si = sl[i];
			if ((si!=null)&&(si.length()==nh1)) {
				lst.add(si);
				sl[i] = null;
			}
		}
		int n1 = lst.size();
		if (n1>0) {
			if (n1>1) {
				sb.append(br?']':'[');
				for (int i=0; i<n1; i++) {
					CharUtil.escapeAndAppend((String)lst.get(i),nh,nh1,br,sb);
				}
				sb.append(br?'[':']');
			} else {
				CharUtil.escapeAndAppend((String)lst.get(0),nh,nh1,br,sb);
			}
			sb.append('|');
			ne = 1;
		}
		
		int ipc0=i0;
		int nsc=0;
		char c0=0;
		String sp=null;
		boolean bq=false;
		for (int i=i0; i<=i1; i++) {
			String si = (i<i1)?sl[i]:null;
			boolean bs = (si!=null);
			char c;
			if (bs) {
				if (si.length()==nh) {
					bq = true;
					sl[i] = null;
					continue;
				}
				c = si.charAt(nh);
			} else {
				if(c0==0) {
					continue;
				}
				c = 0;
			}
			if (c!=c0) {
				if (nsc>0) {
					boolean bor=true;
					if (nsc>1) {
						CharUtil.escapeAndAppend(c0,br,sb);
						int ic;
						checkloop:
						for (ic=nh1; ; ic++) {
							char cs=0;
							for (int j=ipc0; j<i; j++) {
								String sj = sl[j];
								if (sj==null) {
									continue;
								}
								if (sj.length()<=ic) {
									break checkloop;
								}
								char cj = sj.charAt(ic);
								if (cs==0) {
									cs = cj;
								} else {
									if (cs!=cj) {
										break checkloop;
									}
								}
							}
							CharUtil.escapeAndAppend(cs,br,sb);
						}
						constructQuery(sl,ipc0,i,ic,br,sb,lst);
					} else {
						if (nh>0) {
							CharUtil.escapeAndAppend(sp,nh,sp.length(),br,sb);
							sl[ipc0] = null;
						} else {
							bor = false;
						}
					}
					if (bor) {
						sb.append('|');
						ne += nsc;
					}
				}
				c0 = c;
				if (bs) {
					ipc0 = i;
					nsc = 1;
					sp = si;
				} else {
					nsc = 0;
					sp = null;
				}
			} else {
				nsc++;
			}
		}
		if (ne==0) {
			return;
		}

		sb.setLength(sb.length()-1);
		if (((nh>0)&&(ne>1))||(bq&&(sb.length()-isl0>1))) {
			if (br) {
				sb.insert(isl0,')');
				if (bq) {
					sb.insert(isl0,'?');
				}
				sb.append(":?(");
			} else {
				sb.insert(isl0,"(?:");
				sb.append(')');
			}
		} else if (bq) {
			if (br) {
				sb.insert(isl0,'?');
			}
		}
	}
}
