package com.example.testroot;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UpdateRootApp {

	static List<RootAppItem> rootAppList = new ArrayList<RootAppItem>();

	public static void getPsCommand() {

		rootAppList.clear();
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec("ps");
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			br.readLine();// 去掉标题栏
			while ((line = br.readLine()) != null) {

				String[] list = new String[9];
				int i = 0;
				for (String str : line.split(" ")) {
					if (!str.equals("") && i < 9) {
						list[i++] = str;
					}
				}
				RootAppItem rootApp = new RootAppItem();
				rootApp.pid = Integer.parseInt(list[1]);
				rootApp.ppid = Integer.parseInt(list[2]);
				rootApp.processName = (list[8]);

				rootAppList.add(rootApp);
			}

			isr.close();
		} catch (Exception e) {
			System.out.println(e + "");
		}
	}

	public static void getRootAppList() {
		getPsCommand();
		for (RootAppItem item : rootAppList) {
			for (RootAppItem pidProItem : rootAppList) {

				if (item.equals(pidProItem)) {
					break;
				}
				if (pidProItem.pid == item.ppid) {
					System.out.println(pidProItem.processName + "      "
							+ item.processName);
				}
			}

		}

	}
}
