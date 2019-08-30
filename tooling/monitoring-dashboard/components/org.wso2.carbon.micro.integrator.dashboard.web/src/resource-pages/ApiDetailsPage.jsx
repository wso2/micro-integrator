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
import TableHeaderBox from '../common/TableHeaderBox';

import Box from '@material-ui/core/Box';

export default class ApiDetailsPage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            metaData: []
        };
    }

    /**
     * Retrieve api details from the MI.
     */
    componentDidMount() {
        let url = this.props.location.search;
        const values = queryString.parse(url) || {};
        this.retrieveApiInfo(values.name);
    }

    createData(name, value) {
        return {name, value};
    }

    retrieveApiInfo(name) {
        const metaData = [];
        new ResourceAPI().getApiByName(name).then((response) => {

            metaData.push(this.createData("API Name", response.data.name));
            metaData.push(this.createData("Context", response.data.context));
            metaData.push(this.createData("Host Name", response.data.host));
            metaData.push(this.createData("Port", response.data.port));
            this.setState(
                {
                    metaData: metaData
                });

        }).catch((error) => {
            //Handle errors here
        });
    }

    renderApiDetails() {
        return (
            <div>
                <Box pb={5}>
                    <TableHeaderBox title="API Details"/>
                    <Table size="small">
                        <TableBody>
                            {
                                this.state.metaData.map(row => (
                                    <TableRow>
                                        <TableCell>{row.name}</TableCell>
                                        <TableCell>{row.value}</TableCell>
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
            <ResourceExplorerParent content={this.renderApiDetails()}/>
        );
    }
}
