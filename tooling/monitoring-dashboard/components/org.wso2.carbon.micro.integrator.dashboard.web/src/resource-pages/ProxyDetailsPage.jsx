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
import ResourceExplorerParent from '../common/ResourceExplorerParent';
import ResourceAPI from '../utils/apis/ResourceAPI';
import queryString from 'query-string'
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Typography from '@material-ui/core/Typography';

var format = require('xml-formatter');
import Box from '@material-ui/core/Box';

export default class ProxyDetailsPage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            config: " ",
            tableData: [],
            endpoints: []
        };
    }

    /**
     * Retrieve proxy details from the MI.
     */
    componentDidMount() {
        let url = this.props.location.search;
        const values = queryString.parse(url) || {};
        this.retrieveProxyInfo(values.name);
    }

    createData(name, value) {
        return {name, value};
    }

    retrieveProxyInfo(name) {
        const tableData = [];
        new ResourceAPI().getProxyServiceByName(name).then((response) => {
            const config = response.data.configuration || '';
            tableData.push(this.createData("Service Name", response.data.name));
            tableData.push(this.createData("Statistics", response.data.stats));
            tableData.push(this.createData("Tracing", response.data.tracing));

            const endpoints = response.data.endpoints || []

            this.setState(
                {
                    config: format(config),
                    tableData: tableData,
                    endpoints: endpoints
                });

        }).catch((error) => {
            //Handle errors here
        });
    }

    renderProxyDetails() {
        return (
            <div>
                <Box pb={5}>
                    <Typography variant="h6" id="tableTitle">
                        Service Details
                    </Typography>
                    <Table size="small">
                        <TableBody>
                            {
                                this.state.tableData.map(row => (
                                    <TableRow>
                                        <TableCell>{row.name}</TableCell>
                                        <TableCell>{row.value}</TableCell>
                                    </TableRow>
                                ))
                            }
                        </TableBody>
                    </Table>
                </Box>

                <Box pb={5}>
                    <Typography variant="h6" id="tableTitle">
                        Endpoints
                    </Typography>
                    <Table size="small">
                        <TableBody>
                            {
                                this.state.endpoints.map(row => (
                                    <TableRow>
                                        <TableCell>{row}</TableCell>
                                    </TableRow>
                                ))
                            }
                        </TableBody>
                    </Table>
                </Box>
            </div>
        );
    }

    render() {
        console.log(this.state.config);
        return (
            <ResourceExplorerParent content={this.renderProxyDetails()}/>
        );
    }
}