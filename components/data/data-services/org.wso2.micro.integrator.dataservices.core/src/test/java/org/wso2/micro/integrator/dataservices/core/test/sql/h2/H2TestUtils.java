/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.dataservices.core.test.sql.h2;

import org.h2.tools.SimpleResultSet;

import java.sql.*;

public class H2TestUtils {

	public static ResultSet getCustomerInfo(Connection conn)
			throws SQLException {
		Statement stmt = conn.createStatement();
		return stmt.executeQuery("SELECT customerNumber, customerName, contactLastName, phone, city FROM Customers");
	}

	public static ResultSet getCustomerInfoWithId(Connection conn, int customerNumber)
			throws SQLException {
		PreparedStatement stmt = conn.prepareStatement("SELECT customerNumber, customerName, contactLastName, phone, city FROM Customers WHERE customerNumber = ?");
		stmt.setInt(1, customerNumber);
		return stmt.executeQuery();
	}

	public static ResultSet getCustomerInfoWithIdLastName(Connection conn, int customerNumber, String custLastName)
			throws SQLException {
		PreparedStatement stmt = conn.prepareStatement("SELECT customerNumber, customerName, contactLastName, phone, city FROM Customers WHERE customerNumber = ? and contactLastName = ?");
		stmt.setInt(1, customerNumber);
		stmt.setString(2, custLastName);
		return stmt.executeQuery();
	}

	public static ResultSet getCustomerCreditLimitWithId(Connection conn, int customerNumber)
			throws SQLException {
		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Customers WHERE customerNumber = ?");
		stmt.setInt(1, customerNumber);
		return stmt.executeQuery();
	}
	
	public static ResultSet getPaymentInfo(Connection conn)
			throws SQLException {
		Statement stmt = conn.createStatement();
		return stmt.executeQuery("SELECT customerNumber, checkNumber, paymentDate, amount FROM Payments WHERE customerNumber is NOT NULL");
	}
	
	public static ResultSet getAverageCreditLimit(Connection conn)
			throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT SUM(creditLimit) FROM Customers");
		rs.next();
		double sum = rs.getDouble(1);
		rs = stmt.executeQuery("SELECT COUNT(*) FROM Customers");
		rs.next();
		int noc = rs.getInt(1);
		SimpleResultSet srs = new SimpleResultSet();
		srs.addColumn("averageCreditLimit", Types.DOUBLE, 20, 0);
		srs.addRow(new Object[] { (sum / noc) });
		return srs;
	}

	public static ResultSet getCustomerPhoneNumber(Connection conn,
			int customerNumber) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement("SELECT phone FROM Customers WHERE customerNumber = ?");
		stmt.setInt(1, customerNumber);
		ResultSet rs = stmt.executeQuery();
		rs.next();
		String phoneNo = rs.getString(1);
		SimpleResultSet srs = new SimpleResultSet();
		srs.addColumn("phoneNumber", Types.VARCHAR, 255, 0);
		srs.addRow(new Object[] { phoneNo });
               rs.close();
		return srs;
	}

	public static void updateCustomerInfo(Connection conn, int customerNumber, String contactLastName)
			throws SQLException {
		try {
			PreparedStatement stmt = conn
					.prepareStatement("update Customers SET contactLastName = ? where customerNumber = ?");
			stmt.setString(1, contactLastName);
			stmt.setInt(2, customerNumber);
			stmt.executeUpdate();
			conn.createStatement().execute("WRONG SQL STATEMENT");
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		}
	}

    public static void addCustomerInfo(Connection conn,
                                          int customerNumber, String customerName, String contactLastName, String contactFirstName) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO Customers (customerNumber, customerName, contactLastName, contactFirstName, phone, " +
                "addressLine1, addressLine2, city, state, postalCode, country, salesRepEmployeeNumber, creditLimit)" +
                "VALUES (?, ?, ?, ?, '07-98 9556', 'Erling Skakkes gate 77', NULL," +
                "'Stavern',NULL,'4110','Norway',1504,81700.0);");
        stmt.setInt(1, customerNumber);
        stmt.setString(2, customerName);
        stmt.setString(3, contactLastName);
        stmt.setString(4, contactFirstName);
        stmt.executeUpdate();

    }
}
