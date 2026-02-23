package com.han.youtubespam.gateway.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {
	public static <T> List<List<T>> partition(List<T> list, int size) {
		List<List<T>> result = new ArrayList<>();
		for (int i = 0; i < list.size(); i += size) {
			result.add(list.subList(i, Math.min(i + size, list.size())));
		}
		return result;
	}
}
