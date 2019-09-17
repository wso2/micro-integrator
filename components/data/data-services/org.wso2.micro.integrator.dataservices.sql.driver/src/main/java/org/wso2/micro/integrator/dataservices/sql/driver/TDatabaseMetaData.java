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
package org.wso2.micro.integrator.dataservices.sql.driver;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

public class TDatabaseMetaData implements DatabaseMetaData {

    private Connection connection;

    public TDatabaseMetaData(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        return false;  
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        return false;  
    }

    @Override
    public String getURL() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public String getUserName() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return false;  
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        return false;  
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        return false;  
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        return false;  
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return false;  
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public String getDriverName() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public String getDriverVersion() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public int getDriverMajorVersion() {
        return 0;  
    }

    @Override
    public int getDriverMinorVersion() {
        return 0;  
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        return false;  
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return false;  
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return false;  
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return false;  
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return false;  
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return false;  
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return false;  
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return false;  
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public String getNumericFunctions() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");
    }

    @Override
    public String getStringFunctions() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");
    }

    @Override
    public String getSystemFunctions() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");
    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");
    }

    @Override
    public boolean supportsConvert() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");
    }

    @Override
    public boolean supportsConvert(int i, int i1) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return false;  
    }

    @Override
    public String getSchemaTerm() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public String getCatalogTerm() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        return false;  
    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return false;  
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxConnections() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        return 0;  
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return false;  
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxStatements() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        return 0;  
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        return 0;  
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        return 0;  
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;  
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return false;  
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;  
    }

    @Override
    public ResultSet getProcedures(String s, String s1, String s2) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getProcedureColumns(String s, String s1, String s2,
                                         String s3) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getTables(String s, String s1, String s2,
                               String[] strings) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getColumns(String s, String s1, String s2, String s3) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getColumnPrivileges(String s, String s1, String s2, String s3) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getTablePrivileges(String s, String s1, String s2) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getBestRowIdentifier(String s, String s1, String s2, int i,
                                          boolean b) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getVersionColumns(String s, String s1, String s2) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getPrimaryKeys(String s, String s1, String s2) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getImportedKeys(String s, String s1, String s2) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getExportedKeys(String s, String s1, String s2) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getCrossReference(String s, String s1, String s2, String s3, String s4, String s5) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getIndexInfo(String s, String s1, String s2, boolean b, boolean b1) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsResultSetType(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsResultSetConcurrency(int i, int i1) throws SQLException {
        return false;  
    }

    @Override
    public boolean ownUpdatesAreVisible(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean ownDeletesAreVisible(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean ownInsertsAreVisible(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean othersUpdatesAreVisible(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean othersDeletesAreVisible(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean othersInsertsAreVisible(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean updatesAreDetected(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean deletesAreDetected(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean insertsAreDetected(int i) throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        return false;  
    }

    @Override
    public ResultSet getUDTs(String s, String s1, String s2, int[] ints) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return false;  
    }

    @Override
    public ResultSet getSuperTypes(String s, String s1, String s2) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getSuperTables(String s, String s1, String s2) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getAttributes(String s, String s1, String s2, String s3) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsResultSetHoldability(int i) throws SQLException {
        return false;  
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;  
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return 0;  
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return 0;  
    }

    @Override
    public int getJDBCMajorVersion() throws SQLException {
        return 0;  
    }

    @Override
    public int getJDBCMinorVersion() throws SQLException {
        return 0;  
    }

    @Override
    public int getSQLStateType() throws SQLException {
        return 0;  
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        return false;  
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException {
        return false;  
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getSchemas(String s, String s1) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return false;  
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return false;  
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getFunctions(String s, String s1, String s2) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public ResultSet getFunctionColumns(String s, String s1, String s2, String s3) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public <T> T unwrap(Class<T> tClass) throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");  
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return false;  
    }

    public ResultSet getPseudoColumns(String catalog, String schemaPattern,
                               String tableNamePattern, String columnNamePattern)
            throws SQLException {
        throw new SQLFeatureNotSupportedException("Functionality is not supported");
    }

    public boolean  generatedKeyAlwaysReturned() throws SQLException {
        return true;
    }

}
