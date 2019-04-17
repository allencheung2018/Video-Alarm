package com.hri.ess.dbservice.control;

import java.sql.SQLException;
import java.util.List;
import com.hri.ess.dbservice.VillaSecurityDBHelper;
import com.hri.ess.dbservice.domain.LoginUser;
import com.j256.ormlite.dao.Dao;

import android.content.Context;

public class LoginUserControl {
	private Context mcontext;
	private VillaSecurityDBHelper vHelper = null;

	private Dao<LoginUser, Integer> dao;

	public LoginUserControl(Context context) {
		this.mcontext = context;

		vHelper = new VillaSecurityDBHelper(context);
		try {
			dao = vHelper.getDao(LoginUser.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 添加用户
	 * 
	 * @param login
	 */
	public void add(LoginUser login) {
		try {
			List<LoginUser> list = queryAll();
			for (int i = 0; i < list.size(); i++) {
				String name = list.get(i).getName();
				if (login.getName().equals(name)) {
					return;
				}
			}
			dao.createIfNotExists(login);
		} catch (SQLException e) {
		}
	}

	/**
	 * 查询�?��
	 * 
	 * @return
	 */
	public List<LoginUser> queryAll() {
		List<LoginUser> users = null;
		try {
			users = dao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return users;
	}

	/*
	 * 查询�?��用户�?
	 */
	public String[] queryNames() {
		List<LoginUser> lists = queryAll();
		String[] names = null;
		if (lists != null && lists.size() > 0) {
			names = new String[lists.size()];
			for (int i = 0; i < lists.size(); i++) {
				names[i] = lists.get(i).getName();
			}
		}
		return names;
	}

	public void delete(LoginUser user) {
		try {
			List<LoginUser> list = dao.queryBuilder().where()
					.eq("name", user.getName()).and().eq("pwd", user.getPwd())
					.query();
			if (list.size() > 0) {
				LoginUser dUser = list.get(0);
				dao.delete(dUser);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
