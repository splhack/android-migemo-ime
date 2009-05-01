package org.monazilla.migemo;

import org.splhack.android.migemizesoftkeyboard.*;
import java.io.*;
import java.util.*;

class Dictionary {
	File fFile;
	char[] clDict;
	int[] ilKeyIndex;
	short[] shlKeyLength;
	int nEntry;

	static Dictionary create() {
		Dictionary dict = new Dictionary();
		return dict.load(null)?dict:null;
	}

	static Dictionary create(File fd, String se) {
		if ((fd==null)||!fd.isFile()||!fd.canRead()) {
			return null;
		}
		Dictionary dict = new Dictionary(fd);
		return dict.load(se)?dict:null;
	}

	static void sortBranch(int[] ils, int is, int ie, char[] cld, int[] il, short[] shl) {
		int i0=is;
		int i1;
		while ((i1=(2*i0+1))<=ie) {
			if ((i1<ie)&&(compareForSort(ils[i1],ils[i1+1],cld,il,shl)<0)) {
				i1++;
			}
			if (compareForSort(ils[i0],ils[i1],cld,il,shl)>=0) {
				return;
			}
			int it = ils[i0];
			ils[i0] = ils[i1];
			ils[i1] = it;
			i0 = i1;
		}
	}

	static int compareForSort(int i0, int i1, char[] cld, int[] il, short[] shl) {
		int nc0 = shl[i0];
		int nc1 = shl[i1];
		i0 = il[i0];
		i1 = il[i1];
		int n0 = i0 + ((nc0<nc1)?nc0:nc1);
		for (; i0<n0; i0++,i1++) {
			int d = cld[i0] - cld[i1];
			if (d!=0) {
				return d;
			}
		}
		return nc0 - nc1;
	}

	Dictionary() {
	}

	Dictionary(File fd) {
		fFile = fd;
	}

	boolean load(String se) {
		// Read
		/*
		int nl = (int)fFile.length();
		if (nl==0) {
			return true;
		}
		*/
		int nl;
		char[] cld;
		int nc=0;
		try {
			InputStream is =
				MigemizeSoftKeyboard.me.getResources().getAssets().open("dict");
			nl = is.available();
			byte[] b = new byte[nl];
			is.read(b);
			is.close();
			cld = (new String(b)).toCharArray();
			nc = cld.length;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (nc==0)
			return true;

		// Parse
		int[] il = new int[nc];
		short[] shl = new short[nc];
		int ne=0;
		int ic=0;
		if (cld[0]=='\uFEFF') {
			ic++;
		}
		int i0=ic;
		for (; ic<nc; ic++) {
			int c = cld[ic];
			if ((c=='\n')||(c=='\r')) {
				i0 = ic+1;
			} else if (c=='\t') {
				int nkl = ic - i0;
				if ((nkl>0)&&(nkl<=Short.MAX_VALUE)&&(cld[i0]!=';')) {
					il[ne] = i0;
					shl[ne] = (short)nkl;
					ne++;
				}
			}
		}
		if (ne==0) {
			return true;
		}

		// Sort
		int[] iln;
		short[] shln;
		if (ne>1) {
			int[] ils = new int[ne];
			for (int i=0; i<ne; i++) {
				ils[i] = i;
			}
			for (int i=(ne-1)/2; i>=0; i--) {
				sortBranch(ils,i,ne-1,cld,il,shl);
			}
			for (int i=ne-1; i>0; i--) {
				int it = ils[i];
				ils[i] = ils[0];
				ils[0] = it;
				sortBranch(ils,0,i-1,cld,il,shl);
			}
			iln = new int[ne];
			shln = new short[ne];
			for (int i=0; i<ne; i++) {
				int is = ils[i];
				iln[i] = il[is];
				shln[i] = shl[is];
			}
		} else {
			iln = new int[] {il[0]};
			shln = new short[] {shl[0]};
		}
		ilKeyIndex = iln;
		shlKeyLength = shln;
		clDict = cld;
		nEntry = ne;

		return true;
	}

	int compare(int i, char[] cl1, int nc1) {
		int nc0 = shlKeyLength[i];
		int i0 = ilKeyIndex[i];
		int nc = (nc0<nc1)?nc0:nc1;
		char[] cl0 = clDict;
		for (int i1=0; i1<nc; i1++,i0++) {
			int d = cl0[i0] - cl1[i1];
			if (d!=0) {
				return d;
			}
		}
		return nc0 - nc1;
	}

	boolean startsWith(int i, char[] cl1, int nc1) {
		int nc0 = shlKeyLength[i];
		if (nc0<nc1) {
			return false;
		}
		int i0 = ilKeyIndex[i];
		char[] cl0 = clDict;
		for (int i1=0; i1<nc1; i1++,i0++) {
			int d = cl0[i0] - cl1[i1];
			if (d!=0) {
				return false;
			}
		}
		return true;
	}

	boolean equals(int i, char[] cl1, int nc1) {
		int nc0 = shlKeyLength[i];
		if (nc0!=nc1) {
			return false;
		}
		int i0 = ilKeyIndex[i];
		char[] cl0 = clDict;
		for (int i1=0; i1<nc1; i1++,i0++) {
			int d = cl0[i0] - cl1[i1];
			if (d!=0) {
				return false;
			}
		}
		return true;
	}

	int findStart(char[] cl1, int nc1) {
		if (compare(0,cl1,nc1)>=0) {
			return 0;
		}
		int ip1=nEntry-1;
		if (ip1==0) {
			return 1;
		}
		int d = compare(ip1,cl1,nc1);
		if (d<=0) {
			return (d==0)?ip1:(ip1+1);
		}
		if (ip1==1) {
			return 1;
		}
		int ip0=0;
		while (true) {
			int ipm = (ip0+ip1)/2;
			d = compare(ipm,cl1,nc1);
			if (d==0) {
				return ipm;
			}
			if (d>0) {
				ip1 = ipm;
			} else {
				ip0 = ipm + 1;
			}
			if (ip0+1>=ip1) {
				return ip1;
			}
		}
	}

	void addWords(int ie, Set set) {
		char[] cld = clDict;
		int nc = cld.length;
		int ip0 = ilKeyIndex[ie] + shlKeyLength[ie] + 1;
		for (int i=ip0; i<nc; i++) {
			int ci = cld[i];
			if ((ci=='\t')||(ci=='\n')||(ci=='\r')) {
				if (ip0<i) {
					set.add(new String(cld,ip0,i-ip0));
				}
				if (ci!='\t') {
					break;
				}
				ip0 = i+1;
			}
		}
	}

	void lookup(String sk0, boolean be, Set set) {
		int ne = nEntry;
		if (ne==0) {
			return;
		}
		int nck = sk0.length();
		if (nck==0) {
			return;
		}
		char[] clk = new char[nck];
		sk0.getChars(0,nck,clk,0);
		int is = findStart(clk,nck);
		for (int i=is-1; i>=0; i--) {
			if (!(be?startsWith(i,clk,nck):equals(i,clk,nck))) {
				break;
			}
			addWords(i,set);
		}
		for (int i=is; i<ne; i++) {
			if (!(be?startsWith(i,clk,nck):equals(i,clk,nck))) {
				break;
			}
			addWords(i,set);
		}
	}
}
