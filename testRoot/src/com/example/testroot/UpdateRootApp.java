package com.example.testroot;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class UpdateRootApp {

	private static List<PsAppItem> getPsCommand() {
		List<PsAppItem> rootAppList = new ArrayList<PsAppItem>();

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
				PsAppItem rootApp = new PsAppItem();
				rootApp.pid = Integer.parseInt(list[1]);
				rootApp.ppid = Integer.parseInt(list[2]);
				rootApp.processName = (list[8]);

				rootAppList.add(rootApp);
			}

			isr.close();
		} catch (Exception e) {
			System.out.println(e + "");
		}

		return rootAppList;
	}

	private static String getLabelByPkg(Context ctx, String packageName) {
		PackageManager pm = ctx.getPackageManager();
		String name = null;
		try {
			name = pm.getApplicationLabel(
					pm.getApplicationInfo(packageName,
							PackageManager.GET_META_DATA)).toString();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return name;
	}

	private static List<String> getRunningProcess(Context ctx) {
		List<String> runningApp = new ArrayList<String>();
		if (ctx == null)
			return null;

		ActivityManager am = (ActivityManager) ctx
				.getSystemService(ctx.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> rapps = am.getRunningAppProcesses();

		PackageManager packageManager = ctx.getPackageManager();
		List<PackageInfo> packageInfoList = packageManager
				.getInstalledPackages(0);
		List<String> apps = new ArrayList<String>();
		for (int i = 0; i < packageInfoList.size(); i++) {
			PackageInfo pak = packageInfoList.get(i);
			// 判断是否为系统预装的应用
			if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
				// 第三方应用
				apps.add(pak.packageName);
			}
		}

		for (ActivityManager.RunningAppProcessInfo appProcessInfo : rapps) {
			for (String pkg : appProcessInfo.pkgList) {
				runningApp.add(pkg);
			}
		}
		return runningApp;
	}

	public static void getRootAppList(Context ctx) {

		Map<String, String> rootAppList = new HashMap<String, String>();
		List<PsAppItem> appList = getPsCommand();
		List<String> runningApp = getRunningProcess(ctx);
		String label;

		for (PsAppItem item : appList) {
			if (!(item.processName.contains(".") || item.processName
					.contains("/")))
				continue;

			for (PsAppItem pidProItem : appList) {
				if (item.equals(pidProItem)) {
					continue;
				}

				// 遍历当前进程，查找当前进程的父进程和子进程，如果为sh则认为此进程有root权限，
				// 并与当前运行的应用包名匹配，最终挑出具有root权限的应用。
				if (((pidProItem.pid == item.ppid) || (pidProItem.ppid == item.pid))
						&& (pidProItem.processName.equals("sh") || pidProItem.processName
								.equals("/system/bin/sh"))) {

					for (String runningStr : runningApp) {
						if (item.processName.contains(runningStr)) {
							label = getLabelByPkg(ctx, runningStr);
							if (label != null) {
								rootAppList.put(runningStr, label);
							}
						}
					}
				}
			}
		}
	}
}
