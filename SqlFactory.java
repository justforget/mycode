package cn.gsj.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.gsj.datasource.DataSourceHelper;
import sun.applet.Main;

public class SqlFactory {

	private Connection conn = null;
	private String tableName;
	private String sql;
	private List<String> columns;

	public SqlFactory(Connection conn, String table) {
		this.tableName = table;
		this.sql = "desc " + table;
		this.conn = conn;
		columns = new ArrayList<String>();
		getColumns(columns);
	}

	private void getColumns(List<String> columns) {
		try {
			PreparedStatement pstm = conn.prepareStatement(sql);
			ResultSet rs = pstm.executeQuery();
			while (rs.next()) {
				String str = rs.getString(1);
				columns.add(str);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public String getDeleteSql() {
		return "DELETE FROM " + tableName + " WHERE id=?";
	}

	public String getQuerySql() {
		return "SELECT * FROM " + tableName + " WHERE id=?";
	}

	public String getSaveSql() {
		String sql = "INSERT INTO " + tableName + " (";
		for (int i = 0; i < columns.size(); i++) {
			if (!"id".equals(columns.get(i))) {
				if (i != columns.size() - 1) {
					sql += columns.get(i) + ", ";
				} else {
					sql += columns.get(i);
				}
			}
		}
		sql += ") VALUES (";
		for (int i = 0; i < columns.size(); i++) {
			if (!"id".equals(columns.get(i))) {
				if (i != columns.size() - 1) {
					sql += "?, ";
				} else {
					sql += "?)";
				}
			}
		}
		return sql;
	}

	public String getUpdateSql() {
		String sql = "UPDATE " + tableName + " SET ";
		for (int i = 0; i < columns.size(); i++) {
			if (!"id".equals(columns.get(i))) {
				if (i != columns.size() - 1) {
					sql += columns.get(i) + "=?, ";
				} else {
					sql += columns.get(i) + "=? ";
				}
			}
		}
		sql += "WHERE id=?";
		return sql;
	}

	public String getColumns() {
		String column = "";
		for (int i = 0; i < columns.size(); i++) {
			if (i != columns.size() - 1) {
				column += "\"" + columns.get(i) + "\", ";
			} else {
				column += "\"" + columns.get(i) + "\"";
			}
		}
		return column;
	}
}
