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
import Typography from '@material-ui/core/Typography';
import Breadcrumbs from '@material-ui/core/Breadcrumbs';
import { Link } from "react-router-dom";

import Box from '@material-ui/core/Box';

export default class InboundEndpointDetailsPage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            metaData: [],
            parameters: [],
            response: {}
        };
    }

    /**
     * Retrieve inbound-endpoint details from the MI.
     */
    componentDidMount() {
        let url = this.props.location.search;
        const values = queryString.parse(url) || {};
        this.retrieveEndpointInfo(values.name);
    }

    createData(name, value) {
        return {name, value};
    }

    retrieveEndpointInfo(name) {
        const metaData = [];
        new ResourceAPI().getInboundEndpointByName(name).then((response) => {

            metaData.push(this.createData("Inbound Endpoint Name", response.data.name));
            metaData.push(this.createData("Protocol", response.data.protocol));
            metaData.push(this.createData("Tracing", response.data.tracing));
            metaData.push(this.createData("Statistics", response.data.stats));
            metaData.push(this.createData("Sequence", response.data.sequence));
            metaData.push(this.createData("On Error", response.data.error));

            this.setState(
                {
                    metaData: metaData,
                    parameters: response.data.parameters,
                    response: response.data,
                });

        }).catch((error) => {
            //Handle errors here
        });
    }

    renderInboundEndpointDetails() {
        return (
            <div>
                <Box pb={5}>
                    <TableHeaderBox title="Inbound Endpoint Details"/>
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

                <Box pb={5}>
                    <TableHeaderBox title="Parameters"/>
                    <Table size="small">
                        <TableBody>
                            {
                                this.state.parameters.map(row => (
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

    renderBreadCrumbs() {
        return (
            <Breadcrumbs separator="›" aria-label="breadcrumb">
                <Box color="inherit" component={Link} to="/inbound-endpoint" fontSize={14}>
                    Inbound Endpoints
                </Box>
                <Box color="textPrimary" fontSize={14}>{this.state.response.name}</Box>
            </Breadcrumbs>);
    }

    render() {
        console.log(this.state.config);
        return (
            <ResourceExplorerParent title={this.state.response.name + " Explorer"}
                                    content={this.renderInboundEndpointDetails()} breadcrumb={this.renderBreadCrumbs()}/>
        );
    }
}