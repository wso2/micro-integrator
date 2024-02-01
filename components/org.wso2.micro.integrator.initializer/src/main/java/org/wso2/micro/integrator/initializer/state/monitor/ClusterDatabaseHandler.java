package org.wso2.micro.integrator.initializer.state.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.coordination.query.QueryManager;
import org.wso2.micro.integrator.initializer.ServiceBusIntializerHolder;
import org.wso2.micro.integrator.ndatasource.common.DataSourceException;
import org.wso2.micro.integrator.ndatasource.core.CarbonDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ClusterDatabaseHandler {

    public static final String COORDINATION_DB_NAME = "WSO2_COORDINATION_DB";

    private static final Log LOG = LogFactory.getLog(ClusterDatabaseHandler.class);

    private Connection connection;
    private String databaseType;
    private QueryManager queryManager;

    public ClusterDatabaseHandler() throws StateMonitorException {
        try {
            CarbonDataSource carbonDataSource = ServiceBusIntializerHolder.getInstance().getDataSourceService()
                    .getDataSource(COORDINATION_DB_NAME);
            if (carbonDataSource == null) {
                throw new StateMonitorException("Datasource " + COORDINATION_DB_NAME + " not found");
            }
            DataSource coordinationDataSource = (DataSource) carbonDataSource.getDSObject();
            connection = coordinationDataSource.getConnection();

            DatabaseMetaData metaData = connection.getMetaData();
            this.databaseType = metaData.getDatabaseProductName();
            this.queryManager = new QueryManager(this.databaseType);

        } catch (DataSourceException e) {
            throw new StateMonitorException("Error while reading datasource " + COORDINATION_DB_NAME, e);
        } catch (SQLException e) {
            throw new StateMonitorException("Error getting connection to the datasource " + COORDINATION_DB_NAME, e);
        }
    }

    public Map<ArtifactIdentifier, ArtifactStatus> getArtifactStatusTableMap(String nodeId) throws StateMonitorException{
        Map<ArtifactIdentifier, ArtifactStatus> artifactStatusTableMap =
                new HashMap<ArtifactIdentifier, ArtifactStatus>();

        DatabaseQueryManager databaseQueryManager = new DatabaseQueryManager(databaseType);
        // Get the status of artifacts that are common to all nodes
        try {
            PreparedStatement preparedStatement = connection
                    .prepareStatement(databaseQueryManager.getQuery(DatabaseQueryManager.DBQueries.GET_ARTIFACT_STATUS_TABLE_FOR_ALL_NODES));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ArtifactIdentifier artifactIdentifier = new ArtifactIdentifier(
                        resultSet.getString("ARTIFACT_NAME"),
                        resultSet.getString("ARTIFACT_TYPE"));
                ArtifactStatus artifactStatus = new ArtifactStatus(
                        resultSet.getString("ARTIFACT_NAME"),
                        resultSet.getString("ARTIFACT_TYPE"),
                        resultSet.getString("ARTIFACT_STATE"),
                        resultSet.getString("NODE_ID")
                );
                artifactStatusTableMap.put(artifactIdentifier, artifactStatus);
            }

        } catch (SQLException e) {
            throw new StateMonitorException("Connection error for " + COORDINATION_DB_NAME, e);
        }

        // Override the status of artifacts that are specific to this node
        try {
            PreparedStatement preparedStatement = connection
                    .prepareStatement(databaseQueryManager.getQuery(DatabaseQueryManager.DBQueries.GET_ARTIFACT_STATUS_TABLE_FOR_GIVEN_NODE));
            preparedStatement.setString(1, nodeId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ArtifactIdentifier artifactIdentifier = new ArtifactIdentifier(
                        resultSet.getString("ARTIFACT_NAME"),
                        resultSet.getString("ARTIFACT_TYPE"));
                ArtifactStatus artifactStatus = new ArtifactStatus(
                        resultSet.getString("ARTIFACT_NAME"),
                        resultSet.getString("ARTIFACT_TYPE"),
                        resultSet.getString("ARTIFACT_STATE"),
                        resultSet.getString("NODE_ID")
                );
                artifactStatusTableMap.put(artifactIdentifier, artifactStatus);
            }
        } catch (SQLException e) {
            throw new StateMonitorException("Connection error for " + COORDINATION_DB_NAME, e);
        }
        return artifactStatusTableMap;
    }

    public static boolean isDataSourceAvailable() {
        try {
            return ServiceBusIntializerHolder.getInstance().getDataSourceService()
                    .getDataSource(COORDINATION_DB_NAME) != null;
        } catch (DataSourceException e) {
            return false;
        }
    }
}
