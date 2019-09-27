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


import React, {Component} from 'react';
import Grid from '@material-ui/core/Grid';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import ResourceExplorerParent from '../common/ResourceExplorerParent';
import ResourceAPI from '../utils/apis/ResourceAPI';

const styles = {
    gridItem: {
        border: 'ridge'
    },
    serverMetaDataItem: {
        paddingBottom: '50px'
    }

};

export default class HomePage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            response: {}
        };
    }

    componentDidMount() {
        this.retrieveMetaData();
    }

    createData(name, value) {
        return {name, value};
    }

    retrieveMetaData() {
        new ResourceAPI().getServerMetaData().then((response) => {
            this.setState(
                {
                    response: response.data,
                });

        }).catch((error) => {
            //Handle errors here
        });
    }

    renderHomePageView() {
        return (
            <Grid container spacing={4}>
                <Grid item xs={12} style={styles.serverMetaDataItem}>
                    {this.createMetaDataTable(this.state.response)}
                </Grid>
                {/*<Grid item xs={6} sm={4} style={styles.gridItem}>*/}
                {/*<Typography>Proxy Services</Typography>*/}
                {/*</Grid>*/}
                {/*<Grid item xs={6} sm={4} style={styles.gridItem}>*/}
                {/*<Typography>Endpoints</Typography>*/}
                {/*</Grid>*/}
                {/*<Grid item xs={6} sm={4} style={styles.gridItem}>*/}
                {/*<Typography>Inbound Endpoints</Typography>*/}
                {/*</Grid>*/}
                {/*<Grid item xs={6} sm={4} style={styles.gridItem}>*/}
                {/*<Typography>Message Processors</Typography>*/}
                {/*</Grid>*/}
                {/*<Grid item xs={6} sm={4} style={styles.gridItem}>*/}
                {/*<Typography>Message Stores</Typography>*/}
                {/*</Grid>*/}
                {/*<Grid item xs={6} sm={4} style={styles.gridItem}>*/}
                {/*<Typography>API</Typography>*/}
                {/*</Grid>*/}
                {/*<Grid item xs={6} sm={4} style={styles.gridItem}>*/}
                {/*<Typography>Templates</Typography>*/}
                {/*</Grid>*/}
                {/*<Grid item xs={6} sm={4} style={styles.gridItem}>*/}
                {/*<Typography>Sequences</Typography>*/}
                {/*</Grid>*/}
                {/*<Grid item xs={6} sm={4} style={styles.gridItem}>*/}
                {/*<Typography>Tasks</Typography>*/}
                {/*</Grid>*/}
                {/*<Grid item xs={6} sm={4} style={styles.gridItem}>*/}
                {/*<Typography>Local Entries</Typography>*/}
                {/*</Grid>*/}
                {/*<Grid item xs={6} sm={4} style={styles.gridItem}>*/}
                {/*<Typography>Connectors</Typography>*/}
                {/*</Grid>*/}
                {/*<Grid item xs={6} sm={4} style={styles.gridItem}>*/}
                {/*<Typography>Carbon Applications</Typography>*/}
                {/*</Grid>*/}
            </Grid>
        );
    }

    createMetaDataTable(metadata) {
        const options = [];
        options.push(this.createData("Server Name", metadata.productName));
        options.push(this.createData("Carbon Home", metadata.carbonHome));
        options.push(this.createData("Java Home", metadata.javaHome));
        options.push(this.createData("Work Directory", metadata.workDirectory));
        options.push(this.createData("Repository Location", metadata.repositoryLocation));
        return (
            <Table>
                <TableBody>
                    {this.renderRowsFromData(options)}
                </TableBody>
            </Table>);
    }

    renderRowsFromData(data) {
        return (
            data.map(row => (
                <TableRow>
                    <TableCell>{row.name}</TableCell>
                    <TableCell>{row.value}</TableCell>
                </TableRow>
            ))
        );
    }


    render() {
        return (<ResourceExplorerParent title={"SERVER HOME"} content={this.renderHomePageView()}/>);
    }
}
