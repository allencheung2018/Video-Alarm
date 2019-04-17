package com.hri.ess.command;

import java.util.ArrayList;
import java.util.List;

import com.hri.ess.util.Util;

public class AqlistPack {
	public int tailIndex;

	/**
	 * 分包
	 * 
	 * @param pack
	 */
	public List<Byte[]> sqlist(byte[] data) {
		byte[] bstart = new byte[] { 0x48, 0x72, 0x49, 0x3c };
		byte[] btail = new byte[] { 0x3e, 0x68, 0x52, 0x69 };
		tailIndex = -1;
		List<Byte[]> lst = new ArrayList<Byte[]>();
		int start = 0;
		while (true) {
			// 查找�?
			int fIndex = Find(data, start, bstart);
			
			if (fIndex<0) {
				break;
			}else if(fIndex + 2*bstart.length-1 >=data.length - 1){
				tailIndex = fIndex-1;
				break;
			}
			byte[] pkgLenByte = Util.getBytes(data, fIndex+bstart.length, bstart.length);
			int pkgLen = Util.getInt(pkgLenByte, 0)+4;
			if(pkgLen<10){
				start = fIndex +1;
				continue;
			}else if((fIndex + pkgLen +bstart.length +btail.length-1) >data.length - 1){
				tailIndex = fIndex-1;
				break;
			}
			// 查找�?
			boolean isTail = isTail(fIndex + pkgLen+bstart.length, data, btail);
			if(!isTail){
				start = fIndex +1;
				continue;
			}
			int tIndex = fIndex + pkgLen+bstart.length;
			Byte[] pack = new Byte[pkgLen];
			//数据包长�?
			Util.bytestoBytes(fIndex + bstart.length, pack.length, data, pack);
			lst.add(pack);
			start = tIndex + btail.length;
			tailIndex = tIndex + btail.length - 1;

			if (start > data.length - 1) {
				break;
			}
		}
		return lst;
	}

	private int find(byte[] data, int start, byte fbyte) {
		int index = -1;
		for (int i = start; i < data.length; i++) {
			if (data[i] == fbyte) {
				index = i;
				break;
			}
		}
		return index;
	}

	/**
	 * 查找索引
	 */
	private int Find(byte[] data, int index, byte[] bytes) {
		byte bfirst = bytes[0];
		int start = index;
		int retIndex = -1;
		while (true) {
			int fIndex = find(data, start, bfirst);
			if (fIndex < 0) {
				break;
			}
			if (fIndex + bytes.length - 1 > data.length - 1) {
				break;
			}
			boolean flag = true;
			int sIndex = fIndex;
			int sEnd = fIndex + bytes.length;
			for (int i = sIndex; i < sEnd; i++) {
				if (data[i] != bytes[i - sIndex]) {
					flag = false;
				}
			}
			if (flag) {
				retIndex = fIndex;
				break;
			} else if (fIndex + 1 > data.length - 1) {
				break;
			} else {
				start = fIndex + 1;
			}
		}
		return retIndex;
	}
	/**
	 * 根据特定的长度判断是否为包尾
	 * @return
	 */
	private boolean isTail(int start,byte[] data,byte[] tails){
		boolean flag = true;
		int x = 0;
		for(int i=start;i<start+tails.length;i++){
			if(data[i]!=tails[x]){
				flag = false;
			}
			x++;
		}
		return flag;
	}
}
